package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.blockface.BlockFace;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.node.CableNode;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.node.INode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record FrontCornerCableConnection(INode.INodeIdentifyData<BlockFace> from,
                                         INode.INodeIdentifyData<BlockFace> to,
                                         long createTime) implements ICableConnection {
    public static final FrontCornerCableConnectionBuilder BUILDER = new FrontCornerCableConnectionBuilder();
    public static final String TYPE = "cable_front_corner";

    public FrontCornerCableConnection(BlockFace from, BlockFace to, long createTime) {
        this(from.toIdentifyData(), to.toIdentifyData(), createTime);
    }

    @Override
    public Direction relativeDirection(CableNode node) {
        assert node.getIdentifyData().equals(from) || node.getIdentifyData().equals(to);
        if(node.getIdentifyData().equals(from)) return to.data().attach();
        else if(node.getIdentifyData().equals(to)) return from.data().attach();
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
    public FrontCornerCableConnectionBuilder getBuilder() {
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

    public static class FrontCornerCableConnectionBuilder extends ICableConnectionBuilder {

        private FrontCornerCableConnectionBuilder() {};

        @Override
        public boolean canBuildConnection(CableNode from, CableNode to) {
            boolean sameBlock = Objects.equals(from.blockFace().pos(), to.blockFace().pos());
            boolean notOpposite = !Objects.equals(from.blockFace().attach(), to.blockFace().attach().getOpposite());
            boolean notSame = !Objects.equals(from.blockFace().attach(), to.blockFace().attach());
            return sameBlock && notSame && notOpposite;
        }

        @Override
        public @NotNull FrontCornerCableConnection build(CableNode from, CableNode to, long gameTime) {
            return new FrontCornerCableConnection(from.getIdentifyData(), to.getIdentifyData(), gameTime);
        }



        @Override
        public FrontCornerCableConnection fromNBT(CompoundTag tag) {
            CompoundTag content = tag.getCompound("content");
            BlockFace from = BlockFace.fromNBT(content.getCompound("from"));
            BlockFace to = BlockFace.fromNBT(content.getCompound("to"));
            long createTime = content.getLong("game_time");
            return new FrontCornerCableConnection(from, to, createTime);
        }

        //TODO translate compactable connection
    }

    public String getTypeName() {
        return TYPE;
    }

}
