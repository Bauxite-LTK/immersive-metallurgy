package net.bauxite_ltk.immersive_metallurgy.util;

import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Quaternionf;

import java.text.DecimalFormat;
import java.util.EnumMap;
import java.util.Map;

public class Helper {
    static final DecimalFormat FORMATTER = new DecimalFormat("#,###.##");

    public static String fDecimal(byte number){
        return FORMATTER.format(number);
    }

    public static String fDecimal(short number){
        return FORMATTER.format(number);
    }

    public static String fDecimal(int number){
        return FORMATTER.format(number);
    }

    public static String fDecimal(long number){
        return FORMATTER.format(number);
    }

    public static String fDecimal(float number){
        return FORMATTER.format(number);
    }

    public static String fDecimal(double number){
        return FORMATTER.format(number);
    }







    public static void applyRotationX_old(double pivotY, double pivotZ, double degree, PoseStack poseStack){
        double arcDegree = -degree/180 * Math.PI;
        //transY, transZ are the Y and Z position of the pivot after *directly* applying the rotation.
        double transY = Math.cos(arcDegree)*pivotY + Math.sin(arcDegree)*pivotZ;
        double transZ = -Math.sin(arcDegree)*pivotY + Math.cos(arcDegree)*pivotZ;
        //System.out.println("(" + transY + "," + transZ + ")");
        poseStack.translate(0, (pivotY-transY)/16, (pivotZ-transZ)/16);

        poseStack.mulPose(Axis.XP.rotationDegrees((float) degree));
    }

    public static void applyRotationY_old( double pivotX, double pivotZ, double degree, PoseStack poseStack){
        double arcDegree = -degree/180 * Math.PI;
        //transY, transZ are the Y and Z position of the pivot after *directly* applying the rotation.
        double transZ = Math.cos(arcDegree)*pivotZ + Math.sin(arcDegree)*pivotX;
        double transX = -Math.sin(arcDegree)*pivotZ + Math.cos(arcDegree)*pivotX;
        //System.out.println("(" + transY + "," + transZ + ")");
        poseStack.translate((pivotX-transX)/16,0, (pivotZ-transZ)/16);

        poseStack.mulPose(Axis.YP.rotationDegrees((float) degree));
    }

    public static void applyRotationZ_old(double pivotX, double pivotY, double degree, PoseStack poseStack){
        double arcDegree = -degree/180 * Math.PI;
        //transY, transZ are the Y and Z position of the pivot after *directly* applying the rotation.
        double transX = Math.cos(arcDegree)*pivotX + Math.sin(arcDegree)*pivotY;
        double transY = -Math.sin(arcDegree)*pivotX + Math.cos(arcDegree)*pivotY;
        //System.out.println("(" + transY + "," + transZ + ")");
        poseStack.translate((pivotX-transX)/16, (pivotY-transY)/16, 0);

        poseStack.mulPose(Axis.ZP.rotationDegrees((float) degree));
    }

    public static float calculateSpeedWithFactors(int x, int min, int max, int best, float strict){
        float s = strict;
        if(s > 1) s = 1f;
        else if(s < -1) s = -1f;

        float matchFactor = x > best? (float)(max-x)/(max-best) : (float)(x-min)/(best-min);
        if(x == best) matchFactor = 1f;
        if(s == 0) return matchFactor;

        float var0 = (matchFactor + 1/(2*s) - 0.5f);
        float var1 = 1/(2*s*s) + 0.5f - var0 * var0;
        float var2 = s > 0 ? -Mth.sqrt(var1) : Mth.sqrt(var1);
        float var3 = var2 + 1/(2*s) + 0.5f;

        return var3;
    }

    public static float calculateSpeedWithFactors(int x, int min, int max, int best, float strict, float tolerance){
        float s = strict;
        if(s > 1) s = 1f;
        else if(s < -1) s = -1f;

        float matchFactor = x > best? (float)(max-x)/(max-best) : (float)(x-min)/(best-min);
        if(x == best) matchFactor = 1f;
        if(s == 0) return matchFactor;

        float var0 = (matchFactor + 1/(2*s) - 0.5f);
        float var1 = 1/(2*s*s) + 0.5f - var0 * var0;
        float var2 = s > 0 ? -Mth.sqrt(var1) : Mth.sqrt(var1);
        float var3 = var2 + 1/(2*s) + 0.5f;

        return Mth.clamp(var3*tolerance,0,1);
    }





    public static void playSound(Level level, BlockPos worldPosition, SoundEvent soundEvent){
        level.playSound(null, worldPosition, soundEvent, SoundSource.BLOCKS, 1.0F + level.getRandom().nextFloat(), level.getRandom().nextFloat() + 0.7F + 0.3F);
    }


