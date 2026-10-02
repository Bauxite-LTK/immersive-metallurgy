package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import net.bauxite_ltk.immersive_metallurgy.block.IMBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class RFCableBlock extends IEEntityBlock<RFCableBlockEntity> {

    public static final EnumProperty<Direction> DEFAULT_FACING_PROP = IEProperties.FACING_ALL;

    public RFCableBlock(Supplier<BlockEntityType<RFCableBlockEntity>> tileType, Properties blockProps) {
        super(tileType, blockProps.sound(SoundType.NETHERITE_BLOCK));
    }

    public static RFCableBlock forHv(Properties blockProps){
        return new RFCableBlock(IMBlockEntities.ELECTRIC_CABLE_HV, blockProps);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(DEFAULT_FACING_PROP, BlockStateProperties.WATERLOGGED);
    }
}
