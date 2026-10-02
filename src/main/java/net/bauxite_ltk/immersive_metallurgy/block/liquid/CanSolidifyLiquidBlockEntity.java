package net.bauxite_ltk.immersive_metallurgy.block.liquid;

import net.bauxite_ltk.immersive_metallurgy.ImmersiveMetallurgy;
import net.bauxite_ltk.immersive_metallurgy.block.IMBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;


public class CanSolidifyLiquidBlockEntity extends BlockEntity{

    int solidifyTicks = -1;
    int tickRemain = -1;
    Supplier<Block> solid = null;
    int baseColor;

    public CanSolidifyLiquidBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    public static CanSolidifyLiquidBlockEntity forPigIron(BlockPos pos, BlockState blockState){
        return new CanSolidifyLiquidBlockEntity(IMBlockEntities.MOLTEN_PIG_IRON.get(), pos, blockState);
    }

    public static CanSolidifyLiquidBlockEntity forGold(BlockPos pos, BlockState blockState){
        return new CanSolidifyLiquidBlockEntity(IMBlockEntities.MOLTEN_GOLD.get(), pos, blockState);
    }

    public static CanSolidifyLiquidBlockEntity forCopper(BlockPos pos, BlockState blockState){
        return new CanSolidifyLiquidBlockEntity(IMBlockEntities.MOLTEN_COPPER.get(), pos, blockState);
    }

    public static CanSolidifyLiquidBlockEntity forSilver(BlockPos pos, BlockState blockState){
        return new CanSolidifyLiquidBlockEntity(IMBlockEntities.MOLTEN_SILVER.get(), pos, blockState);
    }

    public static CanSolidifyLiquidBlockEntity forLead(BlockPos pos, BlockState blockState){
        return new CanSolidifyLiquidBlockEntity(IMBlockEntities.MOLTEN_LEAD.get(), pos, blockState);
    }

    public static CanSolidifyLiquidBlockEntity forNickel(BlockPos pos, BlockState blockState){
        return new CanSolidifyLiquidBlockEntity(IMBlockEntities.MOLTEN_NICKEL.get(), pos, blockState);
    }

    public static CanSolidifyLiquidBlockEntity forUranium(BlockPos pos, BlockState blockState){
        return new CanSolidifyLiquidBlockEntity(IMBlockEntities.MOLTEN_URANIUM.get(), pos, blockState);
    }

    public static CanSolidifyLiquidBlockEntity forAluminum(BlockPos pos, BlockState blockState){
        return new CanSolidifyLiquidBlockEntity(IMBlockEntities.MOLTEN_ALUMINUM.get(), pos, blockState);
    }

    public void setSolidProperties(Supplier<Block> solid, int solidifyTicks, int baseColor){
        this.solid = solid;
        this.solidifyTicks = solidifyTicks;
        this.tickRemain = solidifyTicks;
        this.baseColor = baseColor;
        syncToClient();
    }



    public void serverTick(){
        if(tickRemain > 0){
            tickRemain --;
            if(tickRemain % 4 == 0){
                //ImmersiveMetallurgy.LOGGER.info("tick ticksRemain:{}", tickRemain);
                syncToClient();
            }
            return;
        }
        if (level != null) {
            if(level.getFluidState(getBlockPos()).isSource()){
                BlockState newState = solid.get().defaultBlockState();
                level.setBlockAndUpdate(worldPosition, newState);
                level.playSound(null,getBlockPos(), SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1,1);
            }
            else{
                level.setBlockAndUpdate(getBlockPos(), Blocks.AIR.defaultBlockState());
            }
        }
    }

    public void clientTick(){
        if(tickRemain % 4 == 0){
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.levelRenderer.setBlocksDirty(
                    getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(),
                    getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()
            );
        }
    }

