package net.bauxite_ltk.immersive_metallurgy.compat.jei;

import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import blusunrize.immersiveengineering.common.util.compat.jei.IEFluidTooltipCallback;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.IMMultiblockLogic;
import net.bauxite_ltk.immersive_metallurgy.crafting.*;
import net.bauxite_ltk.immersive_metallurgy.gui.multiblock.*;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

@JeiPlugin
public class JEIHelper implements IModPlugin
{
    private static final ResourceLocation UID = IMUtils.modRL("main");
    //public static final ResourceLocation JEI_GUI = IEApi.ieLoc("textures/gui/jei_elements.png");
    public static IDrawableStatic slotDrawable;
    public static IRecipeSlotRichTooltipCallback fluidTooltipCallback = new IEFluidTooltipCallback();

    @Override
    public ResourceLocation getPluginUid()
    {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry)
    {
        //Recipes
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(
                new BallMillCategory(guiHelper),
                new FlotationCellCategory(guiHelper),
                new HydrocycloneCategory(guiHelper),
                new ThickenerCategory(guiHelper),
                new EliteBlastFurnaceCategory(guiHelper),
                new HotAirFurnaceCategory(guiHelper),
                new ContinuousCastingMachineCategory(guiHelper),
                new GasFuelCategory(guiHelper),
                new AdvancedCokeOvenCategory(guiHelper)
        );

        slotDrawable = guiHelper.getSlotDrawable();
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration)
    {
        registration.addRecipes(JEIRecipeTypes.BALL_MILL, getRecipes(BallMillRecipe.RECIPES));
        registration.addRecipes(JEIRecipeTypes.FLOTATION_CELL, getRecipes(FlotationCellRecipe.RECIPES));
        registration.addRecipes(JEIRecipeTypes.HYDROCYCLONE, getRecipes(HydrocycloneRecipe.RECIPES));
        registration.addRecipes(JEIRecipeTypes.THICKENER, getRecipes(ThickenerRecipe.RECIPES));
        registration.addRecipes(JEIRecipeTypes.ELITE_BLAST_FURNACE, getRecipes(EliteBlastFurnaceRecipe.RECIPES));
        registration.addRecipes(JEIRecipeTypes.HOT_AIR_FURNACE, getRecipes(HotAirFurnaceRecipe.RECIPES));
        registration.addRecipes(JEIRecipeTypes.CONTINUOUS_CASTING_MACHINE, getRecipes(ContinuousCastingMachineRecipe.RECIPES));
        registration.addRecipes(JEIRecipeTypes.GAS_FUEL, getRecipes(GasFuelRecipe.RECIPES));
        registration.addRecipes(JEIRecipeTypes.ADVANCED_COKE_OVEN, getRecipes(AdvancedCokeOvenRecipe.RECIPES));

    }

    private <T extends Recipe<?>> List<RecipeHolder<T>> getRecipes(CachedRecipeList<T> cachedList)
    {
        return getFiltered(cachedList, $ -> true);
    }

    private <T extends Recipe<?>> List<RecipeHolder<T>> getFiltered(CachedRecipeList<T> cachedList, Predicate<T> include)
    {
        return getFilteredAndSorted(cachedList, include, null);
    }

    private <T extends Recipe<?>> List<RecipeHolder<T>> getFilteredAndSorted(CachedRecipeList<T> cachedList, Predicate<T> include, @Nullable Comparator<RecipeHolder<T>> sorting)
    {
        Stream<RecipeHolder<T>> ret = cachedList.getRecipes(Minecraft.getInstance().level).stream()
                .filter(h -> include.test(h.value()));
        if(sorting!=null)
            ret = ret.sorted(sorting);
        return ret.toList();
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
    {
        registration.addRecipeCatalyst(IMMultiblockLogic.BALL_MILL.iconStack(), JEIRecipeTypes.BALL_MILL);
        registration.addRecipeCatalyst(IMMultiblockLogic.FLOTATION_CELL.iconStack(), JEIRecipeTypes.FLOTATION_CELL);
        registration.addRecipeCatalyst(IMMultiblockLogic.HYDROCYCLONE.iconStack(), JEIRecipeTypes.HYDROCYCLONE);
        registration.addRecipeCatalyst(IMMultiblockLogic.THICKENER.iconStack(), JEIRecipeTypes.THICKENER);
        registration.addRecipeCatalyst(IMMultiblockLogic.ELITE_BLAST_FURNACE.iconStack(), JEIRecipeTypes.ELITE_BLAST_FURNACE);
        registration.addRecipeCatalyst(IMMultiblockLogic.HOT_AIR_FURNACE.iconStack(), JEIRecipeTypes.HOT_AIR_FURNACE);
        registration.addRecipeCatalyst(IMMultiblockLogic.CONTINUOUS_CASTING_MACHINE.iconStack(), JEIRecipeTypes.CONTINUOUS_CASTING_MACHINE);
        registration.addRecipeCatalyst(IMMultiblockLogic.CONTINUOUS_CASTING_MACHINE.iconStack(), JEIRecipeTypes.GAS_FUEL);
        registration.addRecipeCatalyst(IMMultiblockLogic.ADVANCED_COKE_OVEN.iconStack(), JEIRecipeTypes.ADVANCED_COKE_OVEN);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration)
    {
        registration.addRecipeClickArea(BallMillScreen.class, 36, 57, 72, 13, JEIRecipeTypes.BALL_MILL);
        registration.addRecipeClickArea(FlotationCellScreen.class, 153, 59, 16, 14, JEIRecipeTypes.FLOTATION_CELL);
        registration.addRecipeClickArea(HydrocycloneScreen.class, 96, 38, 17, 13, JEIRecipeTypes.HYDROCYCLONE);
        registration.addRecipeClickArea(ThickenerScreen.class, 153, 59, 16, 14, JEIRecipeTypes.THICKENER);
        registration.addRecipeClickArea(EliteBlastFurnaceScreen.class, 40, 10, 16, 14, JEIRecipeTypes.ELITE_BLAST_FURNACE);
        registration.addRecipeClickArea(HotAirFurnaceScreen.class, 53, 35, 15, 13, JEIRecipeTypes.HOT_AIR_FURNACE);
        registration.addRecipeClickArea(ContinuousCastingMachineScreen.class, 133, 13, 15, 13, JEIRecipeTypes.CONTINUOUS_CASTING_MACHINE);
        registration.addRecipeClickArea(ContinuousCastingMachineScreen.class, 116, 13, 12, 10, JEIRecipeTypes.GAS_FUEL);
        for(int i = 0; i < AdvancedCokeOvenMenu.COKE_OVEN_THREAD_COUNT; i++) {
            registration.addRecipeClickArea(AdvancedCokeOvenScreen.class, 21 + i*40, 30, 14, 17, JEIRecipeTypes.ADVANCED_COKE_OVEN);
        }
    }
}
