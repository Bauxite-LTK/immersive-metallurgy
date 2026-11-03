package net.bauxite_ltk.immersive_metallurgy.worldgen;

import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class IMBiomeModifiers {
    public static final ResourceKey<BiomeModifier> ADD_MASON_PINE = registerKey("add_mason_pine");
    public static final ResourceKey<BiomeModifier> ADD_PLATINUM_ORE = registerKey("add_platinum_ore");

    public static void bootstrap(BootstrapContext<BiomeModifier> context){
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);

        context.register(ADD_PLATINUM_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(IMPlacedFeatures.PLATINUM_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));


        context.register(ADD_MASON_PINE, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.OLD_GROWTH_PINE_TAIGA), biomes.getOrThrow(Biomes.OLD_GROWTH_SPRUCE_TAIGA)),
                HolderSet.direct(placedFeatures.getOrThrow(IMPlacedFeatures.MASON_PINE_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION
        ));
    }

    public static ResourceKey<BiomeModifier> registerKey(String name){
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, IMUtils.modRL(name));
    }
}
