package net.bauxite_ltk.immersive_metallurgy.block.multiblock.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Function;

public class AdvancedCokeOvenShapes implements Function<BlockPos, VoxelShape>{

    public static final Function<BlockPos, VoxelShape> SHAPE_GETTER = new AdvancedCokeOvenShapes();

    public AdvancedCokeOvenShapes()
    {
    }

    @Override
    public VoxelShape apply(BlockPos posInMultiBlock) {
        return Shapes.block();
    }
}
