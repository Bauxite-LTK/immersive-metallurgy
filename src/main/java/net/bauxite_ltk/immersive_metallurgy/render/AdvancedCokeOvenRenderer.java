package net.bauxite_ltk.immersive_metallurgy.render;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.client.render.tile.IEMultiblockRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.logic.AdvancedCokeOvenLogic;
import net.bauxite_ltk.immersive_metallurgy.render.utils.ColorModifiableVertexBuilder;
import net.bauxite_ltk.immersive_metallurgy.render.utils.IMAnimationHelper;
import net.bauxite_ltk.immersive_metallurgy.render.utils.IMRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class AdvancedCokeOvenRenderer extends IEMultiblockRenderer<AdvancedCokeOvenLogic.State> {
    public static final String DOOR_NAME = "advanced_coke_oven_door";
    public static IMDynamicModel DOOR;

    @Override
    public void render(@NotNull IMultiblockContext<AdvancedCokeOvenLogic.State> ctx, float partialTicks, @NotNull PoseStack matrixStack, @NotNull MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        for(int i = 0; i < AdvancedCokeOvenLogic.THREAD_COUNT; i++){
            renderDoor(i, ctx, partialTicks, matrixStack, bufferIn, combinedLightIn, combinedOverlayIn);
        }
    }

    public void renderDoor(
            int index,
            @NotNull IMultiblockContext<AdvancedCokeOvenLogic.State> ctx,
            float partialTicks,
            PoseStack matrixStack,
            @NotNull MultiBufferSource bufferIn,
            int combinedLightIn,
            int combinedOverlayIn
    ){
        final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = DOOR.get();
        final MultiblockOrientation orientation = ctx.getLevel().getOrientation();

        matrixStack.pushPose();

        MultiBufferSource bufferSource = IMRenderHelper.mirror(orientation, matrixStack,8,8, bufferIn);

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.translucent());
        rotateForFacing(matrixStack, orientation.front());

        //different doors
        matrixStack.translate(index-1,0,0);

        long curGameTime = ctx.getLevel().getRawLevel().getGameTime();
        AdvancedCokeOvenLogic.State state = ctx.getState();
        if(state.getAndUpdateOutputFlagClientSide(index)){
            state.addAnimationInstance(index, curGameTime, partialTicks);
        }

        CokeOvenAnimation animation = state.getAnimationInstance(index);
        VertexConsumer builder = buffer;
        if(animation!=null){
            // took frame from animation and fetch parameters of the transformation
            AtomicReference<Float> transZ = new AtomicReference<>((float) 0);
            AtomicReference<Float> alpha = new AtomicReference<>((float) 0);
            animation.applyAnimation(curGameTime, partialTicks, transZ::set, alpha::set);

            //apply the parameter
            matrixStack.translate(0,0, transZ.get());
            builder = ColorModifiableVertexBuilder.forAlpha(buffer, alpha.get());

            if(animation.isEnd){
                state.removeAnimationInstance(index);
            }
        }

        matrixStack.pushPose();
        blockRenderer.getModelRenderer().renderModel(
                matrixStack.last(), builder, null, model,
                1, 1, 1,
                combinedLightIn, combinedOverlayIn, ModelData.EMPTY, RenderType.translucent()
        );
        matrixStack.popPose();

        matrixStack.pushPose();
        float recipeProgress = state.getRecipeProgress(index);
        matrixStack.translate(9f/16, 1f/16, 49.01f/16);
        IMAnimationHelper.applyRotationY(0,0,180,matrixStack);
        IMRenderHelper.drawRepeatedFluidSprite(builder, matrixStack, new FluidStack(Fluids.FLOWING_LAVA,1), 0,0,2f/16,recipeProgress * 14f/16);
        matrixStack.popPose();

        matrixStack.popPose();

    }

    public static class CokeOvenAnimation{
        final int totalFrame;
        final int fps;
        final long startGameTime;
        final float startGamePartialTime;
        boolean isEnd = false;

        public CokeOvenAnimation(long startGameTime, float startGamePartialTime, int totalFrames, int fps){
            this.fps = fps;
            this.totalFrame = totalFrames;
            this.startGameTime = startGameTime;
            this.startGamePartialTime = startGamePartialTime;
        }

        float startZ = 0;
        float startTranslucent = 1;

        float midZ = 1;
        float midTranslucent = -1;

        float endZ = 0;
        float endTranslucent = 1;

        public void applyAnimation(long curGameTime, float partialTick, Consumer<Float> transZSetter, Consumer<Float> translucentSetter){
            if(isEnd) return;
            float percentage = getAnimationPercentage(curGameTime, partialTick);
            if(percentage >= 1f){
                percentage = 1f;
                isEnd = true;
            }

            if(percentage < 0.5f){
                float transZ = IMAnimationHelper.catmullRom(
                        0,0.5f, percentage,
                        null,startZ,midZ,endZ
                        );
                transZSetter.accept(transZ);
                float alpha = IMAnimationHelper.catmullRom(
                        0,0.5f, percentage,
                        null,startTranslucent,midTranslucent,endTranslucent
                );
                alpha = Math.clamp(alpha,0,1);
                translucentSetter.accept(alpha);
            }

            else if(percentage <= 1f){
                float transZ = IMAnimationHelper.catmullRom(
                        0.5f,1f, percentage,
                        startZ,midZ,endZ,null
                );
                transZSetter.accept(transZ);
                float alpha = IMAnimationHelper.catmullRom(
                        0.5f,1f, percentage,
                        startTranslucent,midTranslucent,endTranslucent,null
                );
                alpha = Math.clamp(alpha,0,1);
                translucentSetter.accept(alpha);
            }
        }

        public float getAnimationPercentage(long curGameTime, float partialTick){
            float aniTick = (float)(curGameTime - startGameTime) + partialTick - startGamePartialTime;
            float curFrameFloat = (aniTick / 20f) * fps;
            return curFrameFloat/totalFrame;
        }

    }

}
