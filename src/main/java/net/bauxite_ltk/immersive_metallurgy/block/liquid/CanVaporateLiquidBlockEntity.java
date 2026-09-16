package net.bauxite_ltk.immersive_metallurgy.block.liquid;

import net.bauxite_ltk.immersive_metallurgy.block.IMBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;


public class CanVaporateLiquidBlockEntity extends BlockEntity{

    int vaporateTicks = -1;
    int tickRemain = -1;

    public CanVaporateLiquidBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    public static CanVaporateLiquidBlockEntity forHotAir(BlockPos pos, BlockState blockState){
        return new CanVaporateLiquidBlockEntity(IMBlockEntities.HOT_AIR.get(), pos, blockState);
    }

    public static CanVaporateLiquidBlockEntity forWaterGas(BlockPos pos, BlockState blockState){
        return new CanVaporateLiquidBlockEntity(IMBlockEntities.WATER_GAS.get(), pos, blockState);
    }

    public static CanVaporateLiquidBlockEntity forBlastFurnaceGas(BlockPos pos, BlockState blockState){
        return new CanVaporateLiquidBlockEntity(IMBlockEntities.BLAST_FURNACE_GAS.get(), pos, blockState);
    }

    public static CanVaporateLiquidBlockEntity forCokeOvenGas(BlockPos pos, BlockState blockState){
        return new CanVaporateLiquidBlockEntity(IMBlockEntities.COKE_OVEN_GAS.get(), pos, blockState);
    }


    public void setVaporateProperties(int vaporateTicks){
        this.vaporateTicks = vaporateTicks;
        this.tickRemain = vaporateTicks;
        syncToClient();
    }

    public void serverTick(){
        if(tickRemain > 0){
            tickRemain --;
            return;
        }
        if (level != null) {
            if(level.getFluidState(getBlockPos()).isSource()){
                BlockState newState = Blocks.AIR.defaultBlockState();
                level.setBlockAndUpdate(worldPosition, newState);
                level.playSound(null,getBlockPos(), SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1,1);
            }
            else{
                level.setBlockAndUpdate(getBlockPos(), Blocks.AIR.defaultBlockState());
            }
        }
    }

    public void clientTick(){
//        if(tickRemain % 4 == 0){
//            Minecraft minecraft = Minecraft.getInstance();
//            minecraft.levelRenderer.setBlocksDirty(
//                    getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(),
//                    getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()
//            );
//        }
    }

    public void readCustomNBT(CompoundTag nbt, boolean descPacket, HolderLookup.Provider provider){
        this.tickRemain = nbt.getInt("ticksRemain");
        this.setChanged();
    }

    public void writeCustomNBT(CompoundTag nbt, boolean descPacket, HolderLookup.Provider provider){
        nbt.putInt("ticksRemain", tickRemain);
    }


    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this, (be, access) -> {
            CompoundTag nbtTagCompound = new CompoundTag();
            this.writeCustomNBT(nbtTagCompound, true, access);
            return nbtTagCompound;
        });
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider provider)
    {
        this.readCustomNBT(pkt.getTag(), true, provider);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider)
    {
        this.readCustomNBT(tag, true, provider);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider provider)
    {
        CompoundTag nbt = super.getUpdateTag(provider);
        writeCustomNBT(nbt, true, provider);
        return nbt;
    }


    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        writeCustomNBT(tag, false, registries);
    }


    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        readCustomNBT(tag, false, registries);
    }


    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            setChanged();
            BlockState state = getBlockState();
            //ImmersiveMetallurgy.LOGGER.info("state:{}", state);
            level.sendBlockUpdated(getBlockPos(), state, state, 3);
        }
    }

}
