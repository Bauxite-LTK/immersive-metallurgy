package net.bauxite_ltk.immersive_metallurgy.block.multiblock.logic;


import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.register.IEParticles;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import blusunrize.immersiveengineering.common.util.inventory.WrappingItemHandler;
import blusunrize.immersiveengineering.common.util.sound.MultiblockSound;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.process.IMMultiblockProcessInMachine;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.shapes.EliteBlastFurnaceShapes;
import net.bauxite_ltk.immersive_metallurgy.crafting.EliteBlastFurnaceRecipe;
import net.bauxite_ltk.immersive_metallurgy.fluid.IMFluids;
import net.bauxite_ltk.immersive_metallurgy.util.IMMultiblockSound;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EliteBlastFurnaceLogic implements
        IMultiblockLogic<EliteBlastFurnaceLogic.State>,
        IServerTickableComponent<EliteBlastFurnaceLogic.State>,
        IClientTickableComponent<EliteBlastFurnaceLogic.State> {
    public static final BlockPos MASTER_OFFSET = new BlockPos(2, 1, 1);
    public static final BlockPos REDSTONE_POS = new BlockPos(2, 2, 3);
    private static final MultiblockFace OUTPUT_SLAG_OFFSET = new MultiblockFace(2,0,-1,RelativeBlockFace.BACK);
    private static final MultiblockFace OUTPUT_METAL_OFFSET = new MultiblockFace(2,0,4,RelativeBlockFace.FRONT);
    private static final MultiblockFace OUTPUT_GAS_OFFSET = new MultiblockFace(2,6,4,RelativeBlockFace.FRONT);
    private static final MultiblockFace INPUT_AIR_LEFT_OFFSET = new MultiblockFace(-1,0,1,RelativeBlockFace.LEFT);
    private static final MultiblockFace INPUT_AIR_RIGHT_OFFSET = new MultiblockFace(5,0,1,RelativeBlockFace.RIGHT);
    private static final MultiblockFace INPUT_ORE_OFFSET = new MultiblockFace(2,7,1,RelativeBlockFace.DOWN);

    private static final CapabilityPosition OUTPUT_METAL_CAP = CapabilityPosition.opposing(OUTPUT_METAL_OFFSET);
    private static final CapabilityPosition OUTPUT_SLAG_CAP = CapabilityPosition.opposing(OUTPUT_SLAG_OFFSET);
    private static final CapabilityPosition OUTPUT_GAS_CAP = CapabilityPosition.opposing(OUTPUT_GAS_OFFSET);

    private static final CapabilityPosition INPUT_AIR_LEFT_CAP = CapabilityPosition.opposing(INPUT_AIR_LEFT_OFFSET);
    private static final CapabilityPosition INPUT_AIR_RIGHT_CAP = CapabilityPosition.opposing(INPUT_AIR_RIGHT_OFFSET);
    private static final CapabilityPosition INPUT_ORE_CAP = CapabilityPosition.opposing(INPUT_ORE_OFFSET);


    public static final int HOT_AIR_CAPACITY = 400;
    public static final int METAL_CAPACITY = 12 * FluidType.BUCKET_VOLUME;
    public static final int GAS_CAPACITY = 48 * FluidType.BUCKET_VOLUME;
    public static final int NUM_INPUT_SLOTS = 4;
    public static final int OUTPUT_SLOT = NUM_INPUT_SLOTS;
    public static final int NUM_SLOTS = 5;
    private static final int NATURAL_TEMPERATURE = 24;


    public State createInitialState(IInitialMultiblockContext<State> capabilitySource) {
        return new State(capabilitySource);
    }

    @Override
    public void tickServer(IMultiblockContext<State> context) {
        final State state = context.getState();

        // IE Multiblock Processor seems like it will fail while handle recipes that cost Energy of 0.
        // So we have to set its energy cost to a non-zero value for our Elite Blast Furnace Recipes.
        // With code below, we actually make tricks to avoid energy cost, by continuously insert energy into the machine.
        state.energy.receiveEnergy(1000,false);

        boolean active = state.processor.tickServer(state, context.getLevel(), state.rsState.isEnabled(context));
        if(active!=state.active)
        {
            state.active = active;
            context.requestMasterBESync();
        }

        tryEnqueueProcesses(state, context.getLevel().getRawLevel());

        EliteBlastFurnaceTanks tanks = state.tanks;
        if(context.getLevel().shouldTickModulo(10)){
            heatUpOrCoolDown(context, state.rsState.isEnabled(context));
        }

        FluidUtils.multiblockFluidOutput(
                state.fluidOutputMetal.get(), state.tanks.outputMetal,
                -1, -1,null
        );
        FluidUtils.multiblockFluidOutput(
                state.fluidOutputGas.get(), state.tanks.outputGas,
                -1, -1,null
        );
    }

    private void tryEnqueueProcesses(State state, Level level) {
        if(state.processor.getQueueSize() >= state.processor.getMaxQueueSize())
            return;
        final int[] usedInvSlots = new int[NUM_INPUT_SLOTS];
        for(MultiblockProcess<?, ?> process : state.processor.getQueue())
            if(process instanceof IMMultiblockProcessInMachine)
                for(int i : ((IMMultiblockProcessInMachine<?>)process).getInputSlots())
                    usedInvSlots[i]++;

        Integer[] preferredSlots = new Integer[]{0, 1, 2, 3};
        Arrays.sort(preferredSlots, 0, NUM_INPUT_SLOTS, Comparator.comparingInt(arg0 -> usedInvSlots[arg0]));

        for(int slot : preferredSlots)
        {
            ItemStack stack = state.inventory.getStackInSlot(slot);
            if(stack.getCount() <= usedInvSlots[slot])
                continue;
            stack = stack.copy();
            stack.shrink(usedInvSlots[slot]);
            RecipeHolder<EliteBlastFurnaceRecipe> recipe = EliteBlastFurnaceRecipe.findRecipe(level, stack);
            if(recipe!=null && recipe.value().temperature <= state.temperature){
                IMMultiblockProcessInMachine<EliteBlastFurnaceRecipe> process = new IMMultiblockProcessInMachine<>(recipe, slot);
                state.processor.addProcessToQueue(process, level, false);
            }
        }
    }

    private void heatUpOrCoolDown(IMultiblockContext<EliteBlastFurnaceLogic.State> ctx, boolean doHeat){
        final State state = ctx.getState();
        EliteBlastFurnaceTanks tanks = state.tanks;
        int heatUp = 0;
        int curTemp = state.temperature;
        if(doHeat){
            if(!tanks.inputAirRight.isEmpty()){
                int extractR = tanks.inputAirRight.drain(400, IFluidHandler.FluidAction.EXECUTE).getAmount();
                heatUp += extractR;
            }
            if(!tanks.inputAirLeft.isEmpty()){
                int extractL = tanks.inputAirLeft.drain(400, IFluidHandler.FluidAction.EXECUTE).getAmount();
                heatUp += extractL;
            }
        }
        int coolDown = curTemp > NATURAL_TEMPERATURE ? (curTemp+4)/2 : 0;
        int deltaTemperature = Integer.compare(heatUp - coolDown, 0)*4;
        state.temperature += deltaTemperature;
        ctx.markMasterDirty();
        ctx.requestMasterBESync();
        handleItemOutput(ctx);
    }

    private void handleItemOutput(IMultiblockContext<EliteBlastFurnaceLogic.State> ctx)
    {
        final EliteBlastFurnaceLogic.State state = ctx.getState();
        final ItemStack fullOutputStack = state.inventory.getStackInSlot(OUTPUT_SLOT);
        if(fullOutputStack.isEmpty())
            return;
        int outputCount = Math.min(fullOutputStack.getCount(), 32);
        ItemStack stack = fullOutputStack.copyWithCount(outputCount);
        final ItemStack remaining = Utils.insertStackIntoInventory(state.itemOutputSlag, stack, false);
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
    public void tickClient(IMultiblockContext<State> context) {
        final State state = context.getState();
        if(!state.isPlayingSound.getAsBoolean())
        {
            final Vec3 soundPos = context.getLevel().toAbsolute(new Vec3(3.5, 1.5, 1.5));
            state.isPlayingSound = IMMultiblockSound.startSound(
                    () -> state.temperature>100, context.isValid(), soundPos, IESounds.refinery , () -> (float) state.temperature / 3200
            );
        }
        if(!state.isPlayingFlameSound.getAsBoolean()){
            final Vec3 soundPos = context.getLevel().toAbsolute(new Vec3(3.5, 1.5, 1.5));
            state.isPlayingFlameSound = MultiblockSound.startSound(
                    () -> state.active, context.isValid(), soundPos, IESounds.sprayFire , 1f
            );
        }
        if(!state.isPlayingBlastSound.getAsBoolean()){
            final Vec3 soundPos = context.getLevel().toAbsolute(new Vec3(3.5, 1.5, 1.5));
            state.isPlayingBlastSound = MultiblockSound.startSound(
                    () -> state.active, context.isValid(), soundPos, IESounds.preheater , 1f
            );
        }

        if(!state.active)
            return;
        final IMultiblockLevel level = context.getLevel();
        final Level rawLevel = level.getRawLevel();
        for(int i = 0; i < Math.max(1, state.processor.getQueueSize()*3); i++)
        {
            final Vec3 smokePos = level.toAbsolute(new Vec3(2.5,6.1,1.5));
            rawLevel.addAlwaysVisibleParticle(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    smokePos.x, smokePos.y, smokePos.z,
                    particleSpeed(0.009375), .0625, particleSpeed(0.009375)
            );
        }
    }

    private static double particleSpeed(double max)
    {
        return ApiUtils.RANDOM.nextDouble(-max, max);
    }

    @Override
    public void registerCapabilities(CapabilityRegistrar<State> register)
    {
        register.register(Capabilities.FluidHandler.BLOCK, (state,position) -> {
            if(OUTPUT_GAS_CAP.equals(position))
                return state.outputFluidGasCap;
            else if(OUTPUT_METAL_CAP.equals(position))
                return state.outputFluidMetalCap;
            else if(INPUT_AIR_LEFT_CAP.equals(position))
                return state.inputFluidAirLeftCap;
            else if(INPUT_AIR_RIGHT_CAP.equals(position))
                return state.inputFluidAirRightCap;
            else
                return null;
        });

        register.register(Capabilities.ItemHandler.BLOCK, (state, position) -> {
            if(OUTPUT_SLAG_CAP.equals(position))
                return state.outputItemSlagCap;
            else if(INPUT_ORE_CAP.equals(position))
                return state.inputItemOreCap;
            else
                return null;
        });
        register.registerAtBlockPos(MachineInterfaceHandler.IMachineInterfaceConnection.CAPABILITY, REDSTONE_POS, state -> state.mifHandler);
    }

    @Override
    public void dropExtraItems(State state, Consumer<ItemStack> drop)
    {
        MBInventoryUtils.dropItems(state.inventory, drop);
    }


    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType)
    {
        return EliteBlastFurnaceShapes.SHAPE_GETTER;
    }


    public static class State implements IMultiblockState, ProcessContext.ProcessContextInMachine<EliteBlastFurnaceRecipe>
    {
        private final AveragingEnergyStorage energy = new AveragingEnergyStorage(1000);

        private boolean active;
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final MultiblockProcessor.InMachineProcessor<EliteBlastFurnaceRecipe> processor;
        public final EliteBlastFurnaceTanks tanks = new EliteBlastFurnaceTanks();
        private final SlotwiseItemHandler inventory;
        private int temperature = NATURAL_TEMPERATURE;


        private final IFluidTank[] tankArray = {tanks.inputAirLeft, tanks.inputAirRight, tanks.outputMetal, tanks.outputGas};
        private final Supplier<@Nullable IFluidHandler> fluidOutputMetal;
        private final Supplier<@Nullable IFluidHandler> fluidOutputGas;
        private final Supplier<@Nullable IItemHandler> itemOutputSlag;
        private final IItemHandler inputItemOreCap;
        private final IItemHandler outputItemSlagCap;
        private final IFluidHandler inputFluidAirLeftCap;
        private final IFluidHandler inputFluidAirRightCap;
        private final IFluidHandler outputFluidMetalCap;
        private final IFluidHandler outputFluidGasCap;
        private BooleanSupplier isPlayingSound = () -> false;
        private BooleanSupplier isPlayingFlameSound = () -> false;
        private BooleanSupplier isPlayingBlastSound = () -> false;
        private final MachineInterfaceHandler.IMachineInterfaceConnection mifHandler;

        public State(IInitialMultiblockContext<State> ctx)
        {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            //this.fluidOutput = ctx.getCapabilityAt(Capabilities.FluidHandler.BLOCK, OUTPUT_FLUID_OFFSET);
            this.processor = new MultiblockProcessor.InMachineProcessor<>(
                    1, 0, 1, markDirty, EliteBlastFurnaceRecipe.RECIPES::getById
            );
            this.inventory = SlotwiseItemHandler.makeWithGroups(List.of(
                    new SlotwiseItemHandler.IOConstraintGroup(SlotwiseItemHandler.IOConstraint.NO_CONSTRAINT, NUM_INPUT_SLOTS),
                    new SlotwiseItemHandler.IOConstraintGroup(SlotwiseItemHandler.IOConstraint.OUTPUT, 1)
            ), markDirty);

            this.fluidOutputMetal = ctx.getCapabilityAt(Capabilities.FluidHandler.BLOCK, OUTPUT_METAL_OFFSET);
            this.itemOutputSlag = ctx.getCapabilityAt(Capabilities.ItemHandler.BLOCK, OUTPUT_SLAG_OFFSET);
            this.fluidOutputGas = ctx.getCapabilityAt(Capabilities.FluidHandler.BLOCK, OUTPUT_GAS_OFFSET);
            this.inputFluidAirLeftCap = new ArrayFluidHandler(
                    false, true, markDirty, tanks.inputAirLeft
            );
            this.inputFluidAirRightCap = new ArrayFluidHandler(
                    false, true, markDirty, tanks.inputAirRight
            );
            this.inputItemOreCap = new WrappingItemHandler(
                    getInventory(), true,false, new WrappingItemHandler.IntRange(0, NUM_INPUT_SLOTS)
            );
            this.outputItemSlagCap = new WrappingItemHandler(
                        getInventory(),false,true, new WrappingItemHandler.IntRange(OUTPUT_SLOT, OUTPUT_SLOT+1)
            );
            this.outputFluidMetalCap = new ArrayFluidHandler(
                true, false, markDirty, tanks.outputMetal
            );
            this.outputFluidGasCap = new ArrayFluidHandler(
                    true, false, markDirty, tanks.outputGas
            );
            this.mifHandler = () -> new MachineInterfaceHandler.MachineCheckImplementation[]{
                    new MachineInterfaceHandler.MachineCheckImplementation<>((BooleanSupplier)() -> this.active, MachineInterfaceHandler.BASIC_ACTIVE),
                    new MachineInterfaceHandler.MachineCheckImplementation<>(energy, MachineInterfaceHandler.BASIC_ENERGY),
                    new MachineInterfaceHandler.MachineCheckImplementation<>(tanks.inputAirLeft, MachineInterfaceHandler.BASIC_FLUID_IN),
                    new MachineInterfaceHandler.MachineCheckImplementation<>(tanks.inputAirRight, MachineInterfaceHandler.BASIC_FLUID_IN),
                    new MachineInterfaceHandler.MachineCheckImplementation<>(tanks.outputMetal, MachineInterfaceHandler.BASIC_FLUID_OUT),
                    new MachineInterfaceHandler.MachineCheckImplementation<>(tanks.outputMetal, MachineInterfaceHandler.BASIC_FLUID_OUT),
            };
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider)
        {
            nbt.put("energy", energy.serializeNBT(provider));
            nbt.put("inventory", inventory.serializeNBT(provider));
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.put("processor", processor.toNBT(provider));
            nbt.putInt("temperature", temperature);
        }

        @Override
        public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            energy.deserializeNBT(provider, nbt.get("energy"));
            inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
            processor.fromNBT(
                    nbt.get("processor"),
                    (getRecipe, data, p) -> new IMMultiblockProcessInMachine<>(getRecipe, data),
                    provider
            );
            tanks.readNBT(provider, nbt.getCompound("tanks"));
            temperature = nbt.getInt("temperature");
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt, HolderLookup.Provider provider)
        {

            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.putInt("temperature", temperature);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt, HolderLookup.Provider provider)
        {

            active = nbt.getBoolean("active");
            tanks.readNBT(provider,nbt.getCompound("tanks"));
            temperature = nbt.getInt("temperature");
        }

        @Override
        public int[] getOutputSlots()
        {
            return new int[]{OUTPUT_SLOT};
        }

        @Override
        public AveragingEnergyStorage getEnergy() { return energy; }

        @Override
        public IItemHandlerModifiable getInventory()
        {
            return inventory;
        }

        public int getTemperature()
        {
            return temperature;
        }

        @Override
        public IFluidTank[] getInternalTanks()
        {
            return tankArray;
        }

        @Override
        public int[] getOutputTanks()
        {
            return new int[]{2,3};
        }

        public boolean shouldRenderActive()
        {
            return true;
        }


        public boolean isActive()
        {
            return active;
        }




    }


    public record EliteBlastFurnaceTanks(FluidTank inputAirLeft, FluidTank inputAirRight, FluidTank outputMetal, FluidTank outputGas)
    {

        public EliteBlastFurnaceTanks()
        {
            this(
                    new FluidTank(HOT_AIR_CAPACITY){
                        @Override
                        public boolean isFluidValid(final FluidStack stack) {
                            return stack.is(IMFluids.HOT_AIR.source());
                        }
                    },
                    new FluidTank(HOT_AIR_CAPACITY){
                        @Override
                        public boolean isFluidValid(final FluidStack stack) {
                            return stack.is(IMFluids.HOT_AIR.source());
                        }
                    },
                    new FluidTank(METAL_CAPACITY),
                    new FluidTank(GAS_CAPACITY));
        }

        public Tag toNBT(HolderLookup.Provider provider)
        {
            CompoundTag tag = new CompoundTag();
            tag.put("inAirLeft", inputAirLeft.writeToNBT(provider, new CompoundTag()));
            tag.put("inAirRight", inputAirRight.writeToNBT(provider, new CompoundTag()));
            tag.put("outMetal", outputMetal.writeToNBT(provider, new CompoundTag()));
            tag.put("outGas", outputGas.writeToNBT(provider, new CompoundTag()));
            return tag;
        }

        public void readNBT(HolderLookup.Provider provider, CompoundTag tag)
        {
            inputAirLeft.readFromNBT(provider, tag.getCompound("inAirLeft"));
            inputAirRight.readFromNBT(provider, tag.getCompound("inAirRight"));
            outputMetal.readFromNBT(provider, tag.getCompound("outMetal"));
            outputGas.readFromNBT(provider, tag.getCompound("outGas"));
        }
    }
}
