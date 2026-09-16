package net.bauxite_ltk.immersive_metallurgy.render.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.FastColor;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ColorModifiableVertexBuilder extends VertexConsumerWrapper {
    int r;
    int g;
    int b;
    int a;

    public ColorModifiableVertexBuilder(VertexConsumer parent, float r, float g, float b, float a) {
        super(parent);
        this.r = (int)(r*255f);
        this.g = (int)(g*255f);
        this.b = (int)(b*255f);
        this.a = (int)(a*255f);
    }

    public static ColorModifiableVertexBuilder forAlpha(VertexConsumer parent, float a){
        return new ColorModifiableVertexBuilder(parent,1f,1f,1f,a);
    }

    @Override
    public VertexConsumer addVertex(Vector3f pos) {
        return super.addVertex(pos).setColor(r,g,b,a);
    }

    @Override
    public VertexConsumer addVertex(PoseStack.Pose pose, Vector3f pos) {
        return super.addVertex(pose, pos).setColor(r,g,b,a);
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        return super.addVertex(x, y, z).setColor(r,g,b,a);
    }

    @Override
    public VertexConsumer addVertex(PoseStack.Pose pose, float x, float y, float z) {
        return super.addVertex(pose, x, y, z).setColor(r,g,b,a);
    }

    @Override
    public VertexConsumer addVertex(Matrix4f pose, float x, float y, float z) {
        return super.addVertex(pose, x, y, z).setColor(r,g,b,a);
    }

    @Override
    public void addVertex(float x, float y, float z, int color, float u, float v, int packedOverlay, int packedLight, float normalX, float normalY, float normalZ) {
        super.addVertex(x, y, z, FastColor.ARGB32.color(a,r,g,b), u, v, packedOverlay, packedLight, normalX, normalY, normalZ);
    }
}
