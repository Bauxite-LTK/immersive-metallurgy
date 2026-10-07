package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable;

import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches;
import net.bauxite_ltk.immersive_metallurgy.block.BlockCapabilityRegistration;
import net.bauxite_ltk.immersive_metallurgy.block.IMBlockEntities;
import net.bauxite_ltk.immersive_metallurgy.block.IMBlocks;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.CableConnectionKey;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.resolver.CableBlockEnergyManager;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.shapes.ICableCollisionAndSelection;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RFCableBlockEntity extends IEBaseBlockEntity
        implements ICableBEImplements, IEBlockInterfaces.IPlacementInteraction,
        IEBlockInterfaces.IStateBasedDirectional, IEServerTickableBE,
        ICableCollisionAndSelection, IEBlockInterfaces.IPlayerInteraction
{
    final CableBlockEnergyManager rfBlockManager;
    final Item cableItem;

    final Map<Direction, IEBlockCapabilityCaches.IEBlockCapabilityCache<IEnergyStorage>> neighbors = IEBlockCapabilityCaches.allNeighbors(
            Capabilities.EnergyStorage.BLOCK, this
    );

    private RFCableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int transferLimit, Item cableItem) {
        super(type, pos, state);
        this.cableItem = cableItem;
        this.rfBlockManager = new CableBlockEnergyManager(this, transferLimit);
    }

    public static RFCableBlockEntity createLv(BlockPos pos, BlockState state){
        return new RFCableBlockEntity(IMBlockEntities.ELECTRIC_CABLE_LV.get(), pos, state, 512, IMBlocks.ELECTRIC_CABLE_LV.asItem());
    }

    public static RFCableBlockEntity createMv(BlockPos pos, BlockState state){
        return new RFCableBlockEntity(IMBlockEntities.ELECTRIC_CABLE_MV.get(), pos, state, 4096, IMBlocks.ELECTRIC_CABLE_MV.asItem());
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

        //TODO Deal with situations of BE Orientation Change

        if(!pastKey.equals(rfBlockManager.getCableConnectionKey())){
            markContainingBlockForUpdate(getBlockState());
            if(level instanceof ClientLevel) {
                rfBlockManager.refreshCableConnectionKey();
            }
        }

    }

    @Override
    public void writeCustomNBT(CompoundTag nbt, boolean descPacket, HolderLookup.Provider provider) {
        nbt.put("manager", rfBlockManager.toNBT(provider));
        nbt.putLong("be_pos", this.worldPosition.asLong());
        nbt.putInt("be_facing", this.getFacing().get3DDataValue());
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
        IEnergyStorage dirHandler = neighbors.get(firstAttach).getCapability();
        rfBlockManager.updateSideCapability(worldPosition ,firstAttach, dirHandler, level.getGameTime());
        markSyncToClient();
    }




    @Override
    public ItemInteractionResult interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
        if(!(level instanceof ServerLevel))
            return ItemInteractionResult.SUCCESS;
        if(!heldItem.is(cableItem) && !heldItem.is(IEItems.Tools.WIRECUTTER.asItem())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        Vec3 hitVec = new Vec3(hitX,hitY,hitZ);

        if(heldItem.is(cableItem)){
            Direction chosenAtt = ICableCollisionAndSelection.chooseTerminalBoundingDir(this, hitVec);
            if(chosenAtt != null && hasBlockForAttachment(chosenAtt)) {

                boolean changed = rfBlockManager.addNodeAndFrontConnect(chosenAtt, level.getGameTime());
                IEnergyStorage dirHandler = neighbors.get(chosenAtt).getCapability();
                changed |= rfBlockManager.updateSideCapability(worldPosition ,chosenAtt, dirHandler, level.getGameTime());

                if(changed){
                    rfBlockManager.updateConnectedFaces();
                    markSyncToClient();
                    if(!player.isCreative()) heldItem.shrink(1);
                    level.playSound(null, worldPosition, SoundEvents.NETHERITE_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F + level.getRandom().nextFloat(), level.getRandom().nextFloat() + 0.7F + 0.3F);
                    return ItemInteractionResult.SUCCESS;
                }
            }
            level.playSound(null, worldPosition, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1.0F + level.getRandom().nextFloat(), level.getRandom().nextFloat() + 0.7F + 0.3F);
            return ItemInteractionResult.SUCCESS;
        }
        if(heldItem.is(IEItems.Tools.WIRECUTTER.asItem())){
            Direction chosenAtt = ICableCollisionAndSelection.chooseConfigFaceBoundingDir(this, hitVec);
            Direction chosenCon = ICableCollisionAndSelection.chooseConfigDir(this, chosenAtt, hitVec);
            rfBlockManager.updateObstacle(chosenAtt,chosenCon, level.getGameTime());
            IEnergyStorage dirHandler = neighbors.get(chosenAtt).getCapability();
            rfBlockManager.updateSideCapability(worldPosition ,chosenAtt, dirHandler, level.getGameTime());
            rfBlockManager.checkAllConnections();
            rfBlockManager.updateConnectedFaces();
            markSyncToClient();
            level.playSound(null, worldPosition, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.0F + level.getRandom().nextFloat(), level.getRandom().nextFloat() + 0.7F + 0.3F);
            IMUtils.LOGGER.info("att: {}, con: {}", chosenAtt.getName(), chosenCon.getName());
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public RFCableBlockEntity getOther(BlockPos position, Consumer<NeighborStatus> result){
        if(!(level instanceof ServerLevel serverLevel)) return null;
        if(!serverLevel.isLoaded(position)){
            result.accept(NeighborStatus.UNLOAD);
            return null;
        }
        BlockEntity blockEntity = serverLevel.getBlockEntity(position);
        if(blockEntity instanceof RFCableBlockEntity rfCable && isSameCable(rfCable)){
            result.accept(NeighborStatus.ACTIVE);
            return rfCable;
        }
        else {
            result.accept(NeighborStatus.NOT_SAME_CABLE);
            return null;
        }
    }

    public IEnergyStorage getNeighborEnergyHandler(Direction direction){
        return neighbors.get(direction).getCapability();
    }


    @Override
    public void notifiedStraight(BlockPos changedPos) {
        if (level == null || level.isClientSide) return;
        Vec3i delta = changedPos.subtract(this.worldPosition);
        Direction changedDir = Direction.fromDelta(delta.getX(), delta.getY(), delta.getZ());

        boolean changed = false;

        changed |= updateSideCapability(changedDir);

        if(SafeChunkUtils.getSafeBE(level, changedPos) instanceof RFCableBlockEntity cable
                && isSameCable(cable)){
            changed |= rfBlockManager.proactivelyStraightConnect(cable.rfBlockManager, changedDir, level.getGameTime());
            if(changed){
                cable.getRFBlockManager().updateConnectedFaces();
                cable.markSyncToClient();
            }
        }
        changed |= rfBlockManager.checkConnectionsTo(changedDir);
        if(changed){
            rfBlockManager.updateConnectedFaces();
            sendUpdateToGlobalData = true;
            markSyncToClient();
        }
    }

    public boolean updateSideCapability(Direction att){
        if (level == null || level.isClientSide) return false;
        boolean changed = false;
        IEnergyStorage dirHandler = neighbors.get(att).getCapability();
        changed |= rfBlockManager.updateSideCapability(worldPosition ,att, dirHandler, level.getGameTime());
        return changed;
    }


    @Override
    public void notifiedBackCorner(BlockPos changedPos) {
        if (level == null || level.isClientSide) return;
        Vec3i delta = changedPos.subtract(this.worldPosition);
        List<Direction> associateDirs = new ArrayList<>(2);
        if(delta.getX()!=0){
            associateDirs.add(Direction.fromDelta(delta.getX(),0,0));
        }
        if(delta.getY()!=0){
            associateDirs.add(Direction.fromDelta(0, delta.getY(),0));
        }
        if(delta.getZ()!=0){
            associateDirs.add(Direction.fromDelta(0, 0,delta.getZ()));
        }

        boolean changed = false;
        for(int i = 0; i < 2; i++){
            Direction attBlockDir = associateDirs.get(i);
            Direction conBlockDir = associateDirs.get(1-i);
            if(SafeChunkUtils.getSafeBE(level, changedPos) instanceof RFCableBlockEntity cable
                    && isSameCable(cable)
            ){
                changed |= rfBlockManager.proactivelyBackCornerConnect(cable.rfBlockManager, attBlockDir, conBlockDir, level.getGameTime());
                if(changed){
                    cable.getRFBlockManager().updateConnectedFaces();
                    cable.markSyncToClient();
                }
            }
            changed |= rfBlockManager.checkConnectionsTo(attBlockDir);
        }
        if(changed){
            rfBlockManager.updateConnectedFaces();
            sendUpdateToGlobalData = true;
            markSyncToClient();
        }
    }

    public void removeAndDropItems(Player player, Level level){
        player.addItem(new ItemStack(cableItem, rfBlockManager.getNodeCount()));
        level.playSound(null, worldPosition, SoundEvents.COPPER_BREAK, SoundSource.BLOCKS, 1.0F + level.getRandom().nextFloat(), level.getRandom().nextFloat() + 0.7F + 0.3F);
        level.setBlockAndUpdate(getBlockPos(), Blocks.AIR.defaultBlockState());
    }

    private boolean isSameCable(RFCableBlockEntity cable){
        if(cable == null) return false;
        return cable.cableItem.equals(this.cableItem);
    }

    protected void markSyncToClient(){
        assert level instanceof ServerLevel serverLevel;
        markContainingBlockForUpdate(null);
        setChanged();
        invalidateCapabilities();
    }


    public static void registerCapabilities(BlockCapabilityRegistration.BECapabilityRegistrar<RFCableBlockEntity> registrar)
    {
        registrar.register(Capabilities.EnergyStorage.BLOCK, RFCableBlockEntity::getCapabilities);
    }

    public static IEnergyStorage getCapabilities(RFCableBlockEntity be, Direction side){
        if (side != null){
            return be.rfBlockManager.getSideCapabilityForTerminal(side);
        }
        return null;
    }





    @Override
    public CableConnectionKey getConnectionKey() {
        return rfBlockManager.getCableConnectionKey(this.level.getGameTime());
    }

    @Override
    public @NotNull Property<Direction> getFacingProperty() {
        return RFCableBlock.DEFAULT_FACING_PROP;
    }

    @Override
    public @NotNull PlacementLimitation getFacingLimitation() {
        return PlacementLimitation.SIDE_CLICKED;
    }

    @Override
    public CableConnectionKey getConnectionKeyForBoundingBox() {
        return getConnectionKey();
    }

    @Override
    public BlockPos getWorldPositionForBoundingBox() {
        return worldPosition;
    }

    @Override
    public boolean hasBlockForAttachment(Direction att) {
        return !getLevelNonnull().isEmptyBlock(worldPosition.relative(att));
    }


    @Override
    public boolean mirrorFacingOnPlacement(LivingEntity placer)
    {
        return true;
    }

    @Override
    public Item getCableItemForBoundingBox() {
        return cableItem;
    }

    public enum NeighborStatus {
        ACTIVE(0),
        UNLOAD(1),
        NOT_SAME_CABLE(2);
        final int code;
        NeighborStatus(int code){
            this.code = code;
        }
    }
}
