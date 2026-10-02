package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.node.INode;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

public interface INodeConnection<Node extends INode> {

    // Every INodeConnection only have one builder for indicating the connection's type.
    // This is useful when you want multiple nodes to connect others,
    // Obviously you don't want all of them try to build connection independently at the same time.
    // So in that node's logic, it should be notifiable, and know the connection is building by a representative node.
    // And for notifying, we need to convey the specific type of connection now building.
    // That's the time we use getBuilder()
    INodeConnectionBuilder<Node> getBuilder();

    long createTime();

    INode.INodeIdentifyData<?> from();

    INode.INodeIdentifyData<?> to();

    CompoundTag toNBT();

    interface INodeConnectionBuilder<Node extends INode>{
        boolean canBuildConnection(Node from, Node to);
        @Nonnull
        INodeConnection<Node> build(Node from, Node to, long createTime);
        INodeConnection<Node> fromNBT(CompoundTag content);
    }
}
