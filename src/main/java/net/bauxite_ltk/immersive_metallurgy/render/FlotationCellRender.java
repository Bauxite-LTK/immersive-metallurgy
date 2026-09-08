package net.bauxite_ltk.immersive_metallurgy.render;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.client.render.tile.BERenderUtils;
import blusunrize.immersiveengineering.client.render.tile.IEMultiblockRenderer;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.logic.FlotationCellLogic;
import net.bauxite_ltk.immersive_metallurgy.render.utils.IMAnimationHelper;
import net.bauxite_ltk.immersive_metallurgy.util.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class FlotationCellRender extends IEMultiblockRenderer<FlotationCellLogic.State> {
    public static final String NAME = "flotation_cell_blade";
    public static IMDynamicModel BLADE;
    private FluidStack currentOutput = null;


    @Override
    public void render(@NotNull IMultiblockContext<FlotationCellLogic.State> ctx, float partialTicks, @NotNull PoseStack matrixStack, @NotNull MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = BLADE.get();
        final MultiblockOrientation orientation = ctx.getLevel().getOrientation();

        matrixStack.pushPose();
        bufferIn = BERenderUtils.mirror(orientation, matrixStack, bufferIn);
        VertexConsumer buffer = bufferIn.getBuffer(RenderType.solid());
        rotateForFacing(matrixStack, orientation.front());

        boolean active = ctx.getState().isActive();
        float bladeAngle = ctx.getState().getBladeAngle()+ (active? 9 * partialTicks: 0);

        IMAnimationHelper.applyRotationX(27,31,-bladeAngle,matrixStack);



        blockRenderer.getModelRenderer().renderModel(
                matrixStack.last(), buffer, null, model,
                1, 1, 1,
                combinedLightIn, combinedOverlayIn, ModelData.EMPTY, RenderType.solid()
        );

        matrixStack.popPose();


        FlotationCellLogic.FlotationCellTanks tanks = ctx.getState().tanks;
        FluidStack tailing = tanks.outputTailing().getFluid();
        FluidStack concentrate = tanks.outputConcentrate().getFluid();
        FluidStack ore = tanks.inputOre().getFluid();

        if(!concentrate.isEmpty()) {
            if(currentOutput==null){
                currentOutput = new FluidStack(concentrate.getFluid(),1000);
            }
            else if(!currentOutput.getFluid().equals(concentrate.getFluid())){
                currentOutput = new FluidStack(concentrate.getFluid(),1000);
            }
        }

        int maxHeight = 24;
        float capacity = (float)(tanks.inputOre().getCapacity() + tanks.outputConcentrate().getCapacity() + tanks.outputTailing().getCapacity());
        float tailingHeight = 1 + (float) maxHeight * tailing.getAmount() / capacity;
        float oreHeight = 1 + tailingHeight + (float) maxHeight * ore.getAmount() / capacity;
        float concentrateHeight = 1 + oreHeight + (float) maxHeight * Math.max(concentrate.getAmount(),1000) / capacity;

//        TFCTrihydrate.LOGGER.info("OreHeight: " + oreHeight);
//        TFCTrihydrate.LOGGER.info("Ore: " + ore);

        if(!tailing.isEmpty()){
            renderFluidLayer(matrixStack,orientation,bufferIn,tailing,Math.round(tailingHeight));
        }
        if(!ore.isEmpty()){
            renderFluidLayer(matrixStack,orientation,bufferIn,ore,Math.round(oreHeight));
        }
        if (currentOutput != null) {
            if(ctx.getState().isActive() || !concentrate.isEmpty()){
                renderFluidLayer(matrixStack,orientation,bufferIn,currentOutput,Math.round(concentrateHeight));
            }
        }

    }

    private static void renderFluidLayer(
            PoseStack matrixStack,
            MultiblockOrientation orientation,
            MultiBufferSource bufferIn,
            FluidStack fluidStack,
            int height)
    {
        float baseScale = .0625f;
        matrixStack.pushPose();
        rotateForFacing(matrixStack, orientation.front());
        matrixStack.scale(baseScale, baseScale, baseScale);
        matrixStack.translate(-24,0,-2);
        matrixStack.translate(0, height,0);
        IMAnimationHelper.applyRotationX(height,-2,90,matrixStack);
        GuiHelper.drawRepeatedFluidSprite(bufferIn.getBuffer(RenderType.translucent()), matrixStack, fluidStack,
                0, 0, 64, 39);
        matrixStack.popPose();
    }
}
