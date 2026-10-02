package net.bauxite_ltk.immersive_metallurgy.compat.jei;

import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.IMMultiblockLogic;
import net.bauxite_ltk.immersive_metallurgy.crafting.AdvancedCokeOvenRecipe;
import net.bauxite_ltk.immersive_metallurgy.gui.info.IMFuelInfoArea;
import net.bauxite_ltk.immersive_metallurgy.util.Helper;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.Arrays;

public class AdvancedCokeOvenCategory extends IERecipeCategory<AdvancedCokeOvenRecipe> {


    public AdvancedCokeOvenCategory(IGuiHelper helper) {
        super(helper, JEIRecipeTypes.ADVANCED_COKE_OVEN, "block.immersive_metallurgy.advanced_coke_oven");
        ResourceLocation background = IMUtils.modRL("textures/gui/advanced_coke_oven_recipe.png");
        setBackground(helper.createDrawable(background, 0, 0, 90, 62));
        setIcon(IMMultiblockLogic.ADVANCED_COKE_OVEN.iconStack());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AdvancedCokeOvenRecipe recipe, IFocusGroup focuses)
    {


        builder.addSlot(RecipeIngredientRole.INPUT, 8, 23)
                .addItemStacks(Arrays.asList(recipe.inputItem.getMatchingStacks()));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 48, 23)
                .addItemStack(recipe.outputItem.get());

        int oilTankSize = Math.max(FluidType.BUCKET_VOLUME*5, recipe.outputOil.getAmount());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 69, 5)
                .setFluidRenderer(oilTankSize, false, 4, 52)
                .addIngredient(NeoForgeTypes.FLUID_STACK, recipe.outputOil)
                .addRichTooltipCallback(JEIHelper.fluidTooltipCallback);

        int gasTankSize = Math.max(FluidType.BUCKET_VOLUME*5, recipe.outputGas.getAmount());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 79, 5)
                .setFluidRenderer(gasTankSize, false, 4, 52)
                .addIngredient(NeoForgeTypes.FLUID_STACK, recipe.outputGas)
                .addRichTooltipCallback(JEIHelper.fluidTooltipCallback);

    }

    @Override
    public void draw(AdvancedCokeOvenRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IDrawable background = this.getBackground();
        int bWidth = background.getWidth();
        int bHeight = background.getHeight();
        Font font = Minecraft.getInstance().font;

        float timeSec = (float)recipe.getTotalProcessTime() / 20f;

        guiGraphics.pose().pushPose();
        {
            //guiGraphics.pose().translate(-8, 0, 0);

            String text = I18n.get("desc.immersive_metallurgy.info.secs", Helper.fDecimal(timeSec));
            guiGraphics.drawString(font, text, (33*2 - font.width(text))/2,  bHeight - font.lineHeight - font.lineHeight/2 , 0xFFCCCCCC, true);
        }
        guiGraphics.pose().popPose();
    }
}
