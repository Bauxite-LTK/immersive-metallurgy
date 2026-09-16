package net.bauxite_ltk.immersive_metallurgy.crafting;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.utils.codec.IEDualCodecs;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeMapCodecs;
import malte0811.dualcodecs.DualMapCodec;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.IMMultiblockLogic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class AdvancedCokeOvenRecipeSerializer extends IERecipeSerializer<AdvancedCokeOvenRecipe> {

    public static final DualMapCodec<RegistryFriendlyByteBuf, AdvancedCokeOvenRecipe> CODEC = DualCompositeMapCodecs.composite(
            optionalFluidOutput("result_oil"), f -> f.outputOil,
            optionalFluidOutput("result_gas"), f -> f.outputGas,
            TagOutput.CODECS.fieldOf("result_item"), f -> f.outputItem,
            IngredientWithSize.CODECS.fieldOf("input_item"), r -> r.inputItem,
            DualCodecs.INT.fieldOf("time"), MultiblockRecipe::getBaseTime,
            AdvancedCokeOvenRecipe::new
    );

    @Override
    public ItemStack getIcon() {
        return IMMultiblockLogic.ADVANCED_COKE_OVEN.iconStack();
    }

    @Override
    protected DualMapCodec<RegistryFriendlyByteBuf, AdvancedCokeOvenRecipe> codecs() {
        return CODEC;
    }
}
