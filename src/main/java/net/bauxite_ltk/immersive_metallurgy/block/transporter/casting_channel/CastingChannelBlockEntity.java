package net.bauxite_ltk.immersive_metallurgy.block.transporter.casting_channel;


import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.api.utils.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.register.IEItems;
import com.google.common.collect.Lists;
import net.bauxite_ltk.immersive_metallurgy.block.BlockCapabilityRegistration;
import net.bauxite_ltk.immersive_metallurgy.block.IMBlockEntities;
import net.bauxite_ltk.immersive_metallurgy.block.IMBlocks;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.api.BlocklikeFluidTransporterBE;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.api.resourceStorage.FluidUniStorage;
import net.bauxite_ltk.immersive_metallurgy.fluid.IMFluids;
import net.bauxite_ltk.immersive_metallurgy.tags.IMTags;
import net.bauxite_ltk.immersive_metallurgy.util.Helper;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//@EventBusSubscriber(modid = TFCTrihydrate.MODID, value = Dist.CLIENT)
public class CastingChannelBlockEntity extends BlocklikeFluidTransporterBE
        implements IEServerTickableBE, IEBlockInterfaces.IPlacementInteraction, IEBlockInterfaces.IPlayerInteraction,
        IEBlockInterfaces.ICollisionBounds, IEBlockInterfaces.ISelectionBounds, IEBlockInterfaces.IHammerInteraction

