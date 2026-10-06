package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.tool.IElectricEquipment;
import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import net.bauxite_ltk.immersive_metallurgy.block.IMBlockEntities;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.blockface.BlockFace;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.global.GlobalRFCableConnectionData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
import org.joml.Vector3f;

import java.util.Set;
import java.util.function.Supplier;

public class RFCableBlock extends IEEntityBlock<RFCableBlockEntity> {

    public static final EnumProperty<Direction> DEFAULT_FACING_PROP = IEProperties.FACING_ALL;
    public final int transferLimit;

    public RFCableBlock(Supplier<BlockEntityType<RFCableBlockEntity>> tileType, Properties blockProps, int transferLimit) {
        super(tileType, blockProps.sound(SoundType.COPPER));
        this.transferLimit = transferLimit;
    }

    public static RFCableBlock forHv(Properties blockProps){
        return new RFCableBlock(IMBlockEntities.ELECTRIC_CABLE_HV, blockProps, 65536);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(DEFAULT_FACING_PROP, BlockStateProperties.WATERLOGGED);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
    {
        BlockEntity te = world.getBlockEntity(pos);
        if(te instanceof RFCableBlockEntity cable)
        {
            Set<Direction> allAttachments = cable.rfBlockManager.getCableConnectionKey().nodes();
            super.neighborChanged(state, world, pos, block, fromPos, isMoving);
            boolean remove = true;
            for(Direction d : allAttachments){
                if(!world.isEmptyBlock(pos.relative(d))) remove = false;
                else {
                    cable.rfBlockManager.removeNode(d);
                    cable.rfBlockManager.updateConnectedFaces();
                    cable.markSyncToClient();
                    popResource(world, pos, new ItemStack(this));
                }
            }
            if(remove){
                Level level = cable.getLevelNonnull();
                level.removeBlock(pos, false);
                level.sendBlockUpdated(pos,state,state,3);
                level.updateNeighborsAt(pos,block);
            }
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        int extract = GlobalRFCableConnectionData.tryElectrocute(level, pos, transferLimit);
        if(extract > 0 && entity instanceof LivingEntity){
            entity.hurt(IEDamageSources.causeWireDamage(level, extract/256f, new IElectricEquipment.ElectricSource(2)), extract/256f);
            level.playSound(null, pos, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.BLOCKS, 1.5f, 1);
            if(level instanceof ServerLevel serverLevel){
                serverLevel.sendParticles(
                        new DustParticleOptions(new Vector3f(1,1,0), 1.0F),   // ★ ARGB 颜色 + 大小
                        entity.getX(),entity.getY(),entity.getZ(),
                        10,
                        0.3, 0.3, 0.3,
                        0.0);
            }
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {

        if(!state.is(newState.getBlock()) && world instanceof ServerLevel){
            for(Direction att : Direction.values()){
                GlobalRFCableConnectionData.removeVertex(world, new BlockFace(pos, att));
            }
        }
        super.onRemove(state, world, pos, newState, isMoving);

    }
}
