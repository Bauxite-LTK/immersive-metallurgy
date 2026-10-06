package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.blockface.BlockFace;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.node.CableNode;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.node.DummyCableNode;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.node.INode;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record TerminalCableConnection(INode.INodeIdentifyData<BlockFace> from,
                                      INode.INodeIdentifyData<BlockFace> to,
                                      boolean isDummy,
                                      long createTime) implements ICableConnection {
    public static final FrontCornerCableConnectionBuilder BUILDER = new FrontCornerCableConnectionBuilder();
    public static final String TYPE = "cable_terminal";

    public TerminalCableConnection(BlockFace from, BlockFace to, boolean isDummy, long createTime) {
        this(from.toIdentifyData(), to.toIdentifyData(), isDummy, createTime);
    }

    @Override
    public Direction relativeDirection(CableNode node) {
        assert node.getIdentifyData().equals(from) || node.getIdentifyData().equals(to);
        if(node.getIdentifyData().equals(from)) return from.data().attach();
        else if(node.getIdentifyData().equals(to)) return to.data().attach();
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
        content.putBoolean("is_dummy", isDummy);
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

            boolean attachOpposite = Objects.equals(from.blockFace().attach(), to.blockFace().attach().getOpposite());
            boolean posRelative = from.blockFace().pos().relative(from.blockFace().attach()).equals(to.blockFace().pos());
            return attachOpposite && posRelative;
        }

        @Override
        public @NotNull TerminalCableConnection build(CableNode from, CableNode to, long gameTime) {
            boolean hasDummy = from instanceof DummyCableNode || to instanceof DummyCableNode;
            return new TerminalCableConnection(from.getIdentifyData(), to.getIdentifyData(), hasDummy, gameTime);
        }



        @Override
        public TerminalCableConnection fromNBT(CompoundTag tag) {
            CompoundTag content = tag.getCompound("content");
            BlockFace from = BlockFace.fromNBT(content.getCompound("from"));
            BlockFace to = BlockFace.fromNBT(content.getCompound("to"));
            boolean isDummy = tag.getBoolean("is_dummy");
            long createTime = content.getLong("game_time");
            return new TerminalCableConnection(from, to, isDummy, createTime);
        }

        //TODO translate compactable connection
    }

    public String getTypeName() {
        return TYPE;
    }

}
