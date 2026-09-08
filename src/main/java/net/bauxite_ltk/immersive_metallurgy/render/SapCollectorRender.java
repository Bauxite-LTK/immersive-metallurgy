package net.bauxite_ltk.immersive_metallurgy.render;

import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.bauxite_ltk.immersive_metallurgy.block.sapCollector.SapCollectorBlockEntity;
import net.bauxite_ltk.immersive_metallurgy.render.utils.IMAnimationHelper;
import net.bauxite_ltk.immersive_metallurgy.util.Helper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import org.jetbrains.annotations.NotNull;

public class SapCollectorRender implements BlockEntityRenderer<SapCollectorBlockEntity> {

    private static final float X_MIN = 3f;
    private static final float Z_MIN = 3f;
    private static final float X_MAX = 13f;
    private static final float Z_MAX = 13f;
    private static final float Y_MIN = 1.5f;
    private static final float Y_MAX = 4.5f;


    @Override
    public void render(@NotNull SapCollectorBlockEntity sapCollectorBlockEntity, float v, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        IFluidTank tank = sapCollectorBlockEntity.tank;
        int amount = tank.getFluidAmount();
        int capacity = tank.getCapacity();
        float height = ((float) amount/capacity) * (Y_MAX-Y_MIN);

        FluidStack fluidStack = tank.getFluid();

        Direction facing = sapCollectorBlockEntity.getBlockState().getValue(HorizontalDirectionalBlock.FACING);

        if(!fluidStack.isEmpty()){
            renderFluidTopLayer(matrixStack,facing,bufferIn,fluidStack, X_MIN, Z_MIN, X_MAX, Z_MAX, Y_MIN, height);
        }


    }



    private static void renderFluidTopLayer(
            PoseStack matrixStack,
            Direction facing,
            MultiBufferSource bufferIn,
            FluidStack fluidStack,
            float minX, float minZ,
            float maxX, float maxZ,
            float bottom, float height
            )
    {
        float baseScale = .0625f;
        matrixStack.pushPose();
        IMAnimationHelper.rotateForFacing(matrixStack, facing);
        matrixStack.scale(baseScale, baseScale, baseScale);
        matrixStack.translate(minX,bottom,minZ);
        matrixStack.translate(0, height,0);
        IMAnimationHelper.applyRotationX( bottom+height, minZ,90,matrixStack);
        GuiHelper.drawRepeatedFluidSprite(bufferIn.getBuffer(RenderType.translucent()), matrixStack, fluidStack,
                0, 0, maxX-minX, maxZ-minZ);
        matrixStack.popPose();
    }
}
