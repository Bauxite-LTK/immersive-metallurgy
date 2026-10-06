package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.resolver;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.RFCableBlockEntity;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.CableConnectionKey;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.blockface.BlockFace;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection.*;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.global.GlobalRFCableConnectionData;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.node.CableNode;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.node.DummyCableNode;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.resolver.handler.CableEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class CableBlockEnergyManager implements ICableNodeInBlockManager<Integer>{
    Map<Direction, RFCableNodeInWorld> nodeMap = new EnumMap<>(Direction.class);
    RFCableBlockEntity parentBE;
    Map<BlockFace, Integer> connectFaces = new Object2IntArrayMap<>();
    EnumSet<Direction> hasEnergyStorageDirections = EnumSet.noneOf(Direction.class);
    final int transferLimit;

    public static final Direction[] DIRECTIONS = Direction.values();


    public CableBlockEnergyManager(RFCableBlockEntity be, int transferLimit){
        this.parentBE = be;
        this.transferLimit = transferLimit;
    }

    private RFCableNodeInWorld createNode(Direction att){
        BlockPos pos = parentBE.getBlockPos();
        BlockFace face = new BlockFace(pos, att);
        CableEnergyStorage handlerWrapper = new CableEnergyStorage(this, face);
        return new RFCableNodeInWorld(face,this, handlerWrapper);
    }

    public boolean addNode(Direction att){
        if(nodeMap.get(att)!=null) return false;
        nodeMap.put(att, this.createNode(att));
        GlobalRFCableConnectionData.addOrUpdateVertexes(parentBE.getLevel(), nodeMap.get(att).blockFace(), List.of());
        return true;
    }

    public IEnergyStorage getSideCapabilityForTerminal(Direction att){
        if(nodeMap.get(att) == null || !hasEnergyStorageDirections.contains(att)) return null;
        return nodeMap.get(att).energyStorage;
    }

    public boolean updateSideCapability(BlockPos thisPos, Direction att, IEnergyStorage energyStorage, long gameTime){
        boolean changed = false;
        RFCableNodeInWorld selfNode = nodeMap.get(att);
        if(selfNode == null) return false;
        if(energyStorage == null || !selfNode.isPermittedDirection(att)) {
            if(hasEnergyStorageDirections.contains(att)){
                hasEnergyStorageDirections.remove(att);
                GlobalRFCableConnectionData.updateVertexEnergyStorage(parentBE.getLevel(), selfNode.blockFace(), null);
                return true;
            }
            return false;
        }
        CableNode dummyNode = new DummyCableNode(new BlockFace(thisPos.relative(att), att.getOpposite()));
        changed = selfNode.tryConnect(dummyNode, TerminalCableConnection.BUILDER, gameTime);
        if(changed){
            hasEnergyStorageDirections.add(att);
            GlobalRFCableConnectionData.updateVertexEnergyStorage(parentBE.getLevel(), selfNode.blockFace(), energyStorage);
        }
        return changed;
    }

    public boolean proactivelyStraightConnect(CableBlockEnergyManager otherManager, Direction conDir, long gameTime){
        boolean changed = false;
        for(Direction direction : DIRECTIONS){
            if(conDir.equals(direction.getOpposite())) continue;
            if(conDir.equals(direction)){
                changed |= proactivelyTerminalConnect(otherManager, conDir, gameTime);
                continue;
            }
            RFCableNodeInWorld selfNode = nodeMap.get(direction);
            if(selfNode == null) continue;
            RFCableNodeInWorld otherNode = otherManager.nodeMap.get(direction);
            if(otherNode == null) continue;
            changed |= selfNode.tryConnect(otherNode, StraightCableConnection.BUILDER, gameTime);
        }
        return changed;
    }

    public boolean proactivelyTerminalConnect(CableBlockEnergyManager otherManager, Direction conDir, long gameTime){
        boolean changed = false;
        RFCableNodeInWorld selfNode = nodeMap.get(conDir);
        if(selfNode == null) return false;
        RFCableNodeInWorld otherNode = otherManager.nodeMap.get(conDir.getOpposite());
        if(otherNode == null) return false;
        changed = selfNode.tryConnect(otherNode, TerminalCableConnection.BUILDER, gameTime);
        return changed;
    }

    public boolean proactivelyBackCornerConnect(CableBlockEnergyManager otherManager, Direction attDir, Direction conDir, long gameTime){
        boolean changed = false;
        RFCableNodeInWorld selfNode = nodeMap.get(attDir);
        if(selfNode == null) return false;
        RFCableNodeInWorld otherNode = otherManager.nodeMap.get(conDir.getOpposite());
        if(otherNode == null) return false;
        changed = selfNode.tryConnect(otherNode, BackCornerCableConnection.BUILDER, gameTime);
        return changed;
    }

    public boolean addNodeAndFrontConnect(Direction att, long gameTime){
        if(!addNode(att)) return false;
        var thisNode = nodeMap.get(att);
        for(Direction otherAtt : DIRECTIONS){
            if(otherAtt == att || otherAtt == att.getOpposite()) continue;
            if(nodeMap.get(otherAtt) == null) continue;
            var otherNode = nodeMap.get(otherAtt);
            thisNode.tryConnect(otherNode, FrontCornerCableConnection.BUILDER, gameTime);
        }
        return true;
    }

    public void updateObstacle(Direction att, Direction con, long gameTime){
        if(nodeMap.get(att) == null) return;
        var thisNode = nodeMap.get(att);
        boolean isPermitted = thisNode.isPermittedDirection(con);
        thisNode.setSidePermission(con, !isPermitted);
        if(!isPermitted){
            for(Direction otherAtt : DIRECTIONS){
                if(otherAtt == att || otherAtt == att.getOpposite()) continue;
                if(nodeMap.get(otherAtt) == null) continue;
                var otherNode = nodeMap.get(otherAtt);
                thisNode.tryConnect(otherNode, FrontCornerCableConnection.BUILDER, gameTime);
            }
        }
    }

    public boolean checkAllConnections(){
        boolean changed = false;
        for(Direction att : DIRECTIONS){
            changed |= checkConnectionsTo(att);
        }
        return changed;
    }

    public boolean checkConnectionsTo(Direction conDir){
        boolean changed = false;
        for(Direction att : DIRECTIONS){
            RFCableNodeInWorld thisNode = nodeMap.get(att);
            if(thisNode==null) continue;
            ICableConnection connection = thisNode.getConnectionsTo(conDir);
            if(connection == null) continue;

            BlockFace otherBlockFace = connection.other(thisNode).data();
            AtomicReference<RFCableBlockEntity.NeighborStatus> status = new AtomicReference<>();
            RFCableBlockEntity otherCable = parentBE.getOther(otherBlockFace.pos(), status::set);
            if(status.get().equals(RFCableBlockEntity.NeighborStatus.NOT_SAME_CABLE)){
                if(connection instanceof TerminalCableConnection){
                    if(parentBE.getNeighborEnergyHandler(conDir) != null)
                        continue;
                }
                thisNode.confirmDisconnect(connection);
                GlobalRFCableConnectionData.removeVertex(parentBE.getLevel(), otherBlockFace);
                changed = true;

            }
            else if(status.get().equals(RFCableBlockEntity.NeighborStatus.ACTIVE)){
                if(!otherCable.getRFBlockManager().hasConnection(connection, otherBlockFace.attach())){
                    thisNode.confirmDisconnect(connection);
                    GlobalRFCableConnectionData.removeConnection(parentBE.getLevel(), thisNode.blockFace(), otherBlockFace);
                    changed = true;
                }
            }

        }
        return changed;
    }

    public boolean hasConnection(ICableConnection connection, Direction attach) {
        RFCableNodeInWorld node = nodeMap.get(attach);
        if(node == null) return false;
        return node.hasConnection(connection);
    }




    public void updateConnectedFaces(){
        connectFaces.clear();
        Set<Direction> passedAttachments = EnumSet.noneOf(Direction.class);
        for(Direction att : Direction.values()){
            if(passedAttachments.contains(att)) continue;
            List<BlockFace> connectedBlockFaces = getAllConnectedOthers(att, passedAttachments);
            for(var bf : connectedBlockFaces){
                connectFaces.put(bf, att.get3DDataValue());
            }
            //GlobalCableConnectionData.addOrUpdateVertexes(connectedBlockFaces);
        }

    }

    public static int MAX_ITERATE = 2048;
    public static int MAX_SEARCH = 2048;

    public List<BlockFace> getAllConnectedOthers(Direction attach, Set<Direction> passedAttachments){
        if(nodeMap.get(attach) == null) return new ArrayList<>();
        BlockPos center = parentBE.getBlockPos();
        List<BlockFace> out = new ArrayList<>();
        Queue<BlockFace> open = new ArrayDeque<>();
        Set<BlockFace> close = new HashSet<>();
        open.add(new BlockFace(center, attach));
        for(int i = 0; i < MAX_ITERATE && close.size() < MAX_SEARCH && !open.isEmpty(); i++){
            BlockFace face = open.poll();
            if (!close.add(face)) continue;

            out.add(face);
            if (!center.equals(face.pos())) continue;

            RFCableNodeInWorld node = nodeMap.get(face.attach());
            assert node != null;
            //if(node == null) continue;
            passedAttachments.add(face.attach());
            List<BlockFace> allNeighbors = node.getAllConnectedCables();
            open.addAll(allNeighbors);
            GlobalRFCableConnectionData.addOrUpdateVertexes(parentBE.getLevel(), face, allNeighbors);
        }
        return out;
    }




    public void removeNode(Direction attach){
        BlockFace face = nodeMap.get(attach).blockFace();
        GlobalRFCableConnectionData.removeVertex(parentBE.getLevel(), face);
        nodeMap.remove(attach);
        checkAllConnections();
    }

    public int getNodeCount(){
        return nodeMap.size();
    }


    @Override
    public int handleResourceInput(Integer resource, BlockFace inputCableFace, boolean simulate) {
        return GlobalRFCableConnectionData.allocateEnergy(parentBE.getLevel(), inputCableFace, Math.min(resource, transferLimit), simulate);
    }

    @Override
    public Integer handleResourceOutput(Integer resource, BlockFace outputFace, boolean simulate) {
        return 0;
    }

    @Override
    public Integer handleResourceOutput(int amount, BlockFace outputFace, boolean simulate) {
        return 0;
    }

    private CableConnectionKey keyLastTick = CableConnectionKey.defaultKey();
    private long timeLastGetKey = 0;
    @Override
    public CableConnectionKey getCableConnectionKey(long gameTime) {
        if(gameTime == timeLastGetKey) return keyLastTick;
        timeLastGetKey = gameTime;
        keyLastTick = new CableConnectionKey(nodeMap);
        return keyLastTick;
    }

    public CableConnectionKey getCableConnectionKey(){
        return new CableConnectionKey(nodeMap);
    }

    public void refreshCableConnectionKey(){
        keyLastTick = new CableConnectionKey(nodeMap);
    }

    @Override
    public CompoundTag toNBT(HolderLookup.Provider provider) {
        CompoundTag main = new CompoundTag();
        ListTag nodes = new ListTag();
        for(var e : nodeMap.entrySet()){
            CompoundTag nodeElement = new CompoundTag();
            nodeElement.putInt("attach", e.getKey().get3DDataValue());
            nodeElement.put("node", e.getValue().toNBT(provider));
            nodes.add(nodeElement);
        }
        main.put("node_list", nodes);

        List<Integer> hasESDir3dValueArray = hasEnergyStorageDirections.stream().map(Direction::get3DDataValue).toList();
        main.putIntArray("has_energy_storage_directions", hasESDir3dValueArray);
        return main;
    }

    @Override
    public void loadFromNBT(CompoundTag main, HolderLookup.Provider provider) {
        nodeMap.clear();
        var nodes = main.getList("node_list", Tag.TAG_COMPOUND);
        for(var tag : nodes){
            CompoundTag nodeTag = (CompoundTag)tag;
            Direction att = Direction.from3DDataValue(nodeTag.getInt("attach"));
            var node = nodeMap.computeIfAbsent(att, this::createNode);
            node.loadFromNBT(nodeTag.getCompound("node"), provider);
        }

        hasEnergyStorageDirections.clear();
        int[] hasESDir3dValueArray = main.getIntArray("has_energy_storage_directions");
        for(int dir3dValue : hasESDir3dValueArray){
            Direction dir = Direction.from3DDataValue(dir3dValue);
            hasEnergyStorageDirections.add(dir);
        }

    }

    public static RFCableNodeInWorld getRFCableNodeInWorld(BlockFace blockFace, Level level){
        assert level != null;
        BlockEntity be = level.getBlockEntity(blockFace.pos());
        if(be instanceof RFCableBlockEntity cableBE){
            return cableBE.getRFBlockManager().nodeMap.get(blockFace.attach());
        }
        return null;
    }
}
