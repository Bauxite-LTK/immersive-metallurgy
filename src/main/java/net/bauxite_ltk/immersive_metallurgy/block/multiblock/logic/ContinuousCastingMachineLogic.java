package net.bauxite_ltk.immersive_metallurgy.block.multiblock.logic;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.FermenterLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import blusunrize.immersiveengineering.common.util.inventory.WrappingItemHandler;
import blusunrize.immersiveengineering.common.util.sound.MultiblockSound;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.process.ContinuousCastingMachineProcessInMachine;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.shapes.ContinuousCastingMachineShapes;
import net.bauxite_ltk.immersive_metallurgy.crafting.GasFuelRecipe;
import net.bauxite_ltk.immersive_metallurgy.crafting.ContinuousCastingMachineRecipe;
import net.bauxite_ltk.immersive_metallurgy.fluid.IMFluids;
import net.bauxite_ltk.immersive_metallurgy.tags.IMTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ContinuousCastingMachineLogic implements
        IMultiblockLogic<ContinuousCastingMachineLogic.State>,
        IServerTickableComponent<ContinuousCastingMachineLogic.State>,
        IClientTickableComponent<ContinuousCastingMachineLogic.State> {
    public static final BlockPos MASTER_OFFSET = new BlockPos(1, 0, 3);
    public static final BlockPos REDSTONE_POS = new BlockPos(1, 3, 6);
    public static final MultiblockFace INPUT_ENERGY_OFFSET = new MultiblockFace(1,1,-1,RelativeBlockFace.BACK);
    public static final MultiblockFace INPUT_GAS_LEFT_OFFSET = new MultiblockFace(3,0,3, RelativeBlockFace.RIGHT);
    public static final MultiblockFace INPUT_GAS_RIGHT_OFFSET = new MultiblockFace(-1,0,3, RelativeBlockFace.LEFT);
    public static final MultiblockFace INPUT_METAL_OFFSET = new MultiblockFace(1,4,-1, RelativeBlockFace.BACK);
    public static final MultiblockFace INPUT_WATER_OFFSET = new MultiblockFace(1,4,7, RelativeBlockFace.FRONT);
    public static final MultiblockFace OUTPUT_INGOT_OFFSET = new MultiblockFace(1,1,7, RelativeBlockFace.FRONT);


    public static final CapabilityPosition INPUT_ENERGY = CapabilityPosition.opposing(INPUT_ENERGY_OFFSET);
    public static final CapabilityPosition INPUT_GAS_LEFT_CAP = CapabilityPosition.opposing(INPUT_GAS_LEFT_OFFSET);
    public static final CapabilityPosition INPUT_GAS_RIGHT_CAP = CapabilityPosition.opposing(INPUT_GAS_RIGHT_OFFSET);
    public static final CapabilityPosition INPUT_METAL_CAP = CapabilityPosition.opposing(INPUT_METAL_OFFSET);
    public static final CapabilityPosition INPUT_WATER_CAP = CapabilityPosition.opposing(INPUT_WATER_OFFSET);
    public static final CapabilityPosition OUTPUT_INGOT_CAP = CapabilityPosition.opposing(OUTPUT_INGOT_OFFSET);


    public static final int LIQUID_METAL_CAPACITY = 4 * FluidType.BUCKET_VOLUME;
    public static final int WATER_CAPACITY = 8 * FluidType.BUCKET_VOLUME;
    public static final int GAS_CAPACITY = 8 * FluidType.BUCKET_VOLUME;
    public static final int ENERGY_CAPACITY = 96000;
    public static final int MAX_FUEL_TICKS = 1200;
    public static final int NUM_SLOTS = 1;

    @Override
    public State createInitialState(IInitialMultiblockContext<State> capabilitySource) {
        return new ContinuousCastingMachineLogic.State(capabilitySource);
    }

    @Override
    public void tickClient(IMultiblockContext<State> context) {
        final State state = context.getState();
        if(!state.isPlayingSound.getAsBoolean())
        {
            final Vec3 soundPos = context.getLevel().toAbsolute(new Vec3(1.5, 2.5, 3.5));
            state.isPlayingSound = MultiblockSound.startSound(
                    () -> state.active, context.isValid(), soundPos, IESounds.oreConveyor , 0.5f
            );
        }
        if(state.playNoWaterSound && context.getLevel().shouldTickModulo(40)){
            final Vec3 soundPos = context.getLevel().toAbsolute(new Vec3(1.5, 2.5, 3.5));
            context.getLevel().getRawLevel().playLocalSound(soundPos.x,soundPos.y,soundPos.z, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS,1f,1f,true);
        }
        if(state.playNoGasSound && context.getLevel().shouldTickModulo(40)){
            final Vec3 soundPos = context.getLevel().toAbsolute(new Vec3(1.5, 2.5, 3.5));
            context.getLevel().getRawLevel().playLocalSound(soundPos.x,soundPos.y,soundPos.z, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS,1f,1f, true);
        }
    }

    @Override
    public void tickServer(IMultiblockContext<State> context) {
        // Get State
        final ContinuousCastingMachineLogic.State state = context.getState();

        // 'state.processor' do Tick and get status of 'active'
        final boolean active = state.processor.tickServer(state, context.getLevel(), state.rsState.isEnabled(context));

        // Request an update to the BE if the value of 'state.active' changes
        if(active!=state.active)
        {
            state.active = active;
            context.requestMasterBESync();
        }

        // Entry Try consume gas fuel
        consumeGasFuel(state, context.getLevel().getRawLevel());

        // Entry: Try adding new processes to the queue of processor
        enqueueProcesses(state, context.getLevel().getRawLevel());

        // output item
        if(context.getLevel().shouldTickModulo(10))
            handleItemOutput(context);

        //update recipe progress percentage
        if(!state.processor.getQueue().isEmpty()){
            ContinuousCastingMachineRecipe recipe = state.processor.getQueue().getFirst().getRecipe(context.getLevel().getRawLevel());
            if(recipe!= null){
                int current =  state.processor.getQueue().getFirst().processTick;
                int total = recipe.getTotalProcessTime();
                state.recipeProgress = (float) current/total;
            }
            else{
                state.recipeProgress = 0;
            }
        }
        else{
            state.recipeProgress = 0;
        }

    }

    private void enqueueProcesses(ContinuousCastingMachineLogic.State state, Level level) {

        final FluidStack inputFluid = state.tanks.inputMetal.getFluid();

        if(state.energy.getEnergyStored() <= 0 || state.processor.getQueueSize() >= state.processor.getMaxQueueSize())
            return;
        RecipeHolder<ContinuousCastingMachineRecipe> recipe = ContinuousCastingMachineRecipe.findRecipe(level, inputFluid);
        if(recipe==null){
            return;
        }
        ContinuousCastingMachineProcessInMachine<ContinuousCastingMachineRecipe> process = new ContinuousCastingMachineProcessInMachine<>(recipe);
        process.setInputTanks(2);
        state.processor.addProcessToQueue(process, level, false);
    }

    private void consumeGasFuel(ContinuousCastingMachineLogic.State state, Level level){
        // Get fluid in the fuel tank
        final FluidStack inputGas = state.tanks.inputGas.getFluid();

        // Check fluid in the fuel tank. If it's invalid then quit.
        RecipeHolder<GasFuelRecipe> fuelRecipe = GasFuelRecipe.findRecipe(level, inputGas);
        if(fuelRecipe == null) {
            return;
        }

        // Get details of the gas fuel burning recipe
        int burnTime = fuelRecipe.value().burnTime;
        int consumeAmount = fuelRecipe.value().inputGas.amount();

        // If burning the gas fuel will cause an excess of burn ticks, then quit.
        if(state.fuelTicks + burnTime > MAX_FUEL_TICKS){
            return;
        }

        // If all the requirements are met, then consume the fuel and increase the fuelTicks.
        state.tanks.inputGas.drain(consumeAmount, IFluidHandler.FluidAction.EXECUTE);
        state.fuelTicks += burnTime;
    }

    private void handleItemOutput(IMultiblockContext<ContinuousCastingMachineLogic.State> ctx)
    {
        final ContinuousCastingMachineLogic.State state = ctx.getState();
        final ItemStack fullOutputStack = state.inventory.getStackInSlot(0);
        if(fullOutputStack.isEmpty())
            return;
        int outputCount = Math.min(fullOutputStack.getCount(), 16);
        ItemStack stack = fullOutputStack.copyWithCount(outputCount);
        final ItemStack remaining = Utils.insertStackIntoInventory(state.itemIngotOutput, stack, false);
        if(remaining.isEmpty())
        {
            fullOutputStack.shrink(outputCount);
            ctx.markMasterDirty();
        }
        else{
            fullOutputStack.shrink(outputCount - remaining.getCount());
            ctx.markMasterDirty();
        }
    }

    @Override
    public void dropExtraItems(State state, Consumer<ItemStack> drop)
    {
        MBInventoryUtils.dropItems(state.inventory, drop);
    }



    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType) {
        return ContinuousCastingMachineShapes.SHAPE_GETTER;
    }

    @Override
    public void registerCapabilities(CapabilityRegistrar<ContinuousCastingMachineLogic.State> register)
    {
        register.registerAtOrNull(Capabilities.EnergyStorage.BLOCK, INPUT_ENERGY, state -> state.energy);
        register.register(Capabilities.FluidHandler.BLOCK, (state,position) -> {
            if(INPUT_GAS_LEFT_CAP.equals(position) || INPUT_GAS_RIGHT_CAP.equals(position))
                return state.inputFluidGasCap;
            else if(INPUT_WATER_CAP.equals(position))
                return state.inputFluidWaterCap;
            else if(INPUT_METAL_CAP.equals(position))
                return state.inputFluidMetalCap;
            else
                return null;
        });
        register.registerAtOrNull(Capabilities.ItemHandler.BLOCK, OUTPUT_INGOT_CAP, state -> state.outputItemIngotCap);
        register.registerAtBlockPos(MachineInterfaceHandler.IMachineInterfaceConnection.CAPABILITY, REDSTONE_POS, state -> state.mifHandler);
    }


    public static class State implements IMultiblockState, ProcessContext.ProcessContextInMachine<ContinuousCastingMachineRecipe>
    {
        private final AveragingEnergyStorage energy = new AveragingEnergyStorage(ENERGY_CAPACITY);
        private int fuelTicks = 0;

        private boolean active;
        private float recipeProgress = 0;
        private BooleanSupplier isPlayingSound = () -> false;
        private boolean playNoWaterSound = false;
        private boolean playNoGasSound = false;

        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final MultiblockProcessor.InMachineProcessor<ContinuousCastingMachineRecipe> processor;
        public final ContinuousCastingMachineLogic.ContinuousCastingMachineTanks tanks = new ContinuousCastingMachineLogic.ContinuousCastingMachineTanks();
        //private int temperature = 0;
        private final SlotwiseItemHandler inventory;

        private final IFluidTank[] tankArray = {tanks.inputGas, tanks.inputWater, tanks.inputMetal};
        private final Supplier<@Nullable IItemHandler> itemIngotOutput;
        private final IFluidHandler inputFluidGasCap;
        private final IFluidHandler inputFluidWaterCap;
        private final IFluidHandler inputFluidMetalCap;
        private final IItemHandler outputItemIngotCap;

        private final MachineInterfaceHandler.IMachineInterfaceConnection mifHandler;

        public State(IInitialMultiblockContext<ContinuousCastingMachineLogic.State> ctx)
        {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            //this.fluidOutput = ctx.getCapabilityAt(Capabilities.FluidHandler.BLOCK, OUTPUT_FLUID_OFFSET);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(
                    1, 0, 1, markDirty, ContinuousCastingMachineRecipe.RECIPES::getById
            );
            this.inventory = SlotwiseItemHandler.makeWithGroups(List.of(
                    new SlotwiseItemHandler.IOConstraintGroup(SlotwiseItemHandler.IOConstraint.OUTPUT, 1)
            ), markDirty);
            this.itemIngotOutput = ctx.getCapabilityAt(Capabilities.ItemHandler.BLOCK, OUTPUT_INGOT_OFFSET);
            this.outputItemIngotCap = new WrappingItemHandler(
                    getInventory(), false, true, new WrappingItemHandler.IntRange(0, 1)
            );
            this.inputFluidGasCap = new ArrayFluidHandler(
                    false, true, markDirty, tanks.inputGas
            );
            this.inputFluidWaterCap = new ArrayFluidHandler(
                    false, true, markDirty, tanks.inputWater
            );
            this.inputFluidMetalCap = new ArrayFluidHandler(
                    false, true, markDirty, tanks.inputMetal
            );

            this.mifHandler = () -> new MachineInterfaceHandler.MachineCheckImplementation[]{
                    new MachineInterfaceHandler.MachineCheckImplementation<>((BooleanSupplier)() -> this.active, MachineInterfaceHandler.BASIC_ACTIVE),
                    new MachineInterfaceHandler.MachineCheckImplementation<>(energy, MachineInterfaceHandler.BASIC_ENERGY),
                    new MachineInterfaceHandler.MachineCheckImplementation<>(tanks.inputGas, MachineInterfaceHandler.BASIC_FLUID_IN),
                    new MachineInterfaceHandler.MachineCheckImplementation<>(tanks.inputWater, MachineInterfaceHandler.BASIC_FLUID_IN),
                    new MachineInterfaceHandler.MachineCheckImplementation<>(tanks.inputMetal, MachineInterfaceHandler.BASIC_FLUID_IN),
            };
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider)
        {
            nbt.put("energy", energy.serializeNBT(provider));
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.put("processor", processor.toNBT(provider));
            nbt.putInt("fuelTicks", fuelTicks);
            nbt.put("inventory", inventory.serializeNBT(provider));
        }

        @Override
        public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            energy.deserializeNBT(provider, nbt.get("energy"));
            processor.fromNBT(
                    nbt.get("processor"),
                    (getRecipe, data, p) -> new ContinuousCastingMachineProcessInMachine<>(getRecipe, data),
                    provider
            );
            tanks.readNBT(provider, nbt.getCompound("tanks"));
            fuelTicks = nbt.getInt("fuelTicks");
            inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt, HolderLookup.Provider provider)
        {

            nbt.putBoolean("active", active);
            nbt.putBoolean("play_no_water_sound", playNoWaterSound);
            nbt.putBoolean("play_no_gas_sound", playNoGasSound);
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.putFloat("progress", recipeProgress);
            nbt.putInt("fuelTicks", fuelTicks);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt, HolderLookup.Provider provider)
        {

            active = nbt.getBoolean("active");
            playNoWaterSound = nbt.getBoolean("play_no_water_sound");
            playNoGasSound = nbt.getBoolean("play_no_gas_sound");
            tanks.readNBT(provider,nbt.getCompound("tanks"));
            recipeProgress = nbt.getFloat("progress");
            fuelTicks = nbt.getInt("fuelTicks");
        }


        @Override
        public AveragingEnergyStorage getEnergy() { return energy; }

        @Override
        public IFluidTank[] getInternalTanks()
        {
            return tankArray;
        }

        @Override
        public int[] getOutputSlots() {
            return new int[]{0};
        }

        @Override
        public SlotwiseItemHandler getInventory() {
            return inventory;
        }

        public int getFuelTicks() {return fuelTicks;}

        public boolean canConsumeWater() {
            return tanks.inputWater.drain(50, IFluidHandler.FluidAction.SIMULATE).getAmount() == 50;
        }

        public void decreaseFuelTicks() {fuelTicks--;}
        public void consumeWater() {tanks.inputWater.drain(50, IFluidHandler.FluidAction.EXECUTE);}

        public float getRecipeProgress() {
            return recipeProgress;
        }

        public boolean shouldRenderActive()
        {
            return true;
        }

        public boolean isActive()
        {
            return active;
        }

        public void setPlayNoWaterSound(boolean playNoWaterSound) {
            this.playNoWaterSound = playNoWaterSound;
        }

        public void setPlayNoGasSound(boolean setPlayNoGasSound) {
            this.playNoGasSound = setPlayNoGasSound;
        }

    }

    public record ContinuousCastingMachineTanks(FluidTank inputGas, FluidTank inputWater, FluidTank inputMetal){
        public ContinuousCastingMachineTanks(){
            this(
                    new FluidTank(GAS_CAPACITY) {
                        @Override
                        public boolean isFluidValid(final FluidStack stack) {
                            return stack.is(IMTags.Fluids.GAS_FUEL);
                        }
                    },
                    new FluidTank(WATER_CAPACITY){
                        @Override
                        public boolean isFluidValid(final FluidStack stack) {
                            return stack.is(Fluids.WATER);
                        }
                    },

                    new FluidTank(LIQUID_METAL_CAPACITY){
                        @Override
                        public boolean isFluidValid(final FluidStack stack){
                            return stack.is(IMTags.Fluids.TEMPERATURE_MOLTEN_FLUID);
                        }
                    }
            );
        }

        public Tag toNBT(HolderLookup.Provider provider)
        {
            CompoundTag tag = new CompoundTag();
            tag.put("inputGas", inputGas.writeToNBT(provider, new CompoundTag()));
            tag.put("inputWater", inputWater.writeToNBT(provider, new CompoundTag()));
            tag.put("inputMetal", inputMetal.writeToNBT(provider, new CompoundTag()));
            return tag;
        }

        public void readNBT(HolderLookup.Provider provider, CompoundTag tag)
        {
            inputGas.readFromNBT(provider, tag.getCompound("inputGas"));
            inputWater.readFromNBT(provider, tag.getCompound("inputWater"));
            inputMetal.readFromNBT(provider, tag.getCompound("inputMetal"));
        }
    }
}
