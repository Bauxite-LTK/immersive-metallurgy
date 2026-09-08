package net.bauxite_ltk.immersive_metallurgy.render.utils;

import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public class IMAnimationHelper {
    public static void applyRotationX(float pivotY, float pivotZ, float degree, PoseStack poseStack){
        //Rx(y,z) = T(y,z) * Rx(0,0) * T(-y,-z)
        poseStack.translate(0, pivotY / 16.0, pivotZ / 16.0);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) degree));
        poseStack.translate(0, -pivotY / 16.0, -pivotZ / 16.0);
    }

    public static void applyRotationY(float pivotX, float pivotZ, float degree, PoseStack poseStack){
        //Rx(x,z) = T(x,z) * R(0,0) * T(-x,-z)
        poseStack.translate(pivotX / 16.0, 0, pivotZ / 16.0);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) degree));
        poseStack.translate(-pivotX / 16.0, 0, -pivotZ / 16.0);
    }

    public static void applyRotationZ(float pivotX, float pivotY, float degree, PoseStack poseStack){
        //Rx(x,z) = T(x,z) * R(0,0) * T(-x,-z)
        poseStack.translate(pivotX / 16.0, pivotY / 16.0, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) degree));
        poseStack.translate(-pivotX / 16.0, -pivotY / 16.0, 0);
    }

    public static void applyScale(float pivotX, float pivotY, float pivotZ, float scaleX, float scaleY, float scaleZ, PoseStack poseStack){
        poseStack.translate(pivotX / 16.0, pivotY / 16.0, pivotZ / 16.0);
        poseStack.scale(scaleX, scaleY, scaleZ);
        poseStack.translate(-pivotX / 16.0, -pivotY / 16.0, -pivotZ / 16.0);
    }


    public static final Map<Direction, Quaternionf> ROTATE_FOR_FACING = Util.make(
            new EnumMap<>(Direction.class), m -> {
                for(Direction facing : DirectionUtils.BY_HORIZONTAL_INDEX)
                    m.put(facing, new Quaternionf().rotateY(Mth.DEG_TO_RAD*(180-facing.toYRot())));
            }
    );

    public static void rotateForFacingNoCentering(PoseStack stack, Direction facing)
    {
        stack.mulPose(ROTATE_FOR_FACING.get(facing));
    }

    public static void rotateForFacing(PoseStack stack, Direction facing)
    {
        stack.translate(0.5f, 0.5f, 0.5f);
        rotateForFacingNoCentering(stack, facing);
        stack.translate(-0.5f, -0.5f, -0.5f);
    }


    public static float catmullRomBE(float t, @Nullable Float p0, float p1, float p2, @Nullable Float p3){
        p0 = (p0 == null? p1 : p0);
        p3 = (p3 == null? p2 : p3);
        float tt = t*t;
        float ttt = tt*t;
        return 0.5f * (
                (2f*p1)
                + (-p0 + p2) * t
                + (2f*p0 - 5f*p1 + 4f*p2 - p3) * tt
                + (-p0 + 3f*p1 - 3f*p2 + p3) * ttt
        );
    }

    public static float catmullRom(float ft1, float ft2, float ct, @Nullable Float fv0, float fv1, float fv2, @Nullable Float fv3){
        try {
            float pt = (ct - ft1) / (ft2 - ft1);
            return catmullRomBE(pt, fv0, fv1, fv2, fv3);
        } catch (ArithmeticException e){
            throw new RuntimeException("CatmullRom calculate error:" + e.getMessage() + ", caused by:" + e.getCause());
        }
    }
}
