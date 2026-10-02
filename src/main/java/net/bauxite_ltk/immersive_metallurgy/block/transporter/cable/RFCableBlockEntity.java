package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable;

import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import net.bauxite_ltk.immersive_metallurgy.block.IMBlockEntities;
import net.bauxite_ltk.immersive_metallurgy.block.IMBlocks;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.CableConnectionKey;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.resolver.CableBlockEnergyManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.function.Consumer;

public class RFCableBlockEntity extends IEBaseBlockEntity
        implements ICableBEImplements, IEBlockInterfaces.IPlacementInteraction,
        IEBlockInterfaces.IStateBasedDirectional, IEServerTickableBE
{
    final CableBlockEnergyManager rfBlockManager = new CableBlockEnergyManager(this);
    final int transferLimit;
    final Item cableItem;

    private RFCableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int transferLimit, Item cableItem) {
        super(type, pos, state);
        this.cableItem = cableItem;
        this.transferLimit = transferLimit;
    }

    public static RFCableBlockEntity createHv(BlockPos pos, BlockState state){
        return new RFCableBlockEntity(IMBlockEntities.ELECTRIC_CABLE_HV.get(), pos, state, 65536, IMBlocks.ELECTRIC_CABLE_HV.asItem());
    }

    public CableBlockEnergyManager getRFBlockManager() {
        return rfBlockManager;
    }


    /**
     * ======== Read and Write ========
     * ________________________________
     */
    @Override
    public void readCustomNBT(CompoundTag nbt, boolean descPacket, HolderLookup.Provider provider) {
        boolean changed = false;
        var pastKey = rfBlockManager.getCableConnectionKey();
        rfBlockManager.loadFromNBT(nbt.getCompound("manager"), provider);

        if(!pastKey.equals(rfBlockManager.getCableConnectionKey())){
            if(level instanceof ClientLevel) {
                markContainingBlockForUpdate(getBlockState());
                rfBlockManager.refreshCableConnectionKey();
            }
            else if(level instanceof ServerLevel){
                rfBlockManager.updateConnectedFaces();
            }
        }

    }

    @Override
    public void writeCustomNBT(CompoundTag nbt, boolean descPacket, HolderLookup.Provider provider) {
        nbt.put("manager", rfBlockManager.toNBT(provider));
    }

    boolean sendUpdateToGlobalData = false;
    @Override
    public void tickServer() {
        sendUpdateToGlobalData = false;
    }

    /**
     * ======== Placement ========
     * ___________________________
     */
    @Override
    public void onBEPlaced(BlockPlaceContext ctx) {
        if (!(level instanceof ServerLevel)) return;
        Direction firstAttach = getFacing();
        rfBlockManager.addNode(firstAttach);
        invalidateCapabilities();
        markContainingBlockForUpdate(null);
    }

    public RFCableBlockEntity getOther(BlockPos position, Consumer<NeighborStatus> result){
        if(!(level instanceof ServerLevel serverLevel)) return null;
        if(!serverLevel.isLoaded(position)){
            result.accept(NeighborStatus.UNLOAD);
            return null;
        }
        BlockEntity blockEntity = serverLevel.getBlockEntity(position);
        if(blockEntity instanceof RFCableBlockEntity rfCable){
            result.accept(NeighborStatus.ACTIVE);
            return rfCable;
        }
        else {
            result.accept(NeighborStatus.MISSING);
            return null;
        }
    }


    @Override
    public void notifiedStraight(BlockPos changedPos) {
        if (level == null || level.isClientSide) return;
        Vec3i delta = changedPos.subtract(this.worldPosition);
        Direction changedDir = Direction.fromDelta(delta.getX(), delta.getY(), delta.getZ());

        boolean changed = false;

        if(SafeChunkUtils.getSafeBE(level, changedPos) instanceof RFCableBlockEntity cable){
            changed |= rfBlockManager.proactivelyStraightConnect(cable.rfBlockManager, changedDir, level.getGameTime());
            if(changed){
                cable.getRFBlockManager().updateConnectedFaces();
                cable.markContainingBlockForUpdate(null);
            }
        }
        changed |= rfBlockManager.checkConnectionsTo(changedDir);
        if(changed){
            rfBlockManager.updateConnectedFaces();
            sendUpdateToGlobalData = true;
            markContainingBlockForUpdate(null);
            invalidateCapabilities();
        }
    }

    @Override
    public void notifiedBackCorner(BlockPos changedPos) {
        //TODO notifiedBackCorner
    }

    @Override
    public CableConnectionKey getConnectionKey() {
        return rfBlockManager.getCableConnectionKey(this.level.getGameTime());
    }

    @Override
    public Property<Direction> getFacingProperty() {
        return RFCableBlock.DEFAULT_FACING_PROP;
    }

    @Override
    public PlacementLimitation getFacingLimitation() {
        return PlacementLimitation.SIDE_CLICKED;
    }

    @Override
    public boolean mirrorFacingOnPlacement(LivingEntity placer)
    {
        return true;
    }

    public enum NeighborStatus {
        ACTIVE(0),
        UNLOAD(1),
        MISSING(2);
        final int code;
        NeighborStatus(int code){
            this.code = code;
        }
    }
}
