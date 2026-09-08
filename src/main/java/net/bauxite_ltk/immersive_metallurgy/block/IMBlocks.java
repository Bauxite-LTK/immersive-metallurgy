package net.bauxite_ltk.immersive_metallurgy.block;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import net.bauxite_ltk.immersive_metallurgy.block.liquid.CanSolidifyLiquidBlock;
import net.bauxite_ltk.immersive_metallurgy.block.liquid.CanVaporateLiquidBlock;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.ElectricCableBlock;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.casting_channel.CastingChannelBlock;
import net.bauxite_ltk.immersive_metallurgy.block.sapCollector.SapCollectorBlock;
import net.bauxite_ltk.immersive_metallurgy.block.wood.*;
import net.bauxite_ltk.immersive_metallurgy.block.wood.planks.*;
import net.bauxite_ltk.immersive_metallurgy.fluid.IMFluids;
import net.bauxite_ltk.immersive_metallurgy.item.IMBaseBlockItem;
import net.bauxite_ltk.immersive_metallurgy.item.IMItems;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.bauxite_ltk.immersive_metallurgy.worldgen.tree.IMTreeGrowers;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class IMBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(IMUtils.MOD_ID);

    
    public static final DeferredBlock<LiquidBlock> MOLTEN_PIG_IRON = registerNoItem("fluid/molten_pig_iron", () -> new CanSolidifyLiquidBlock(IMFluids.MOLTEN_PIG_IRON.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).noLootTable(), IMBlocks.PIG_IRON_BLOCK, IMBlockEntities.MOLTEN_PIG_IRON,100, 0x3c2d2d));
    public static final DeferredBlock<LiquidBlock> MOLTEN_GOLD = registerNoItem("fluid/molten_gold", () -> new CanSolidifyLiquidBlock(IMFluids.MOLTEN_GOLD.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).noLootTable(), Blocks.GOLD_BLOCK,IMBlockEntities.MOLTEN_GOLD, 100, 0xffd241));
    public static final DeferredBlock<LiquidBlock> MOLTEN_COPPER = registerNoItem("fluid/molten_copper", () -> new CanSolidifyLiquidBlock(IMFluids.MOLTEN_COPPER.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).noLootTable(), Blocks.COPPER_BLOCK,IMBlockEntities.MOLTEN_COPPER,100, 0xc16348));
    public static final DeferredBlock<LiquidBlock> MOLTEN_SILVER = registerNoItem("fluid/molten_silver", () -> new CanSolidifyLiquidBlock(IMFluids.MOLTEN_SILVER.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).noLootTable(), IEBlocks.Metals.STORAGE.get(EnumMetals.SILVER).get(), IMBlockEntities.MOLTEN_SILVER ,100, 0xd4d2cb));
    public static final DeferredBlock<LiquidBlock> MOLTEN_LEAD = registerNoItem("fluid/molten_lead", () -> new CanSolidifyLiquidBlock(IMFluids.MOLTEN_LEAD.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).noLootTable(), IEBlocks.Metals.STORAGE.get(EnumMetals.LEAD).get(), IMBlockEntities.MOLTEN_LEAD ,100, 0x413d50));
    public static final DeferredBlock<LiquidBlock> MOLTEN_NICKEL = registerNoItem("fluid/molten_nickel", () -> new CanSolidifyLiquidBlock(IMFluids.MOLTEN_NICKEL.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).noLootTable(), IEBlocks.Metals.STORAGE.get(EnumMetals.NICKEL).get(), IMBlockEntities.MOLTEN_NICKEL ,100, 0xa3a092));
    public static final DeferredBlock<LiquidBlock> MOLTEN_URANIUM = registerNoItem("fluid/molten_uranium", () -> new CanSolidifyLiquidBlock(IMFluids.MOLTEN_URANIUM.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).noLootTable(), IEBlocks.Metals.STORAGE.get(EnumMetals.URANIUM).get(), IMBlockEntities.MOLTEN_URANIUM ,100, 0xa3a092));
    public static final DeferredBlock<LiquidBlock> MOLTEN_ALUMINUM = registerNoItem("fluid/molten_aluminum", () -> new CanSolidifyLiquidBlock(IMFluids.MOLTEN_ALUMINUM.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).noLootTable(), IEBlocks.Metals.STORAGE.get(EnumMetals.ALUMINUM).get(), IMBlockEntities.MOLTEN_ALUMINUM ,100, 0xa3a092));

    public static final DeferredBlock<LiquidBlock> HOT_AIR = registerNoItem("fluid/hot_air", () -> new CanVaporateLiquidBlock(IMFluids.HOT_AIR.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable(), IMBlockEntities.HOT_AIR ,5));
    public static final DeferredBlock<LiquidBlock> WATER_GAS = registerNoItem("fluid/water_gas", () -> new CanVaporateLiquidBlock(IMFluids.WATER_GAS.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable(), IMBlockEntities.WATER_GAS ,5));
    public static final DeferredBlock<LiquidBlock> BLAST_FURNACE_GAS = registerNoItem("fluid/blast_furnace_gas", () -> new CanVaporateLiquidBlock(IMFluids.BLAST_FURNACE_GAS.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable(), IMBlockEntities.BLAST_FURNACE_GAS ,5));


    public static final DeferredBlock<LiquidBlock> MASON_PINE_SAP = registerNoItem("fluid/mason_pine_sap", () -> new LiquidBlock(IMFluids.MASON_PINE_SAP.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> TURPENTINE_OIL = registerNoItem("fluid/turpentine_oil", () -> new LiquidBlock(IMFluids.TURPENTINE_OIL.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> TERPENIC_OIL = registerNoItem("fluid/terpenic_oil", () -> new LiquidBlock(IMFluids.TERPENIC_OIL.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    public static final DeferredBlock<FlameableRotatedPillarBlock> MASON_PINE_LOG = registerBlock("mason_pine_log", () -> new FlameableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)));
    public static final DeferredBlock<FlameableRotatedPillarBlock> MASON_PINE_LOG_LIVE = registerBlock("mason_pine_log_live", () -> new FlameableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<FlameableRotatedPillarBlock> MASON_PINE_LOG_SAPPY = registerBlock("mason_pine_log_sappy", () -> new FlameableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<FlameableRotatedPillarBlock> MASON_PINE_WOOD = registerBlock("mason_pine_wood", () -> new FlameableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD)));
    public static final DeferredBlock<FlameableRotatedPillarBlock> STRIPPED_MASON_PINE_LOG = registerBlock("stripped_mason_pine_log", () -> new FlameableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG)));
    public static final DeferredBlock<FlameableRotatedPillarBlock> STRIPPED_MASON_PINE_WOOD = registerBlock("stripped_mason_pine_wood", () -> new FlameableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_WOOD)));

    public static final DeferredBlock<Block> MASON_PINE_PLANKS = registerBlock("mason_pine_planks", () -> new FlameableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS), 20,5));
    public static final DeferredBlock<LeavesBlock> MASON_PINE_LEAVES = registerBlock("mason_pine_leaves", () -> new FlameableLeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES), 60 ,30));
    public static final DeferredBlock<SaplingBlock> MASON_PINE_SAPLING = registerBlock("mason_pine_sapling", () -> new SaplingBlock(IMTreeGrowers.MASON_PINE, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING)));

    public static final DeferredBlock<StairBlock> MASON_PINE_STAIRS = registerBlock("mason_pine_stairs", ()-> new FlameableStairBlock(MASON_PINE_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS), 20, 5));
    public static final DeferredBlock<SlabBlock> MASON_PINE_SLAB = registerBlock("mason_pine_slab", ()-> new FlameableSlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB), 20, 5));
    public static final DeferredBlock<PressurePlateBlock> MASON_PINE_PRESSURE_PLATE = registerBlock("mason_pine_pressure_plate", ()-> new FlameablePressurePlateBlock(BlockSetType.SPRUCE ,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PRESSURE_PLATE), 20, 5));
    public static final DeferredBlock<ButtonBlock> MASON_PINE_BUTTON= registerBlock("mason_pine_button", ()-> new FlameableButtonBlock(BlockSetType.SPRUCE ,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_BUTTON),30, 20, 5));
    public static final DeferredBlock<FlameableFenceBlock> MASON_PINE_FENCE= registerBlock("mason_pine_fence", ()-> new FlameableFenceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE), 20, 5));
    public static final DeferredBlock<FlameableFenceGateBlock> MASON_PINE_FENCE_GATE= registerBlock("mason_pine_fence_gate", ()-> new FlameableFenceGateBlock(WoodType.SPRUCE, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE), 20, 5));
    public static final DeferredBlock<FlameableDoorBlock> MASON_PINE_DOOR= registerBlock("mason_pine_door", ()-> new FlameableDoorBlock(BlockSetType.SPRUCE, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR), 20, 5));
    public static final DeferredBlock<FlameableTrapDoorBlock> MASON_PINE_TRAP_DOOR= registerBlock("mason_pine_trapdoor", ()-> new FlameableTrapDoorBlock(BlockSetType.SPRUCE, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR), 20, 5));

    public static final DeferredBlock<Block> PIG_IRON_BLOCK = registerBlock("pig_iron_block", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));
    public static final DeferredBlock<Block> ADVANCED_COKE_OVEN_BRICKS = registerBlock("advanced_coke_oven_bricks", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_BRICKS)));


    public static final DeferredBlock<SapCollectorBlock> SAP_COLLECTOR = registerBlock("sap_collector", () -> new SapCollectorBlock(BlockBehaviour.Properties.of().noOcclusion().sound(SoundType.METAL).strength(3, 15)));

    public static final DeferredBlock<LiquidBlock> RAW_IRON_SLURRY = registerNoItem("fluid/raw_iron_slurry", () -> new LiquidBlock(IMFluids.RAW_IRON_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_IRON_PROCESSED_SLURRY = registerNoItem("fluid/raw_iron_processed_slurry", () -> new LiquidBlock(IMFluids.RAW_IRON_PROCESSED_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_IRON_CONCENTRATE_SLURRY = registerNoItem("fluid/raw_iron_concentrate_slurry", () -> new LiquidBlock(IMFluids.RAW_IRON_CONCENTRATE_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_IRON_TAILING_SLURRY = registerNoItem("fluid/raw_iron_tailing_slurry", () -> new LiquidBlock(IMFluids.RAW_IRON_TAILING_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    public static final DeferredBlock<LiquidBlock> RAW_GOLD_SLURRY = registerNoItem("fluid/raw_gold_slurry", () -> new LiquidBlock(IMFluids.RAW_GOLD_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_GOLD_PROCESSED_SLURRY = registerNoItem("fluid/raw_gold_processed_slurry", () -> new LiquidBlock(IMFluids.RAW_GOLD_PROCESSED_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_GOLD_CONCENTRATE_SLURRY = registerNoItem("fluid/raw_gold_concentrate_slurry", () -> new LiquidBlock(IMFluids.RAW_GOLD_CONCENTRATE_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_GOLD_TAILING_SLURRY = registerNoItem("fluid/raw_gold_tailing_slurry", () -> new LiquidBlock(IMFluids.RAW_GOLD_TAILING_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    public static final DeferredBlock<LiquidBlock> RAW_COPPER_SLURRY = registerNoItem("fluid/raw_copper_slurry", () -> new LiquidBlock(IMFluids.RAW_COPPER_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_COPPER_PROCESSED_SLURRY = registerNoItem("fluid/raw_copper_processed_slurry", () -> new LiquidBlock(IMFluids.RAW_COPPER_PROCESSED_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_COPPER_CONCENTRATE_SLURRY = registerNoItem("fluid/raw_copper_concentrate_slurry", () -> new LiquidBlock(IMFluids.RAW_COPPER_CONCENTRATE_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_COPPER_TAILING_SLURRY = registerNoItem("fluid/raw_copper_tailing_slurry", () -> new LiquidBlock(IMFluids.RAW_COPPER_TAILING_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    public static final DeferredBlock<LiquidBlock> RAW_SILVER_SLURRY = registerNoItem("fluid/raw_silver_slurry", () -> new LiquidBlock(IMFluids.RAW_SILVER_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_SILVER_PROCESSED_SLURRY = registerNoItem("fluid/raw_silver_processed_slurry", () -> new LiquidBlock(IMFluids.RAW_SILVER_PROCESSED_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_SILVER_CONCENTRATE_SLURRY = registerNoItem("fluid/raw_silver_concentrate_slurry", () -> new LiquidBlock(IMFluids.RAW_SILVER_CONCENTRATE_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_SILVER_TAILING_SLURRY = registerNoItem("fluid/raw_silver_tailing_slurry", () -> new LiquidBlock(IMFluids.RAW_SILVER_TAILING_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    public static final DeferredBlock<LiquidBlock> RAW_LEAD_SLURRY = registerNoItem("fluid/raw_lead_slurry", () -> new LiquidBlock(IMFluids.RAW_LEAD_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_LEAD_PROCESSED_SLURRY = registerNoItem("fluid/raw_lead_processed_slurry", () -> new LiquidBlock(IMFluids.RAW_LEAD_PROCESSED_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_LEAD_CONCENTRATE_SLURRY = registerNoItem("fluid/raw_lead_concentrate_slurry", () -> new LiquidBlock(IMFluids.RAW_LEAD_CONCENTRATE_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_LEAD_TAILING_SLURRY = registerNoItem("fluid/raw_lead_tailing_slurry", () -> new LiquidBlock(IMFluids.RAW_LEAD_TAILING_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    public static final DeferredBlock<LiquidBlock> RAW_NICKEL_SLURRY = registerNoItem("fluid/raw_nickel_slurry", () -> new LiquidBlock(IMFluids.RAW_NICKEL_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_NICKEL_PROCESSED_SLURRY = registerNoItem("fluid/raw_nickel_processed_slurry", () -> new LiquidBlock(IMFluids.RAW_NICKEL_PROCESSED_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_NICKEL_CONCENTRATE_SLURRY = registerNoItem("fluid/raw_nickel_concentrate_slurry", () -> new LiquidBlock(IMFluids.RAW_NICKEL_CONCENTRATE_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_NICKEL_TAILING_SLURRY = registerNoItem("fluid/raw_nickel_tailing_slurry", () -> new LiquidBlock(IMFluids.RAW_NICKEL_TAILING_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    
    public static final DeferredBlock<LiquidBlock> RAW_URANIUM_SLURRY = registerNoItem("fluid/raw_uranium_slurry", () -> new LiquidBlock(IMFluids.RAW_URANIUM_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_URANIUM_PROCESSED_SLURRY = registerNoItem("fluid/raw_uranium_processed_slurry", () -> new LiquidBlock(IMFluids.RAW_URANIUM_PROCESSED_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_URANIUM_CONCENTRATE_SLURRY = registerNoItem("fluid/raw_uranium_concentrate_slurry", () -> new LiquidBlock(IMFluids.RAW_URANIUM_CONCENTRATE_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_URANIUM_TAILING_SLURRY = registerNoItem("fluid/raw_uranium_tailing_slurry", () -> new LiquidBlock(IMFluids.RAW_URANIUM_TAILING_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    public static final DeferredBlock<LiquidBlock> RAW_ALUMINUM_SLURRY = registerNoItem("fluid/raw_aluminum_slurry", () -> new LiquidBlock(IMFluids.RAW_ALUMINUM_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_ALUMINUM_PROCESSED_SLURRY = registerNoItem("fluid/raw_aluminum_processed_slurry", () -> new LiquidBlock(IMFluids.RAW_ALUMINUM_PROCESSED_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_ALUMINUM_CONCENTRATE_SLURRY = registerNoItem("fluid/raw_aluminum_concentrate_slurry", () -> new LiquidBlock(IMFluids.RAW_ALUMINUM_CONCENTRATE_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> RAW_ALUMINUM_TAILING_SLURRY = registerNoItem("fluid/raw_aluminum_tailing_slurry", () -> new LiquidBlock(IMFluids.RAW_ALUMINUM_TAILING_SLURRY.getFlowing(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));


    private static final Supplier<BlockBehaviour.Properties> METAL_PROPERTIES_NO_OVERLAY =
            () -> Block.Properties.of()
                    .mapColor(MapColor.METAL)
                    .sound(SoundType.METAL)
                    .strength(3, 15)
                    .requiresCorrectToolForDrops()
                    .isViewBlocking((state, blockReader, pos) -> false);

    public static final Supplier<BlockBehaviour.Properties> METAL_PROPERTIES_NO_OCCLUSION = () -> METAL_PROPERTIES_NO_OVERLAY.get().noOcclusion().forceSolidOn();

    private static final Supplier<BlockBehaviour.Properties> METAL_PROPERTIES_DYNAMIC = () -> METAL_PROPERTIES_NO_OCCLUSION.get().dynamicShape();

    public static final DeferredBlock<ElectricCableBlock> ELECTRIC_CABLE_LV = registerBlockIM("electric_cable_lv", "electric_cable_lv.idle", () -> ElectricCableBlock.forLv(METAL_PROPERTIES_DYNAMIC.get()));
    public static final DeferredBlock<ElectricCableBlock> ELECTRIC_CABLE_MV = registerBlockIM("electric_cable_mv","electric_cable_mv.idle", () -> ElectricCableBlock.forMv(METAL_PROPERTIES_DYNAMIC.get()));
    public static final DeferredBlock<CastingChannelBlock> CASTING_CHANNEL = registerBlockIM("casting_channel","casting_channel.idle", () -> new CastingChannelBlock(METAL_PROPERTIES_DYNAMIC.get()));


    public static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block){
        DeferredBlock<T> toReturn = BLOCKS.register(name,block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    public static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block){
        IMItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static <T extends Block> DeferredBlock<T> registerBlockIE(String name, Supplier<T> block){
        DeferredBlock<T> toReturn = BLOCKS.register(name,block);
        registerBlockItemIE(name, toReturn);
        return toReturn;
    }

    public static <T extends Block> DeferredBlock<T> registerBlockIM(String name, String tooltip, Supplier<T> block){
        DeferredBlock<T> toReturn = BLOCKS.register(name,block);
        registerBlockItemIM(name, tooltip, toReturn);
        return toReturn;
    }

    public static <T extends Block> void registerBlockItemIE(String name, DeferredBlock<T> block){
        IMItems.ITEMS.register(name, () -> new BlockItemIE(block.get(), new Item.Properties()));
    }

    public static <T extends Block> void registerBlockItemIM(String name, String tooltip, DeferredBlock<T> block){
        IMItems.ITEMS.register(name, () -> new IMBaseBlockItem(block.get(), tooltip, new Item.Properties()));
    }

    private static <T extends Block> DeferredBlock<T> registerNoItem(String name, Supplier<T> block)
    {
        return BLOCKS.register(name, block);
    }

    public static void init(IEventBus modEventBus){
        BLOCKS.register(modEventBus);
    }
}