{

    public CastingChannelBlockEntity(BlockPos pos, BlockState state) {
        super(new FluidUniStorage(200), IMBlockEntities.CASTING_CHANNEL.get(), pos, state);

    }

    @Override
    public void tickServer() {
        super.tickServer();
        fluidLight();
    }



    private void fluidLight(){
        int lightLevel = 0;
        if(tank.getResource(0).is(IMTags.Fluids.TEMPERATURE_MOLTEN_FLUID)){
            lightLevel = 15;
        }
        level.setBlockAndUpdate(getBlockPos(),getState().setValue(CastingChannelBlock.LIGHT_LEVEL, lightLevel));

    }

    public void toggleSide(Direction side)
    {
        boolean newSideConnected = !sideConfig.getBoolean(side);
        setSide(side, newSideConnected);
    }

    public void setSide(Direction side, boolean connectable)
    {
        setSide(side, connectable, true);
    }

    public void setSide(Direction side, boolean connectable, boolean firstPipe)
    {
        if((connections & 3) != 0)
            this.horizontalNeighborOfVerticalConnection = getUniqueHorizontalDirection();
        sideConfig.put(side, connectable);
        if(connectable)
            setValidHandler(side);
        else
            invalidateHandler(side);
        setChanged();
        if(firstPipe)
        {
            BlockEntity neighborTile = level.getBlockEntity(getBlockPos().relative(side));
            if(neighborTile instanceof CastingChannelBlockEntity)
                ((CastingChannelBlockEntity)neighborTile).setSide(side.getOpposite(), connectable, false);
            updateConnectionByte(side); //yes, this is not meant for neighborTile
        }
        level.blockEvent(getBlockPos(), getBlockState().getBlock(), 0, 0);
    }


    public boolean updateConnectionByte(Direction dir)
    {
        if(level==null||level.isClientSide||!SafeChunkUtils.isChunkSafe(level, worldPosition.relative(dir)))
            return false;
        final byte oldConn = connections;
        int i = dir.get3DDataValue();
        int mask = 1<<i;
        connections &= ~mask;

        if(sideConfig.getBoolean(dir))
        {
            IFluidHandler handler = neighbors.get(dir).getCapability();
            if(handler!=null&&handler.getTanks() > 0)
                connections |= mask;
        }
        return oldConn!=connections;
    }


    @Override
    public boolean triggerEvent(int id, int arg)
    {
        if(id==0)
        {
            this.markContainingBlockForUpdate(null);
            return true;
        }
        return false;
    }



    @Override
    public void onNeighborBlockChange(BlockPos otherPos)
    {
        super.onNeighborBlockChange(otherPos);
        Direction dir = Direction.getNearest(otherPos.getX()-worldPosition.getX(),
                otherPos.getY()-worldPosition.getY(), otherPos.getZ()-worldPosition.getZ());
        IMUtils.LOGGER.info("{} onNeighborBlockChange, dir: {}", getBlockPos(), dir);
        if(updateConnectionByte(dir))
        {
            if(checkDownAndSide()) return;
            if(checkUpAndSide()) return;
            Level world = getLevelNonnull();
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    //When a Vertical Connection is destroyed because of horizontal neighbor's toggle,
    //this variable will record the horizontal neighbor's direction
    private Direction horizontalNeighborOfVerticalConnection = null;
    public boolean checkDownAndSide(){
        if((connections & 1) == 1){
            Direction horizon = null;
            for (int i = 2; i < 6; i++){
                if(sideConfig.getBoolean(Direction.from3DDataValue(i)))
                    horizon = Direction.from3DDataValue(i);
            }
            if(getNeighborChannel(horizon)==null){
                IMUtils.LOGGER.info("checkDownAndSide {}", getBlockPos());
                CastingChannelBlockEntity down = getNeighborChannel(Direction.DOWN);
                for(Direction d : DirectionUtils.VALUES){
                    sideConfig.put(d,d.get3DDataValue() > 1 && !d.equals(horizontalNeighborOfVerticalConnection));
                    down.sideConfig.put(d,d.get3DDataValue() > 1);
                    updateConnectionByte(d);
                    down.updateConnectionByte(d);
                }
                invalidateCapabilities();
                down.invalidateCapabilities();
                markContainingBlockForUpdate(null);
                down.markContainingBlockForUpdate(null);
                return true;
            }
            else if(getNeighborChannel(Direction.DOWN) == null){
                for(Direction d : DirectionUtils.VALUES){
                    sideConfig.put(d,d.get3DDataValue() > 1 && !d.equals(horizontalNeighborOfVerticalConnection));

                }
                invalidateCapabilities();
                markContainingBlockForUpdate(getBlockState());
                horizontalNeighborOfVerticalConnection = null;
                return true;
            }
        }
        return false;
    }

    public boolean checkUpAndSide(){
        if(((connections >> 1) & 1) == 1){
            Direction horizon = null;
            for (int i = 2; i < 6; i++){
                if(sideConfig.getBoolean(Direction.from3DDataValue(i)))
                    horizon = Direction.from3DDataValue(i);
            }
            if(getNeighborChannel(horizon)==null){
                CastingChannelBlockEntity up = getNeighborChannel(Direction.UP);
                for(Direction d : DirectionUtils.VALUES){
                    sideConfig.put(d,d.get3DDataValue() > 1 && !d.equals(horizontalNeighborOfVerticalConnection));
                    up.sideConfig.put(d,d.get3DDataValue() > 1);
                    updateConnectionByte(d);
                    up.updateConnectionByte(d);
                }
                invalidateCapabilities();
                up.invalidateCapabilities();
                markContainingBlockForUpdate(getBlockState());
                up.markContainingBlockForUpdate(up.getBlockState());
                return true;
            }
            else if(getNeighborChannel(Direction.UP) == null){
                for(Direction d : DirectionUtils.VALUES){
                    sideConfig.put(d,d.get3DDataValue() > 1 && !d.equals(horizontalNeighborOfVerticalConnection));
                }
                invalidateCapabilities();
                markContainingBlockForUpdate(getBlockState());
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBEPlaced(BlockPlaceContext ctx) {
        boolean doUpdate = false;
        for(Direction d : DirectionUtils.VALUES){
            if(updateConnectionByte(d)){
                doUpdate = true;
            }
        }
        if(doUpdate){
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }


    }

    public CastingChannelBlockEntity getNeighborChannel(Direction direction){
        if(direction==null) return null;
        BlockEntity con = SafeChunkUtils.getSafeBE(level, getBlockPos().relative(direction));
        if (con instanceof CastingChannelBlockEntity up) {
            return up;
        }
        return null;
    }

    public Direction getUniqueHorizontalDirection(){
        int horizon = (connections >> 2) & 15;
        if(horizon > 0 && (horizon & (horizon - 1))==0){
            for(int i = 0; i < 4; i++){
                if((horizon >> i & 1) == 1)
                    return Direction.from3DDataValue(i+2);
            }
        }
        return null;
    }

    @Override
    public ItemInteractionResult interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
        if(!heldItem.is(IMBlocks.CASTING_CHANNEL.asItem())) {
            if (heldItem.is(Items.STICK)) {
//                TFCTrihydrate.LOGGER.info("----Channel Info----");
//                TFCTrihydrate.LOGGER.info("Pos:{}", getBlockPos());
//                TFCTrihydrate.LOGGER.info("fluidAmount:{}", tank.getFluidAmount());
//                for (BlockPos root : statusMap.keySet()) {
//                    TFCTrihydrate.LOGGER.info("Root:{}", root);
//                    TFCTrihydrate.LOGGER.info("Previous:{}", previous.getList(root).getFirst());
//                    for (Direction nextDir : next.getList(root)) {
//                        TFCTrihydrate.LOGGER.info("Next:{}", nextDir);
//                    }
//                }
//                TFCTrihydrate.LOGGER.info("----=========----");
                return ItemInteractionResult.sidedSuccess(getLevelNonnull().isClientSide);
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        CastingChannelBlockEntity up = getNeighborChannel(Direction.UP);
        if(up!=null && (connections & 3) == 0 && (up.connections & 3) == 0){
            Direction upFlowFrom = up.getUniqueHorizontalDirection();
            Direction downFlowTo = getUniqueHorizontalDirection();
            if(upFlowFrom!=null && downFlowTo!=null){
                if(upFlowFrom.getOpposite().equals(downFlowTo)){
                    for(Direction d : DirectionUtils.VALUES){
                        sideConfig.put(d, d.equals(Direction.UP) || d.equals(downFlowTo));
                        up.sideConfig.put(d, d.equals(Direction.DOWN) || d.equals(upFlowFrom));
                        updateConnectionByte(d);
                        up.updateConnectionByte(d);
                    }
                    invalidateCapabilities();
                    up.invalidateCapabilities();
                    markContainingBlockForUpdate(null);
                    up.markContainingBlockForUpdate(null);
                    Helper.playSound(level, worldPosition, SoundEvents.AMETHYST_BLOCK_PLACE);
                    return ItemInteractionResult.sidedSuccess(getLevelNonnull().isClientSide);
                }
            }
        }

        Helper.playSound(level, worldPosition, SoundEvents.ITEM_BREAK);


        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }


    @Override
    public boolean hammerUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
    {
        if(level.isClientSide)
            return true;
        hitVec = hitVec.subtract(Vec3.atLowerCornerOf(worldPosition));
        Direction fd = side;
        List<AABB> boxes = getBoxes(new BoundingBoxKey(true, this));
        for(AABB box : boxes) {
            if (box.inflate(.002).contains(hitVec)) {
                for (Direction d : DirectionUtils.VALUES) {
                    Vec3 testVec = new Vec3(0.5 + 0.5 * d.getStepX(), 4d / 16 + 0.5 * d.getStepY(), 0.5 + 0.5 * d.getStepZ());
                    if (box.inflate(0.002).contains(testVec)) {
                        fd = d;
                        break;
                    }
                }
                break;
            }
        }
        if((connections & 3) == 0 && fd.get3DDataValue() > 1)
        {
            toggleSide(fd);
            this.markContainingBlockForUpdate(null);
            Helper.playSound(level, worldPosition, SoundEvents.NETHERITE_BLOCK_PLACE);
            return true;
        }
        Helper.playSound(level, worldPosition, SoundEvents.ITEM_BREAK);
        return false;
    }

    public boolean isConnectTo(Direction direction){
        return ((connections >> direction.get3DDataValue()) & 1) == 1;
    }

    public byte getAvailableConnectionByte()
    {
        byte availableConnections = connections;
        int mask = 1;
        for(Direction dir : DirectionUtils.VALUES)
        {
            if((availableConnections&mask)==0)
            {
                if(level.getBlockEntity(getBlockPos().relative(dir)) instanceof CastingChannelBlockEntity)
                    availableConnections |= mask;
                else
                {
                    IFluidHandler handler = neighbors.get(dir).getCapability();
                    if(handler!=null&&handler.getTanks() > 0)
                        availableConnections |= mask;
                }
            }
            mask <<= 1;
        }
        return availableConnections;
    }


    private static final CachedVoxelShapes<BoundingBoxKey> SHAPES = new CachedVoxelShapes<>(CastingChannelBlockEntity::getBoxes);

    @Override
    public VoxelShape getCollisionShape(CollisionContext ctx)
    {
        return SHAPES.get(new BoundingBoxKey(false, this));
    }

    @Override
    public VoxelShape getSelectionShape(@Nullable CollisionContext ctx)
    {
        //TODO needs to be a more generic check!
        boolean hammer = ctx!=null&&ctx.isHoldingItem(IEItems.Tools.HAMMER.get());
        if(ctx instanceof EntityCollisionContext ecc){
            if(ecc.getEntity() instanceof Player player){
                hammer = player.getItemInHand(InteractionHand.MAIN_HAND).getTags()
                        .anyMatch(tagKey -> tagKey.equals(IETags.hammers));
            }
        }
        return SHAPES.get(new BoundingBoxKey(hammer, this));
    }


    private static List<AABB> getBoxes(BoundingBoxKey key)
    {
        List<AABB> list = Lists.newArrayList();
        byte availableConnections = key.availableConnections;
        byte activeConnections = key.connections;
        for(Direction d : DirectionUtils.VALUES)
        {
            int i = d.get3DDataValue();
            if(((availableConnections >> i) & 1)==1)
            {
                if(((activeConnections >> i) & 1)==1||key.showToolView)
                {
                    switch (d){
                        case Direction.UP:{
                            list.add(new AABB(3d/16, 0, 0, 13d/16,1,1));
                            break;
                        }
                        case Direction.NORTH:{
                            list.add(new AABB(3d/16, 0, 0, 13d/16, 8d/16,3d/16));
                            break;
                        }
                        case Direction.SOUTH:{
                            list.add(new AABB(3d/16, 0, 13d/16, 13d/16, 8d/16,1));
                            break;
                        }
                        case Direction.EAST:{
                            list.add(new AABB(13d/16, 0, 3d/16, 1, 8d/16,13d/16));
                            break;
                        }
                        case Direction.WEST:{
                            list.add(new AABB(0, 0, 3d/16, 3d/16, 8d/16,13d/16));
                            break;
                        }
                    }
//                    if(key.connectionStyles.get(d)== ConnectionStyle.TO_UP_NORTH)
//                        list.add(new AABB(
//                                i==4?0: i==5?0.875: 0.125, i==0?0: i==1?0.875: 0.125, i==2?0: i==3?0.875: 0.125,
//                                i==4?0.125: i==5?1: 0.875, i==0?0.125: i==1?1: 0.875, i==2?0.125: i==3?1: 0.875
//                        ));
                }
            }
        }
        list.add(new AABB(3d/16, 0, 3d/16, 13d/16, 8d/16, 13d/16));
        return list;
    }


    private static class BoundingBoxKey
    {
        private final boolean showToolView;
        private final byte connections;
        private final byte availableConnections;
        private final Map<Direction, ConnectionStyle> connectionStyles = new EnumMap<>(Direction.class);

        private BoundingBoxKey(boolean showToolView, CastingChannelBlockEntity te)
        {
            this.showToolView = showToolView;
            this.connections = te.connections;
            this.availableConnections = te.getAvailableConnectionByte();
            for(Direction d : DirectionUtils.VALUES)
                connectionStyles.put(d, te.getConnectionStyle(d));
        }

        @Override
        public boolean equals(Object o)
        {
            if(this==o) return true;
            if(o==null||getClass()!=o.getClass()) return false;
            BoundingBoxKey that = (BoundingBoxKey)o;
            return showToolView==that.showToolView&&
                    connections==that.connections&&
                    availableConnections==that.availableConnections&&
                    connectionStyles.equals(that.connectionStyles);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(showToolView, connections, availableConnections, connectionStyles);
        }

    }



    public ConnectionStyle getConnectionStyle(Direction connection)
    {
        if((connections&(1<<connection.get3DDataValue()))==0)
            return ConnectionStyle.NO_CONNECTION;

        // Here goes to the Up Connection cases:
        // Casting Channel can only go straight down.
        // So if current channel has a connection on its top,
        // the channel on its top must have only one connection in its horizontal direction,
        // and current channel's horizontal direction must be opposite to the channel on top
        if(connection.equals(Direction.UP)){
            BlockEntity con = SafeChunkUtils.getSafeBE(level, getBlockPos().relative(connection));
            if(con instanceof CastingChannelBlockEntity castingChannel){
                int upChannelConnections = castingChannel.connections|(1<<connection.getOpposite().get3DDataValue());
                if((upChannelConnections >> 2 & 1) != 0){
                    // up connect to north, then flow goes south
                    return ConnectionStyle.TO_UP_SOUTH;
                }
                else if((upChannelConnections >> 3 & 1)!=0){
                    // up connect to south, then flow goes north
                    return ConnectionStyle.TO_UP_NORTH;
                }
                else if((upChannelConnections >> 4 & 1)!=0){
                    // up connect to west, then flow goes east
                    return ConnectionStyle.TO_UP_EAST;
                }
                else if((upChannelConnections >> 5 & 1)!=0){
                    // up connect to east, then flow goes west
                    return ConnectionStyle.TO_UP_WEST;
                }
            }
        }
        return ConnectionStyle.COMMON;
    }

    public enum Status{
        UNINITIALIZED,
        COMMON,
        SUB_ROOT,
        ROOT
    }

    public enum ConnectionStyle
    {
        NO_CONNECTION,
        COMMON,
        TO_UP_SOUTH,
        TO_UP_NORTH,
        TO_UP_EAST,
        TO_UP_WEST,
    }


    public static void registerCapabilities(BlockCapabilityRegistration.BECapabilityRegistrar<CastingChannelBlockEntity> registrar)
    {
        registrar.register(Capabilities.FluidHandler.BLOCK, (be, side) -> {
            if(side!=null&&be.sideConfig.getBoolean(side))
                return be.sidedHandlers.get(side);
            else
                return null;
        });
    }

}