    public void readCustomNBT(CompoundTag nbt, boolean descPacket, HolderLookup.Provider provider){
        this.tickRemain = nbt.getInt("ticksRemain");
        //ImmersiveMetallurgy.LOGGER.info("load ticksRemain:{}", tickRemain);
        this.solid = () -> BuiltInRegistries.BLOCK.get(ResourceLocation.parse(nbt.getString("solidBlock")));
        //ImmersiveMetallurgy.LOGGER.info("load solid:{}", solid);

        this.setChanged();
    }

    public void writeCustomNBT(CompoundTag nbt, boolean descPacket, HolderLookup.Provider provider){
        nbt.putInt("ticksRemain", tickRemain);
        nbt.putString("solidBlock", BuiltInRegistries.BLOCK.getKey(solid.get()).toString());
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

    public static int getColorFromTickRemain(BlockAndTintGetter blockAndTintGetter, BlockPos pos){

        BlockEntity be = blockAndTintGetter.getBlockEntity(pos);
        if(be instanceof CanSolidifyLiquidBlockEntity solidifyLiquidBE){
            int tickRemain = solidifyLiquidBE.tickRemain;
            int tickTotal = solidifyLiquidBE.solidifyTicks;
            double percent = 0.5 + 0.5*((double)tickRemain/tickTotal);
            int green = (int)(0xFF * percent);
            int alpha = (int)(0xFF * (percent*2-1));
            int tempColor = (alpha << 24) | (0xFF << 16) | (green << 8) | 0x80;
            //int multiplied = multiplyColor(solidifyLiquidBE.baseColor, tempColor);
            int mixed = mixColor(solidifyLiquidBE.baseColor, tempColor);
            ImmersiveMetallurgy.LOGGER.info("getColorFromTickRemain: {}", mixed);
            return mixed;
        }

        return 0;
    }

    public static int mixColor(int base, int brush){
        float r1 = ((base >> 16) & 0xFF) / 255f;
        float g1 = ((base >> 8)  & 0xFF) / 255f;
        float b1 = (base & 0xFF) / 255f;
        float a1 = ((base >> 24) & 0xFF) / 255f;

        float r2 = ((brush >> 16) & 0xFF) / 255f;
        float g2 = ((brush >> 8)  & 0xFF) / 255f;
        float b2 = (brush & 0xFF) / 255f;
        float a2 = ((brush >> 24) & 0xFF) / 255f;

        float a = a2 + (1 - a2) * a1;
        //if (a <= 0.0001f) return 0;

        float r = (a2 * r2 + (1-a2) * r1);
        float g = (a2 * g2 + (1-a2) * g1);
        float b = (a2 * b2 + (1-a2) * b1);

        int ia = Math.min(255, Math.max(0, (int)(a * 255)));
        int ir = Math.min(255, Math.max(0, (int)(r * 255)));
        int ig = Math.min(255, Math.max(0, (int)(g * 255)));
        int ib = Math.min(255, Math.max(0, (int)(b * 255)));

        return (ia << 24) | (ir << 16) | (ig << 8) | ib;
    }

    public static int multiplyColor(int base, int brush){
        float r1 = (float) ((base & 0x00FF0000) >> 16) / 0xFF;
        float g1 = (float) ((base & 0x0000FF00) >> 8) / 0xFF;
        float b1 = (float) (base & 0x000000FF) / 0xFF;

        float r2 = (float) ((brush & 0x00FF0000) >> 16) / 0xFF;
        float g2 = (float) ((brush & 0x0000FF00) >> 8) / 0xFF;
        float b2 = (float) (brush & 0x000000FF) / 0xFF;
        float a2 = (float) ((brush & 0xFF000000) >> 24) / 0xFF;

        int mixed = 0xFF000000 + ((int)(0xFF * r1 * r2) << 16) + ((int)(0xFF * g1 * g2) << 8) + ((int)(0xFF * b1 * b2));

        int result = (int)(a2 * mixed + (1-a2) * base);

        return result;
    }



}
