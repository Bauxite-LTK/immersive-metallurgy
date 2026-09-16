package net.bauxite_ltk.immersive_metallurgy.block.multiblock.logic;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MBInventoryUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.util.DroppingMultiblockOutput;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import blusunrize.immersiveengineering.common.util.inventory.WrappingItemHandler;
import blusunrize.immersiveengineering.common.util.sound.MultiblockSound;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.process.IMMultiblockProcessInMachine;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.shapes.AdvancedCokeOvenShapes;
import net.bauxite_ltk.immersive_metallurgy.block.util.IMRelativeBlockFace;
import net.bauxite_ltk.immersive_metallurgy.crafting.AdvancedCokeOvenRecipe;
import net.bauxite_ltk.immersive_metallurgy.render.AdvancedCokeOvenRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AdvancedCokeOvenLogic implements
        IMultiblockLogic<AdvancedCokeOvenLogic.State>,
        IServerTickableComponent<AdvancedCokeOvenLogic.State>,
        IClientTickableComponent<AdvancedCokeOvenLogic.State> {
    public static final BlockPos MASTER_OFFSET = new BlockPos(1, 1, 1);
    public static final int THREAD_COUNT = 4;
    public static final int TANK_CAPACITY = 16000;
    public static final List<AdvancedCokeOvenMFOffsets> MF_OFFSETS = new ArrayList<>(THREAD_COUNT);
    public static final List<AdvancedCokeOvenCaps> CAPS = new ArrayList<>(THREAD_COUNT);

    static {
        for(int i = 0; i < THREAD_COUNT; i++){

            MultiblockFace redstone = new MultiblockFace(i,3,0, IMRelativeBlockFace.ZN.toIE());
            MultiblockFace inputItem = new MultiblockFace(i,4,2, IMRelativeBlockFace.YN.toIE());
            MultiblockFace outputGas = new MultiblockFace(i,4,1, IMRelativeBlockFace.YN.toIE());
            MultiblockFace outputOil = new MultiblockFace(i,3,4, IMRelativeBlockFace.ZN.toIE());
            MultiblockFace outputItem = new MultiblockFace(i,0,4, IMRelativeBlockFace.ZN.toIE());


            MF_OFFSETS.add(i, new AdvancedCokeOvenMFOffsets(
                    redstone,
                    inputItem,
                    outputOil,
                    outputGas,
                    outputItem
            ));

            CAPS.add(i, new AdvancedCokeOvenCaps(
                    CapabilityPosition.opposing(inputItem),
                    CapabilityPosition.opposing(outputOil),
                    CapabilityPosition.opposing(outputGas),
                    CapabilityPosition.opposing(outputItem)
            ));

        }
    }

    @Override
    public void tickServer(IMultiblockContext<State> context) {
        // Get State
        final State state = context.getState();
        state.energy.receiveEnergy(100000,false);
        state.tickServer(context);
    }


    @Override
    public void tickClient(IMultiblockContext<State> context) {
        final State state = context.getState();
        state.tickClient(context);
        if(!state.isPlayingSound.getAsBoolean())
        {
            final Vec3 soundPos = context.getLevel().toAbsolute(new Vec3(1.5, 2.5, 3.5));
            state.isPlayingSound = MultiblockSound.startSound(
                    state::anyActive, context.isValid(), soundPos, IESounds.preheater, 0.5f
            );
        }
    }



    @Override
    public State createInitialState(IInitialMultiblockContext<State> capabilitySource) {
        return new State(capabilitySource);
    }

    @Override
    public void registerCapabilities(CapabilityRegistrar<State> register) {
        for(int i = 0; i < THREAD_COUNT; i++){
            int finalI = i;
            register.registerAtOrNull(
                    Capabilities.FluidHandler.BLOCK,
                    CAPS.get(i).outputOil,
                    state -> state.getFluidOilOutputCap(finalI)
            );
            register.registerAtOrNull(
                    Capabilities.FluidHandler.BLOCK,
                    CAPS.get(i).outputGas,
                    state -> state.getFluidGasOutputCap(finalI)
            );
            register.registerAtOrNull(
                    Capabilities.ItemHandler.BLOCK,
                    CAPS.get(i).inputItem,
                    state -> state.getItemInputCap(finalI)
            );
            register.registerAtOrNull(
                    Capabilities.ItemHandler.BLOCK,
                    CAPS.get(i).outputItem,
                    state -> state.getItemOutputCap(finalI)
            );
            register.registerAtBlockPos(
                    MachineInterfaceHandler.IMachineInterfaceConnection.CAPABILITY,
                    MF_OFFSETS.get(i).redStone.posInMultiblock(),
                    state -> state.getMIFHandler(finalI));
        }
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType) {
        return AdvancedCokeOvenShapes.SHAPE_GETTER;
    }

    @Override
    public void dropExtraItems(State state, Consumer<ItemStack> drop)
    {
        state.threads.forEach(t -> MBInventoryUtils.dropItems(t.inventory, drop));
    }




    public static class CokeOvenThread
            implements ProcessContext.ProcessContextInMachine<AdvancedCokeOvenRecipe>
    {
        private static final int INPUT_SLOT_I = 0;
        private static final int OUTPUT_SLOT_I = 1;
        public static final int NUM_SLOTS = 2;
        private boolean active;
        private final Supplier<AveragingEnergyStorage> energySup;

        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final MultiblockProcessor.InMachineProcessor<AdvancedCokeOvenRecipe> processor;

        private final SlotwiseItemHandler inventory;


        public final AdvancedCokeOvenTanks tanks = new AdvancedCokeOvenLogic.AdvancedCokeOvenTanks();
        private final IFluidTank[] tankArray = {tanks.outputOil, tanks.outputGas};

        private final Supplier<@Nullable IFluidHandler> fluidOilOutput;
        private final Supplier<@Nullable IFluidHandler> fluidGasOutput;
        private final IItemHandler itemInputCap;
        private final IItemHandler itemOutputCap;
        private final DroppingMultiblockOutput outputItemInWorld;
        private final IFluidHandler fluidOilOutputCap;
        private final IFluidHandler fluidGasOutputCap;

        private final MachineInterfaceHandler.IMachineInterfaceConnection mifHandler;

        private int index;
        private float recipeProgress = 0;

        // Client will trace the flag value down below.
        // When Server Flag change(add 1), client will compare its own value and find its difference.
        // So that client will know what time it should update its Animation.
        private int outputFlag = 0;
        private int outputFlagClient = 0;

        private int outputFlagforTickingClient = 0;

        public CokeOvenThread(int index, Supplier<AveragingEnergyStorage> energySup , IInitialMultiblockContext<AdvancedCokeOvenLogic.State> ctx){
            this.index = index;
            this.energySup = energySup;
            this.outputItemInWorld = new DroppingMultiblockOutput(MF_OFFSETS.get(index).outputItem, ctx);
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            this.inventory = SlotwiseItemHandler.makeWithGroups(List.of(
                    new SlotwiseItemHandler.IOConstraintGroup(
                            SlotwiseItemHandler.IOConstraint.input((item) -> AdvancedCokeOvenRecipe.isInputValid(ctx.levelSupplier().get(), item)), 1),
                    new SlotwiseItemHandler.IOConstraintGroup(SlotwiseItemHandler.IOConstraint.OUTPUT, 1)
            ), markDirty);
            this.fluidOilOutput = ctx.getCapabilityAt(Capabilities.FluidHandler.BLOCK, MF_OFFSETS.get(index).outputOil);
            this.fluidGasOutput = ctx.getCapabilityAt(Capabilities.FluidHandler.BLOCK, MF_OFFSETS.get(index).outputGas);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(
                    1,0,1,markDirty, AdvancedCokeOvenRecipe.RECIPES::getById
            );
            this.itemInputCap = new WrappingItemHandler(
                    inventory, true, false, new WrappingItemHandler.IntRange(INPUT_SLOT_I, INPUT_SLOT_I+1)
            );
            this.itemOutputCap = new WrappingItemHandler(
                    inventory, false, true, new WrappingItemHandler.IntRange(OUTPUT_SLOT_I, OUTPUT_SLOT_I+1)
            );
            this.fluidOilOutputCap = ArrayFluidHandler.drainOnly(tanks.outputOil, markDirty);
            this.fluidGasOutputCap = ArrayFluidHandler.drainOnly(tanks.outputGas, markDirty);
            this.mifHandler = () -> new MachineInterfaceHandler.MachineCheckImplementation[]{
                    new MachineInterfaceHandler.MachineCheckImplementation<>((BooleanSupplier)() -> this.active, MachineInterfaceHandler.BASIC_ACTIVE),
                    //new MachineInterfaceHandler.MachineCheckImplementation<>(processor, MachineInterfaceHandler.BASIC_ITEM_IN, processor.getMachineInterfaceOptions(true)),
                    new MachineInterfaceHandler.MachineCheckImplementation<>(itemInputCap, MachineInterfaceHandler.BASIC_ITEM_IN),
                    new MachineInterfaceHandler.MachineCheckImplementation<>(itemOutputCap, MachineInterfaceHandler.BASIC_ITEM_OUT),
                    new MachineInterfaceHandler.MachineCheckImplementation<>(fluidOilOutputCap, MachineInterfaceHandler.BASIC_FLUID_OUT),
                    new MachineInterfaceHandler.MachineCheckImplementation<>(fluidGasOutputCap, MachineInterfaceHandler.BASIC_FLUID_OUT)
            };

        }

        public void tickServer(IMultiblockContext<AdvancedCokeOvenLogic.State> context, Consumer<Boolean> shouldUpdate){
            // processor do Tick and get status of 'active'
            final boolean active = this.processor.tickServer(this, context.getLevel(), context.getRedstoneInputValue(MF_OFFSETS.get(index).redStone, 0) == 0);

            // Request an update to the BE if the value of 'state.active' changes
            if(active!=this.active)
            {
                this.active = active;
                shouldUpdate.accept(true);
            }

            // Entry: Try adding new processes to the queue of processor
            enqueueProcesses(context.getLevel().getRawLevel());

            // output item
            if(context.getLevel().shouldTickModulo(10))
                handleItemOutput(context, shouldUpdate);

            FluidUtils.multiblockFluidOutput(
                    this.fluidGasOutput.get(), this.tanks.outputGas,
                    -1, -1,null
            );
            FluidUtils.multiblockFluidOutput(
                    this.fluidOilOutput.get(), this.tanks.outputOil,
                    -1, -1,null
            );

            //update recipe progress percentage
            if(!this.processor.getQueue().isEmpty()){
                AdvancedCokeOvenRecipe recipe = this.processor.getQueue().getFirst().getRecipe(context.getLevel().getRawLevel());
                if(recipe!= null){
                    int current =  this.processor.getQueue().getFirst().processTick;
                    int total = recipe.getTotalProcessTime();
                    if(this.recipeProgress != (float)current/total)
                        shouldUpdate.accept(true);
                    this.recipeProgress = (float) current/total;
                }
                else{
                    this.recipeProgress = 0;
                    shouldUpdate.accept(true);
                }
            }
            else{
                this.recipeProgress = 0;
                shouldUpdate.accept(true);
            }

        }

        public void tickClient(IMultiblockContext<AdvancedCokeOvenLogic.State> ctx){
            if(outputFlagforTickingClient != outputFlag){
                outputFlagforTickingClient = outputFlag;
                final Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(1.5, 2.5, 3.5));
                ctx.getLevel().getRawLevel().playLocalSound(soundPos.x,soundPos.y,soundPos.z, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1f, 0.75f, true);
            }
        }

        private void enqueueProcesses(Level level){
            final ItemStack inputItem = this.inventory.getStackInSlot(INPUT_SLOT_I);

            if(this.energySup.get().getEnergyStored() <= 0 || this.processor.getQueueSize() >= this.processor.getMaxQueueSize())
                return;
            RecipeHolder<AdvancedCokeOvenRecipe> recipe = AdvancedCokeOvenRecipe.findRecipe(level, inputItem);
            if(recipe==null){
                return;
            }
            IMMultiblockProcessInMachine<AdvancedCokeOvenRecipe> process = new IMMultiblockProcessInMachine<>(recipe, INPUT_SLOT_I);
            this.processor.addProcessToQueue(process, level, false);
        }

        private void handleItemOutput(IMultiblockContext<AdvancedCokeOvenLogic.State> ctx,  Consumer<Boolean> shouldUpdate)
        {
            final ItemStack fullOutputStack = this.inventory.getStackInSlot(OUTPUT_SLOT_I);
            if(fullOutputStack.isEmpty())
                return;
            int outputCount = Math.min(fullOutputStack.getCount(), 16);
            ItemStack stackTake = fullOutputStack.copyWithCount(outputCount);
            this.outputItemInWorld.insertOrDrop(stackTake, ctx.getLevel());
            fullOutputStack.shrink(outputCount);

            outputFlag++;
            shouldUpdate.accept(true);


        }


        public CompoundTag toNBT(HolderLookup.Provider provider){
            CompoundTag tag = new CompoundTag();
            tag.putInt("index", this.index);
            tag.put("inventory", inventory.serializeNBT(provider));
            tag.put("tanks", tanks.toNBT(provider));
            tag.put("processor", processor.toNBT(provider));
            return tag;
        }

        public void fromNBT(CompoundTag nbt, HolderLookup.Provider provider){
            this.index = nbt.getInt("index");
            inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
            tanks.readNBT(provider, nbt.getCompound("tanks"));
            processor.fromNBT(
                    nbt.get("processor"),
                    (getRecipe, data, p) -> new IMMultiblockProcessInMachine<>(getRecipe, data),
                    provider
            );
        }

        public CompoundTag toSyncNBT(HolderLookup.Provider provider)
        {
            CompoundTag tag =  new CompoundTag();
            tag.putBoolean("active", active);
            tag.putFloat("progress", recipeProgress);
            tag.putInt("output_flag", outputFlag);
            return tag;
        }

        public void fromSyncNBT(CompoundTag nbt, HolderLookup.Provider provider)
        {
            active = nbt.getBoolean("active");
            recipeProgress = nbt.getFloat("progress");
            outputFlag = nbt.getInt("output_flag");
        }

        @Override
        public AveragingEnergyStorage getEnergy() {
            return energySup.get();
        }

        @Override
        public SlotwiseItemHandler getInventory() {
            return inventory;
        }

        @Override
        public IFluidTank[] getInternalTanks() {
            return tankArray;
        }

        @Override
        public int[] getOutputSlots() {
            return new int[]{1};
        }

        @Override
        public int[] getOutputTanks() {
            return new int[]{0,1};
        }

        public AdvancedCokeOvenTanks getTanks(){
            return tanks;
        }

        public float getRecipeProgress() {
            return recipeProgress;
        }

        // Client Side Only. Update its own flag copy for next comparing
        public boolean getAndUpdateOutputFlagClientSide(){
            boolean isDifferent = outputFlag != outputFlagClient;
            outputFlagClient = outputFlag;
            return isDifferent;
        }
        
    }



    //===========================================================
    //                         State
    //===========================================================

    public static class State implements IMultiblockState {
        public final AveragingEnergyStorage energy = new AveragingEnergyStorage(100000);
        public List<CokeOvenThread> threads = new ArrayList<>(THREAD_COUNT);
        private BooleanSupplier isPlayingSound = () -> false;
        private final AdvancedCokeOvenRenderer.CokeOvenAnimation[] animationInstances = new AdvancedCokeOvenRenderer.CokeOvenAnimation[THREAD_COUNT];

        public State(IInitialMultiblockContext<AdvancedCokeOvenLogic.State> ctx) {
            for(int i = 0; i < THREAD_COUNT; i++){
                threads.add(new CokeOvenThread(i, () -> energy, ctx));
            }
        }

        Boolean doUpdate = false;
        public void tickServer(IMultiblockContext<AdvancedCokeOvenLogic.State> context){
            threads.forEach(cokeOvenThread -> cokeOvenThread.tickServer(context, b -> doUpdate = b));
            if(doUpdate){
                context.requestMasterBESync();
                doUpdate = false;
            }
        }

        public void tickClient(IMultiblockContext<AdvancedCokeOvenLogic.State> context){
            threads.forEach(t -> t.tickClient(context));
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            ListTag tagList = new ListTag(THREAD_COUNT);
            for(CokeOvenThread thread : threads){
                tagList.add(thread.toNBT(provider));
            }

            nbt.put("threads", tagList);
        }

        @Override
        public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            ListTag tagList = nbt.getList("threads", Tag.TAG_COMPOUND);
            for(int i = 0; i < THREAD_COUNT; i++){
                threads.get(i).fromNBT(tagList.getCompound(i), provider);
            }
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            ListTag tagList = new ListTag(THREAD_COUNT);
            for(CokeOvenThread thread : threads){
                tagList.add(thread.toSyncNBT(provider));
            }

            nbt.put("threads", tagList);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            ListTag tagList = nbt.getList("threads", Tag.TAG_COMPOUND);
            for(int i = 0; i < THREAD_COUNT; i++){
                threads.get(i).fromSyncNBT(tagList.getCompound(i), provider);
            }
        }

        public Supplier<@Nullable IFluidHandler> getFluidOilFluidHandler(int index){
            return threads.get(index).fluidOilOutput;
        }

        public Supplier<@Nullable IFluidHandler> getFluidGasFluidHandler(int index){
            return threads.get(index).fluidGasOutput;
        }

        public IItemHandler getItemInputCap(int index){
            return threads.get(index).itemInputCap;
        }
        public IItemHandler getItemOutputCap(int index){
            return threads.get(index).itemOutputCap;
        }
        public IFluidHandler getFluidOilOutputCap(int index){
            return threads.get(index).fluidOilOutputCap;
        }
        public IFluidHandler getFluidGasOutputCap(int index){
            return threads.get(index).fluidGasOutputCap;
        }

        public MachineInterfaceHandler.IMachineInterfaceConnection getMIFHandler(int index){
            return threads.get(index).mifHandler;
        }

        public boolean anyActive(){
            return threads.stream().anyMatch(t -> t.active);
        }

        public float getRecipeProgress(int index){
            return threads.get(index).recipeProgress;
        }

        public <T> List<T> getAllOf(Function<CokeOvenThread, T> getWhatFromThread){
            List<T> list = new ArrayList<>(THREAD_COUNT);
            for(CokeOvenThread t : threads){
                list.add(getWhatFromThread.apply(t));
            }
            return list;
        }

        public boolean getAndUpdateOutputFlagClientSide(int index){
            return threads.get(index).getAndUpdateOutputFlagClientSide();
        }

        public AdvancedCokeOvenRenderer.CokeOvenAnimation getAnimationInstance(int index) {
            return animationInstances[index];
        }
        public void removeAnimationInstance(int index) {
            animationInstances[index] = null;
        }
        public void addAnimationInstance(int index, long gameTime, float partialTicks) {
            animationInstances[index] = new AdvancedCokeOvenRenderer.CokeOvenAnimation(gameTime, partialTicks, 24, 24);
        }
    }

    public record AdvancedCokeOvenMFOffsets(MultiblockFace redStone, MultiblockFace inputItem, MultiblockFace outputOil, MultiblockFace outputGas, MultiblockFace outputItem){};
    public record AdvancedCokeOvenCaps(CapabilityPosition inputItem, CapabilityPosition outputOil, CapabilityPosition outputGas, CapabilityPosition outputItem){};

    public record AdvancedCokeOvenTanks(FluidTank outputOil, FluidTank outputGas)
    {

        public AdvancedCokeOvenTanks()
        {
            this(new FluidTank(TANK_CAPACITY), new FluidTank(TANK_CAPACITY));
        }

        public Tag toNBT(HolderLookup.Provider provider)
        {
            CompoundTag tag = new CompoundTag();
            tag.put("output_oil", outputOil.writeToNBT(provider, new CompoundTag()));
            tag.put("output_gas", outputGas.writeToNBT(provider, new CompoundTag()));
            return tag;
        }

        public void readNBT(HolderLookup.Provider provider, CompoundTag tag)
        {
            outputOil.readFromNBT(provider, tag.getCompound("output_oil"));
            outputGas.readFromNBT(provider, tag.getCompound("output_gas"));
        }
    }
}
