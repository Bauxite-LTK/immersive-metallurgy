package net.bauxite_ltk.immersive_metallurgy.block.transporter;

import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.api.utils.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches;
import net.bauxite_ltk.immersive_metallurgy.ImmersiveMetallurgy;
import net.bauxite_ltk.immersive_metallurgy.block.BlockCapabilityRegistration;
import net.bauxite_ltk.immersive_metallurgy.block.IMBlockEntities;
import net.bauxite_ltk.immersive_metallurgy.block.IMBlocks;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class ElectricCableBlockEntity extends IEBaseBlockEntity implements IElectricCableConnectionBE, IEServerTickableBE,
        IEBlockInterfaces.IStateBasedDirectional, IEBlockInterfaces.IPlacementInteraction,
        IEBlockInterfaces.ICollisionBounds, IEBlockInterfaces.ISelectionBounds,
        IEBlockInterfaces.IPlayerInteraction{


    protected int transferLimit;
    Item instanceCableItem;

    //protected byte allConnections = 0;
    //protected byte backCornerConnectionByte = 0;

    //protected Direction mainDir;
    //protected Direction subDir;
    //protected boolean mainTerminal = false;
    //protected boolean subTerminal = false;
    public static class PhysicalConnectionsInfo {
        public static class CableState{
            boolean isExist = false;
            boolean isStraightConnect = false;
            boolean isBackCornerConnect = false;
            boolean isFrontCornerConnect = false;

            public boolean isExist(){
                return isExist;
            }

            public boolean isStraightConnect() {
                return isStraightConnect;
            }

            public boolean isBackCornerConnect() {
                return isBackCornerConnect;
            }

            public boolean isFrontCornerConnect() {
                return isFrontCornerConnect;
            }
        }
        //index 0~5:    attachment = DOWN (0)   connection = index - attachment*6 (DOWN~EAST)
        //index 6~11:   attachment = UP (1)     connection = index - attachment*6 (DOWN~EAST)
        //index 12~17:  attachment = NORTH (2)  connection = index - attachment*6 (DOWN~EAST)
        //index 18~23:  attachment = SOUTH (3)  connection = index - attachment*6 (DOWN~EAST)
        //index 24~29:  attachment = WEST (4)   connection = index - attachment*6 (DOWN~EAST)
        //index 30~35:  attachment = EAST (5)   connection = index - attachment*6 (DOWN~EAST)
        CableState[] statesList = new CableState[36];
        {
            for(int i = 0; i < 36; i ++){
                statesList[i] = new CableState();
            }
        }

        public CableState[] getStatesByConnection(Direction connectionDir){
            CableState[] result = new CableState[6];
            for(int i = 0; i < 6; i++){
                result[i] = statesList[6*i + connectionDir.get3DDataValue()];
            }
            return result;
        }

        public CableState getState(int index) {
            return statesList[index];
        }

        public CableState getState(Direction attachmentDir, Direction connectionDir){
            return getState(attachmentDir.get3DDataValue()*6 + connectionDir.get3DDataValue());
        }

        public void buildCenter(Direction attachmentDir){
            final int targetIndex = attachmentDir.get3DDataValue() * 6 + attachmentDir.get3DDataValue();
            statesList[targetIndex].isExist = true;
        }

        public void buildStraightConnection(Direction attachmentDir, Direction connectionDir){
            final int targetIndex = attachmentDir.get3DDataValue() * 6 + connectionDir.get3DDataValue();
            if(statesList[targetIndex].isExist && attachmentDir!=connectionDir){
                throw new RuntimeException("error: Try to build straight connection state but a duplicate connection already exists");
            }
            statesList[targetIndex].isExist = true;
            statesList[targetIndex].isStraightConnect = true;
        }

        public void buildBackCornerConnection(Direction attachmentDir, Direction connectionDir){
            final int targetIndex = attachmentDir.get3DDataValue() * 6 + connectionDir.get3DDataValue();
            if(statesList[targetIndex].isExist){
                throw new RuntimeException("error: Try to build back corner connection state but a duplicate connection already exists");
            }
            statesList[targetIndex].isExist = true;
            statesList[targetIndex].isBackCornerConnect = true;
        }

        public void buildFrontCornerConnection(Direction attachmentDir, Direction connectionDir){
            final int targetIndex = attachmentDir.get3DDataValue() * 6 + connectionDir.get3DDataValue();
            if(statesList[targetIndex].isExist){
                throw new RuntimeException("error: Try to build front corner connection state but a duplicate connection already exists");
            }
            statesList[targetIndex].isExist = true;
            statesList[targetIndex].isFrontCornerConnect = true;
        }

        public void deleteConnection(Direction attachmentDir, Direction connectionDir){
            final int targetIndex = attachmentDir.get3DDataValue() * 6 + connectionDir.get3DDataValue();
            if(attachmentDir!=connectionDir) statesList[targetIndex].isExist = false;
            statesList[targetIndex].isStraightConnect = false;
            statesList[targetIndex].isBackCornerConnect = false;
            statesList[targetIndex].isFrontCornerConnect = false;
        }

        public void deleteCenter(Direction attachmentDir){
            final int targetIndex = attachmentDir.get3DDataValue() * 7;
            statesList[targetIndex].isExist = false;
            statesList[targetIndex].isStraightConnect = false;
            statesList[targetIndex].isBackCornerConnect = false;
            statesList[targetIndex].isFrontCornerConnect = false;
        }

        public byte[] toByteArray(){
            byte[] result = new byte[18];
            for(int i = 0; i < 36; i ++){
                int currentByte = i/2;
                int currentBit = (i%2)*4;
                CableState state = statesList[i];
                result[currentByte] |= state.isExist? (byte) (1 << currentBit) : 0;
                result[currentByte] |= state.isStraightConnect? (byte) (1 << currentBit+1) : 0;
                result[currentByte] |= state.isBackCornerConnect? (byte) (1 << currentBit+2) : 0;
                result[currentByte] |= state.isFrontCornerConnect? (byte) (1 << currentBit+3) : 0;
            }
            return result;
        }

        public void readFromByteArray(byte[] buffer){
            for(int i = 0; i < 36; i ++){
                int currentByte = i/2;
                int currentBit = (i%2)*4;
                CableState state = statesList[i];
                state.isExist = (buffer[currentByte] & (byte) (1 << currentBit)) != 0;
                state.isStraightConnect = (buffer[currentByte] & (byte) (1 << currentBit + 1)) != 0;
                state.isBackCornerConnect = (buffer[currentByte] & (byte) (1 << currentBit + 2)) != 0;
                state.isFrontCornerConnect = (buffer[currentByte] & (byte) (1 << currentBit + 3)) != 0;
            }
        }

        public boolean isDirectionTerminal(Direction direction){
            int dirV = direction.get3DDataValue();
            if(this.statesList[dirV*7].isExist){
                int outerConnectionCount = 0;
                for(int i = 0; i < 6; i++){
                    if(i == dirV) continue;
                    PhysicalConnectionsInfo.CableState outerState = this.statesList[dirV*6 + i];
                    if(outerState.isStraightConnect || outerState.isBackCornerConnect || outerState.isFrontCornerConnect){
                        outerConnectionCount++;
                    }
                }
                if(outerConnectionCount < 2){
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof PhysicalConnectionsInfo other){
                return Arrays.equals(this.toByteArray(), other.toByteArray());
            }
            return false;
        }
    }
    protected PhysicalConnectionsInfo physicalConnectionsInfo = new PhysicalConnectionsInfo();
    //protected Map<Direction, Direction> connectionAndAttachment = new HashMap<>(6);

    protected List<ConnectionInfo> connectionInfoList = new ArrayList<>();

    public Boolean[] sideConfig = new Boolean[36];
    {
        Arrays.fill(sideConfig,false);
    }
    protected final Map<Direction, IEnergyStorage> sidedHandlers = new EnumMap<>(Direction.class);
    protected final Map<Direction, IEBlockCapabilityCaches.IEBlockCapabilityCache<IEnergyStorage>> neighbors = IEBlockCapabilityCaches.allNeighbors(
            Capabilities.EnergyStorage.BLOCK, this
    );
    {
        for(Direction f : DirectionUtils.VALUES)
            sidedHandlers.put(f, new ElectricCableEnergyStorge(transferLimit, this, f));
    }




    public ElectricCableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int transferLimit, Item cableItem) {
        super(type, pos, state);
        this.transferLimit = transferLimit;
        this.instanceCableItem = cableItem;
    }

    public static ElectricCableBlockEntity forLv(BlockPos pos, BlockState state){
        return new ElectricCableBlockEntity(IMBlockEntities.ELECTRIC_CABLE_LV.get(), pos, state, 512, IMBlocks.ELECTRIC_CABLE_LV.asItem());
    }

    public static ElectricCableBlockEntity forMv(BlockPos pos, BlockState state){
        return new ElectricCableBlockEntity(IMBlockEntities.ELECTRIC_CABLE_MV.get(), pos, state, 2048, IMBlocks.ELECTRIC_CABLE_MV.asItem());
    }

//    public static ElectricCableBlockEntity forHv(BlockPos pos, BlockState state){
//        return new ElectricCableBlockEntity(IMBlockEntities.ELECTRIC_CABLE_MV.get(), pos, state, 8192);
//    }

    @Override
    public void readCustomNBT(CompoundTag nbt, boolean descPacket, HolderLookup.Provider provider) {
        boolean changed = false;
        byte[] oldBuffer = physicalConnectionsInfo.toByteArray();
        byte[] newBuffer = nbt.getByteArray("physicalConnectionsInfo");
        if(!Arrays.equals(oldBuffer, newBuffer)) changed = true;
        physicalConnectionsInfo.readFromByteArray(newBuffer);

        Boolean[] oldSideConfig = sideConfig;
        byte[] newConfigBuffer = nbt.getByteArray("sideConfig");
        for(int i = 0; i < 36; i ++){
            sideConfig[i] = (newConfigBuffer[i / 8] & (1 << (i % 8))) != 0;
        }
        if(!Arrays.equals(oldSideConfig, sideConfig)) changed = true;


        if(level!=null&&level.isClientSide&&changed)
        {
            IMUtils.LOGGER.info("readCustomNbt setChanged");
            IMUtils.LOGGER.info(Arrays.toString(physicalConnectionsInfo.toByteArray()));
            markContainingBlockForUpdate(getBlockState());
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.levelRenderer.setBlocksDirty(
                    getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(),
                    getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()
            );
        }
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt, boolean descPacket, HolderLookup.Provider provider) {
        byte[] infoBuffer = physicalConnectionsInfo.toByteArray();
        nbt.putByteArray("physicalConnectionsInfo", infoBuffer);
        byte[] configBuffer = new byte[5];
        for(int i = 0; i < 36; i++){
            if(sideConfig[i]) configBuffer[i/8] |= (byte) (1 << (i%8));
        }
        nbt.putByteArray("sideConfig", configBuffer);
    }

    int updateTickCount;
    @Override
    public void tickServer() {
//        if(updateTickCount < 20){
//            updateTickCount++;
//            return;
//        }
//        updateTickCount = 0;
//
    }

    public boolean updateStraightConnection(Direction connectionDir)
    {
        if(level==null||level.isClientSide||!SafeChunkUtils.isChunkSafe(level, worldPosition.relative(connectionDir)))
            return false;
        PhysicalConnectionsInfo.CableState[] curConnStates = physicalConnectionsInfo.getStatesByConnection(connectionDir);
        //for every possible attachment
        boolean update = false;
        for(int i = 0; i < 6; i++){
            Direction attachmentDir = Direction.from3DDataValue(i);
            if(!this.isAttachOn(attachmentDir)) continue;
            boolean wasExist = curConnStates[i].isExist;
            boolean wasStraightConnected = curConnStates[i].isStraightConnect;

            if(wasStraightConnected){
                ImmersiveMetallurgy.LOGGER.info("execute wasConnected updateStraightConnection");
                boolean doRemove = false;
                IEnergyStorage energyStorage = neighbors.get(connectionDir).getCapability();
                BlockPos neighborPos = getBlockPos().relative(connectionDir);
                ElectricCableBlockEntity electricCable = (ElectricCableBlockEntity) getNeighborIElectricCable(neighborPos);
                if(!sideConfigValue(attachmentDir,connectionDir)) doRemove = true;
                if(energyStorage == null) doRemove = true;
                if(electricCable == null && !isDirectionTerminal(connectionDir)) doRemove = true;
                if(electricCable != null && !electricCable.sideConfigValue(attachmentDir,connectionDir.getOpposite())) doRemove = true;
                if(doRemove){
                    physicalConnectionsInfo.deleteConnection(attachmentDir,connectionDir);
                    ImmersiveMetallurgy.LOGGER.info("disconnected neighbor cable");
                    update = true;
                    continue;
                }
            }
            else if(!isConnectionSaturated(attachmentDir)){
                ImmersiveMetallurgy.LOGGER.info("execute regular updateConnectionByte");

                if (wasExist && connectionDir!=attachmentDir) {
                    IMUtils.LOGGER.info("updateStraightConnection exit: dir is occupied");
                    continue;
                }

                if(!sideConfigValue(attachmentDir, connectionDir)){
                    IMUtils.LOGGER.info("updateStraightConnection exit: this side config not allowed");
                    continue;
                }

                IEnergyStorage energyStorage = neighbors.get(connectionDir).getCapability();
                BlockPos neighborPos = getBlockPos().relative(connectionDir);
                IElectricCableConnectionBE be = getNeighborIElectricCable(neighborPos);
                //check self sideConfig and neighbor's capability
                if(energyStorage!=null){
                    //ImmersiveMetallurgy.LOGGER.info("regular 1");
                    if(be == null && isDirectionTerminal(connectionDir)){
                        physicalConnectionsInfo.buildStraightConnection(connectionDir, connectionDir);
                        ImmersiveMetallurgy.LOGGER.info("connected to block faceDir");
                        update = true;
                        continue;
                    }
                    else if(be instanceof ElectricCableBlockEntity otherElectricCable){
                        Direction otherConnectionDir = connectionDir.getOpposite();

                        if(otherElectricCable.isConnectedTo(attachmentDir, otherConnectionDir)
                                && !otherElectricCable.isStraightConnectedTo(attachmentDir, otherConnectionDir)){
                            IMUtils.LOGGER.info("updateStraightConnection exit: other cable's direction is occupied");
                            continue;
                        }

                        if(!otherElectricCable.sideConfigValue(attachmentDir, otherConnectionDir)){
                            IMUtils.LOGGER.info("updateStraightConnection exit: forbidden by other cable's sideConfig");
                            continue;
                        }

                        if(otherElectricCable.isConnectionSaturated(attachmentDir) && !otherElectricCable.isStraightConnectedTo(attachmentDir, otherConnectionDir)){
                            IMUtils.LOGGER.info("updateStraightConnection exit: other cable has max allConnections of 2, also it is not connect to this cable in advance");
                            continue;
                        }

                        ImmersiveMetallurgy.LOGGER.info("connected neighbor cable");
                        physicalConnectionsInfo.buildStraightConnection(attachmentDir, connectionDir);
                        update = true;
                        continue;

                    }
                }
            }
        }
        //ImmersiveMetallurgy.LOGGER.info("Pos:{}", getBlockPos());
        //ImmersiveMetallurgy.LOGGER.info("allConnections:{}", allConnections);
        //ImmersiveMetallurgy.LOGGER.info("mask:{}", mask);
        //ImmersiveMetallurgy.LOGGER.info("wasConnected:{}", (allConnections & (byte) mask));
        return update;
    }

    public boolean updateFrontCornerConnection(Direction face){
        if(level==null||level.isClientSide)
            return false;
        //for every possible attachment
        boolean update = false;
        for(Direction otherFace : Direction.values()){
            if(otherFace == face || otherFace == face.getOpposite()) continue;
            boolean wasExist = physicalConnectionsInfo.getState(face,otherFace).isExist;
            boolean wasOtherExist = physicalConnectionsInfo.getState(otherFace,face).isExist;
            boolean wasFrontCornerConnected = physicalConnectionsInfo.getState(face,otherFace).isFrontCornerConnect;
            boolean wasOtherFrontCornerConnected = physicalConnectionsInfo.getState(face,otherFace).isFrontCornerConnect;

            if(wasFrontCornerConnected || wasOtherFrontCornerConnected){
                ImmersiveMetallurgy.LOGGER.info("execute wasConnected updateFrontCornerConnection");
                boolean doRemove = false;
                if(!sideConfigValue(face, otherFace) || !sideConfigValue(otherFace, face)) doRemove = true;
                else if(!isAttachOn(face) || !isAttachOn(otherFace)) doRemove = true;
                if(doRemove){
                    physicalConnectionsInfo.deleteConnection(face,otherFace);
                    physicalConnectionsInfo.deleteConnection(otherFace,face);
                    ImmersiveMetallurgy.LOGGER.info("disconnected frontCorner");
                    update = true;
                    continue;
                }
            }
            else if(!isConnectionSaturated(face) && !isConnectionSaturated(otherFace)){
                ImmersiveMetallurgy.LOGGER.info("execute regular updateFrontCornerConnection");

                if (wasExist || wasOtherExist) {
                    IMUtils.LOGGER.info("updateFrontCornerConnection exit: this or other dir is occupied");
                    continue;
                }

                if(!sideConfigValue(face, otherFace) || !sideConfigValue(otherFace, face)){
                    IMUtils.LOGGER.info("updateFrontCornerConnection exit: this or other side config not allowed");
                    continue;
                }

                ImmersiveMetallurgy.LOGGER.info("connected other face");
                physicalConnectionsInfo.buildFrontCornerConnection(face, otherFace);
                physicalConnectionsInfo.buildFrontCornerConnection(otherFace, face);
                update = true;
                continue;
            }
        }
        //ImmersiveMetallurgy.LOGGER.info("Pos:{}", getBlockPos());
        //ImmersiveMetallurgy.LOGGER.info("allConnections:{}", allConnections);
        //ImmersiveMetallurgy.LOGGER.info("mask:{}", mask);
        //ImmersiveMetallurgy.LOGGER.info("wasConnected:{}", (allConnections & (byte) mask));
        return update;
    }

    public boolean updateBackCornerConnection(BlockPos notifiedPos){
        IMUtils.LOGGER.info("execute onNeighborNotified");
        Direction attachmentDir = null;
        Direction connectionDir = null;
        //find first valid attachment direction
        for(Direction dir: Direction.values()){
            attachmentDir = dir;
            if(!this.isAttachOn(attachmentDir)) continue;
            connectionDir = checkBackCornerPosition(dir, notifiedPos);
            if(connectionDir!=null) break;
        }
        if(connectionDir == null){
            IMUtils.LOGGER.info("exit: not back corner position");
            return false;
        }


        BlockEntity be = SafeChunkUtils.getSafeBE(getLevelNonnull(), notifiedPos);
        boolean wasExist = physicalConnectionsInfo.getState(attachmentDir,connectionDir).isExist;
        boolean wasBackCornerConnected = physicalConnectionsInfo.getState(attachmentDir,connectionDir).isBackCornerConnect;
        IMUtils.LOGGER.info("prepare to execute corner connect");

        if(wasBackCornerConnected){
            IMUtils.LOGGER.info("try to disconnect back corner");
            boolean disconnect = false;
//            if(!getLevelNonnull().getBlockState(notifiedPos.relative(connectionDir)).canOcclude()){
//                IMUtils.LOGGER.info("disconnect: solid block blocked connection");
//                disconnect = true;
//            }
            if(!sideConfigValue(attachmentDir,connectionDir)){
                IMUtils.LOGGER.info("disconnect: this sideConfig forbidden");
                disconnect = true;
            }
            else if(!(be instanceof ElectricCableBlockEntity)){
                IMUtils.LOGGER.info("disconnect: other cable disappear");
                disconnect = true;
            }
            else{
                ElectricCableBlockEntity otherCable = (ElectricCableBlockEntity)be;
                Direction otherAttachmentDir = connectionDir.getOpposite();
                Direction otherConnectionDir = attachmentDir.getOpposite();
                boolean otherCableHasValidAttachment = otherCable.allAttachments().contains(otherAttachmentDir);
                if (!otherCableHasValidAttachment) {
                    IMUtils.LOGGER.info("disconnect: other cable do not have valid attachment");
                    disconnect = true;
                }
                else if(!otherCable.sideConfigValue(otherAttachmentDir,otherConnectionDir)){
                    IMUtils.LOGGER.info("disconnect: other cable's backCornerDir is forbidden by its sideConfig");
                    disconnect = true;
                }
            }
            if(disconnect){
                physicalConnectionsInfo.deleteConnection(attachmentDir,connectionDir);
                ImmersiveMetallurgy.LOGGER.info("disconnected back corner neighbor cable");
                return true;
            }
            //FIXME complete disconnect logic
        }
        else if(!isConnectionSaturated(attachmentDir)){
            IMUtils.LOGGER.info("info: try to link back corner");

            if(wasExist){
                IMUtils.LOGGER.info("exit: direction is occupied");
                return false;
            }

            if(!sideConfigValue(attachmentDir,connectionDir)) {
                IMUtils.LOGGER.info("exit: this sideConfig not allowed: a={} c={}",attachmentDir,connectionDir);
                return false;
            }

            boolean isElectricCable = be instanceof ElectricCableBlockEntity;
            if (!isElectricCable) {
                IMUtils.LOGGER.info("exit: not electric cable");
                return false;
            }

            ElectricCableBlockEntity otherCable = (ElectricCableBlockEntity)be;
            Direction otherAttachmentDir = connectionDir.getOpposite();
            Direction otherConnectionDir = attachmentDir.getOpposite();

            boolean otherCableHasValidAttachment = otherCable.allAttachments().contains(otherAttachmentDir);
            if (!otherCableHasValidAttachment) {
                IMUtils.LOGGER.info("exit: other cable do not have valid attachment");
                return false;
            }

            if(otherCable.isConnectedTo(otherAttachmentDir,otherConnectionDir)
                    && !otherCable.isBackCornerConnectedTo(otherAttachmentDir, otherConnectionDir)){
                IMUtils.LOGGER.info("exit: other cable's direction is occupied");
                return false;
            }


            if(otherCable.isConnectionSaturated(otherAttachmentDir) && !otherCable.isBackCornerConnectedTo(otherAttachmentDir, otherConnectionDir)){
                IMUtils.LOGGER.info("exit: other cable has max allConnections of 2, also it is not connect to this cable in advance");
                return false;
            }


            if(!otherCable.sideConfigValue(otherAttachmentDir,otherConnectionDir)){
                IMUtils.LOGGER.info("exit: other cable's backCornerDir is forbidden by its sideConfig");
                return false;
            }


            IMUtils.LOGGER.info("info: backCornerDir is free");
            physicalConnectionsInfo.buildBackCornerConnection(attachmentDir, connectionDir);
            ImmersiveMetallurgy.LOGGER.info("connected back corner neighbor cable");
            return true;
        }
        return false;
    }

    private Direction checkBackCornerPosition(Direction attachDir, BlockPos otherPos){
        if(attachDir == null) return null;
        BlockPos attachPos = getBlockPos().relative(attachDir);
        for(Direction dir : Direction.values()){
            if(dir == attachDir || dir == attachDir.getOpposite()) continue;
            if(attachPos.relative(dir).equals(otherPos)) return dir;
        }
        return null;
    }


    @Override
    public void onNeighborBlockChange(BlockPos otherPos)
    {
        super.onNeighborBlockChange(otherPos);
        Direction dir = Direction.getNearest(otherPos.getX()-worldPosition.getX(),
                otherPos.getY()-worldPosition.getY(), otherPos.getZ()-worldPosition.getZ());
        ImmersiveMetallurgy.LOGGER.info("{} onNeighborBlockChange, dir: {}", getBlockPos(), dir);
        if(updateStraightConnection(dir))
        {
            updateNode();
            updateAllRootNode();
            Level world = getLevelNonnull();
            world.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }


    public boolean isDirectionTerminal(Direction direction){
        return physicalConnectionsInfo.isDirectionTerminal(direction);
    }


    public boolean isConnectionSaturated(Direction attachmentSide){
        int count = 0;
        for(Direction dir : Direction.values()){
            if(dir == attachmentSide){
                if(isStraightConnectedTo(attachmentSide, dir)) count++;
            }
            else if(isConnectedTo(attachmentSide, dir)) count++;
        }
        return count >= 2;
    }

    public PhysicalConnectionsInfo getPhysicalConnectionsInfo() {
        return physicalConnectionsInfo;
    }

    @Override
    public void onBEPlaced(BlockPlaceContext ctx) {
        if (getLevel() != null && getLevel().isClientSide) return;
        Direction firstAttach = getFacing();
        activateFace(firstAttach);

        //getLevelNonnull().blockEvent(getBlockPos(), getBlockState().getBlock(), 0, 0);
        //setSide(mainDir.getOpposite(), false);

        //ImmersiveMetallurgy.LOGGER.info("mainAttachment: {}", mainDir);

        invalidateCapabilities();
        //markContainingBlockForUpdate(null);
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);

    }

    @Override
    public ItemInteractionResult interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
        if(level.isClientSide)
            return ItemInteractionResult.SUCCESS;
        if(!heldItem.is(instanceCableItem) && !heldItem.is(IEItems.Tools.WIRECUTTER.asItem())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        Vec3 hitVec = new Vec3(hitX,hitY,hitZ);
        if(heldItem.is(instanceCableItem)){

            Direction fd = null;
            List<AABB> boxes = getBoxes(new BoundingBoxKey(this, true,null,false,null, physicalConnectionsInfo));
            for(AABB box : boxes) {
                if (box.inflate(.002).contains(hitVec)) {
                    for (Direction d : DirectionUtils.VALUES) {
                        Vec3 testVec = new Vec3(0.5 + 0.5 * d.getStepX(), 0.5 + 0.5 * d.getStepY(), 0.5 + 0.5 * d.getStepZ());
                        if (box.inflate(0.002).contains(testVec)) {
                            fd = d;
                            break;
                        }
                    }
                    break;
                }
            }
            if(fd!= null && !isAttachOn(fd)) {
                activateFace(fd);
                updateFrontCornerConnection(fd);
                updateAllRootNode();
                updateNode();
                invalidateCapabilities();
                level.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),3);
                markContainingBlockForUpdate(null);
                level.playSound(null, worldPosition, SoundEvents.NETHERITE_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F + level.getRandom().nextFloat(), level.getRandom().nextFloat() + 0.7F + 0.3F);
                return ItemInteractionResult.SUCCESS;
            }
            level.playSound(null, worldPosition, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1.0F + level.getRandom().nextFloat(), level.getRandom().nextFloat() + 0.7F + 0.3F);
            return ItemInteractionResult.SUCCESS;
        }
        else if(heldItem.is(IEItems.Tools.WIRECUTTER.asItem())){
            Direction faceToConfigure = null;
            List<AABB> boxes = allAvailableConnectionsAABBs(this);
            for (AABB box : boxes) {
                if (box.inflate(.002).contains(hitVec)) {
                    for (Direction d : DirectionUtils.VALUES) {
                        if (box.inflate(0.002).intersects(getFaceAABB(d))) {
                            faceToConfigure = d;
                            IMUtils.LOGGER.info(faceToConfigure.getName());
                            break;
                        }
                    }
                    break;
                }
            }

            if(faceToConfigure==null){
                level.playSound(null, worldPosition, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1.0F + level.getRandom().nextFloat(), level.getRandom().nextFloat() + 0.7F + 0.3F);
                return ItemInteractionResult.CONSUME;
            }

            List<AABB> availableBoxes = specificAvailableConnectionsAABBs(physicalConnectionsInfo, faceToConfigure);
            Direction directionToConfigure = null;
            for(AABB box : availableBoxes) {
                if (box.inflate(.002).contains(hitVec)) {
                    Vec3 terminalBoxCenter = new Vec3(
                            0.5 + 6.5/16 * faceToConfigure.getStepX(),
                            0.5 + 6.5/16 * faceToConfigure.getStepY(),
                            0.5 + 6.5/16 * faceToConfigure.getStepZ());
                    for (Direction d : DirectionUtils.VALUES) {
                        if(d == faceToConfigure.getOpposite()) continue;
                        Vec3 testVec;
                        if(d == faceToConfigure) testVec = terminalBoxCenter;
                        else testVec = terminalBoxCenter.add(5d/16*d.getStepX(),5d/16*d.getStepY(),5d/16*d.getStepZ());
                        //IMUtils.LOGGER.info("test vector:{}", testVec);
                        if (box.inflate(0.002).contains(testVec)) {
                            directionToConfigure = d;
                            break;
                        }
                    }
                    break;
                }
            }
            if(directionToConfigure != null){
                IMUtils.LOGGER.info("hit face:{}, dirOnFace:{}", faceToConfigure,directionToConfigure);
                //IMUtils.LOGGER.info("sideConfigValue:{}", !sideConfigValue(faceToConfigure,faceToConfigure));
                setSideConfig(faceToConfigure, directionToConfigure, !sideConfigValue(faceToConfigure,directionToConfigure));
                IMUtils.LOGGER.info("setSideConfigTo:{}", sideConfigValue(faceToConfigure,directionToConfigure));

                if(updateStraightConnection(directionToConfigure)){
                    BlockPos otherPos = getBlockPos().relative(directionToConfigure);
                    ElectricCableBlockEntity otherCable = (ElectricCableBlockEntity) SafeChunkUtils.getSafeBE(level,otherPos);
                    otherCable.updateStraightConnection(directionToConfigure.getOpposite());
                    level.sendBlockUpdated(otherPos, level.getBlockState(otherPos),level.getBlockState(otherPos),3);
                }

                BlockPos otherBackCornerPos = getBlockPos().relative(faceToConfigure).relative(directionToConfigure);
                if(SafeChunkUtils.getSafeBE(level, otherBackCornerPos) instanceof ElectricCableBlockEntity targetElectricCable){
                    if(targetElectricCable.updateBackCornerConnection(getBlockPos())) {
                        targetElectricCable.updateAllRootNode();
                        //targetElectricCable.invalidateCapabilities();
                        Level world = targetElectricCable.getLevelNonnull();
                        world.sendBlockUpdated(otherBackCornerPos, targetElectricCable.getBlockState(), targetElectricCable.getBlockState(), 3);
                    }
                    if(SafeChunkUtils.getSafeBE(level, getBlockPos()) instanceof ElectricCableBlockEntity thisElectricCable){
                        if(thisElectricCable.updateBackCornerConnection(otherBackCornerPos)) {
                            thisElectricCable.updateAllRootNode();
                            //thisElectricCable.invalidateCapabilities();
                            Level world = thisElectricCable.getLevelNonnull();
                            world.sendBlockUpdated(getBlockPos(), thisElectricCable.getBlockState(), thisElectricCable.getBlockState(), 3);
                        }
                    }
                }

                updateFrontCornerConnection(faceToConfigure);
                updateFrontCornerConnection(directionToConfigure);

                updateAllRootNode();
                updateNode();
                invalidateCapabilities();
                level.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),3);
                //markContainingBlockForUpdate(null);
            }


            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.SUCCESS;
    }



    public List<Direction> allAttachments(){
        List<Direction> attachments = new ArrayList<>(6);
        for(int i = 0; i < 6; i++){
            if(physicalConnectionsInfo.statesList[i*7].isExist)
                attachments.add(Direction.from3DDataValue(i));
        }
        return attachments;
    }

    protected void activateFace(Direction face){
        for(Direction dir : Direction.values()){
            if(dir == face.getOpposite()) continue;
            setSideConfig(face, dir, true);
        }
        physicalConnectionsInfo.buildCenter(face);
        for(Direction d : DirectionUtils.VALUES){
            updateStraightConnection(d);
        }
    }

    protected void deactivateFace(Direction face){
        for(Direction dir : Direction.values()){
            setSideConfig(face, dir, false);
        }
        for(Direction d : DirectionUtils.VALUES){
            updateStraightConnection(d);
            updateFrontCornerConnection(d);
            physicalConnectionsInfo.deleteConnection(face,d);
        }
        physicalConnectionsInfo.deleteCenter(face);
    }

    protected boolean sideConfigValue(Direction attachmentDir, Direction connectionDir){
        return sideConfig[attachmentDir.get3DDataValue()*6 + connectionDir.get3DDataValue()];
    }

    protected void setSideConfig(Direction attachmentDir, Direction connectionDir, boolean value){
        sideConfig[attachmentDir.get3DDataValue()*6 + connectionDir.get3DDataValue()] = value;
    }


    @Override
    public @NotNull Property<Direction> getFacingProperty() {
        return ElectricCableBlock.DEFAULT_FACING_PROP;
    }

    @Override
    public @NotNull PlacementLimitation getFacingLimitation() {
        return PlacementLimitation.SIDE_CLICKED;
    }

    @Override
    public boolean mirrorFacingOnPlacement(LivingEntity placer)
    {
        return true;
    }

    @Override
    public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
    {
        return false;
    }

    @Override
    public List<ConnectionInfo> getConnectionInfoList() {
        return connectionInfoList;
    }

    @Override
    public PhysicalConnectionsInfo getPhysicalConnectionInfo() {
        return physicalConnectionsInfo;
    }

    @Override
    public List<BlockFaceConnection> getAllConnectBlockFace(BlockFace fromFace) {
        //forgive my code using BFS, I just don't want to write a new logic.
        List<BlockFaceConnection> result = new ArrayList<>();
        List<BlockFace> openList = new ArrayList<>();
        List<BlockFace> closeList = new ArrayList<>();
        openList.add(fromFace);
        for(int i = 0; i < 20; i++){
            if(openList.isEmpty()) break;
            BlockFace curBlockFace = openList.getFirst();
            openList.removeFirst();
            if(closeList.contains(curBlockFace)) continue;
            closeList.add(curBlockFace);
            BlockPos curPos = curBlockFace.pos();
            Direction curFaceDir = curBlockFace.faceDir();
            for(Direction nextDir : Direction.values()){
                if(isStraightConnectedTo(curFaceDir, nextDir)){
                    BlockFace connectFace;
                    if(curFaceDir==nextDir) connectFace = new BlockFace(curPos.relative(nextDir),curFaceDir.getOpposite());
                    else connectFace = new BlockFace(curPos.relative(nextDir), curFaceDir);
                    result.add(new BlockFaceConnection(curBlockFace,connectFace));
                }
                else if(isBackCornerConnectedTo(curFaceDir,nextDir)){
                    BlockFace connectFace = new BlockFace(curPos.relative(curFaceDir).relative(nextDir), nextDir.getOpposite());
                    result.add(new BlockFaceConnection(curBlockFace,connectFace));
                }
                else if(isFrontCornerConnectedTo(curFaceDir, nextDir)){
                    openList.add(new BlockFace(curPos,nextDir));
                }
            }
        }
        return result;
    }

