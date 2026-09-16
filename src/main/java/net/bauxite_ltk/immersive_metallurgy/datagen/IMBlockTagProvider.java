package net.bauxite_ltk.immersive_metallurgy.datagen;

import blusunrize.immersiveengineering.api.IETags;
import net.bauxite_ltk.immersive_metallurgy.block.IMBlocks;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.IMMultiblocks;
import net.bauxite_ltk.immersive_metallurgy.fluid.IMFluids;
import net.bauxite_ltk.immersive_metallurgy.tags.IMTags;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class IMBlockTagProvider extends BlockTagsProvider {
    public IMBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, IMUtils.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(IMBlocks.ELECTRIC_CABLE_LV.get())
                .add(IMBlocks.ELECTRIC_CABLE_MV.get())
                .add(IMBlocks.CASTING_CHANNEL.get());

        this.tag(BlockTags.LOGS_THAT_BURN)
                .add(IMBlocks.MASON_PINE_LOG.get())
                .add(IMBlocks.MASON_PINE_LOG_LIVE.get())
                .add(IMBlocks.MASON_PINE_LOG_SAPPY.get())
                .add(IMBlocks.MASON_PINE_WOOD.get())
                .add(IMBlocks.STRIPPED_MASON_PINE_LOG.get())
                .add(IMBlocks.STRIPPED_MASON_PINE_WOOD.get());

        this.tag(BlockTags.LEAVES).add(IMBlocks.MASON_PINE_LEAVES.get());

        this.tag(BlockTags.SAPLINGS).add(IMBlocks.MASON_PINE_SAPLING.get());

        this.tag(BlockTags.PLANKS).add(IMBlocks.MASON_PINE_PLANKS.get());

        this.tag(BlockTags.LOGS)
                .add(IMBlocks.MASON_PINE_LOG.get())
                .add(IMBlocks.MASON_PINE_LOG_LIVE.get())
                .add(IMBlocks.MASON_PINE_LOG_SAPPY.get())
                .add(IMBlocks.MASON_PINE_WOOD.get())
                .add(IMBlocks.STRIPPED_MASON_PINE_LOG.get())
                .add(IMBlocks.STRIPPED_MASON_PINE_WOOD.get());

        this.tag(BlockTags.WOODEN_FENCES)
                .add(IMBlocks.MASON_PINE_FENCE.get());
        this.tag(BlockTags.FENCES)
                .add(IMBlocks.MASON_PINE_FENCE.get());

        this.tag(BlockTags.FENCE_GATES)
                .add(IMBlocks.MASON_PINE_FENCE_GATE.get());
        this.tag(BlockTags.UNSTABLE_BOTTOM_CENTER)
                .add(IMBlocks.MASON_PINE_FENCE_GATE.get());

        this.tag(BlockTags.WOODEN_DOORS)
                .add(IMBlocks.MASON_PINE_DOOR.get());
        this.tag(BlockTags.DOORS)
                .add(IMBlocks.MASON_PINE_DOOR.get());

        this.tag(BlockTags.TRAPDOORS)
                .add(IMBlocks.MASON_PINE_TRAP_DOOR.get());
        this.tag(BlockTags.WOODEN_TRAPDOORS)
                .add(IMBlocks.MASON_PINE_TRAP_DOOR.get());

        this.tag(BlockTags.WOODEN_STAIRS)
                .add(IMBlocks.MASON_PINE_STAIRS.get());
        this.tag(BlockTags.STAIRS)
                .add(IMBlocks.MASON_PINE_STAIRS.get());

        this.tag(BlockTags.WOODEN_SLABS)
                .add(IMBlocks.MASON_PINE_SLAB.get());
        this.tag(BlockTags.SLABS)
                .add(IMBlocks.MASON_PINE_SLAB.get());

        this.tag(BlockTags.PRESSURE_PLATES)
                .add(IMBlocks.MASON_PINE_PRESSURE_PLATE.get());
        this.tag(BlockTags.WOODEN_PRESSURE_PLATES)
                .add(IMBlocks.MASON_PINE_PRESSURE_PLATE.get());

        this.tag(BlockTags.BUTTONS)
                .add(IMBlocks.MASON_PINE_BUTTON.get());
        this.tag(BlockTags.WOODEN_BUTTONS)
                .add(IMBlocks.MASON_PINE_BUTTON.get());

        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(IMMultiblocks.BALL_MILL.getBlock())
                .add(IMMultiblocks.HYDROCYCLONE.getBlock())
                .add(IMMultiblocks.FLOTATION_CELL.getBlock())
                .add(IMMultiblocks.THICKENER.getBlock())
                .add(IMMultiblocks.ELITE_BLAST_FURNACE.getBlock())
                .add(IMMultiblocks.HYDROCYCLONE.getBlock())
                .add(IMMultiblocks.CONTINUOUS_CASTING_MACHINE.getBlock())
                .add(IMMultiblocks.ADVANCED_COKE_OVEN.getBlock())
                .add(IMBlocks.PIG_IRON_BLOCK.get())
                .add(IMBlocks.ADVANCED_COKE_OVEN_BRICKS.get())
                .add(IMBlocks.ELECTRIC_CABLE_LV.get())
                .add(IMBlocks.ELECTRIC_CABLE_MV.get())
                .add(IMBlocks.CASTING_CHANNEL.get())
                .add(IMBlocks.SAP_COLLECTOR.get());



    }
}
