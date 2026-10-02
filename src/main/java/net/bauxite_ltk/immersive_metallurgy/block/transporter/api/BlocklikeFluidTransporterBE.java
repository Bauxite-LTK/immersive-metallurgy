package net.bauxite_ltk.immersive_metallurgy.block.transporter.api;

import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.api.resourceHandler.FluidUniHandler;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.api.resourceHandler.IUniHandler;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.api.resourceHandler.blt.BLTSingleFluidUniHandler;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.api.resourceStorage.FluidUniStorage;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.api.resourceStorage.IUniStorage;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


public class BlocklikeFluidTransporterBE extends IEBaseBlockEntity implements IBlocklikeResourceTransporter<FluidStack> {

    /**
     * FluidResourceStorage works same as FluidTank
     * {@link IUniStorage} provides unified methods for handling specific type of Resources
     * In this case, {@link FluidUniStorage} handles {@link FluidStack}.
     */
    public final FluidUniStorage tank;

    public int forceAllocatedExecuteCount = 0;

    //Record six faces' allConnections
    protected byte connections = 0;

    List<TransportationData> tdList = new ArrayList<>(6);

    public Object2BooleanMap<Direction> sideConfig = new Object2BooleanOpenHashMap<>();
    {
        for(Direction d : DirectionUtils.VALUES){
            sideConfig.put(d, d.get3DDataValue() > 1);
        }
    }

    protected final Map<Direction, IEBlockCapabilityCaches.IEBlockCapabilityCache<IFluidHandler>> neighbors = IEBlockCapabilityCaches.allNeighbors(
            Capabilities.FluidHandler.BLOCK, this
    );

    protected final Map<Direction, IFluidHandler> sidedHandlers = new EnumMap<>(Direction.class);

