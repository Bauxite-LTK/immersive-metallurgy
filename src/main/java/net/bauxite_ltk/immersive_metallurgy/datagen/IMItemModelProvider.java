package net.bauxite_ltk.immersive_metallurgy.datagen;

import net.bauxite_ltk.immersive_metallurgy.block.IMBlocks;
import net.bauxite_ltk.immersive_metallurgy.item.IMItems;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SaplingBlock;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

public class IMItemModelProvider extends ItemModelProvider {
    public IMItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, IMUtils.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(IMItems.Ores.RAW_IRON_ORE_CHUNK.get());
        basicItem(IMItems.Ores.RAW_IRON_ORE_COARSE_POWDER.get());
        basicItem(IMItems.Ores.RAW_IRON_ORE_FINES.get());
        basicItem(IMItems.Ores.RAW_IRON_CONCENTRATE_PELLET.get());

        basicItem(IMItems.Ores.RAW_GOLD_ORE_CHUNK.get());
        basicItem(IMItems.Ores.RAW_GOLD_ORE_COARSE_POWDER.get());
        basicItem(IMItems.Ores.RAW_GOLD_ORE_FINES.get());
        basicItem(IMItems.Ores.RAW_GOLD_CONCENTRATE_PELLET.get());

        basicItem(IMItems.Ores.RAW_COPPER_ORE_CHUNK.get());
        basicItem(IMItems.Ores.RAW_COPPER_ORE_COARSE_POWDER.get());
        basicItem(IMItems.Ores.RAW_COPPER_ORE_FINES.get());
        basicItem(IMItems.Ores.RAW_COPPER_CONCENTRATE_PELLET.get());

        basicItem(IMItems.Ores.RAW_SILVER_ORE_CHUNK.get());
        basicItem(IMItems.Ores.RAW_SILVER_ORE_COARSE_POWDER.get());
        basicItem(IMItems.Ores.RAW_SILVER_ORE_FINES.get());
        basicItem(IMItems.Ores.RAW_SILVER_CONCENTRATE_PELLET.get());

        basicItem(IMItems.Ores.RAW_LEAD_ORE_CHUNK.get());
        basicItem(IMItems.Ores.RAW_LEAD_ORE_COARSE_POWDER.get());
        basicItem(IMItems.Ores.RAW_LEAD_ORE_FINES.get());
        basicItem(IMItems.Ores.RAW_LEAD_CONCENTRATE_PELLET.get());

        basicItem(IMItems.Ores.RAW_NICKEL_ORE_CHUNK.get());
        basicItem(IMItems.Ores.RAW_NICKEL_ORE_COARSE_POWDER.get());
        basicItem(IMItems.Ores.RAW_NICKEL_ORE_FINES.get());
        basicItem(IMItems.Ores.RAW_NICKEL_CONCENTRATE_PELLET.get());

        basicItem(IMItems.Ores.RAW_URANIUM_ORE_CHUNK.get());
        basicItem(IMItems.Ores.RAW_URANIUM_ORE_COARSE_POWDER.get());
        basicItem(IMItems.Ores.RAW_URANIUM_ORE_FINES.get());
        basicItem(IMItems.Ores.RAW_URANIUM_CONCENTRATE_PELLET.get());

        basicItem(IMItems.Ores.RAW_ALUMINUM_ORE_CHUNK.get());
        basicItem(IMItems.Ores.RAW_ALUMINUM_ORE_COARSE_POWDER.get());
        basicItem(IMItems.Ores.RAW_ALUMINUM_ORE_FINES.get());
        basicItem(IMItems.Ores.RAW_ALUMINUM_CONCENTRATE_PELLET.get());
        
        basicItem(IMItems.COLOPHONY.get());
        basicItem(IMItems.PIG_IRON_INGOT.get());
        basicItem(IMItems.RAW_PLATINUM.get());
        basicItem(IMItems.PLATINUM_INGOT.get());
        basicItem(IMItems.PLATINUM_NUGGET.get());
        basicItem(IMItems.PIG_IRON_BLAST_FURNACE_PELLET.get());
        basicItem(IMItems.MASON_PINE_SAP_BOTTLE.get());
        basicItem(IMItems.COLOPHONY_BOTTLE.get());
        basicItem(IMBlocks.MASON_PINE_DOOR.asItem());

        buttonItem(IMBlocks.MASON_PINE_BUTTON, IMBlocks.MASON_PINE_PLANKS);
        fenceItem(IMBlocks.MASON_PINE_FENCE, IMBlocks.MASON_PINE_PLANKS);


        saplingItem(IMBlocks.MASON_PINE_SAPLING);
    }

    private ItemModelBuilder saplingItem(DeferredBlock<SaplingBlock> sapling){
        return withExistingParent(sapling.getId().getPath(), ResourceLocation.parse("item/generated"))
                .texture("layer0", IMUtils.modRL("block/" + sapling.getId().getPath()));
    }

    private ItemModelBuilder slashPathItem(DeferredItem<Item> item){
        return withExistingParent("item/" + item.getId().getPath(), ResourceLocation.parse("item/generated"))
                .texture("layer0", IMUtils.modRL("item/" + item.getId().getPath()));
    }

    private void buttonItem(DeferredBlock<?> block, DeferredBlock<Block> baseBlock){
        this.withExistingParent(block.getId().getPath(),mcLoc("block/button_inventory"))
                .texture("texture", IMUtils.modRL("block/" + baseBlock.getId().getPath()));
    }

    private void fenceItem(DeferredBlock<?> block, DeferredBlock<Block> baseBlock){
        this.withExistingParent(block.getId().getPath(),mcLoc("block/fence_inventory"))
                .texture("texture", IMUtils.modRL("block/" + baseBlock.getId().getPath()));
    }
}
