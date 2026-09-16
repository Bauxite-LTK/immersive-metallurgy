package net.bauxite_ltk.immersive_metallurgy.crafting;

import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IMRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(
            BuiltInRegistries.RECIPE_SERIALIZER, IMUtils.MOD_ID
    );

    static {
        BallMillRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
                "ball_mill", BallMillRecipeSerializer::new
        );

        FlotationCellRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
                "flotation_cell", FlotationCellRecipeSerializer::new
        );

        HydrocycloneRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
                "hydrocyclone", HydrocycloneRecipeSerializer::new
        );

        ThickenerRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
                "thickener", ThickenerRecipeSerializer::new
        );

        EliteBlastFurnaceRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
                "elite_blast_furnace", EliteBlastFurnaceRecipeSerializer::new
        );

        HotAirFurnaceRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
                "hot_air_furnace", HotAirFurnaceRecipeSerializer::new
        );

        ContinuousCastingMachineRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
                "continuous_casting_machine", ContinuousCastingMachineRecipeSerializer::new
        );

        GasFuelRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
                "gas_fuel", GasFuelRecipeSerializer::new
        );

        AdvancedCokeOvenRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
                "advanced_coke_oven", AdvancedCokeOvenRecipeSerializer::new
        );
    }

    public static void init(IEventBus modEventBus){
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}
