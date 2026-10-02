package net.bauxite_ltk.immersive_metallurgy.block.transporter.casting_channel;

import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
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
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;

@Deprecated(since = "2026-6-12")
public abstract class PressurePipeBlockEntity extends IEBaseBlockEntity implements IEServerTickableBE{

    public FluidTank tank;
    protected byte connections = 0;
    List<PipeConnectionInfo> connectionInfoList = new ArrayList<>();
    public Object2BooleanMap<Direction> sideConfig = new Object2BooleanOpenHashMap<>();
    {
        for(Direction d : DirectionUtils.VALUES){
            sideConfig.put(d, d.get3DDataValue() > 1);
        }
    }
    protected final Map<Direction, IFluidHandler> sidedHandlers = new EnumMap<>(Direction.class);
    protected final Map<Direction, IEBlockCapabilityCaches.IEBlockCapabilityCache<IFluidHandler>> neighbors = IEBlockCapabilityCaches.allNeighbors(
            Capabilities.FluidHandler.BLOCK, this
    );
    {
        for(Direction f : DirectionUtils.VALUES)
            sidedHandlers.put(f, new PressurePipeFluidHandler(this, f));
    }


    public PressurePipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState, int tankSize) {
        super(type, pos, blockState);
        tank = new FluidTank(tankSize);
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
        tank.readFromNBT(provider, nbt.getCompound("tank"));

        byte oldConns = connections;
        connections = nbt.getByte("allConnections");
        if(level!=null&&level.isClientSide&&(connections!=oldConns))
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
        nbt.put("tank", tank.writeToNBT(provider, new CompoundTag()));
        nbt.putByte("allConnections", connections);
    }


    int tickCount = 0;
    @Override
    public void tickServer() {
        checkHeightLevelForSendingUpdate();
        if(tickCount < 5){
            tickCount++;
            return;
        }
        tickCount = 0;
        //remove Invalid Connections
        removeInvalidConnectionInfo();

        for(PipeConnectionInfo info : connectionInfoList){
            BlockPos rootPos = info.root;
            ConnectionStatus status = info.status;

            if(status.equals(ConnectionStatus.ROOT)){
                rootUpdateSubnet(rootPos);
                List<IFluidHandler> sources = info.previousList.stream().map(d -> neighbors.get(d).getCapability()).filter(Objects::nonNull).toList();
                if(sources.isEmpty() || tank.isEmpty()) {
                    IMUtils.LOGGER.info("set subroot");
                    info.setStatus(ConnectionStatus.SUB_ROOT);
                    info.previousList.clear();
                }
            }

            else if(status.equals(ConnectionStatus.SUB_ROOT)){
                subrootUpdateSubnet(rootPos, getBlockPos());
                if(isClaimedByOtherRoot(rootPos) || tank.isEmpty()){
                    for(Direction nextDir : info.nextList){
                        if(getNeighborPipe(nextDir)!= null && getNeighborPipe(nextDir).getConnectionInfo(rootPos) != null){
                            getNeighborPipe(nextDir).getConnectionInfo(rootPos).setStatus(ConnectionStatus.SUB_ROOT);
                        }
                    }
                    info.setStatus(ConnectionStatus.INVALID);
                }

            }

            else if(status.equals(ConnectionStatus.COMMON)){
                List<IFluidHandler> sources = info.previousList.stream().map(d -> neighbors.get(d).getCapability()).filter(Objects::nonNull).toList();
                if (sources.isEmpty()){
                    info.setStatus(ConnectionStatus.SUB_ROOT);
                }
            }
        }
    }

    protected void rootUpdateSubnet(BlockPos rootOfSubnet){
        List<BlockPos> openList = new LinkedList<>();
        List<BlockPos> closeList = new LinkedList<>();
        openList.add(rootOfSubnet);
        for(int i = 0; i < 1024; i++){
            //TFCTrihydrate.LOGGER.info("rootUpdateSubnet: i = {}", i);
            if(openList.isEmpty()) break;
            BlockPos curPos = openList.getFirst();
            openList.removeFirst();
            if(closeList.contains(curPos)) continue;
            BlockEntity be = null;
            if (level != null) be = SafeChunkUtils.getSafeBE(level, curPos);
            if(be == null) continue;
            if(be instanceof PressurePipeBlockEntity pressurePipe){
                pressurePipe.tryClaimNext(rootOfSubnet);
                pressurePipe.allocateFluidLocal(rootOfSubnet, false);
                for(Direction direction : pressurePipe.getConnectionInfo(rootOfSubnet).nextList){
                    openList.addLast(curPos.relative(direction));
                }
                closeList.addFirst(curPos);
            }
        }
    }

    public void subrootUpdateSubnet(BlockPos oldRoot, BlockPos newSubroot){
        List<BlockPos> openList = new LinkedList<>();
        List<BlockPos> closeList = new LinkedList<>();
        openList.add(newSubroot);
        for(int i = 0; i < 1024; i++){
            //TFCTrihydrate.LOGGER.info("rootUpdateSubnet: i = {}", i);
            if(openList.isEmpty()) break;
            BlockPos curPos = openList.getFirst();
            openList.removeFirst();
            if(closeList.contains(curPos)) continue;
            BlockEntity be = null;
            if (level != null) be = SafeChunkUtils.getSafeBE(level, curPos);
            if(be == null) continue;
            if(be instanceof PressurePipeBlockEntity pressurePipe){

                if(oldRoot != newSubroot && getConnectionInfo(oldRoot) != null){
                    getConnectionInfo(oldRoot).root = newSubroot;
                }
                pressurePipe.tryClaimNext(newSubroot);
                pressurePipe.allocateFluidLocal(newSubroot, true);
                for(Direction direction : pressurePipe.getConnectionInfo(newSubroot).nextList){
                    openList.addLast(curPos.relative(direction));
                }
                closeList.addFirst(curPos);
            }
        }
    }

    public void tryClaimNext(BlockPos rootOfSubnet){

        // update nextList
        for(int i = 0; i < 6; i++) {
            Direction dir = Direction.from3DDataValue(i);
            //node should not check previous direction
            if(getConnectionInfo(rootOfSubnet).previousList.contains(dir)) continue;
            //check if the direction is connected
            if (((connections >> i) & 1) != 1) {
                getConnectionInfo(rootOfSubnet).removeNext(dir);
                continue;
            }
            //check if the direction has available handler
            IFluidHandler handler = neighbors.get(dir).getCapability();
            if(handler!=null&&handler.getTanks() > 0){
                getConnectionInfo(rootOfSubnet).addNext(dir);

            }
            else{
                getConnectionInfo(rootOfSubnet).removeNext(dir);
            }
        }

        // from nextList, try claim neighbor pipe
        for(Direction nextDir : getConnectionInfo(rootOfSubnet).nextList){
            PressurePipeBlockEntity neighbor = getNeighborPipe(nextDir);
            int thisDepth = getConnectionInfo(rootOfSubnet).depth;
            if(neighbor!=null){

                //if pipe is not in subnet, then add it
                if(neighbor.getConnectionInfo(rootOfSubnet)==null){
                    neighbor.setSelfCommonToSubnet(rootOfSubnet, nextDir.getOpposite(), thisDepth+1);
                }

                // If depth of next is abnormally large, then reverse its next and previous direction
                // Note that depth will not smaller than thisDepth+1
                // Because we use BFS in an indirection graph, if a new connection links to a smaller depth
                // Then this path would have been detected earlier when BFS runs on the smaller depth node
                else if(neighbor.getConnectionInfo(rootOfSubnet).depth > thisDepth + 1){
                    PipeConnectionInfo neighborInfo = neighbor.getConnectionInfo(rootOfSubnet);
                    neighborInfo.previousList.clear();
                    neighborInfo.addPrevious(nextDir.getOpposite());
                    neighborInfo.removeNext(nextDir.getOpposite());
                    neighborInfo.depth = thisDepth + 1;
                }


                // When the depth of next is valid, nextList still can be invalid.
                // This will occur when a subpath join back and form a ring.
                // The path will rearrange, a series of node will be reversed.
                // But at last, two BFS branches will merge into a single node (Proof is Down Below this method)
                // At this point slower branch will see the depth is valid, but its nextList still needs to change.
                else if(neighbor.getConnectionInfo(rootOfSubnet).depth == thisDepth + 1){
                    PipeConnectionInfo neighborInfo = neighbor.getConnectionInfo(rootOfSubnet);
                    //try to remove invalid nextDir of neighbor node
                    if(neighborInfo.nextList.contains(nextDir.getOpposite())){
                        neighborInfo.removeNext(nextDir.getOpposite());
                    }
                    //add multiple preDir.
                    neighborInfo.addPrevious(nextDir.getOpposite());
                }
                /* In a grid network, obviously a ring must contain even number of nodes.
                *  While doing BFS, obviously every branch shares a same root.
                *  When BFS branches b1 and b2 are going to merge,
                *  we can construct a ring [b1_tail--root--b2_tail--b1_tail], call it LOOP_12;
                *  Define assumption P: b1 and b2 will merge into an edge.
                *  First We can consider LOOP_12's node count;
                *  And the count is: node_count_of_b1 + node_count_of_b2 - 1, minus one because they share a same root;
                *  Since we are doing BFS, b1's length is equal to b2, call it N;
                *  Then the LOOP_12's total node count will simplify into 2*N - 1, which must be an odd;
                *  but at first we know a ring in a grid network cannot form a ring with odd number of nodes;
                *  Assumption P Contradicts to the basic facts, so the assumption P does not hold.
                *  Then we proved BFS branches b1 and b2 must merge into a node, not an edge.
                */

            }
        }
    }

    public void allocateFluidLocal(BlockPos rootOfSubnet, boolean tryEmptySelf){
        // Get current Fluid in self
        Fluid fluid = tank.getFluid().getFluid();
        if(tank.isEmpty()) return;
        
        // Get total fluid amount in pipe to allocate
        // Only count fluid in pipe because we do not want to extract and reallocate fluid from consumer.
        int totalAmount = tank.getFluidAmount();
        for(Direction dir : getConnectionInfo(rootOfSubnet).nextList){
            PressurePipeBlockEntity pipe = getNeighborPipe(dir);
            if(pipe!=null){
                if(pipe.tank.getFluid().getFluid().isSame(fluid)){
                    totalAmount += pipe.tank.getFluidAmount();
                }
            }
        }

        // Update localAllocateCache
        // Work to get the capacity limit of every local neighbor container, including self.
        // It is for the next part's allocate algorithm:
        getConnectionInfo(rootOfSubnet).clearLocalAllocateCache();
        getConnectionInfo(rootOfSubnet).addToLocalAllocateCache(this.tank, Math.min(this.tank.getCapacity(), totalAmount), tryEmptySelf);
        for(Direction nextDir : getConnectionInfo(rootOfSubnet).nextList){
            IFluidHandler handler = neighbors.get(nextDir).getCapability();
            int capacity = 0;
            if(handler instanceof PressurePipeFluidHandler  && handler.getFluidInTank(0).getFluid().isSame(fluid)){
                capacity = Math.min(this.tank.getCapacity(), totalAmount);
            }
            else if (handler != null && handler.getTanks() > 0){
                capacity = handler.fill(new FluidStack(fluid, totalAmount), IFluidHandler.FluidAction.SIMULATE);
            }
            else continue;
            getConnectionInfo(rootOfSubnet).addToLocalAllocateCache(handler, capacity, tryEmptySelf);
        }

        // Execute Allocate Algorithm according to localAllocateCache.
        // local Allocate Cache is already sorted by capacity in ascending order
        // This Algorithm simulates Round-robin Dispatch but using minimum resource patch(1mB).
        // Due to properties of Integer division, the process is equivalent to Round-robin from cache's tail to head.
        // that means if the fluid amount cannot be divided, handlers close to cache's tail will be more likely to obtain 1mB more than others
        // It is also equivalent to [Max-Min Fairness Algorithm]
        List<Pair<IFluidHandler, Integer>> localAllocateCache = getConnectionInfo(rootOfSubnet).getLocalAllocateCache();
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
        if(handler instanceof PressurePipeFluidHandler pressurePipe){
            fill = pressurePipe.pipe.setFluidAmount(fluid, amount);
        }
        else if(handler.equals(this.tank)){
            fill = this.setFluidAmount(fluid, amount);
        }
        else{
            fill = handler.fill(new FluidStack(fluid, amount), IFluidHandler.FluidAction.EXECUTE);
        }
        return fill;
    }


    public int setFluidAmount(Fluid fluid, int amount){
        int thisAmount = tank.getFluidAmount();
        if(amount>tank.getCapacity()) IMUtils.LOGGER.warn("Pressure Pipe Set Fluid Amount: Larger Than Capacity!");
        if(amount == thisAmount) return tank.getFluidAmount();;
        if(!tank.isEmpty() && !fluid.isSame(tank.getFluid().getFluid())) return 0;
        if(amount > thisAmount){
            int fillResult = tank.fill(new FluidStack(fluid, amount-thisAmount), IFluidHandler.FluidAction.EXECUTE);
            if(fillResult != amount - thisAmount) IMUtils.LOGGER.error("Pressure Pipe Set Fluid Amount: Unexpected Fill Amount!");
            return tank.getFluidAmount();
        }
        else {
            int drainResult = tank.drain(thisAmount - amount, IFluidHandler.FluidAction.EXECUTE).getAmount();
            if(drainResult != thisAmount - amount) IMUtils.LOGGER.error("Pressure Pipe Set Fluid Amount: Unexpected Drain Amount!");
            return tank.getFluidAmount();
        }
    }

    public PressurePipeBlockEntity getNeighborPipe(Direction direction){
        if(direction==null) return null;
        BlockEntity con = null;
        if (level != null) {
            con = SafeChunkUtils.getSafeBE(level, getBlockPos().relative(direction));
        }
        if (con instanceof PressurePipeBlockEntity up) {
            return up;
        }
        return null;
    }

    protected PipeConnectionInfo getConnectionInfo(BlockPos root){
        for(PipeConnectionInfo info : connectionInfoList){
            if(info.root.equals(root)) return info;
        }
        return null;
    }

    public void removeConnectionInfo(BlockPos root){
        connectionInfoList.removeIf(info -> info.root.equals(root));
    }

    public void removeInvalidConnectionInfo(){
        connectionInfoList.removeIf(info -> info.status.equals(ConnectionStatus.INVALID));
    }

    protected boolean setSelfRootOfSubnet(Direction previousDir){
        for(PipeConnectionInfo info : connectionInfoList){
            if(info.root.equals(getBlockPos())){
//                if(info.status.equals(ConnectionStatus.ROOT)) return false;
//                info.status = ConnectionStatus.ROOT;
                return false;
            }
        }
        connectionInfoList.add(PipeConnectionInfo.forRoot(getBlockPos(), ConnectionStatus.ROOT, previousDir));
        return true;
    }

    protected void setSelfCommonToSubnet(BlockPos rootOfSubnet, Direction previousDir, int depth){

        //FIXME when we complete the algorithm, delete this check.
        for(PipeConnectionInfo info : connectionInfoList){
            if(info.root.equals(rootOfSubnet)){
                throw new RuntimeException("duplicate set pipe common status in subnet: " + rootOfSubnet);
            }
        }

        connectionInfoList.add(PipeConnectionInfo.forCommon(
                rootOfSubnet, ConnectionStatus.COMMON, previousDir, depth));
    }

    protected void setConnectionStatus(BlockPos rootOfSubnet, ConnectionStatus status){
        for(PipeConnectionInfo info : connectionInfoList){
            if(info.root.equals(rootOfSubnet)){
                info.setStatus(status);
                return;
            }
        }
        IMUtils.LOGGER.error("Set PipeConnectionInfo at: {}, but not find Root: {} in connectionInfoList", getBlockPos(), rootOfSubnet);
    }

    protected boolean isNewPreviousDirection(Direction direction){
        for(PipeConnectionInfo info : connectionInfoList){
            if(info.previousList.contains(direction)){
                return false;
            }
        }
        return true;
    }

    protected boolean isClaimedByOtherRoot(BlockPos originalRoot){
        for(PipeConnectionInfo info : connectionInfoList){
            if(info.root != originalRoot && info.status.equals(ConnectionStatus.ROOT)) return true;
        }

        return false;
    }

    int lastHeightLevel = 0;

    public void checkHeightLevelForSendingUpdate(){
        int currentHeightLevel = getFluidHeightLevel(tank.getFluidAmount(), tank.getCapacity());
        if(currentHeightLevel != lastHeightLevel){
            lastHeightLevel = currentHeightLevel;
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    }

    private void invalidateHandler(Direction side)
    {
        IFluidHandler handler = sidedHandlers.get(side);
        if(handler!=null)
        {
            sidedHandlers.put(side, null);
            invalidateCapabilities();
        }
    }

    private void setValidHandler(Direction side)
    {
        IFluidHandler handler = sidedHandlers.get(side);
        if(handler==null)
        {
            sidedHandlers.put(side, new PressurePipeFluidHandler(this, side));
            invalidateCapabilities();
        }
    }


    protected static class PressurePipeFluidHandler implements IFluidHandler{
        PressurePipeBlockEntity pipe;
        Direction facing;

        public PressurePipeFluidHandler(PressurePipeBlockEntity castingChannel, Direction facing)
        {
            this.pipe = castingChannel;
            this.facing = facing;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return pipe.tank.getFluidInTank(0);
        }

        @Override
        public int getTankCapacity(int tank) {
            return pipe.tank.getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return pipe.tank.isFluidValid(tank, stack);
        }

        @Override
        public int fill(FluidStack resource, @NotNull FluidAction doFill)
        {
            int canAccept = resource.getAmount();
            if(canAccept <= 0){
                return 0;
            }

            if(pipe.isNewPreviousDirection(facing)){
                IMUtils.LOGGER.info("Pipe Set Root");
                if(!pipe.setSelfRootOfSubnet(facing)){
                    pipe.getConnectionInfo(pipe.getBlockPos()).addPrevious(facing);
                }
            }

            int result = pipe.tank.fill(resource,doFill);

            pipe.setChanged();
            // Not Update to Client Now.
            // We need to decrease the update frequency to client
            // If the fluid in tank rise or drop to specific height level,
            // like 0, 0.1, 0.3, 0.5, 0.7, 0.9, then we do Update to Client

            return result;
        }


        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, @NotNull FluidAction doDrain)
        {
            return this.drain(resource.getAmount(), doDrain);
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, @NotNull FluidAction doDrain)
        {
            FluidStack fluidStack = pipe.tank.drain(maxDrain, doDrain);
            pipe.setChanged();

            //TODO
            // Not Update to Client Now.
            // We need to decrease the update frequency to client
            // If the fluid in tank rise or drop to specific height level,
            // like 0, 0.33, 0.66, 1, then we do Update to Client

            return fluidStack;
        }
    }



    protected static class PipeConnectionInfo {

        @Nonnull BlockPos root;
        @Nonnull ConnectionStatus status;
        List<Direction> previousList = new ArrayList<>();
        List<Direction> nextList = new ArrayList<>();
        List<Pair<IFluidHandler, Integer>> localAllocateCache = new LinkedList<>() ;
        List<Pair<IFluidHandler, Integer>> networkAllocateCache = new LinkedList<>();

        int depth;


        private PipeConnectionInfo(@Nonnull BlockPos root, @Nonnull ConnectionStatus status, @Nonnull Direction previous, int depth){
            this.root = root;
            this.status = status;
            this.previousList.add(previous);
            this.depth = depth;
        }

        public static PipeConnectionInfo forRoot(@Nonnull BlockPos rootPos, @Nonnull ConnectionStatus status, @Nonnull Direction previous){
            return new PipeConnectionInfo(rootPos, status, previous, 0);
        }

        public static PipeConnectionInfo forCommon(@Nonnull BlockPos rootPos, @Nonnull ConnectionStatus status, @Nonnull Direction previous, int depth){
            return new PipeConnectionInfo(rootPos, status, previous, depth);
        }

        public void setStatus(@NotNull ConnectionStatus status){
            this.status = status;
        }

        public void addNext(@Nonnull Direction nextDir){
            if(!nextList.contains(nextDir)){
                nextList.add(nextDir);
            }
        }

        public void addPrevious(@Nonnull Direction preDir){
            if(!previousList.contains(preDir)){
                previousList.add(preDir);
            }
        }

        public void removeNext(@Nonnull Direction nextDir){
            nextList.remove(nextDir);
        }

        public void clearLocalAllocateCache(){
            localAllocateCache.clear();
        }

        public List<Pair<IFluidHandler, Integer>> getLocalAllocateCache(){
            return localAllocateCache;
        }




        /*-----------------------------------------------------------------------------------------------------
         [Strategy Overview]:
            The variance tryEmptySelf influences allocation priority. This is a little complicate to explain.
            In simple terms, when fluid amount is less than handlers count,
            then tryEmptySelf affect whether these 1mB fluidStack will stay or allocate to neighbors.

         [Problem Example]:
            For example, we have 3 nextDirs, added self, then we have 4 handlers to allocate.
            We will have trouble when we only have 3mB of fluid.

         [Self's Index Depend on Other Pipes]:
            Since self is the first handler that join the cache and its capacity is pipe's capacity,
            if larger container is absent in cache, then pipes, including self will be the largest containers in cache.
            After sorted, they will appear at the tail of cache.
            So self, as the first pipe joined in cache, its index will depend on whether other pipes would add to its front or its back.

         [How tryEmptySelf Works]:
            If tryEmptySelf is true, then other pipes will insert to self's front.
            This means self will be the last element in pipes' region.
            Which according to our allocate algorithm, it has the highest priority, so it will not try to empty self.
            Conversely, self will be the first element in pipes' region and try to empty self.
        -------------------------------------------------------------------------------------------------------*/

        public void addToLocalAllocateCache(IFluidHandler handler, int capacity, boolean tryEmptySelf){
            int size = localAllocateCache.size();
            for(int i = 0; i <= size; i++){
                if(i == size){
                    localAllocateCache.addLast(ObjectIntImmutablePair.of(handler, capacity));
                    break;
                }
                Pair<IFluidHandler, Integer> curPair = localAllocateCache.get(i);
                if(capacity <= curPair.value()){
                    if(!tryEmptySelf || capacity < curPair.value()){
                        localAllocateCache.add(i , ObjectIntImmutablePair.of(handler, capacity));
                        break;
                    }
                }
            }
        }
    }

    // height level : 0~5
    public static int getFluidHeightLevel(int tankAmount, int tankCapacity){
        if(tankAmount == 0) return 0;
        if(tankAmount == tankCapacity) return 6;
        return 1 + (tankAmount + tankCapacity/8) / (tankCapacity/4);
    }

    public enum ConnectionStatus{
        ROOT,
        SUB_ROOT,
        COMMON,
        INVALID
    }
}
