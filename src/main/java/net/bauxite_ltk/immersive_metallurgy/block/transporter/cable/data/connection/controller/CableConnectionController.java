package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection.controller;

import com.google.common.base.Preconditions;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.blockface.BlockFace;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection.ICableConnection;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection.StraightCableConnection;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.*;

public class CableConnectionController implements IConnectionController<ICableConnection>{
    public static final CableConnectionControllerBuilder BUILDER = new CableConnectionControllerBuilder();
    List<ICableConnection> connectionList = new ArrayList<>();



    @Override
    public void add(ICableConnection connection) {
        connectionList.add(connection);
    }

    @Override
    public void remove(ICableConnection connection) {
        connectionList.remove(connection);
    }

    @Override
    public void clear() {
        connectionList.clear();
    }

    public List<ICableConnection> list(){
        return connectionList;
    }

    public int connectionCount(){
        return connectionList.size();
    }

    @Override
    public CompoundTag toNBT() {
        CompoundTag controllerTag = new CompoundTag();
        ListTag connectionListTag = new ListTag();
        for(ICableConnection connection : connectionList){
            connectionListTag.add(connection.toNBT());
        }
        controllerTag.put("connection_list", connectionListTag);
        return controllerTag;
    }

    @Override
    public void loadFromNBT(CompoundTag compoundTag) {
        this.clear();
        ListTag connectionListTag = compoundTag.getList("connection_list", Tag.TAG_COMPOUND);
        for(int i = 0; i < connectionListTag.size(); i++){
            CompoundTag connectionTag = connectionListTag.getCompound(i);
            String type = connectionTag.getString("type");

            // recreate allConnections according to its type.
            ICableConnection connection = null;
            if (type.equals(StraightCableConnection.TYPE)) {
                connection = StraightCableConnection.BUILDER.fromNBT(connectionTag);
            }

            Preconditions.checkNotNull(connection);
            connectionList.add(connection);
        }
    }

    public static class CableConnectionControllerBuilder implements IConnectionControllerBuilder<ICableConnection>{

        @Override
        public CableConnectionController buildFromNBT(CompoundTag compoundTag) {
            CableConnectionController controller = new CableConnectionController();
            controller.loadFromNBT(compoundTag);
            return controller;
        }
    }


}