    @SuppressWarnings("deprecation") public static final ResourceLocation BLOCKS_ATLAS = TextureAtlas.LOCATION_BLOCKS;

    public static int getFluidColor(FluidStack fluid)
    {
        return getFluidColor(fluid.getFluid());
    }

    public static int getFluidColor(Fluid fluid)
    {
        return IClientFluidTypeExtensions.of(fluid).getTintColor();
    }

    public static void renderFluidFace(PoseStack poseStack, FluidStack fluidStack, MultiBufferSource buffer, float minX, float minZ, float maxX, float maxZ, float y, int combinedOverlay, int combinedLight)
    {
        renderFluidFace(poseStack, fluidStack, buffer, getFluidColor(fluidStack), minX, minZ, maxX, maxZ, y, combinedOverlay, combinedLight);
    }

    public static void renderFluidFace(PoseStack poseStack, FluidStack fluidStack, MultiBufferSource buffers, int color, float minX, float minZ, float maxX, float maxZ, float y, int packedOverlay, int packedLight)
    {
        final ResourceLocation texture = IClientFluidTypeExtensions.of(fluidStack.getFluid()).getStillTexture(fluidStack);
        final TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(BLOCKS_ATLAS).apply(texture);
        final VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucentCull(BLOCKS_ATLAS));

        renderTexturedFace(poseStack.last(), buffer, color, minX, minZ, maxX, maxZ, y, packedOverlay, packedLight, sprite);
    }


    public static void renderFluidFace(PoseStack poseStack, FluidStack fluidStack, MultiBufferSource buffers, float x1, float y1, float z1, float x2, float y2, float z2, boolean withRotationX, int packedOverlay, int packedLight)
    {
        final ResourceLocation texture = IClientFluidTypeExtensions.of(fluidStack.getFluid()).getStillTexture(fluidStack);
        final TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(BLOCKS_ATLAS).apply(texture);
        final VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucentCull(BLOCKS_ATLAS));

        renderTexturedFace(poseStack.last(), buffer, getFluidColor(fluidStack), x1, y1, z1, x2, y2, z2, withRotationX, packedOverlay, packedLight, sprite);
    }


    private static void renderTexturedFace(PoseStack.Pose pose, VertexConsumer buffer, int color, float minX, float minZ, float maxX, float maxZ, float y, int packedOverlay, int packedLight, TextureAtlasSprite sprite)
    {
        buffer.addVertex(pose, minX, y, minZ).setColor(color).setUv(sprite.getU(minX), sprite.getV(minZ)).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose, 0, 1, 0);
        buffer.addVertex(pose, minX, y, maxZ).setColor(color).setUv(sprite.getU(minX), sprite.getV(maxZ)).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose, 0, 1, 0);
        buffer.addVertex(pose, maxX, y, maxZ).setColor(color).setUv(sprite.getU(maxX), sprite.getV(maxZ)).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose, 0, 1, 0);
        buffer.addVertex(pose, maxX, y, minZ).setColor(color).setUv(sprite.getU(maxX), sprite.getV(minZ)).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose, 0, 1, 0);
    }

    private static void renderTexturedFace(PoseStack.Pose pose, VertexConsumer buffer, int color, float x1, float y1, float z1, float x2, float y2, float z2, boolean withRotationX, int packedOverlay, int packedLight, TextureAtlasSprite sprite)
    {
        if(withRotationX){
            float dy = y1-y2;
            float dz = z1-z2;
            buffer.addVertex(pose, x1, y1, z1).setColor(color).setUv(sprite.getU(x1), sprite.getV(z1)).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, x1, y2, z2).setColor(color).setUv(sprite.getU(x1), sprite.getV(z2)).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, x2, y2, z2).setColor(color).setUv(sprite.getU(x2), sprite.getV(z2)).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, x2, y1, z1).setColor(color).setUv(sprite.getU(x2), sprite.getV(z1)).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose, 0, 1, 0);

        }
        else{
            float dy = y1-y2;
            float dx = x1-x2;
            buffer.addVertex(pose, x1, y1, z1).setColor(color).setUv(sprite.getU(x1), sprite.getV(z1)).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, x1, y1, z2).setColor(color).setUv(sprite.getU(x1), sprite.getV(z2)).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, x2, y2, z2).setColor(color).setUv(sprite.getU(x2), sprite.getV(z2)).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, x2, y2, z1).setColor(color).setUv(sprite.getU(x2), sprite.getV(z1)).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose, 0, 1, 0);
        }
    }
}
