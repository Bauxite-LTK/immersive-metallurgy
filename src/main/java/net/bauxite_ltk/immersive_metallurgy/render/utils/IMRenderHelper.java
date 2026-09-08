package net.bauxite_ltk.immersive_metallurgy.render.utils;

import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.client.utils.InvertingVertexBuffer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bauxite_ltk.immersive_metallurgy.render.TransformingVertexBuilderForInverting;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import static blusunrize.immersiveengineering.client.ClientUtils.getSprite;

public class IMRenderHelper {

    public static MultiBufferSource mirror(
            MultiblockOrientation orientation, PoseStack mat, float centerX, float centerZ, MultiBufferSource builderIn
    )
    {
        if(orientation.mirrored())
        {
            Direction facing = orientation.front();
            mat.translate(centerX/16,0, centerZ/16);
            mat.scale(facing.getStepX()==0?-1: 1, 1, facing.getStepZ()==0?-1: 1);
            mat.translate(-centerX/16,0, -centerZ/16);
            return type -> new InvertingVertexBuffer(4, builderIn.getBuffer(type));
        }
        else
            return builderIn;
    }

    public static void drawRepeatedFluidSprite(VertexConsumer builder, PoseStack transform, FluidStack fluid, float x, float y, float w, float h)
    {
        IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(fluid.getFluid());
        TextureAtlasSprite sprite = getSprite(props.getStillTexture(fluid));
        int col = props.getTintColor(fluid);
        int iW = sprite.contents().width();
        int iH = sprite.contents().height();
        if(iW > 0&&iH > 0)
            drawRepeatedSprite(builder, transform, x, y, w, h, iW, iH,
                    sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(),
                    (col>>16&255)/255.0f, (col>>8&255)/255.0f, (col&255)/255.0f, (col>>24&255)/255f);
    }

    public static void drawRepeatedSprite(VertexConsumer builder, PoseStack transform, float x, float y, float w,
                                          float h, int iconWidth, int iconHeight, float uMin, float uMax, float vMin, float vMax,
                                          float r, float g, float b, float alpha)
    {
        int iterMaxW = (int)(w/iconWidth);
        int iterMaxH = (int)(h/iconHeight);
        float leftoverW = w%iconWidth;
        float leftoverH = h%iconHeight;
        float leftoverWf = leftoverW/(float)iconWidth;
        float leftoverHf = leftoverH/(float)iconHeight;
        float iconUDif = uMax-uMin;
        float iconVDif = vMax-vMin;
        for(int ww = 0; ww < iterMaxW; ww++)
        {
            for(int hh = 0; hh < iterMaxH; hh++)
                drawTexturedColoredRect(builder, transform, x+ww*iconWidth, y+hh*iconHeight, iconWidth, iconHeight,
                        r, g, b, alpha, uMin, uMax, vMin, vMax);
            drawTexturedColoredRect(builder, transform, x+ww*iconWidth, y+iterMaxH*iconHeight, iconWidth, leftoverH,
                    r, g, b, alpha, uMin, uMax, vMin, (vMin+iconVDif*leftoverHf));

        }
        if(leftoverW > 0)
        {
            for(int hh = 0; hh < iterMaxH; hh++)
                drawTexturedColoredRect(builder, transform, x+iterMaxW*iconWidth, y+hh*iconHeight, leftoverW, iconHeight,
                        r, g, b, alpha, uMin, (uMin+iconUDif*leftoverWf), vMin, vMax);
            drawTexturedColoredRect(builder, transform, x+iterMaxW*iconWidth, y+iterMaxH*iconHeight, leftoverW, leftoverH,
                    r, g, b, alpha, uMin, (uMin+iconUDif*leftoverWf), vMin, (vMin+iconVDif*leftoverHf));

        }
    }

    public static void drawTexturedColoredRect(
            VertexConsumer builder, PoseStack transform,
            float x, float y, float w, float h,
            float r, float g, float b, float alpha,
            float u0, float u1, float v0, float v1
    )
    {
        TransformingVertexBuilderForInverting innerBuilder = new TransformingVertexBuilderForInverting(builder, transform);
        innerBuilder.defaultColor(r, g, b, alpha);
        innerBuilder.setDefaultLight(LightTexture.FULL_BRIGHT);
        innerBuilder.setDefaultOverlay(OverlayTexture.NO_OVERLAY);
        innerBuilder.setDefaultNormal(1, 1, 1);
        innerBuilder.addVertex(x, y+h, 0).setUv(u0, v1);
        innerBuilder.addVertex(x+w, y+h, 0).setUv(u1, v1);
        innerBuilder.addVertex(x+w, y, 0).setUv(u1, v0);
        innerBuilder.addVertex(x, y, 0).setUv(u0, v0);
        innerBuilder.endVertex();
        innerBuilder.unsetDefaultColor();
    }

}
