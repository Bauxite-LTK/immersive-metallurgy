package net.bauxite_ltk.immersive_metallurgy.render;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.client.render.tile.BERenderUtils;
import blusunrize.immersiveengineering.client.render.tile.IEMultiblockRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.logic.BallMillLogic;
import net.bauxite_ltk.immersive_metallurgy.render.utils.IMAnimationHelper;
import net.bauxite_ltk.immersive_metallurgy.render.utils.IMRenderHelper;
import net.bauxite_ltk.immersive_metallurgy.util.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

public class BallMillRender extends IEMultiblockRenderer<BallMillLogic.State> {
    public static final String NAME = "ball_mill_barrel";
    public static IMDynamicModel BARREL;


    @Override
    public void render(@NotNull IMultiblockContext<BallMillLogic.State> ctx, float partialTicks, @NotNull PoseStack matrixStack, @NotNull MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = BARREL.get();
        final MultiblockOrientation orientation = ctx.getLevel().getOrientation();

        matrixStack.pushPose();



        //rotateForFacing(matrixStack, orientation.front());
        matrixStack.translate(0.5, 0.5, 0.5);
        bufferIn = BERenderUtils.mirror(orientation, matrixStack, bufferIn);
        VertexConsumer buffer = bufferIn.getBuffer(RenderType.solid());
        rotateForFacingNoCentering(matrixStack, orientation.front());
        matrixStack.translate(-0.5, -0.5, -0.5);

        //if(orientation.mirrored()) matrixStack.translate(1,0,0);

        boolean active = ctx.getState().shouldRenderActive();
        float barrelAngle = ctx.getState().getBarrelAngle()+ (active? 9 * partialTicks: 0);

        IMAnimationHelper.applyRotationX(14,0,barrelAngle,matrixStack);



        blockRenderer.getModelRenderer().renderModel(
                matrixStack.last(), buffer, null, model,
                1, 1, 1,
                combinedLightIn, combinedOverlayIn, ModelData.EMPTY, RenderType.solid()
        );

        matrixStack.popPose();
    }
}
