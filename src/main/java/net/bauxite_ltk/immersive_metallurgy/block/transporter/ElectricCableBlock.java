package net.bauxite_ltk.immersive_metallurgy.block.transporter;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import net.bauxite_ltk.immersive_metallurgy.block.IMBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.List;
import java.util.function.Supplier;

public class ElectricCableBlock extends IEEntityBlock<ElectricCableBlockEntity> {

    public static final EnumProperty<Direction> DEFAULT_FACING_PROP = IEProperties.FACING_ALL;

    public ElectricCableBlock(Supplier<BlockEntityType<ElectricCableBlockEntity>> tileType, Properties blockProps) {
        super(tileType, blockProps
                .sound(SoundType.NETHERITE_BLOCK)
        );
    }

//    public static ElectricCableBlock forLv(Properties blockProps){
//        return new ElectricCableBlock(IMBlockEntities.ELECTRIC_CABLE_LV, blockProps);
//    }
//
//    public static ElectricCableBlock forMv(Properties blockProps){
//        return new ElectricCableBlock(IMBlockEntities.ELECTRIC_CABLE_MV, blockProps);
//    }


    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
    {
        BlockEntity te = world.getBlockEntity(pos);
        if(te instanceof ElectricCableBlockEntity cable)
        {
            List<Direction> allAttachments = cable.allAttachments();
            super.neighborChanged(state, world, pos, block, fromPos, isMoving);
            boolean remove = true;
            for(Direction d : allAttachments){
                if(!world.isEmptyBlock(pos.relative(d))) remove = false;
                else {
                    cable.deactivateFace(d);
                    cable.invalidateCapabilities();
                    cable.markContainingBlockForUpdate(null);
                }
            }
            if(remove){
                popResource(world, pos, new ItemStack(this));
                Level level = cable.getLevelNonnull();
                level.removeBlock(pos, false);
                level.sendBlockUpdated(pos,state,state,3);
                level.updateNeighborsAt(pos,block);
            }
        }
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(DEFAULT_FACING_PROP, BlockStateProperties.WATERLOGGED);
    }

}
