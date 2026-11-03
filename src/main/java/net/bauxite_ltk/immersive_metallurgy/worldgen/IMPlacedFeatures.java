package net.bauxite_ltk.immersive_metallurgy.worldgen;

import net.bauxite_ltk.immersive_metallurgy.block.IMBlocks;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.bauxite_ltk.immersive_metallurgy.worldgen.ore.IMOreReplacement;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.List;

public class IMPlacedFeatures {
    public static final ResourceKey<PlacedFeature> MASON_PINE_PLACED_KEY = registerKey("mason_pine_placed");
    public static final ResourceKey<PlacedFeature> PLATINUM_ORE_PLACED_KEY = registerKey("platinum_ore_placed");

    public static void bootstrap(BootstrapContext<PlacedFeature> context){
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        register(context, PLATINUM_ORE_PLACED_KEY, configuredFeatures.getOrThrow(IMConfiguredFeatures.OVERWORLD_PLATINUM_ORE_KEY),
                IMOreReplacement.commonOrePlacement(12, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-64), VerticalAnchor.aboveBottom(64)))
                );

        register(context, MASON_PINE_PLACED_KEY, configuredFeatures.getOrThrow(IMConfiguredFeatures.MASON_PINE_KEY),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(1,0.05f,1),
                        IMBlocks.MASON_PINE_SAPLING.get()
                        ));
    }

    public static ResourceKey<PlacedFeature> registerKey(String name){
        return ResourceKey.create(Registries.PLACED_FEATURE, IMUtils.modRL(name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>>
    void register(BootstrapContext<PlacedFeature> context,
                  ResourceKey<PlacedFeature> key,
                  Holder<ConfiguredFeature<?,?>> configuration,
                  List<PlacementModifier> modifiers){
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}
