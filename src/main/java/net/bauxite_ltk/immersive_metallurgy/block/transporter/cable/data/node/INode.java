package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.node;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection.INodeConnection;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;

import java.util.function.Function;

public interface INode{

    INodeIdentifyData<?> getIdentifyData();

    default boolean isSameForIdentify(INode node){
        return isSameForIdentify(node.getIdentifyData());
    }

    default boolean isSameForIdentify(INodeIdentifyData<?> identifyData){
        return this.getIdentifyData().equals(identifyData);
    }

    void confirmConnect(INodeConnection<?> connection);

    void confirmDisconnect(INodeConnection<?> connection);

    boolean canConfirmConnection(INodeConnection<?> connection);

    int getConnectionCount();

    CompoundTag toNBT(HolderLookup.Provider provider);

    void loadFromNBT(CompoundTag tag, HolderLookup.Provider provider);

    void loadFromNBT(Direction rotation, Vec3i translation, CompoundTag tag, HolderLookup.Provider provider);


//    interface INodeBuilder<N extends INode>{
//        N buildFromNBT(CompoundTag tag);
//    }

    // Actually the coder and decoder is kind of for reminding.
    // when we know the exact data type, we better directly use the specific methods
    record INodeIdentifyData<D>(D data, Function<D, CompoundTag> coder, Function<CompoundTag, D> decoder){
        @Override public boolean equals(Object o) {
            return o instanceof INodeIdentifyData<?> x && data.equals(x.data); // 只比 data
        }
        @Override public int hashCode() { return data.hashCode(); }
    };

}