    protected BlocklikeFluidTransporterBE(
            FluidUniStorage storage, BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType,pos,blockState);
        tank = storage;
        for(Direction f : DirectionUtils.VALUES)
            sidedHandlers.put(f, new BLTSingleFluidUniHandler(FluidUniHandler.cast(tank.getCompactParent()),this, f));
    }

    public static BlocklikeFluidTransporterBE create(
            int initialCapacity, BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState){
        return new BlocklikeFluidTransporterBE(
                new FluidUniStorage(initialCapacity),
                blockEntityType,
                pos,
                blockState
        );
    }

    int tickCount = 0;
    @Override
    public boolean shouldTick() {
        if(tickCount<5) return false;
        else return true;
    }

    @Override
    public void tickServer() {
        forceAllocatedExecuteCount = 0;
        if(shouldTick()){
            checkHeightLevelForSendingUpdate();
        }
        IBlocklikeResourceTransporter.super.tickServer();
        if(tickCount<5) tickCount++;
        else tickCount = 0;
    }




    @Override
    public void readCustomNBT(CompoundTag nbt, boolean descPacket, HolderLookup.Provider provider) {
        int[] config = nbt.getIntArray("sideConfig");
        for(int i = 0; i < 6; ++i)
        {
            Direction curDir = Direction.from3DDataValue(i);
            if(i < config.length)
            {
                boolean connected = config[i]!=0;
                sideConfig.put(curDir, connected);
                if(connected)
                    setValidHandler(curDir);
                else
                    invalidateHandler(curDir);
            }
            else
            {
                sideConfig.put(curDir, false);
                invalidateHandler(curDir);
            }
        }
        int oldTankAmount = tank.getInStorageAmount();
        tank.loadFromNBT(nbt.getCompound("tank"), provider);

        byte oldConns = connections;
        connections = nbt.getByte("allConnections");
        if(level!=null&&level.isClientSide&&(connections!=oldConns || tank.getInStorageAmount()!= oldTankAmount))
        {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, 3);
            markContainingBlockForUpdate(getBlockState());
        }
    }


    @Override
    public void writeCustomNBT(CompoundTag nbt, boolean descPacket, HolderLookup.Provider provider) {
        int[] config = new int[6];
        for(int i = 0; i < 6; ++i)
            if(sideConfig.getBoolean(Direction.from3DDataValue(i)))
                config[i] = 1;
        nbt.putIntArray("sideConfig", config);
        nbt.put("tank", tank.toNBT(provider));
        nbt.putByte("allConnections", connections);
    }



    @Override
    public void allocateResourceLocal(BlockFace sourceKey, boolean tryEmptySelf) {
        // Get current Fluid in self
        Fluid fluid = tank.getResourceInStorage().getFluid();
        if(tank.getInStorageAmount() == 0) return;

        // Get total fluid amount in pipe to allocate
        // Only count fluid in pipe because we do not want to extract and reallocate fluid from consumer.
        int totalAmount = tank.getInStorageAmount();
        for(Direction dir : getDataBySourceKey(sourceKey).outputs){
            BlocklikeFluidTransporterBE fluidTransporterBE = getNeighborInstance(dir);
            if(fluidTransporterBE!=null){
                if(fluidTransporterBE.tank.getResourceInStorage().getFluid().isSame(fluid)){
                    totalAmount += fluidTransporterBE.tank.getInStorageAmount();
                }
            }
        }

        // Update localAllocateCache
        // Work to get the capacity limit of every local neighbor container, including self.
        // It is for the next part's allocate algorithm:
        getDataBySourceKey(sourceKey).clearLocalAllocateCache();
        getDataBySourceKey(sourceKey).addToLocalAllocateCacheSorted(this.tank.getCompactParent(), Math.min(this.tank.getStorageCapacity(), totalAmount), tryEmptySelf);
        for(Direction nextDir : getDataBySourceKey(sourceKey).outputs){
            IFluidHandler handler = neighbors.get(nextDir).getCapability();
            int capacity = 0;
            if(handler instanceof BLTSingleFluidUniHandler && handler.getFluidInTank(0).getFluid().isSame(fluid)){
                capacity = Math.min(this.tank.getStorageCapacity(), totalAmount);
                getDataBySourceKey(sourceKey).addToLocalAllocateCacheSorted(handler, capacity, tryEmptySelf);
            }
            else if (handler != null && handler.getTanks() > 0){
                capacity = handler.fill(new FluidStack(fluid, totalAmount), IFluidHandler.FluidAction.SIMULATE);
                getDataBySourceKey(sourceKey).addToLocalAllocateCacheSorted(handler, capacity, true);
            }
        }

        // Execute Allocate Algorithm according to localAllocateCache.
        // local Allocate Cache is already sorted by capacity in ascending order
        // This Algorithm simulates Round-robin Dispatch but using minimum resource patch(1mB).
        // Due to properties of Integer division, the process is equivalent to Round-robin from cache's tail to head.
        // that means if the fluid amount cannot be divided, handlers close to cache's tail will be more likely to obtain 1mB more than others
        // It is also equivalent to [Max-Min Fairness Algorithm]
        List<Pair<IFluidHandler, Integer>> localAllocateCache = getDataBySourceKey(sourceKey).getLocalAllocateCache();
        int handlersCount = localAllocateCache.size();
        if(handlersCount <= 1) return;

        int overflow = totalAmount;
        int bottom = 0;
        for(int i = 0; i < handlersCount; i++){
            int handlersRemain = (handlersCount - i);
            int thatCanFill = localAllocateCache.get(i).right();
            IFluidHandler handler = localAllocateCache.get(i).left();
            if((thatCanFill - bottom) * handlersRemain < overflow){
                int fill = setHandlerFluidAmountForAllocate(handler, fluid, thatCanFill);
                overflow -= (fill - bottom) * handlersRemain;
                bottom = fill;
            }
            else{
                int fill = setHandlerFluidAmountForAllocate(handler, fluid, bottom + overflow/handlersRemain);;
                overflow -= fill - bottom;
            }
        }
    }

    private int setHandlerFluidAmountForAllocate(IFluidHandler handler, Fluid fluid, int amount){
        int fill = 0;
        if(handler instanceof BLTSingleFluidUniHandler fluidUniHandler){
            fill = fluidUniHandler.setFluidAmount(fluid, amount);
        }
        else if(handler.equals(this.tank)){
            fill = this.tank.setFluidAmount(fluid, amount);
        }
        else{
            fill = handler.fill(new FluidStack(fluid, amount), IFluidHandler.FluidAction.EXECUTE);
        }
        return fill;
    }

    @Override
    public int forceAllocateResource(BlockFace sourceKey, FluidStack resource, int amount, boolean simulate) {
        int last = amount;
        last -= tank.receiveResource(resource.copyWithAmount(last),simulate);
        try {
            for (Direction output : getDataBySourceKey(sourceKey).outputs) {
                IFluidHandler handler = neighbors.get(output).getCapability();
                if (handler != null && !(handler instanceof BLTSingleFluidUniHandler)) {
                    last -= handler.fill(resource.copyWithAmount(last), simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
                }
            }
            return amount - last;
        } catch (RuntimeException e){
            IMUtils.LOGGER.warn("force allocate before claim");
            return 0;
        }
    }


    int lastHeightLevel = 0;

    public void checkHeightLevelForSendingUpdate(){
        int currentHeightLevel = getFluidHeightLevel(tank.getInStorageAmount(), tank.getStorageCapacity());
        if(currentHeightLevel != lastHeightLevel){
            lastHeightLevel = currentHeightLevel;
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    }

    // height level : 0~5
    public static int getFluidHeightLevel(int tankAmount, int tankCapacity){
        if(tankAmount == 0) return 0;
        if(tankAmount == tankCapacity) return 6;
        return 1 + (tankAmount + tankCapacity/8) / (tankCapacity/4);
    }


    protected void invalidateHandler(Direction side)
    {
        IFluidHandler handler = sidedHandlers.get(side);
        if(handler!=null)
        {
            sidedHandlers.put(side, null);
            invalidateCapabilities();
        }
    }

    protected void setValidHandler(Direction side)
    {
        IFluidHandler handler = sidedHandlers.get(side);
        if(handler==null)
        {
            sidedHandlers.put(side, new BLTSingleFluidUniHandler(FluidUniHandler.cast(tank.getCompactParent()),this, side));
            invalidateCapabilities();
        }
    }


    @Override
    public Class<FluidStack> getResourceClass() {
        return FluidStack.class;
    }

    @Override
    public IUniHandler<FluidStack> getSelfHandler() {
        return tank;
    }

    @Override
    public List<TransportationData> getTDList() {
        return tdList;
    }

    @Override
    public byte getConnectionByte() {
        return connections;
    }

    @Override
    public IUniHandler<FluidStack> getNeighborCapability(Direction direction) {
        IFluidHandler neighborHandler = neighbors.get(direction).getCapability();
        if(neighborHandler == null) return null;
        return FluidUniHandler.cast(neighborHandler);
    }

    @Override
    public BlocklikeFluidTransporterBE getNeighborInstance(Direction direction) {
        BlockEntity be = SafeChunkUtils.getSafeBE(level,getBlockPos().relative(direction));
        if(be instanceof BlocklikeFluidTransporterBE blocklikeFluidTransporterBE)
            return blocklikeFluidTransporterBE;
        else return null;
    }




}