//    @Override
//    public byte getConnectionByte() {
//        return 0;
//    }

//    @Override
//    public byte getConnectionByte() {
//        return p;
//    }

    @Override
    public BlockEntity getBE() {
        return this;
    }

    @Override
    public ElectricCableEnergyStorge getSideEnergyStorge(Direction attachmentDir, Direction connectionDir) {
        if(sideConfigValue(attachmentDir,connectionDir) && isConnectedTo(attachmentDir,connectionDir)) return (ElectricCableEnergyStorge) sidedHandlers.get(connectionDir);
        else return null;
    }

    @Override
    public IEnergyStorage getNeighborHandler(Direction direction) {
        return neighbors.get(direction).getCapability();
    }

    @Override
    public IElectricCableConnectionBE getNeighborIElectricCable(BlockPos blockPos){
        if(blockPos==null) return null;
        BlockEntity con = null;
        if (level != null) {
            con = SafeChunkUtils.getSafeBE(level, blockPos);
        }
        if (con instanceof IElectricCableConnectionBE up) {
            return up;
        }
        return null;
    }

    @Override
    public int getTransferLimit() {
        return transferLimit;
    }

    public boolean isAttachOn(Direction attachmentDir){
        return isConnectedTo(attachmentDir, attachmentDir);
    }

    public boolean isConnectedTo(Direction attachmentDir, Direction connectionDir){
        return physicalConnectionsInfo.getState(attachmentDir,connectionDir).isExist;
    }

    public boolean isStraightConnectedTo(Direction attachmentDir, Direction connectionDir){
        return physicalConnectionsInfo.getState(attachmentDir, connectionDir).isStraightConnect;
    }

    public boolean isBackCornerConnectedTo(Direction attachmentDir, Direction connectionDir){
        return physicalConnectionsInfo.getState(attachmentDir, connectionDir).isBackCornerConnect;
    }

    public boolean isFrontCornerConnectedTo(Direction attachmentDir, Direction connectionDir){
        return physicalConnectionsInfo.getState(attachmentDir, connectionDir).isFrontCornerConnect;
    }

    private static final CachedVoxelShapes<BoundingBoxKey> SHAPES = new CachedVoxelShapes<>(ElectricCableBlockEntity::getBoxes);

    @Override
    public VoxelShape getCollisionShape(CollisionContext ctx)
    {
        //IMUtils.LOGGER.info(Arrays.toString(physicalConnectionsInfo.toByteArray()));
        return SHAPES.get(new BoundingBoxKey(this, physicalConnectionsInfo));
    }

    @Override
    public VoxelShape getSelectionShape(@Nullable CollisionContext ctx)
    {
        //TODO needs to be a more generic check!
        boolean wireCutter = ctx!=null&&ctx.isHoldingItem(IEItems.Tools.WIRECUTTER.get());
        boolean cable = ctx!=null&&ctx.isHoldingItem(instanceCableItem);
        Direction toolViewDirection = null;
        Direction availableConnectionFace = null;
        if(ctx instanceof EntityCollisionContext ecc && ecc.getEntity() instanceof Player player){
            HitResult hitResult = Minecraft.getInstance().hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK){
                Vec3 hitVec = hitResult.getLocation().subtract(Vec3.atLowerCornerOf(worldPosition));

                Direction terminalFace = null;

                if(cable) {
                    List<AABB> boxes = allCableTerminalsAABBs();
                    for (AABB box : boxes) {
                        if (box.inflate(.002).contains(hitVec)) {
                            for (Direction d : DirectionUtils.VALUES) {
                                Vec3 testVec = new Vec3(0.5 + 0.5 * d.getStepX(), 0.5 + 0.5 * d.getStepY(), 0.5 + 0.5 * d.getStepZ());
                                if (box.inflate(0.002).contains(testVec)) {
                                    terminalFace = d;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
                if(wireCutter){
                    List<AABB> boxes = allAvailableConnectionsAABBs(this);
                    for (AABB box : boxes) {
                        if (box.inflate(.002).contains(hitVec)) {
                            for (Direction d : DirectionUtils.VALUES) {
                                //IMUtils.LOGGER.info("contains");
                                if (box.inflate(0.002).intersects(getFaceAABB(d))) {
                                    availableConnectionFace = d;
                                    //IMUtils.LOGGER.info("intersect");
                                    //IMUtils.LOGGER.info(availableConnectionFace.getName());
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }

                if(terminalFace!= null && !getLevelNonnull().isEmptyBlock(worldPosition.relative(terminalFace)))
                    toolViewDirection = terminalFace;
            }
        }

        return SHAPES.get(new BoundingBoxKey( this, cable && toolViewDirection!=null, toolViewDirection, availableConnectionFace!=null, availableConnectionFace, physicalConnectionsInfo));
    }

    public static AABB getFaceAABB(Direction face){
        return switch (face){
            case DOWN -> new AABB(3d/16,0,3d/16,13d/16,1d/16,13d/16);
            case UP -> new AABB(3d/16,15d/16,3d/16,13d/16,1,13d/16);
            case NORTH -> new AABB(3d/16,3d/16,0,13d/16,13d/16,1d/16);
            case SOUTH -> new AABB(3d/16,3d/16,15d/16,13d/16,13d/16,1);
            case WEST -> new AABB(0,3d/16,3d/16,1d/16,13d/16,13d/16);
            case EAST -> new AABB(15d/16,3d/16,3d/16,1,13d/16,13d/16);
        };
    }



    private static List<AABB> getBoxes(BoundingBoxKey key)
    {
        if(!key.showExtraTerminals && !key.showAvailableConnections) return cableAABBs(key.physicalConnectionsInfo);
        if(key.showExtraTerminals && key.extraTerminalDir != null) return cableTerminalWithToolViewAABBs(key.physicalConnectionsInfo, key.extraTerminalDir);
        if(key.showExtraTerminals) return allCableTerminalsAABBs();
        return specificAvailableConnectionsAABBs(key.physicalConnectionsInfo, key.availableConnectionsFace);
    }

    private static class BoundingBoxKey
    {
        private final ElectricCableBlockEntity te;
        private final boolean showExtraTerminals;
        private final PhysicalConnectionsInfo physicalConnectionsInfo;
        private final Direction extraTerminalDir;
        private final boolean showAvailableConnections;
        private final Direction availableConnectionsFace;



        private BoundingBoxKey(ElectricCableBlockEntity te, PhysicalConnectionsInfo physicalConnectionsInfo)
        {
            this.te = te;
            this.showExtraTerminals = false;
            this.physicalConnectionsInfo = physicalConnectionsInfo;
            this.extraTerminalDir = null;
            this.showAvailableConnections = false;
            this.availableConnectionsFace = null;
        }

        private BoundingBoxKey(ElectricCableBlockEntity te, boolean showExtraTerminals, Direction extraTerminalDir, boolean showAvailableConnections, Direction availableConnectionsFace, PhysicalConnectionsInfo physicalConnectionsInfo)
        {
            this.te = te;
            this.showExtraTerminals = showExtraTerminals;
            this.physicalConnectionsInfo = physicalConnectionsInfo;
            this.extraTerminalDir = extraTerminalDir;
            this.showAvailableConnections = showAvailableConnections;
            this.availableConnectionsFace = availableConnectionsFace;
        }

        @Override
        public boolean equals(Object o)
        {
            if(this==o) return true;
            if(o==null||getClass()!=o.getClass()) return false;
            BoundingBoxKey that = (BoundingBoxKey)o;
            return showExtraTerminals ==that.showExtraTerminals &&
                    extraTerminalDir == that.extraTerminalDir &&
                    showAvailableConnections == that.showAvailableConnections &&
                    availableConnectionsFace == that.availableConnectionsFace &&
                    Arrays.equals(physicalConnectionsInfo.toByteArray(), ((BoundingBoxKey) o).physicalConnectionsInfo.toByteArray());
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(showExtraTerminals, extraTerminalDir, showAvailableConnections, availableConnectionsFace, Arrays.hashCode(physicalConnectionsInfo.toByteArray()));
        }

    }





    public static void registerCapabilities(BlockCapabilityRegistration.BECapabilityRegistrar<ElectricCableBlockEntity> registrar)
    {
        registrar.register(Capabilities.EnergyStorage.BLOCK, ElectricCableBlockEntity::getCapabilities);
    }

    public static IEnergyStorage getCapabilities(ElectricCableBlockEntity be, Direction side){
        if (side != null){
            //if the Requester is also ElectricCable, then return capability
            BlockEntity capabilityRequester = SafeChunkUtils.getSafeBE(be.getLevelNonnull(), be.getBlockPos().relative(side));
            if(capabilityRequester instanceof ElectricCableBlockEntity){
                return be.sidedHandlers.get(side);
            }
            //also if this electric cable has a Terminal on the request side, then return capability
            else if(be.isDirectionTerminal(side)){
                return be.sidedHandlers.get(side);
            }
        }
        return null;
    }

    private static List<AABB> cableAABBs(PhysicalConnectionsInfo physicalConnectionsInfo){
        List<AABB> aabbList = new ArrayList<>();
        for(int i = 0; i < 36; i ++){
            PhysicalConnectionsInfo.CableState state = physicalConnectionsInfo.getState(i);
            Direction attachmentDir = Direction.from3DDataValue(i/6);
            Direction connectionDir = Direction.from3DDataValue(i%6);
            if(attachmentDir == connectionDir){
                if(physicalConnectionsInfo.isDirectionTerminal(attachmentDir)){
                    aabbList.add(terminalAttachmentAABB(attachmentDir));
                }
                else if(state.isExist){
                    aabbList.add(normalAttachmentAABB(attachmentDir));
                }
            }
            else{
                if(state.isExist){
                    AABB aabb = connectionAABB(attachmentDir,connectionDir);
                    if(aabb != null) aabbList.add(aabb);
                }
            }
        }
        return aabbList;
    }

    private static List<AABB> allCableTerminalsAABBs(){
        List<AABB> aabbList = new ArrayList<>();
        for(int i = 0; i < 6; i ++){
            Direction attachmentDir = Direction.from3DDataValue(i);
            aabbList.add(terminalAttachmentAABB(attachmentDir));

        }
        return aabbList;
    }

    private static List<AABB> allAvailableConnectionsAABBs(ElectricCableBlockEntity te){
        List<AABB> aabbList = new ArrayList<>();
        for(Direction face : Direction.values()){
            if(te.isAttachOn(face)){
                aabbList.add(terminalAttachmentAABB(face));
                for(Direction next : Direction.values()){
                    AABB aabb = connectionAABB(face,next);
                    if(aabb != null) aabbList.add(aabb);
                }
            }
        }
        return aabbList;
    }

    private static List<AABB> specificAvailableConnectionsAABBs(PhysicalConnectionsInfo physicalConnectionsInfo, Direction face){
        //IMUtils.LOGGER.info("specificAvailableConnectionsAABBs");
        List<AABB> aabbList = new ArrayList<>();
        for(int i = 0; i < 36; i ++){
            PhysicalConnectionsInfo.CableState state = physicalConnectionsInfo.getState(i);
            Direction attachmentDir = Direction.from3DDataValue(i/6);
            Direction connectionDir = Direction.from3DDataValue(i%6);
            if(attachmentDir == connectionDir){
                if(physicalConnectionsInfo.isDirectionTerminal(attachmentDir) || attachmentDir == face){
                    aabbList.add(terminalAttachmentAABB(attachmentDir));
                }
            }
            else{
                if(attachmentDir == face){
                    AABB aabb = connectionAABB(attachmentDir,connectionDir);
                    if(aabb != null) aabbList.add(aabb);
                }
            }
        }
        return aabbList;
    }

    private static List<AABB> cableTerminalWithToolViewAABBs(PhysicalConnectionsInfo physicalConnectionsInfo, Direction toolViewDir){
        List<AABB> aabbList = new ArrayList<>();
        for(int i = 0; i < 36; i ++){
            PhysicalConnectionsInfo.CableState state = physicalConnectionsInfo.getState(i);
            Direction attachmentDir = Direction.from3DDataValue(i/6);
            Direction connectionDir = Direction.from3DDataValue(i%6);
            if(attachmentDir == connectionDir) {
                if (physicalConnectionsInfo.isDirectionTerminal(attachmentDir)) {
                    aabbList.add(terminalAttachmentAABB(attachmentDir));
                }
                else if (state.isExist) {
                    aabbList.add(normalAttachmentAABB(attachmentDir));
                }
                else if(attachmentDir == toolViewDir){
                    aabbList.add(terminalAttachmentAABB(attachmentDir));
                }
            }
            else{
                if(state.isExist){
                    AABB aabb = connectionAABB(attachmentDir,connectionDir);
                    if(aabb != null) aabbList.add(aabb);
                }
            }
        }
        return aabbList;
    }

    private static AABB connectionAABB(Direction attachmentDir, Direction connectionDir){
        return switch (attachmentDir){
            case DOWN -> downOrUpConnectionAABB(connectionDir,true);
            case UP -> downOrUpConnectionAABB(connectionDir,false);
            case NORTH -> northOrSouthConnectionAABB(connectionDir,true);
            case SOUTH -> northOrSouthConnectionAABB(connectionDir,false);
            case WEST -> westOrEastConnectionAABB(connectionDir,true);
            case EAST -> westOrEastConnectionAABB(connectionDir,false);
        };
    }

    private static AABB normalAttachmentAABB(Direction attachmentDir){
        return switch (attachmentDir){
            case DOWN -> new AABB(6d/16, 0, 6d/16, 10d/16, 2d/16, 10d/16);
            case UP -> new AABB(6d/16, 14d/16, 6d/16, 10d/16, 1, 10d/16);
            case NORTH -> new AABB(6d/16, 6d/16, 0, 10d/16, 10d/16, 2d/16);
            case SOUTH -> new AABB(6d/16, 6d/16, 14d/16, 10d/16, 10d/16, 1);
            case WEST -> new AABB(0, 6d/16, 6d/16, 2d/16, 10d/16, 10d/16);
            case EAST -> new AABB(14d/16, 6d/16, 6d/16, 1, 10d/16, 10d/16);
        };
    }

    private static AABB terminalAttachmentAABB(Direction attachmentDir){
        return switch (attachmentDir){
            case DOWN -> new AABB(5d/16, 0, 5d/16, 11d/16, 3d/16, 11d/16);
            case UP -> new AABB(5d/16, 13d/16, 5d/16, 11d/16, 1, 11d/16);
            case NORTH -> new AABB(5d/16, 5d/16, 0, 11d/16, 11d/16, 3d/16);
            case SOUTH -> new AABB(5d/16, 5d/16, 13d/16, 11d/16, 11d/16, 1);
            case WEST -> new AABB(0, 5d/16, 5d/16, 3d/16, 11d/16, 11d/16);
            case EAST -> new AABB(13d/16, 5d/16, 5d/16, 1, 11d/16, 11d/16);
        };
    }

    private static AABB downOrUpConnectionAABB(Direction connectionDir, boolean isDown){
        double y1 = isDown? 0:14d/16;
        double y2 = isDown? 2d/16:1;
        return switch (connectionDir){
            case NORTH -> new AABB(6d/16,y1,0,10d/16,y2,6d/16);
            case SOUTH -> new AABB(6d/16,y1,10d/16,10d/16,y2,1);
            case WEST -> new AABB(0,y1,6d/16,6d/16,y2,10d/16);
            case EAST -> new AABB(10d/16,y1,6d/16,1,y2,10d/16);
            default -> null;
        };
    }

    private static AABB northOrSouthConnectionAABB(Direction connectionDir, boolean isNorth){
        double z1 = isNorth? 0:14d/16;
        double z2 = isNorth? 2d/16:1;
        return switch (connectionDir){
            case DOWN -> new AABB(6d/16,0,z1,10d/16,6d/16,z2);
            case UP -> new AABB(6d/16,10d/16,z1,10d/16,1,z2);
            case WEST -> new AABB(0,6d/16,z1,6d/16,10d/16,z2);
            case EAST -> new AABB(10d/16,6d/16,z1,1,10d/16,z2);
            default -> null;
        };
    }

    private static AABB westOrEastConnectionAABB(Direction connectionDir, boolean isWest){
        double x1 = isWest? 0:14d/16;
        double x2 = isWest? 2d/16:1;
        return switch (connectionDir){
            case NORTH -> new AABB(x1,6d/16,0,x2,10d/16,6d/16);
            case SOUTH -> new AABB(x1,6d/16,10d/16,x2,10d/16,1);
            case DOWN -> new AABB(x1,0,6d/16,x2,6d/16,10d/16);
            case UP -> new AABB(x1,10d/16,6d/16,x2,1,10d/16);
            default -> null;
        };
    }



    public ConnectionStyle getConnectionStyle(Direction attachmentDir, Direction connectionDir){
        if(isConnectedTo(attachmentDir, connectionDir)) return ConnectionStyle.COMMON;
        return ConnectionStyle.NO_CONNECTION;
    }


    public enum ConnectionStyle{
        NO_CONNECTION,
        COMMON
    }
}
