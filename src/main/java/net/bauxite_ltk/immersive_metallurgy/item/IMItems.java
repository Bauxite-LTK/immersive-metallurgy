package net.bauxite_ltk.immersive_metallurgy.item;

import net.bauxite_ltk.immersive_metallurgy.fluid.IMFluids;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IMItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(IMUtils.MOD_ID);

    public static final DeferredItem<Item> MOLTEN_PIG_IRON_BUCKET =
            ITEMS.register("bucket/molten_pig_iron",
                    () -> new BucketItem(
                            IMFluids.MOLTEN_PIG_IRON.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );

    public static final DeferredItem<Item> MOLTEN_GOLD_BUCKET =
            ITEMS.register("bucket/molten_gold",
                    () -> new BucketItem(
                            IMFluids.MOLTEN_GOLD.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );

    public static final DeferredItem<Item> MOLTEN_COPPER_BUCKET =
            ITEMS.register("bucket/molten_copper",
                    () -> new BucketItem(
                            IMFluids.MOLTEN_COPPER.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );

    public static final DeferredItem<Item> MOLTEN_SILVER_BUCKET =
            ITEMS.register("bucket/molten_silver",
                    () -> new BucketItem(
                            IMFluids.MOLTEN_SILVER.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );

    public static final DeferredItem<Item> MOLTEN_LEAD_BUCKET =
            ITEMS.register("bucket/molten_lead",
                    () -> new BucketItem(
                            IMFluids.MOLTEN_LEAD.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );

    public static final DeferredItem<Item> MOLTEN_NICKEL_BUCKET =
            ITEMS.register("bucket/molten_nickel",
                    () -> new BucketItem(
                            IMFluids.MOLTEN_NICKEL.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );

    public static final DeferredItem<Item> MOLTEN_URANIUM_BUCKET =
            ITEMS.register("bucket/molten_uranium",
                    () -> new BucketItem(
                            IMFluids.MOLTEN_URANIUM.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );

    public static final DeferredItem<Item> MOLTEN_ALUMINUM_BUCKET =
            ITEMS.register("bucket/molten_aluminum",
                    () -> new BucketItem(
                            IMFluids.MOLTEN_ALUMINUM.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );


    public static final DeferredItem<Item> HOT_AIR_BUCKET =
            ITEMS.register("bucket/hot_air",
                    () -> new BucketItem(
                            IMFluids.HOT_AIR.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );

    public static final DeferredItem<Item> WATER_GAS_BUCKET =
            ITEMS.register("bucket/water_gas",
                    () -> new BucketItem(
                            IMFluids.WATER_GAS.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );

    public static final DeferredItem<Item> BLAST_FURNACE_GAS_BUCKET =
            ITEMS.register("bucket/blast_furnace_gas",
                    () -> new BucketItem(
                            IMFluids.BLAST_FURNACE_GAS.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );

    public static final DeferredItem<Item> COKE_OVEN_GAS_BUCKET =
            ITEMS.register("bucket/coke_oven_gas",
                    () -> new BucketItem(
                            IMFluids.COKE_OVEN_GAS.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    
    

    public static final DeferredItem<Item> MASON_PINE_SAP_BUCKET =
            ITEMS.register("bucket/mason_pine_sap",
                    () -> new BucketItem(
                            IMFluids.MASON_PINE_SAP.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );

    public static final DeferredItem<Item> TURPENTINE_OIL_BUCKET =
            ITEMS.register("bucket/turpentine_oil",
                    () -> new BucketItem(
                            IMFluids.TURPENTINE_OIL.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );

    public static final DeferredItem<Item> TERPENIC_OIL_BUCKET =
            ITEMS.register("bucket/terpenic_oil",
                    () -> new BucketItem(
                            IMFluids.TERPENIC_OIL.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );


    /*
    -----------------------------------------------------------
      Slurry Fluid Bucket
      Ore: RAW IRON
    -----------------------------------------------------------
    */
    public static final DeferredItem<Item> RAW_IRON_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_iron_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_IRON_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_IRON_PROCESSED_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_iron_processed_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_IRON_PROCESSED_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_IRON_CONCENTRATE_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_iron_concentrate_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_IRON_CONCENTRATE_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_IRON_TAILING_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_iron_tailing_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_IRON_TAILING_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );



    /*
    -----------------------------------------------------------
      Slurry Fluid Bucket
      Ore: RAW GOLD
    -----------------------------------------------------------
    */
    public static final DeferredItem<Item> RAW_GOLD_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_gold_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_GOLD_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_GOLD_PROCESSED_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_gold_processed_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_GOLD_PROCESSED_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_GOLD_CONCENTRATE_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_gold_concentrate_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_GOLD_CONCENTRATE_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_GOLD_TAILING_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_gold_tailing_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_GOLD_TAILING_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );


    /*
    -----------------------------------------------------------
      Slurry Fluid Bucket
      Ore: RAW COPPER
    -----------------------------------------------------------
    */
    public static final DeferredItem<Item> RAW_COPPER_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_copper_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_COPPER_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_COPPER_PROCESSED_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_copper_processed_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_COPPER_PROCESSED_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_COPPER_CONCENTRATE_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_copper_concentrate_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_COPPER_CONCENTRATE_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_COPPER_TAILING_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_copper_tailing_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_COPPER_TAILING_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );



    /*
    -----------------------------------------------------------
      Slurry Fluid Bucket
      Ore: RAW SILVER
    -----------------------------------------------------------
    */
    public static final DeferredItem<Item> RAW_SILVER_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_silver_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_SILVER_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_SILVER_PROCESSED_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_silver_processed_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_SILVER_PROCESSED_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_SILVER_CONCENTRATE_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_silver_concentrate_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_SILVER_CONCENTRATE_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_SILVER_TAILING_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_silver_tailing_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_SILVER_TAILING_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );



    /*
    -----------------------------------------------------------
      Slurry Fluid Bucket
      Ore: RAW LEAD
    -----------------------------------------------------------
    */
    public static final DeferredItem<Item> RAW_LEAD_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_lead_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_LEAD_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_LEAD_PROCESSED_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_lead_processed_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_LEAD_PROCESSED_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_LEAD_CONCENTRATE_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_lead_concentrate_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_LEAD_CONCENTRATE_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_LEAD_TAILING_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_lead_tailing_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_LEAD_TAILING_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );


    /*
    -----------------------------------------------------------
      Slurry Fluid Bucket
      Ore: RAW NICKEL
    -----------------------------------------------------------
    */
    public static final DeferredItem<Item> RAW_NICKEL_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_nickel_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_NICKEL_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_NICKEL_PROCESSED_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_nickel_processed_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_NICKEL_PROCESSED_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_NICKEL_CONCENTRATE_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_nickel_concentrate_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_NICKEL_CONCENTRATE_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_NICKEL_TAILING_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_nickel_tailing_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_NICKEL_TAILING_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );



    /*
    -----------------------------------------------------------
      Slurry Fluid Bucket
      Ore: RAW URANIUM
    -----------------------------------------------------------
    */
    public static final DeferredItem<Item> RAW_URANIUM_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_uranium_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_URANIUM_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_URANIUM_PROCESSED_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_uranium_processed_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_URANIUM_PROCESSED_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_URANIUM_CONCENTRATE_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_uranium_concentrate_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_URANIUM_CONCENTRATE_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_URANIUM_TAILING_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_uranium_tailing_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_URANIUM_TAILING_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );


    /*
    -----------------------------------------------------------
      Slurry Fluid Bucket
      Ore: RAW ALUMINUM
    -----------------------------------------------------------
    */
    public static final DeferredItem<Item> RAW_ALUMINUM_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_aluminum_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_ALUMINUM_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_ALUMINUM_PROCESSED_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_aluminum_processed_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_ALUMINUM_PROCESSED_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_ALUMINUM_CONCENTRATE_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_aluminum_concentrate_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_ALUMINUM_CONCENTRATE_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );
    public static final DeferredItem<Item> RAW_ALUMINUM_TAILING_SLURRY_BUCKET =
            ITEMS.register("bucket/raw_aluminum_tailing_slurry",
                    () -> new BucketItem(
                            IMFluids.RAW_ALUMINUM_TAILING_SLURRY.getSource(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                    )
            );

    public static final DeferredItem<Item> MASON_PINE_SAP_BOTTLE =
            ITEMS.registerItem("mason_pine_sap_bottle", Item::new, new Item.Properties().stacksTo(16));

    public static final DeferredItem<Item> COLOPHONY_BOTTLE = ITEMS.registerItem("colophony_bottle", Item::new, new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE));

    
    
    public static final class Ores{
        public static final DeferredItem<Item> RAW_IRON_ORE_CHUNK = ITEMS.registerSimpleItem("pure_ore_chunk_raw_iron");
        public static final DeferredItem<Item> RAW_IRON_ORE_COARSE_POWDER = ITEMS.registerSimpleItem("pure_coarse_powder_raw_iron");
        public static final DeferredItem<Item> RAW_IRON_ORE_FINES = ITEMS.registerSimpleItem("ore_fines_raw_iron");
        public static final DeferredItem<Item> RAW_IRON_CONCENTRATE_PELLET = ITEMS.registerSimpleItem("concentrate_pellet_raw_iron");


        public static final DeferredItem<Item> RAW_GOLD_ORE_CHUNK = ITEMS.registerSimpleItem("pure_ore_chunk_raw_gold");
        public static final DeferredItem<Item> RAW_GOLD_ORE_COARSE_POWDER = ITEMS.registerSimpleItem("pure_coarse_powder_raw_gold");
        public static final DeferredItem<Item> RAW_GOLD_ORE_FINES = ITEMS.registerSimpleItem("ore_fines_raw_gold");
        public static final DeferredItem<Item> RAW_GOLD_CONCENTRATE_PELLET = ITEMS.registerSimpleItem("concentrate_pellet_raw_gold");

        public static final DeferredItem<Item> RAW_COPPER_ORE_CHUNK = ITEMS.registerSimpleItem("pure_ore_chunk_raw_copper");
        public static final DeferredItem<Item> RAW_COPPER_ORE_COARSE_POWDER = ITEMS.registerSimpleItem("pure_coarse_powder_raw_copper");
        public static final DeferredItem<Item> RAW_COPPER_ORE_FINES = ITEMS.registerSimpleItem("ore_fines_raw_copper");
        public static final DeferredItem<Item> RAW_COPPER_CONCENTRATE_PELLET = ITEMS.registerSimpleItem("concentrate_pellet_raw_copper");

        public static final DeferredItem<Item> RAW_SILVER_ORE_CHUNK = ITEMS.registerSimpleItem("pure_ore_chunk_raw_silver");
        public static final DeferredItem<Item> RAW_SILVER_ORE_COARSE_POWDER = ITEMS.registerSimpleItem("pure_coarse_powder_raw_silver");
        public static final DeferredItem<Item> RAW_SILVER_ORE_FINES = ITEMS.registerSimpleItem("ore_fines_raw_silver");
        public static final DeferredItem<Item> RAW_SILVER_CONCENTRATE_PELLET = ITEMS.registerSimpleItem("concentrate_pellet_raw_silver");

        public static final DeferredItem<Item> RAW_LEAD_ORE_CHUNK = ITEMS.registerSimpleItem("pure_ore_chunk_raw_lead");
        public static final DeferredItem<Item> RAW_LEAD_ORE_COARSE_POWDER = ITEMS.registerSimpleItem("pure_coarse_powder_raw_lead");
        public static final DeferredItem<Item> RAW_LEAD_ORE_FINES = ITEMS.registerSimpleItem("ore_fines_raw_lead");
        public static final DeferredItem<Item> RAW_LEAD_CONCENTRATE_PELLET = ITEMS.registerSimpleItem("concentrate_pellet_raw_lead");


        public static final DeferredItem<Item> RAW_NICKEL_ORE_CHUNK = ITEMS.registerSimpleItem("pure_ore_chunk_raw_nickel");
        public static final DeferredItem<Item> RAW_NICKEL_ORE_COARSE_POWDER = ITEMS.registerSimpleItem("pure_coarse_powder_raw_nickel");
        public static final DeferredItem<Item> RAW_NICKEL_ORE_FINES = ITEMS.registerSimpleItem("ore_fines_raw_nickel");
        public static final DeferredItem<Item> RAW_NICKEL_CONCENTRATE_PELLET = ITEMS.registerSimpleItem("concentrate_pellet_raw_nickel");


        public static final DeferredItem<Item> RAW_URANIUM_ORE_CHUNK = ITEMS.registerSimpleItem("pure_ore_chunk_raw_uranium");
        public static final DeferredItem<Item> RAW_URANIUM_ORE_COARSE_POWDER = ITEMS.registerSimpleItem("pure_coarse_powder_raw_uranium");
        public static final DeferredItem<Item> RAW_URANIUM_ORE_FINES = ITEMS.registerSimpleItem("ore_fines_raw_uranium");
        public static final DeferredItem<Item> RAW_URANIUM_CONCENTRATE_PELLET = ITEMS.registerSimpleItem("concentrate_pellet_raw_uranium");

        public static final DeferredItem<Item> RAW_ALUMINUM_ORE_CHUNK = ITEMS.registerSimpleItem("pure_ore_chunk_raw_aluminum");
        public static final DeferredItem<Item> RAW_ALUMINUM_ORE_COARSE_POWDER = ITEMS.registerSimpleItem("pure_coarse_powder_raw_aluminum");
        public static final DeferredItem<Item> RAW_ALUMINUM_ORE_FINES = ITEMS.registerSimpleItem("ore_fines_raw_aluminum");
        public static final DeferredItem<Item> RAW_ALUMINUM_CONCENTRATE_PELLET = ITEMS.registerSimpleItem("concentrate_pellet_raw_aluminum");


    }

    public static final DeferredItem<Item> COLOPHONY = ITEMS.register("colophony", ()-> new FurnaceFuelItem(1600));
    public static final DeferredItem<Item> PIG_IRON_INGOT = registerSimpleItemWithTooltip("ingot_pig_iron","ingot_pig_iron.idle");
    public static final DeferredItem<Item> PIG_IRON_BLAST_FURNACE_PELLET = registerSimpleItemWithTooltip("blast_furnace_iron_pellet","blast_furnace_iron_pellet.idle");

    public static DeferredItem<Item> registerSimpleItemWithTooltip(String name, String tooltip){
        return ITEMS.register(name, () -> new IMBaseItem(tooltip, new Item.Properties()));
    }

    public static void init(IEventBus modEventBus){
        try {
            Class.forName("net.bauxite_ltk.immersive_metallurgy.item.IMItems$Ores");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        ITEMS.register(modEventBus);
    }

}
