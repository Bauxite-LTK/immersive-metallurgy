package net.bauxite_ltk.immersive_metallurgy.fluid;

import com.google.common.base.Suppliers;
import net.bauxite_ltk.immersive_metallurgy.block.IMBlocks;
import net.bauxite_ltk.immersive_metallurgy.item.IMItems;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.*;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class IMFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, IMUtils.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, IMUtils.MOD_ID);

    public static final FluidHolder<BaseFlowingFluid> MOLTEN_PIG_IRON =
            registerMolten("pig_iron",
                    IMBlocks.MOLTEN_PIG_IRON,
                    IMItems.MOLTEN_PIG_IRON_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> MOLTEN_GOLD =
            registerMolten("gold",
                    IMBlocks.MOLTEN_GOLD,
                    IMItems.MOLTEN_GOLD_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> MOLTEN_COPPER =
            registerMolten("copper",
                    IMBlocks.MOLTEN_COPPER,
                    IMItems.MOLTEN_COPPER_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> MOLTEN_SILVER =
            registerMolten("silver",
                    IMBlocks.MOLTEN_SILVER,
                    IMItems.MOLTEN_SILVER_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> MOLTEN_LEAD =
            registerMolten("lead",
                    IMBlocks.MOLTEN_LEAD,
                    IMItems.MOLTEN_LEAD_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> MOLTEN_NICKEL =
            registerMolten("nickel",
                    IMBlocks.MOLTEN_NICKEL,
                    IMItems.MOLTEN_NICKEL_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> MOLTEN_URANIUM =
            registerMolten("uranium",
                    IMBlocks.MOLTEN_URANIUM,
                    IMItems.MOLTEN_URANIUM_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> MOLTEN_ALUMINUM =
            registerMolten("aluminum",
                    IMBlocks.MOLTEN_ALUMINUM,
                    IMItems.MOLTEN_ALUMINUM_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> HOT_AIR =
            register(
                    "hot_air",
                    properties -> properties
                            .block(IMBlocks.HOT_AIR)
                            .bucket(IMItems.HOT_AIR_BUCKET),
                    gasLike()
                            .descriptionId("fluid.immersive_metallurgy.hot_air"),
                    BaseFlowingFluid.Source::new,
                    BaseFlowingFluid.Flowing::new
            );

    public static final FluidHolder<BaseFlowingFluid> WATER_GAS =
            register(
                    "water_gas",
                    properties -> properties
                            .block(IMBlocks.WATER_GAS)
                            .bucket(IMItems.WATER_GAS_BUCKET),
                    gasLike()
                            .descriptionId("fluid.immersive_metallurgy.water_gas"),
                    BaseFlowingFluid.Source::new,
                    BaseFlowingFluid.Flowing::new
            );

    public static final FluidHolder<BaseFlowingFluid> BLAST_FURNACE_GAS  =
            register(
                    "blast_furnace_gas",
                    properties -> properties
                            .block(IMBlocks.BLAST_FURNACE_GAS)
                            .bucket(IMItems.BLAST_FURNACE_GAS_BUCKET),
                    gasLike()
                            .descriptionId("fluid.immersive_metallurgy.blast_furnace_gas"),
                    BaseFlowingFluid.Source::new,
                    BaseFlowingFluid.Flowing::new
            );

    public static final FluidHolder<BaseFlowingFluid> COKE_OVEN_GAS  =
            register(
                    "coke_oven_gas",
                    properties -> properties
                            .block(IMBlocks.COKE_OVEN_GAS)
                            .bucket(IMItems.COKE_OVEN_GAS_BUCKET),
                    gasLike()
                            .descriptionId("fluid.immersive_metallurgy.coke_oven_gas"),
                    BaseFlowingFluid.Source::new,
                    BaseFlowingFluid.Flowing::new
            );


    public static final FluidHolder<BaseFlowingFluid> MASON_PINE_SAP =
            register(
                    "mason_pine_sap",
                    properties -> properties
                            .block(IMBlocks.MASON_PINE_SAP)
                            .bucket(IMItems.MASON_PINE_SAP_BUCKET),
                    waterLike()
                            .descriptionId("fluid.immersive_metallurgy.mason_pine_sap"),
                    BaseFlowingFluid.Source::new,
                    BaseFlowingFluid.Flowing::new
            );

    public static final FluidHolder<BaseFlowingFluid> TURPENTINE_OIL =
            register(
                    "turpentine_oil",
                    properties -> properties
                            .block(IMBlocks.TURPENTINE_OIL)
                            .bucket(IMItems.TURPENTINE_OIL_BUCKET),
                    waterLike()
                            .descriptionId("fluid.immersive_metallurgy.turpentine_oil"),
                    BaseFlowingFluid.Source::new,
                    BaseFlowingFluid.Flowing::new
            );

    public static final FluidHolder<BaseFlowingFluid> TERPENIC_OIL =
            register(
                    "terpenic_oil",
                    properties -> properties
                            .block(IMBlocks.TERPENIC_OIL)
                            .bucket(IMItems.TERPENIC_OIL_BUCKET),
                    waterLike()
                            .descriptionId("fluid.immersive_metallurgy.terpenic_oil"),
                    BaseFlowingFluid.Source::new,
                    BaseFlowingFluid.Flowing::new
            );

    /*
     * ----------------------------------------------------
     * Ore Slurry : Raw Iron
     * ----------------------------------------------------
     */
    public static final FluidHolder<BaseFlowingFluid> RAW_IRON_SLURRY =
            registerOreSlurry("raw_iron",
                    IMBlocks.RAW_IRON_SLURRY,
                    IMItems.RAW_IRON_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_IRON_PROCESSED_SLURRY =
            registerOreSlurry("raw_iron_processed",
                    IMBlocks.RAW_IRON_PROCESSED_SLURRY,
                    IMItems.RAW_IRON_PROCESSED_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_IRON_CONCENTRATE_SLURRY =
            registerOreSlurry("raw_iron_concentrate",
                    IMBlocks.RAW_IRON_CONCENTRATE_SLURRY,
                    IMItems.RAW_IRON_CONCENTRATE_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_IRON_TAILING_SLURRY =
            registerOreSlurry("raw_iron_tailing",
                    IMBlocks.RAW_IRON_TAILING_SLURRY,
                    IMItems.RAW_IRON_TAILING_SLURRY_BUCKET);

    
    /*
     * ----------------------------------------------------
     * Ore Slurry : Raw Gold
     * ----------------------------------------------------
     */
    public static final FluidHolder<BaseFlowingFluid> RAW_GOLD_SLURRY =
            registerOreSlurry("raw_gold",
                    IMBlocks.RAW_GOLD_SLURRY,
                    IMItems.RAW_GOLD_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_GOLD_PROCESSED_SLURRY =
            registerOreSlurry("raw_gold_processed",
                    IMBlocks.RAW_GOLD_PROCESSED_SLURRY,
                    IMItems.RAW_GOLD_PROCESSED_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_GOLD_CONCENTRATE_SLURRY =
            registerOreSlurry("raw_gold_concentrate",
                    IMBlocks.RAW_GOLD_CONCENTRATE_SLURRY,
                    IMItems.RAW_GOLD_CONCENTRATE_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_GOLD_TAILING_SLURRY =
            registerOreSlurry("raw_gold_tailing",
                    IMBlocks.RAW_GOLD_TAILING_SLURRY,
                    IMItems.RAW_GOLD_TAILING_SLURRY_BUCKET);



    /*
     * ----------------------------------------------------
     * Ore Slurry : Raw Copper
     * ----------------------------------------------------
     */
    public static final FluidHolder<BaseFlowingFluid> RAW_COPPER_SLURRY =
            registerOreSlurry("raw_copper",
                    IMBlocks.RAW_COPPER_SLURRY,
                    IMItems.RAW_COPPER_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_COPPER_PROCESSED_SLURRY =
            registerOreSlurry("raw_copper_processed",
                    IMBlocks.RAW_COPPER_PROCESSED_SLURRY,
                    IMItems.RAW_COPPER_PROCESSED_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_COPPER_CONCENTRATE_SLURRY =
            registerOreSlurry("raw_copper_concentrate",
                    IMBlocks.RAW_COPPER_CONCENTRATE_SLURRY,
                    IMItems.RAW_COPPER_CONCENTRATE_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_COPPER_TAILING_SLURRY =
            registerOreSlurry("raw_copper_tailing",
                    IMBlocks.RAW_COPPER_TAILING_SLURRY,
                    IMItems.RAW_COPPER_TAILING_SLURRY_BUCKET);


    /*
     * ----------------------------------------------------
     * Ore Slurry : Raw Silver
     * ----------------------------------------------------
     */
    public static final FluidHolder<BaseFlowingFluid> RAW_SILVER_SLURRY =
            registerOreSlurry("raw_silver",
                    IMBlocks.RAW_SILVER_SLURRY,
                    IMItems.RAW_SILVER_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_SILVER_PROCESSED_SLURRY =
            registerOreSlurry("raw_silver_processed",
                    IMBlocks.RAW_SILVER_PROCESSED_SLURRY,
                    IMItems.RAW_SILVER_PROCESSED_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_SILVER_CONCENTRATE_SLURRY =
            registerOreSlurry("raw_silver_concentrate",
                    IMBlocks.RAW_SILVER_CONCENTRATE_SLURRY,
                    IMItems.RAW_SILVER_CONCENTRATE_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_SILVER_TAILING_SLURRY =
            registerOreSlurry("raw_silver_tailing",
                    IMBlocks.RAW_SILVER_TAILING_SLURRY,
                    IMItems.RAW_SILVER_TAILING_SLURRY_BUCKET);


    /*
     * ----------------------------------------------------
     * Ore Slurry : Raw Lead
     * ----------------------------------------------------
     */
    public static final FluidHolder<BaseFlowingFluid> RAW_LEAD_SLURRY =
            registerOreSlurry("raw_lead",
                    IMBlocks.RAW_LEAD_SLURRY,
                    IMItems.RAW_LEAD_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_LEAD_PROCESSED_SLURRY =
            registerOreSlurry("raw_lead_processed",
                    IMBlocks.RAW_LEAD_PROCESSED_SLURRY,
                    IMItems.RAW_LEAD_PROCESSED_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_LEAD_CONCENTRATE_SLURRY =
            registerOreSlurry("raw_lead_concentrate",
                    IMBlocks.RAW_LEAD_CONCENTRATE_SLURRY,
                    IMItems.RAW_LEAD_CONCENTRATE_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_LEAD_TAILING_SLURRY =
            registerOreSlurry("raw_lead_tailing",
                    IMBlocks.RAW_LEAD_TAILING_SLURRY,
                    IMItems.RAW_LEAD_TAILING_SLURRY_BUCKET);


    /*
     * ----------------------------------------------------
     * Ore Slurry : Raw Nickel
     * ----------------------------------------------------
     */
    public static final FluidHolder<BaseFlowingFluid> RAW_NICKEL_SLURRY =
            registerOreSlurry("raw_nickel",
                    IMBlocks.RAW_NICKEL_SLURRY,
                    IMItems.RAW_NICKEL_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_NICKEL_PROCESSED_SLURRY =
            registerOreSlurry("raw_nickel_processed",
                    IMBlocks.RAW_NICKEL_PROCESSED_SLURRY,
                    IMItems.RAW_NICKEL_PROCESSED_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_NICKEL_CONCENTRATE_SLURRY =
            registerOreSlurry("raw_nickel_concentrate",
                    IMBlocks.RAW_NICKEL_CONCENTRATE_SLURRY,
                    IMItems.RAW_NICKEL_CONCENTRATE_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_NICKEL_TAILING_SLURRY =
            registerOreSlurry("raw_nickel_tailing",
                    IMBlocks.RAW_NICKEL_TAILING_SLURRY,
                    IMItems.RAW_NICKEL_TAILING_SLURRY_BUCKET);


    /*
     * ----------------------------------------------------
     * Ore Slurry : Raw Uranium
     * ----------------------------------------------------
     */
    public static final FluidHolder<BaseFlowingFluid> RAW_URANIUM_SLURRY =
            registerOreSlurry("raw_uranium",
                    IMBlocks.RAW_URANIUM_SLURRY,
                    IMItems.RAW_URANIUM_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_URANIUM_PROCESSED_SLURRY =
            registerOreSlurry("raw_uranium_processed",
                    IMBlocks.RAW_URANIUM_PROCESSED_SLURRY,
                    IMItems.RAW_URANIUM_PROCESSED_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_URANIUM_CONCENTRATE_SLURRY =
            registerOreSlurry("raw_uranium_concentrate",
                    IMBlocks.RAW_URANIUM_CONCENTRATE_SLURRY,
                    IMItems.RAW_URANIUM_CONCENTRATE_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_URANIUM_TAILING_SLURRY =
            registerOreSlurry("raw_uranium_tailing",
                    IMBlocks.RAW_URANIUM_TAILING_SLURRY,
                    IMItems.RAW_URANIUM_TAILING_SLURRY_BUCKET);


    /*
     * ----------------------------------------------------
     * Ore Slurry : Raw Aluminum
     * ----------------------------------------------------
     */
    public static final FluidHolder<BaseFlowingFluid> RAW_ALUMINUM_SLURRY =
            registerOreSlurry("raw_aluminum",
                    IMBlocks.RAW_ALUMINUM_SLURRY,
                    IMItems.RAW_ALUMINUM_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_ALUMINUM_PROCESSED_SLURRY =
            registerOreSlurry("raw_aluminum_processed",
                    IMBlocks.RAW_ALUMINUM_PROCESSED_SLURRY,
                    IMItems.RAW_ALUMINUM_PROCESSED_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_ALUMINUM_CONCENTRATE_SLURRY =
            registerOreSlurry("raw_aluminum_concentrate",
                    IMBlocks.RAW_ALUMINUM_CONCENTRATE_SLURRY,
                    IMItems.RAW_ALUMINUM_CONCENTRATE_SLURRY_BUCKET);

    public static final FluidHolder<BaseFlowingFluid> RAW_ALUMINUM_TAILING_SLURRY =
            registerOreSlurry("raw_aluminum_tailing",
                    IMBlocks.RAW_ALUMINUM_TAILING_SLURRY,
                    IMItems.RAW_ALUMINUM_TAILING_SLURRY_BUCKET);

    private static FluidType.Properties waterLike()
    {
        return FluidType.Properties.create()
                .adjacentPathType(PathType.WATER)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .canConvertToSource(false)
                .canDrown(true)
                .canExtinguish(true)
                //.canHydrate(true)
                .canPushEntity(true)
                .canSwim(true)
                .supportsBoating(true);
    }

    private static FluidType.Properties lavaLike()
    {
        return FluidType.Properties.create()
                .adjacentPathType(PathType.LAVA)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                .lightLevel(15)
                .density(7000)
                .viscosity(6000)
                .temperature(1300)
                .canConvertToSource(false)
                .canDrown(false)
                .canExtinguish(false)
                .canHydrate(false)
                .canPushEntity(false)
                .canSwim(false)
                .supportsBoating(false);
    }


    private static FluidType.Properties gasLike()
    {
        return FluidType.Properties.create()
                .adjacentPathType(PathType.OPEN)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .density(-10)
                .viscosity(1000)
                .temperature(300)
                .canConvertToSource(false)
                .canDrown(true)
                .canExtinguish(false)
                .canHydrate(false)
                .canPushEntity(false)
                .canSwim(false)
                .supportsBoating(false)
                .fallDistanceModifier(1.0f);
    }


    public static < L extends LiquidBlock, I extends Item>
    FluidHolder<BaseFlowingFluid> registerOreSlurry(
            String oreName, DeferredBlock<L> liquidBlock, DeferredItem<I> bucketItem)
    {
        return register(
                oreName + "_slurry",
                properties -> properties
                        .block(liquidBlock)
                        .bucket(bucketItem),
                waterLike()
                        .descriptionId("fluid.immersive_metallurgy." + oreName + "_slurry"),
                BaseFlowingFluid.Source::new,
                BaseFlowingFluid.Flowing::new);
    }

    public static < L extends LiquidBlock, I extends Item>
    FluidHolder<BaseFlowingFluid> registerMolten(
            String originalName, DeferredBlock<L> liquidBlock, DeferredItem<I> bucketItem)
    {
        return register(
                "molten_" + originalName,
                properties -> properties
                        .block(liquidBlock)
                        .bucket(bucketItem),
                lavaLike()
                        .descriptionId("fluid.immersive_metallurgy." + "molten_" + originalName),
                MoltenFluid.Source::new,
                MoltenFluid.Flowing::new);
    }


    private static <F extends FlowingFluid> FluidHolder<F> register(String name, Consumer<BaseFlowingFluid.Properties> builder, FluidType.Properties typeProperties, Function<BaseFlowingFluid.Properties, F> sourceFactory, Function<BaseFlowingFluid.Properties, F> flowingFactory)
    {
        // Names `metal/foo` to `metal/flowing_foo`
        final int index = name.lastIndexOf('/');
        final String flowingName = index == -1 ? "flowing_" + name : name.substring(0, index) + "/flowing_" + name.substring(index + 1);

        return registerFluid(FLUID_TYPES, FLUIDS, name, name, flowingName, builder, () -> new FluidType(typeProperties), sourceFactory, flowingFactory);
    }

    public static <F extends FlowingFluid> FluidHolder<F> registerFluid(
            DeferredRegister<FluidType> fluidTypes,
            DeferredRegister<Fluid> fluids,
            String typeName,
            String sourceName,
            String flowingName,
            Consumer<BaseFlowingFluid.Properties> builder,
            Supplier<FluidType> typeFactory,
            Function<BaseFlowingFluid.Properties, F> sourceFactory,
            Function<BaseFlowingFluid.Properties, F> flowingFactory)
    {
        // The type need a reference to both source and flowing
        // In addition, the properties' builder cannot be invoked statically, as it has hard references to registry objects, which may not be populated based on class load order - it must be invoked at registration time.
        // So, first we prepare the source and flowing registry objects, referring to the properties box (which will be opened during registration, which is ok)
        // Then, we populate the properties box lazily, (since it's a mutable lazy), so the properties inside are only constructed when the box is opened (again, during registration)
        final Mutable<Supplier<BaseFlowingFluid.Properties>> typeBox = new MutableObject<>();
        final DeferredHolder<Fluid, F> source = fluids.register(sourceName, () -> sourceFactory.apply(typeBox.getValue().get()));
        final DeferredHolder<Fluid, F> flowing = fluids.register(flowingName, () -> flowingFactory.apply(typeBox.getValue().get()));

        final DeferredHolder<FluidType, FluidType> fluidType = fluidTypes.register(typeName, typeFactory);

        typeBox.setValue(Suppliers.memoize(() -> {
            final BaseFlowingFluid.Properties lazyProperties = new BaseFlowingFluid.Properties(fluidType, source, flowing);
            builder.accept(lazyProperties);
            return lazyProperties;
        }));

        return new FluidHolder<>(fluidType, flowing, source);
    }

    public static void init(IEventBus modEventBus){
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
    }
}
