package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.node.CableNode;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.blockface.BlockFace;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.node.INode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public record StraightCableConnection(Direction buildDirection, INode.INodeIdentifyData<BlockFace> from,
                                      INode.INodeIdentifyData<BlockFace> to,
                                      long createTime) implements ICableConnection {
    public static final StraightCableConnectionBuilder BUILDER = new StraightCableConnectionBuilder();
    public static final String TYPE = "cable_straight";

    public StraightCableConnection(Direction direction, BlockFace from, BlockFace to, long createTime) {
        this(direction, from.toIdentifyData(), to.toIdentifyData(), createTime);
    }

    @Override
    public Direction relativeDirection(CableNode node) {
        assert node.getIdentifyData().equals(from) || node.getIdentifyData().equals(to);
        if(node.getIdentifyData().equals(from)) return buildDirection();
        else if(node.getIdentifyData().equals(to)) return buildDirection.getOpposite();
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
    public StraightCableConnectionBuilder getBuilder() {
        return BUILDER;
    }

    @Override
    public CompoundTag toNBT() {
        CompoundTag content = new CompoundTag();
        content.putInt("direction", buildDirection.get3DDataValue());
        content.put("from", from.data().toNBT());
        content.put("to", to.data().toNBT());
        content.putLong("create_time", createTime);

        CompoundTag outer = new CompoundTag();
        outer.putString("type", TYPE);
        outer.put("content", content);
        return outer;
    }

    public static class StraightCableConnectionBuilder extends ICableConnectionBuilder {

        private StraightCableConnectionBuilder() {};

        @Override
        public boolean canBuildConnection(CableNode from, CableNode to) {
            return calculateDirection(from, to) != null;
        }

        @Override
        public @NotNull StraightCableConnection build(CableNode from, CableNode to, long gameTime) {
            return new StraightCableConnection(calculateDirection(from, to), from.getIdentifyData(), to.getIdentifyData(), gameTime);
        }

        public Direction calculateDirection(CableNode from, CableNode to) {
            Direction direction;
            BlockPos fromPos = from.blockFace().pos();
            BlockPos toPos = to.blockFace().pos();
            direction = Direction.getNearest(toPos.getX() - fromPos.getX(), toPos.getY() - fromPos.getY(), toPos.getZ() - fromPos.getZ());
            if (!fromPos.relative(direction).equals(toPos)) return null;
            return direction;
        }


        @Override
        public StraightCableConnection fromNBT(CompoundTag tag) {
            CompoundTag content = tag.getCompound("content");
            Direction dir = Direction.from3DDataValue(content.getInt("direction"));
            BlockFace from = BlockFace.fromNBT(content.getCompound("from"));
            BlockFace to = BlockFace.fromNBT(content.getCompound("to"));
            long createTime = content.getLong("game_time");
            return new StraightCableConnection(dir, from, to, createTime);
        }

        //TODO translate compactable connection
    }

    public String getTypeName() {
        return TYPE;
    }

}
