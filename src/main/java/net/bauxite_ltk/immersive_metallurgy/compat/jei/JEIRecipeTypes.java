package net.bauxite_ltk.immersive_metallurgy.compat.jei;

import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import net.bauxite_ltk.immersive_metallurgy.crafting.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class JEIRecipeTypes {
    public static final RecipeType<RecipeHolder<BallMillRecipe>> BALL_MILL = create(IMRecipeType.BALL_MILL);
    public static final RecipeType<RecipeHolder<FlotationCellRecipe>> FLOTATION_CELL = create(IMRecipeType.FLOTATION_CELL);
    public static final RecipeType<RecipeHolder<HydrocycloneRecipe>> HYDROCYCLONE = create(IMRecipeType.HYDROCYCLONE);
    public static final RecipeType<RecipeHolder<ThickenerRecipe>> THICKENER = create(IMRecipeType.THICKENER);
    public static final RecipeType<RecipeHolder<EliteBlastFurnaceRecipe>> ELITE_BLAST_FURNACE = create(IMRecipeType.ELITE_BLAST_FURNACE);
    public static final RecipeType<RecipeHolder<HotAirFurnaceRecipe>> HOT_AIR_FURNACE = create(IMRecipeType.HOT_AIR_FURNACE);
    public static final RecipeType<RecipeHolder<ContinuousCastingMachineRecipe>> CONTINUOUS_CASTING_MACHINE = create(IMRecipeType.CONTINUOUS_CASTING_MACHINE);
    public static final RecipeType<RecipeHolder<AdvancedCokeOvenRecipe>> ADVANCED_COKE_OVEN = create(IMRecipeType.ADVANCED_COKE_OVEN);
    public static final RecipeType<RecipeHolder<GasFuelRecipe>> GAS_FUEL = create(IMRecipeType.GAS_FUEL);


    private static <T extends Recipe<?>>
    RecipeType<RecipeHolder<T>> create(IERecipeTypes.TypeWithClass<T> type)
    {
        return RecipeType.createFromVanilla(type.get());
    }

    private static <T extends Recipe<?>>
    RecipeType<RecipeHolder<T>> createManual(ResourceLocation uid)
    {
        Class<? extends RecipeHolder<T>> holderClass = (Class)RecipeHolder.class;
        return new RecipeType<>(uid, holderClass);
    }
}
