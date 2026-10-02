package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.node;

import com.google.common.base.Preconditions;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.blockface.BlockFace;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection.ICableConnection;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection.INodeConnection;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection.controller.CableConnectionController;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection.controller.SidePermissionController;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CableNode implements INode {
    //public static final CableNodeBuilder BUILDER = new CableNodeBuilder();
    protected BlockFace blockFace;
    protected CableConnectionController connectionController;
    protected SidePermissionController sidePermissionController;




    protected CableNode(BlockFace blockFace){
        this(blockFace, new CableConnectionController(), new SidePermissionController(blockFace));
    }

    protected CableNode(BlockFace blockFace, CableConnectionController connections, SidePermissionController permissions){
        this.blockFace = blockFace;
        this.connectionController = connections;
        this.sidePermissionController = permissions;
    }

    @Override
    public INodeIdentifyData<BlockFace> getIdentifyData() {
        return blockFace.toIdentifyData();
    }

    @Override
    public void confirmConnect(INodeConnection<?> connection) throws IllegalArgumentException{
        Preconditions.checkArgument(connection instanceof ICableConnection);

        connectionController.add((ICableConnection)connection);
    }

    @Override
    public void confirmDisconnect(INodeConnection<?> connection) {
        Preconditions.checkArgument(connection instanceof ICableConnection);
        connectionController.remove((ICableConnection)connection);
    }


    @Override
    public boolean canConfirmConnection(INodeConnection<?> connection) {
        Preconditions.checkArgument(connection instanceof ICableConnection);
        ICableConnection cableConnection = (ICableConnection)connection;

        final Direction dir = cableConnection.relativeDirection(this);
        if(!isPermittedDirection(dir)) return false;
        for(ICableConnection c : connectionController.list()){
            if(c.relativeDirection(this).equals(dir)) return false;
        }
        return true;
    }

    public boolean isPermittedDirection(Direction d){
        return sidePermissionController.isPermitted(d);
    }


    public List<BlockFace> getAllConnected(){
        List<BlockFace> list = new ArrayList<>();
        for(ICableConnection c : connectionController.list()){
            if(c.from().equals(this.getIdentifyData()))
                list.add(c.to().data());
            else if(c.to().equals(this.getIdentifyData())){
                list.add(c.from().data());
            }
        }
        return list;
    }

    public List<ICableConnection> getConnectionsTo(Direction direction){
        List<ICableConnection> connections = new ArrayList<>();
        for(var c : connectionController.list()){
            if(c.relativeDirection(this).equals(direction))
                connections.add(c);
        }
        return connections;
    }

    @Override
    public int getConnectionCount() {
        return connectionController.connectionCount();
    }

    public boolean hasConnection(ICableConnection connection) {
        return connectionController.list().contains(connection);
    }

    public Set<Direction> getConnectionDirSet(){
        Set<Direction> dirs = new HashSet<>();
        for(var c : connectionController.list()){
            dirs.add(c.relativeDirection(this));
        }
        return dirs;
    }

    public Set<Direction> getForbiddenDirSet(){
        Set<Direction> dirs = new HashSet<>();
        for(Direction con : Direction.values()){
            if(!isPermittedDirection(con)) dirs.add(con);
        }
        return dirs;
    }


    public BlockFace blockFace() {
        return blockFace;
    }

    @Override
    public CompoundTag toNBT(HolderLookup.Provider provider) {
        CompoundTag connTag = new CompoundTag();

        connTag.put("identify_data", blockFace.toIdentifyData().data().toNBT());
        connTag.put("connection_list", connectionController.toNBT());
        connTag.put("permission_list", sidePermissionController.toNBT());
        return connTag;
    }

    @Override
    public void loadFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        this.blockFace = BlockFace.fromNBT(tag.getCompound("identify_data"));
        this.connectionController.loadFromNBT(tag.getCompound("connection_list"));
        this.sidePermissionController.loadFromNBT(tag.getCompound("permission_list"));
    }

    @Override
    public void loadFromNBT(Direction previousNorthNowPointTo, Vec3i translation, CompoundTag tag, HolderLookup.Provider provider) {
        BlockFace origin = BlockFace.fromNBT(tag.getCompound("identify_data"));
        Direction d = Direction.NORTH;
        Direction att = origin.attach();
        while(!d.equals(previousNorthNowPointTo)){
            d = d.getClockWise();
            att = att.getClockWise();
        }
        this.blockFace = new BlockFace(origin.pos().offset(translation), att);
        //TODO translate compactable connection controller
        this.connectionController.loadFromNBT(tag.getCompound("connection_list"));
        this.sidePermissionController.loadFromNBT(tag.getCompound("permission_list"));
    }

//    public static class CableNodeBuilder implements INodeBuilder<CableNode>{
//
//        public CableNode buildNormal(BlockFace bf){
//            return new CableNode(bf);
//        }
//
//        @Override
//        public CableNode buildFromNBT(CompoundTag tag) {
//            BlockFace blockFace = BlockFace.fromNBT(tag.getCompound("identify_data"));
//            CableConnectionController allConnections = CableConnectionController.BUILDER.buildFromNBT(tag.getCompound("connection_list"));
//            SidePermissionController permissions = SidePermissionController.createFromNBT(tag.getCompound("permission_list"));
//            return new CableNode(blockFace, allConnections, permissions);
//        }
//    }


}
