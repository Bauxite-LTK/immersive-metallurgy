package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.blockface.BlockFace;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.node.CableNode;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.node.INode;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record BackCornerCableConnection(INode.INodeIdentifyData<BlockFace> from,
                                        INode.INodeIdentifyData<BlockFace> to,
                                        long createTime) implements ICableConnection {
    public static final BackCornerCableConnectionBuilder BUILDER = new BackCornerCableConnectionBuilder();
    public static final String TYPE = "cable_back_corner";

    public BackCornerCableConnection(BlockFace from, BlockFace to, long createTime) {
        this(from.toIdentifyData(), to.toIdentifyData(), createTime);
    }

    @Override
    public Direction relativeDirection(CableNode node) {
        assert node.getIdentifyData().equals(from) || node.getIdentifyData().equals(to);
        if(node.getIdentifyData().equals(from)) return to.data().attach().getOpposite();
        else if(node.getIdentifyData().equals(to)) return from.data().attach().getOpposite();
        return null;
    }

    @Override
    public INode.INodeIdentifyData<BlockFace> other(CableNode node) {
        assert node.getIdentifyData().equals(from) || node.getIdentifyData().equals(to);
        if(node.getIdentifyData().equals(from)) return to;
        else if(node.getIdentifyData().equals(to)) return from;
        return null;
    }

    @Override
    public BackCornerCableConnectionBuilder getBuilder() {
        return BUILDER;
    }

    @Override
    public CompoundTag toNBT() {
        CompoundTag content = new CompoundTag();
        content.put("from", from.data().toNBT());
        content.put("to", to.data().toNBT());
        content.putLong("game_time", createTime);

        CompoundTag outer = new CompoundTag();
        outer.putString("type", TYPE);
        outer.put("content", content);
        return outer;
    }

    public static class BackCornerCableConnectionBuilder extends ICableConnectionBuilder {

        private BackCornerCableConnectionBuilder() {};

        @Override
        public boolean canBuildConnection(CableNode from, CableNode to) {
            boolean sameAttachBlock = Objects.equals(
                    from.blockFace().pos().relative(from.blockFace().attach()),
                    to.blockFace().pos().relative(to.blockFace().attach()));
            boolean notOpposite = !Objects.equals(
                    from.blockFace().attach(),
                    to.blockFace().attach().getOpposite());
            return sameAttachBlock && notOpposite;
        }

        @Override
        public @NotNull BackCornerCableConnection build(CableNode from, CableNode to, long gameTime) {
            return new BackCornerCableConnection(from.getIdentifyData(), to.getIdentifyData(), gameTime);
        }



        @Override
        public BackCornerCableConnection fromNBT(CompoundTag tag) {
            CompoundTag content = tag.getCompound("content");
            BlockFace from = BlockFace.fromNBT(content.getCompound("from"));
            BlockFace to = BlockFace.fromNBT(content.getCompound("to"));
            long createTime = content.getLong("game_time");
            return new BackCornerCableConnection(from, to, createTime);
        }

        //TODO move compactable connection
    }

    public String getTypeName() {
        return TYPE;
    }

}
