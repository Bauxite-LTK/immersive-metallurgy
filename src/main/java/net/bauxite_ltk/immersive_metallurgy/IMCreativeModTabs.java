package net.bauxite_ltk.immersive_metallurgy;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.common.register.IEItems;
import net.bauxite_ltk.immersive_metallurgy.block.IMBlocks;
import net.bauxite_ltk.immersive_metallurgy.item.IMItems;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IMCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IMUtils.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("main_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.immersive_metallurgy")) //The language key for the title of your CreativeModeTab
            .icon(() -> IEItems.Metals.INGOTS.get(EnumMetals.STEEL).get().getDefaultInstance())
            .displayItems((parameters, output) -> {

                output.accept(IMBlocks.ADVANCED_COKE_OVEN_BRICKS);
                output.accept(IMBlocks.ELECTRIC_CABLE_LV.asItem());
                output.accept(IMBlocks.ELECTRIC_CABLE_MV.asItem());
                output.accept(IMBlocks.MASON_PINE_LOG);
                output.accept(IMBlocks.MASON_PINE_WOOD);
                output.accept(IMBlocks.STRIPPED_MASON_PINE_LOG);
                output.accept(IMBlocks.STRIPPED_MASON_PINE_WOOD);
                output.accept(IMBlocks.MASON_PINE_LEAVES);
                output.accept(IMBlocks.MASON_PINE_SAPLING);

                output.accept(IMBlocks.MASON_PINE_PLANKS);
                output.accept(IMBlocks.MASON_PINE_STAIRS);
                output.accept(IMBlocks.MASON_PINE_SLAB);
                output.accept(IMBlocks.MASON_PINE_FENCE);
                output.accept(IMBlocks.MASON_PINE_FENCE_GATE);
                output.accept(IMBlocks.MASON_PINE_TRAP_DOOR);
                output.accept(IMBlocks.MASON_PINE_DOOR);
                output.accept(IMBlocks.MASON_PINE_PRESSURE_PLATE);
                output.accept(IMBlocks.MASON_PINE_BUTTON);



                output.accept(IMBlocks.MASON_PINE_LOG_LIVE);
                output.accept(IMBlocks.MASON_PINE_LOG_SAPPY);
                output.accept(IMBlocks.SAP_COLLECTOR);
                output.accept(IMItems.MASON_PINE_SAP_BOTTLE);
                output.accept(IMItems.COLOPHONY_BOTTLE);
                output.accept(IMItems.COLOPHONY);
                output.accept(IMItems.MASON_PINE_SAP_BUCKET);
                output.accept(IMItems.TURPENTINE_OIL_BUCKET);
                output.accept(IMItems.TERPENIC_OIL_BUCKET);

                output.accept(IMItems.Ores.RAW_IRON_ORE_CHUNK);
                output.accept(IMItems.Ores.RAW_IRON_ORE_COARSE_POWDER);
                output.accept(IMItems.Ores.RAW_IRON_ORE_FINES);
                output.accept(IMItems.Ores.RAW_IRON_CONCENTRATE_PELLET);
                output.accept(IMItems.RAW_IRON_SLURRY_BUCKET);
                output.accept(IMItems.RAW_IRON_PROCESSED_SLURRY_BUCKET);
                output.accept(IMItems.RAW_IRON_CONCENTRATE_SLURRY_BUCKET);
                output.accept(IMItems.RAW_IRON_TAILING_SLURRY_BUCKET);
                output.accept(IMItems.MOLTEN_PIG_IRON_BUCKET.get());

                output.accept(IMItems.Ores.RAW_GOLD_ORE_CHUNK);
                output.accept(IMItems.Ores.RAW_GOLD_ORE_COARSE_POWDER);
                output.accept(IMItems.Ores.RAW_GOLD_ORE_FINES);
                output.accept(IMItems.Ores.RAW_GOLD_CONCENTRATE_PELLET);
                output.accept(IMItems.RAW_GOLD_SLURRY_BUCKET);
                output.accept(IMItems.RAW_GOLD_PROCESSED_SLURRY_BUCKET);
                output.accept(IMItems.RAW_GOLD_CONCENTRATE_SLURRY_BUCKET);
                output.accept(IMItems.RAW_GOLD_TAILING_SLURRY_BUCKET);
                output.accept(IMItems.MOLTEN_GOLD_BUCKET.get());

                output.accept(IMItems.Ores.RAW_COPPER_ORE_CHUNK);
                output.accept(IMItems.Ores.RAW_COPPER_ORE_COARSE_POWDER);
                output.accept(IMItems.Ores.RAW_COPPER_ORE_FINES);
                output.accept(IMItems.Ores.RAW_COPPER_CONCENTRATE_PELLET);
                output.accept(IMItems.RAW_COPPER_SLURRY_BUCKET);
                output.accept(IMItems.RAW_COPPER_PROCESSED_SLURRY_BUCKET);
                output.accept(IMItems.RAW_COPPER_CONCENTRATE_SLURRY_BUCKET);
                output.accept(IMItems.RAW_COPPER_TAILING_SLURRY_BUCKET);
                output.accept(IMItems.MOLTEN_COPPER_BUCKET.get());

                output.accept(IMItems.Ores.RAW_SILVER_ORE_CHUNK);
                output.accept(IMItems.Ores.RAW_SILVER_ORE_COARSE_POWDER);
                output.accept(IMItems.Ores.RAW_SILVER_ORE_FINES);
                output.accept(IMItems.Ores.RAW_SILVER_CONCENTRATE_PELLET);
                output.accept(IMItems.RAW_SILVER_SLURRY_BUCKET);
                output.accept(IMItems.RAW_SILVER_PROCESSED_SLURRY_BUCKET);
                output.accept(IMItems.RAW_SILVER_CONCENTRATE_SLURRY_BUCKET);
                output.accept(IMItems.RAW_SILVER_TAILING_SLURRY_BUCKET);
                output.accept(IMItems.MOLTEN_SILVER_BUCKET.get());

                output.accept(IMItems.Ores.RAW_LEAD_ORE_CHUNK);
                output.accept(IMItems.Ores.RAW_LEAD_ORE_COARSE_POWDER);
                output.accept(IMItems.Ores.RAW_LEAD_ORE_FINES);
                output.accept(IMItems.Ores.RAW_LEAD_CONCENTRATE_PELLET);
                output.accept(IMItems.RAW_LEAD_SLURRY_BUCKET);
                output.accept(IMItems.RAW_LEAD_PROCESSED_SLURRY_BUCKET);
                output.accept(IMItems.RAW_LEAD_CONCENTRATE_SLURRY_BUCKET);
                output.accept(IMItems.RAW_LEAD_TAILING_SLURRY_BUCKET);
                output.accept(IMItems.MOLTEN_LEAD_BUCKET.get());

                output.accept(IMItems.Ores.RAW_NICKEL_ORE_CHUNK);
                output.accept(IMItems.Ores.RAW_NICKEL_ORE_COARSE_POWDER);
                output.accept(IMItems.Ores.RAW_NICKEL_ORE_FINES);
                output.accept(IMItems.Ores.RAW_NICKEL_CONCENTRATE_PELLET);
                output.accept(IMItems.RAW_NICKEL_SLURRY_BUCKET);
                output.accept(IMItems.RAW_NICKEL_PROCESSED_SLURRY_BUCKET);
                output.accept(IMItems.RAW_NICKEL_CONCENTRATE_SLURRY_BUCKET);
                output.accept(IMItems.RAW_NICKEL_TAILING_SLURRY_BUCKET);
                output.accept(IMItems.MOLTEN_NICKEL_BUCKET.get());

                output.accept(IMItems.Ores.RAW_URANIUM_ORE_CHUNK);
                output.accept(IMItems.Ores.RAW_URANIUM_ORE_COARSE_POWDER);
                output.accept(IMItems.Ores.RAW_URANIUM_ORE_FINES);
                output.accept(IMItems.Ores.RAW_URANIUM_CONCENTRATE_PELLET);
                output.accept(IMItems.RAW_URANIUM_SLURRY_BUCKET);
                output.accept(IMItems.RAW_URANIUM_PROCESSED_SLURRY_BUCKET);
                output.accept(IMItems.RAW_URANIUM_CONCENTRATE_SLURRY_BUCKET);
                output.accept(IMItems.RAW_URANIUM_TAILING_SLURRY_BUCKET);
                output.accept(IMItems.MOLTEN_URANIUM_BUCKET.get());

                output.accept(IMItems.Ores.RAW_ALUMINUM_ORE_CHUNK);
                output.accept(IMItems.Ores.RAW_ALUMINUM_ORE_COARSE_POWDER);
                output.accept(IMItems.Ores.RAW_ALUMINUM_ORE_FINES);
                output.accept(IMItems.Ores.RAW_ALUMINUM_CONCENTRATE_PELLET);
                output.accept(IMItems.RAW_ALUMINUM_SLURRY_BUCKET);
                output.accept(IMItems.RAW_ALUMINUM_PROCESSED_SLURRY_BUCKET);
                output.accept(IMItems.RAW_ALUMINUM_CONCENTRATE_SLURRY_BUCKET);
                output.accept(IMItems.RAW_ALUMINUM_TAILING_SLURRY_BUCKET);
                output.accept(IMItems.MOLTEN_ALUMINUM_BUCKET.get());


                output.accept(IMItems.PIG_IRON_BLAST_FURNACE_PELLET);
                output.accept(IMItems.PIG_IRON_INGOT);
                output.accept(IMBlocks.PIG_IRON_BLOCK);
                output.accept(IMBlocks.CASTING_CHANNEL);
                output.accept(IMItems.HOT_AIR_BUCKET);
                output.accept(IMItems.WATER_GAS_BUCKET);
                output.accept(IMItems.BLAST_FURNACE_GAS_BUCKET);
                output.accept(IMItems.COKE_OVEN_GAS_BUCKET);

            }).build()
    );


    public static void init(IEventBus modEventBus){
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
