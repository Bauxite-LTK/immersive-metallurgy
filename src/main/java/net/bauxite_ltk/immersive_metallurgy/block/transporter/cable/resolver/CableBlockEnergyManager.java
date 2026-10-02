package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.resolver;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.api.resourceStorage.EnergyUniStorage;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.RFCableBlockEntity;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.CableConnectionKey;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.blockface.BlockFace;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection.ICableConnection;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection.StraightCableConnection;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.resolver.handler.CableHandler;
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
import java.util.function.Consumer;

public class CableBlockEnergyManager implements ICableNodeInBlockManager<Integer>{
    Map<Direction, RFCableNodeInWorld> nodeMap = new EnumMap<>(Direction.class);
    RFCableBlockEntity parentBE;
    Map<BlockFace, Integer> connectFaces = new Object2IntArrayMap<>();

    public static final Direction[] DIRECTIONS = Direction.values();


    public CableBlockEnergyManager(RFCableBlockEntity be){
        this.parentBE = be;
    }

    private RFCableNodeInWorld createNode(Direction att){
        BlockPos pos = parentBE.getBlockPos();
        BlockFace face = new BlockFace(pos, att);
        CableHandler<Integer, IEnergyStorage> handlerWrapper = new CableHandler<>(
                new EnergyUniStorage(0), this, face
        );
        return new RFCableNodeInWorld(face,this, handlerWrapper);
    }

    public boolean addNode(Direction att){
        if(nodeMap.get(att)!=null) return false;
        nodeMap.put(att, this.createNode(att));
        return true;
    }

    public boolean proactivelyStraightConnect(CableBlockEnergyManager otherManager, Direction otherDir, long gameTime){
        boolean changed = false;
        for(Direction direction : DIRECTIONS){
            if(otherDir.equals(direction) || otherDir.equals(direction.getOpposite())) continue;
            RFCableNodeInWorld selfNode = nodeMap.get(direction);
            if(selfNode == null) continue;
            RFCableNodeInWorld otherNode = otherManager.nodeMap.get(direction);
            if(otherNode == null) continue;
            changed |= selfNode.tryConnect(otherNode, StraightCableConnection.BUILDER, gameTime);
        }
        return changed;
    }

    public boolean checkConnectionsTo(Direction direction){
        boolean changed = false;
        for(Direction att : DIRECTIONS){
            RFCableNodeInWorld node = nodeMap.get(att);
            if(node==null) continue;
            List<ICableConnection> connectionsInvolved = node.getConnectionsTo(direction);

            for(var connection : connectionsInvolved){
                BlockFace otherBlockFace = connection.other(node).data();
                AtomicReference<RFCableBlockEntity.NeighborStatus> status = new AtomicReference<>();
                RFCableBlockEntity otherCable = parentBE.getOther(otherBlockFace.pos(), status::set);
                if(status.get().equals(RFCableBlockEntity.NeighborStatus.MISSING)){
                    node.confirmDisconnect(connection);
                    changed = true;
                }
                else if(status.get().equals(RFCableBlockEntity.NeighborStatus.ACTIVE)){
                    if(!otherCable.getRFBlockManager().hasConnection(connection, otherBlockFace.attach())){
                        node.confirmDisconnect(connection);
                        changed = true;
                    }
                }

                //TODO should deal with Unload situation?
            }
        }
        return changed;
    }

    public boolean hasConnection(ICableConnection connection, Direction attach) {
        RFCableNodeInWorld node = nodeMap.get(attach);
        return node.hasConnection(connection);
    }






//    public void removeInvalidConnections(){
//        for(Direction direction : Direction.values()){
//            RFCableNodeInWorld node = nodeMap.get(direction);
//            node.checkInvalidConnections().forEach(node::confirmDisconnect);
//        }
//    }



    public void updateConnectedFaces(){
        connectFaces.clear();
        Set<Direction> passedAttachments = EnumSet.noneOf(Direction.class);
        for(Direction att : Direction.values()){
            if(passedAttachments.contains(att)) continue;
            List<BlockFace> connectedBlockFaces = getAllConnectedOthers(att, passedAttachments);
            for(var bf : connectedBlockFaces){
                connectFaces.put(bf, att.get3DDataValue());
            }
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
            if (!center.equals(face.pos())) {
                out.add(face);
                continue;
            }
            RFCableNodeInWorld node = nodeMap.get(face.attach());
            if(node == null) continue;
            passedAttachments.add(face.attach());
            for (BlockFace next : node.getAllConnected()) {
                if (!close.contains(next))
                    open.add(next);
            }
        }
        return out;
    }




    public void removeConnection(Direction attach){
        //TODO remove allConnections of a removed node
    }


    @Override
    public int handleResourceInput(Integer resource, BlockFace inputFace, boolean simulate) {
        return 0;
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
        return main;
    }

    @Override
    public void loadFromNBT(CompoundTag main, HolderLookup.Provider provider) {
        var nodes = main.getList("node_list", Tag.TAG_COMPOUND);
        for(var tag : nodes){
            CompoundTag nodeTag = (CompoundTag)tag;
            Direction att = Direction.from3DDataValue(nodeTag.getInt("attach"));
            var node = nodeMap.computeIfAbsent(att, this::createNode);
            node.loadFromNBT(nodeTag.getCompound("node"), provider);
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
