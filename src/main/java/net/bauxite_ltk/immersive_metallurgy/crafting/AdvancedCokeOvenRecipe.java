package net.bauxite_ltk.immersive_metallurgy.crafting;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import com.google.common.collect.Lists;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.function.Supplier;

public class AdvancedCokeOvenRecipe extends MultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, IERecipeSerializer<AdvancedCokeOvenRecipe>> SERIALIZER;
    public static final CachedRecipeList<AdvancedCokeOvenRecipe> RECIPES = new CachedRecipeList<>(IMRecipeType.ADVANCED_COKE_OVEN);

    public static Supplier<RecipeMultiplier> MULTIPLIERS = () -> new RecipeMultiplier(()->1.0, ()->1.0);;


    public final FluidStack outputGas;
    public final FluidStack outputOil;

    public TagOutput outputItem;
    public IngredientWithSize inputItem;

    protected <T extends Recipe<?>> AdvancedCokeOvenRecipe(FluidStack outputOil, FluidStack outputGas,
                                                           TagOutput outputItem, IngredientWithSize inputItem,
                                                            int time) {
        super(TagOutput.EMPTY, IMRecipeType.ADVANCED_COKE_OVEN, time, time, MULTIPLIERS);
        this.outputOil = outputOil;
        this.outputGas = outputGas;
        this.inputItem = inputItem;
        this.outputItem = outputItem;

        setInputListWithSizes(Lists.newArrayList(this.inputItem));
        this.outputList = new TagOutputList(this.outputItem);


        this.fluidOutputList = Lists.newArrayList(this.outputOil);
        this.fluidOutputList.add(this.outputGas);

    }

    @Override
    protected IERecipeSerializer<?> getIESerializer() {
        return SERIALIZER.get();
    }

    public static RecipeHolder<AdvancedCokeOvenRecipe> findRecipe(Level level, ItemStack inputItem)
    {
        if(inputItem.isEmpty())
            return null;
        for(RecipeHolder<AdvancedCokeOvenRecipe> recipe : RECIPES.getRecipes(level)){
            if(recipe.value().inputItem!=null && recipe.value().inputItem.test(inputItem)){
                return recipe;
            }

        }
        return null;
    }

    public static boolean isInputValid(Level level, ItemStack inputItem)
    {
        if(inputItem.isEmpty())
            return false;
        for(RecipeHolder<AdvancedCokeOvenRecipe> recipe : RECIPES.getRecipes(level)){
            if(recipe.value().inputItem!=null && recipe.value().inputItem.getBaseIngredient().test(inputItem)){
                return true;
            }

        }
        return false;
    }


    @Override
    public int getMultipleProcessTicks() {
        return 0;
    }
}
