package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.blockface;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.node.INode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;


public record BlockFace(BlockPos pos, Direction attach){
    public BlockFace opposite(){
        return new BlockFace(pos.relative(attach), attach.getOpposite());
    }

    public CompoundTag toNBT(){
        CompoundTag tag = new CompoundTag();
        tag.putInt("x",pos.getX());
        tag.putInt("y",pos.getY());
        tag.putInt("z",pos.getZ());
        tag.putInt("dir", attach.get3DDataValue());
        return tag;
    }

    public INode.INodeIdentifyData<BlockFace> toIdentifyData(){
        return new INode.INodeIdentifyData<>(this, BlockFace::toNBT, BlockFace::fromNBT);
    }


    public static BlockFace fromNBT(CompoundTag tag){
        BlockPos pos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        Direction direction = Direction.from3DDataValue(tag.getInt("dir"));
        return new BlockFace(pos,direction);
    }
}
