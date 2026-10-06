package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.tool.IElectricEquipment;
import blusunrize.immersiveengineering.client.fx.IEParticleType;
import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import net.bauxite_ltk.immersive_metallurgy.block.IMBlockEntities;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.blockface.BlockFace;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.global.GlobalRFCableConnectionData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.*;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class RFCableBlock extends IEEntityBlock<RFCableBlockEntity> {

    public static final EnumProperty<Direction> DEFAULT_FACING_PROP = IEProperties.FACING_ALL;
    public final int transferLimit;

    public RFCableBlock(Supplier<BlockEntityType<RFCableBlockEntity>> tileType, Properties blockProps, int transferLimit) {
        super(tileType, blockProps.sound(SoundType.COPPER).strength(0.5f,2));
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
    static final int ELECTROCUTE_INTERNAL_TICKS = 5;
    int electrocuteTicks = ELECTROCUTE_INTERNAL_TICKS;
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if(level instanceof ClientLevel) return;
        if(electrocuteTicks > 0){
            electrocuteTicks--;
            return;
        }
        int extract = GlobalRFCableConnectionData.tryElectrocute(level, pos, transferLimit);
        if(extract > 0 && entity instanceof LivingEntity){
            float damage = extract/256f;
            float voltageLevel = extract/1024f;
            float soundVolume = extract > 2048? 1.5f: 1.5f*((extract-512)/2048f);
            if(level instanceof ServerLevel serverLevel){
                if(extract > 2048){
                    serverLevel.sendParticles(
                            ParticleTypes.FLASH,
                            entity.getX(),entity.getY(),entity.getZ(),
                            2,
                            0.3, 0.3, 0.3,
                            0.0);
                }
                if(extract > 1024){
                    entity.hurt(IEDamageSources.causeWireDamage(level, damage, new IElectricEquipment.ElectricSource(voltageLevel)), damage);

                    level.playSound(null, pos, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.BLOCKS, soundVolume, 1);
                }

            }
        }
        electrocuteTicks = ELECTROCUTE_INTERNAL_TICKS;
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

    @Override
    protected @NotNull List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, params));
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if(be instanceof RFCableBlockEntity cableBlockEntity){
            int nodeCount = cableBlockEntity.getRFBlockManager().getNodeCount();
            drops.add(new ItemStack(cableBlockEntity.cableItem, nodeCount-1));
        }
        return drops;
    }
}
