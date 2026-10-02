package net.bauxite_ltk.immersive_metallurgy.block.transporter;

import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import net.bauxite_ltk.immersive_metallurgy.ImmersiveMetallurgy;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public interface IElectricCableConnectionBE {

    List<ConnectionInfo> getConnectionInfoList();

    ElectricCableBlockEntity.PhysicalConnectionsInfo getPhysicalConnectionInfo();

    record BlockFaceConnection(BlockFace from, BlockFace to){};

    List<BlockFaceConnection> getAllConnectBlockFace(BlockFace fromFace);

    BlockEntity getBE();

    ElectricCableEnergyStorge getSideEnergyStorge(Direction faceDir, Direction connectionDir);

    IEnergyStorage getNeighborHandler(Direction direction);

    IElectricCableConnectionBE getNeighborIElectricCable(BlockPos blockPos);

    int getTransferLimit();

    default void updateNode(){
        List<ConnectionInfo> connectionInfoList = new ArrayList<>(getConnectionInfoList());
        // int connectionByte = getConnectionByte();
        // remove invalid allConnections
        // TODO Is This Necessary?
        connectionInfoList.removeIf(info -> info.status.equals(ConnectionStatus.INVALID));

        // perform node update behaviour
        for(ConnectionInfo info : connectionInfoList){
            BlockFace rootBlockFace = info.rootTerminal;
            ConnectionStatus status = info.status;

            if(status.equals(ConnectionStatus.ROOT)){
                rootUpdateSubnet(rootBlockFace);
            }

            boolean isPreviousInvalid = false;
            BlockPos prePos = info.previous.pos;
            Direction preFaceDir = info.previous.faceDir;
            BlockPos thisPos = getBE().getBlockPos();
            int dx = prePos.getX()-thisPos.getX();
            int dy = prePos.getY()-thisPos.getY();
            int dz = prePos.getZ()-thisPos.getZ();
            IEnergyStorage source = null;
            if(Mth.abs(dx) + Mth.abs(dy) + Mth.abs(dz) == 1){
                Direction relativeDir = Direction.getNearest(dx,dy,dz);
                source = getSideEnergyStorge(info.thisFace.faceDir, relativeDir);
                if(!this.getPhysicalConnectionInfo().getState(info.thisFace.faceDir,relativeDir).isExist){
                    isPreviousInvalid = true;
                }
            }
            else{
                Direction backRelativeDir = Direction.getNearest(dx+preFaceDir.getStepX(),
                        dy+preFaceDir.getStepY(), dz+preFaceDir.getStepZ()).getOpposite();
                BlockEntity be =  SafeChunkUtils.getSafeBE(Objects.requireNonNull(getBE().getLevel()), prePos);
                if(be instanceof IElectricCableConnectionBE cableConnectionBE){
                    source = cableConnectionBE.getSideEnergyStorge(preFaceDir,backRelativeDir);
                    if(!cableConnectionBE.getPhysicalConnectionInfo().getState(preFaceDir,backRelativeDir).isExist){
                        isPreviousInvalid = true;
                    }
                }
            }
            if (source == null || isPreviousInvalid){
                ImmersiveMetallurgy.LOGGER.info("invalid preFace:{},{}", info.previous.pos, info.previous.faceDir);
                info.setStatus(ConnectionStatus.INVALID);
                invalidateNextNode(rootBlockFace, info.next);
                getConnectionInfoList().remove(info);
                rootUpdateSubnet(rootBlockFace);
                updateNode();
            }
        }
    }

    default void updateAllRootNode(){
        List<ConnectionInfo> connectionInfoList = getConnectionInfoList();
        for(ConnectionInfo info : connectionInfoList){
            BlockEntity rootBE = null;
            if (getBE().getLevel() != null) rootBE = SafeChunkUtils.getSafeBE(getBE().getLevel(), info.rootTerminal.pos);
            if(rootBE == null){
                IMUtils.LOGGER.info("rootBE is null");
                continue;
            }
            if(rootBE instanceof IElectricCableConnectionBE electricCable){
                IMUtils.LOGGER.info("updateAllRootNode");
                electricCable.updateNode();
            }
        }
    }

    default void invalidateNextNode(BlockFace rootBlockFace, BlockFace nextFace){
        if (nextFace == null) return;
        BlockEntity be =  SafeChunkUtils.getSafeBE(Objects.requireNonNull(getBE().getLevel()), nextFace.pos);
        if(be instanceof IElectricCableConnectionBE nextCable){
            ConnectionInfo nextInfo = nextCable.getConnectionInfo(rootBlockFace,nextFace);
            if(nextInfo == null) return;
            nextInfo.setStatus(ConnectionStatus.INVALID);
            nextCable.invalidateNextNode(rootBlockFace, nextInfo.next);
            nextCable.getConnectionInfoList().remove(nextInfo);
            ImmersiveMetallurgy.LOGGER.info("set invalid");
        }
    }

    default void rootUpdateSubnet(BlockFace currentRootPos){
        //forgive my code using BFS, I just don't want to write a new logic.
        List<BlockFace> openList = new LinkedList<>();
        List<BlockFace> closeList = new LinkedList<>();
        openList.add(currentRootPos);
        for(int i = 0; i < 1024; i++){
            ImmersiveMetallurgy.LOGGER.info("rootUpdateSubnet: i = {}", i);
            if(openList.isEmpty()) break;
            BlockFace curFace = openList.getFirst();
            openList.removeFirst();
            if(closeList.contains(curFace)) continue;
            BlockEntity be = null;
            if (getBE().getLevel() != null) be = SafeChunkUtils.getSafeBE(getBE().getLevel(), curFace.pos);
            if(be == null) continue;
            if(be instanceof IElectricCableConnectionBE electricCable){
                //if currentRoot is not equal to root in info, means this node was common and just change to root
//                if(infoRootPos != currentRootPos && getConnectionInfo(infoRootPos) != null){
//                    getConnectionInfo(infoRootPos).rootTerminal = currentRootPos;
//                }
                electricCable.tryClaimNext(currentRootPos, curFace);
                BlockFace nextFace = electricCable.getConnectionInfo(currentRootPos, curFace).next;
                if(nextFace != null)
                    openList.addLast(nextFace);
                closeList.addFirst(curFace);
            }
        }
    }


    default void tryClaimNext(BlockFace rootFaceOfSubnet, BlockFace thisFace){
        //byte allConnections = getConnectionByte();
        List<BlockFaceConnection> connections = getAllConnectBlockFace(thisFace);
        // update nextFace
        getConnectionInfo(rootFaceOfSubnet, thisFace).setNext(null);
        BlockFace previousBFofNextBF = null;
        for(BlockFaceConnection blockFaceConnection : connections){
            if(blockFaceConnection.to.pos.equals(getConnectionInfo(rootFaceOfSubnet, thisFace).previous.pos)) continue;
            getConnectionInfo(rootFaceOfSubnet, thisFace).setNext(blockFaceConnection.to);
            previousBFofNextBF = blockFaceConnection.from;
        }


        // from nextList, try claim neighbor cable
        BlockFace nextBlockFace = getConnectionInfo(rootFaceOfSubnet, thisFace).next;
        if(nextBlockFace != null) {
            IElectricCableConnectionBE neighbor = getNeighborIElectricCable(nextBlockFace.pos);
            if(neighbor!=null && previousBFofNextBF!=null){
                //if pipe is not in subnet, then add it
                if(neighbor.getConnectionInfo(rootFaceOfSubnet, nextBlockFace)==null){
                    neighbor.setSelfCommonToSubnet(rootFaceOfSubnet, nextBlockFace, previousBFofNextBF);
                }
            }
        }
    }

    default void setSelfCommonToSubnet(BlockFace rootFaceOfSubnet, BlockFace thisFace, BlockFace previousFace){
        List<ConnectionInfo> connectionInfoList = getConnectionInfoList();
//        //FIXME when we complete the algorithm, delete this check.
//        for(ConnectionInfo info : connectionInfoList){
//            if(info.rootTerminal.equals(rootFaceOfSubnet)){
//                throw new RuntimeException("duplicate set cable common status in subnet: " + rootFaceOfSubnet);
//            }
//        }

        connectionInfoList.add(new ConnectionInfo(rootFaceOfSubnet, ConnectionStatus.COMMON, thisFace, previousFace, null));

    }

    record energyTransferInfo(IEnergyStorage energyStorage, int transferLimit){};

    default energyTransferInfo getTailEnergyHandler(BlockFace rootTerminal, BlockFace thisFace, int transferLimit){
        //if(getConnectionInfo(rootTerminal, thisFace).status==ConnectionStatus.INVALID) return null;
        if(getConnectionInfo(rootTerminal, thisFace) == null) return null;
        BlockFace nextBlockFace = getConnectionInfo(rootTerminal, thisFace).next;

        if(nextBlockFace == null) return null;


        BlockPos nextPos = nextBlockFace.pos;
        Direction nextFaceDir = nextBlockFace.faceDir;
        BlockPos thisPos = getBE().getBlockPos();
        int dx = nextPos.getX()-thisPos.getX();
        int dy = nextPos.getY()-thisPos.getY();
        int dz = nextPos.getZ()-thisPos.getZ();
        IEnergyStorage energyStorage = null;
        if(Mth.abs(dx) + Mth.abs(dy) + Mth.abs(dz) == 1){
            Direction relativeDir = Direction.getNearest(dx,dy,dz);
            BlockEntity be =  SafeChunkUtils.getSafeBE(Objects.requireNonNull(getBE().getLevel()), nextPos);

            if(be instanceof IElectricCableConnectionBE cableConnectionBE) {
                energyStorage = cableConnectionBE.getSideEnergyStorge(nextFaceDir, relativeDir.getOpposite());
            }
            else{
                energyStorage = getNeighborHandler(relativeDir);
            }
        }
        else{
            Direction backRelativeDir = Direction.getNearest(dx+nextFaceDir.getStepX(),
                    dy+nextFaceDir.getStepY(), dz+nextFaceDir.getStepZ()).getOpposite();
            BlockEntity be =  SafeChunkUtils.getSafeBE(Objects.requireNonNull(getBE().getLevel()), nextPos);

            if(be instanceof IElectricCableConnectionBE cableConnectionBE) {
                energyStorage = cableConnectionBE.getSideEnergyStorge(nextFaceDir, backRelativeDir);
            }

        }

        if(energyStorage == null) return null;
        if(energyStorage instanceof ElectricCableEnergyStorge electricCableES){
            int otherTransferLimit = electricCableES.iElectricCable.getTransferLimit();
            return electricCableES.iElectricCable.getTailEnergyHandler(rootTerminal, nextBlockFace, Math.min(transferLimit, otherTransferLimit));
        }
        else {
            return new energyTransferInfo(energyStorage, transferLimit);
        }
    }




    class ElectricCableEnergyStorge extends EnergyStorage {
        IElectricCableConnectionBE iElectricCable;
        Direction facing;

        public ElectricCableEnergyStorge(int transferLimit, IElectricCableConnectionBE iElectricCable, Direction facing) {
            super(0, transferLimit);
            this.iElectricCable = iElectricCable;
            this.facing = facing;
        }

        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            BlockPos thisPos = iElectricCable.getBE().getBlockPos();
            BlockFace thisFace = new BlockFace(thisPos, facing);
            if(iElectricCable.getConnectionInfo(thisFace, thisFace) == null){
                iElectricCable.getConnectionInfoList().add(
                        new ConnectionInfo(
                                new BlockFace(thisPos, facing),
                                ConnectionStatus.ROOT,
                                new BlockFace(thisPos,facing),
                                new BlockFace(thisPos.relative(facing), facing.getOpposite()),
                                null));
                iElectricCable.updateNode();
            }

            energyTransferInfo energyTransferInfo = iElectricCable.getTailEnergyHandler(thisFace,thisFace,iElectricCable.getTransferLimit());
            if(energyTransferInfo != null)
                return energyTransferInfo.energyStorage.receiveEnergy(Math.min(toReceive, energyTransferInfo.transferLimit), simulate);
            else
                return 0;
        }
    }


    default ConnectionInfo getConnectionInfo(BlockFace root, BlockFace thisFace){
        List<ConnectionInfo> connectionInfoList = getConnectionInfoList();
        for(ConnectionInfo info : connectionInfoList){
            if(info.rootTerminal.equals(root) && info.thisFace.equals(thisFace)) return info;
        }
        return null;
    }

    record BlockFace(BlockPos pos, Direction faceDir){};

    class ConnectionInfo{
        BlockFace rootTerminal;
        ConnectionStatus status;
        BlockFace thisFace;
        BlockFace previous;
        BlockFace next;

        public ConnectionInfo(BlockFace rootTerminal, ConnectionStatus status, BlockFace thisFace, BlockFace previous, BlockFace next){
            this.rootTerminal = rootTerminal;
            this.status = status;
            this.previous = previous;
            this.next = next;
            this.thisFace = thisFace;
        }

        public void setNext(BlockFace next) {
            this.next = next;
        }

        public void setStatus(ConnectionStatus status) {
            this.status = status;
        }
    }

    enum ConnectionStatus{
        ROOT,
        COMMON,
        INVALID
    }

}
