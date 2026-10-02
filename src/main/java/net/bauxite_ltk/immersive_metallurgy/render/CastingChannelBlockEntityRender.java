package net.bauxite_ltk.immersive_metallurgy.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.casting_channel.CastingChannelBlockEntity;
import net.bauxite_ltk.immersive_metallurgy.util.Helper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;

public class CastingChannelBlockEntityRender implements BlockEntityRenderer<CastingChannelBlockEntity> {


    @Override
    public void render(CastingChannelBlockEntity castingChannelBlockEntity, float v, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        FluidStack fluidStack = castingChannelBlockEntity.tank.getFluid();
        poseStack.pushPose();
        if (!fluidStack.isEmpty())
        {
            final float fillPercent = (float) fluidStack.getAmount() / castingChannelBlockEntity.tank.getStorageCapacity();

            if(castingChannelBlockEntity.isConnectTo(Direction.UP)){
                Direction to = castingChannelBlockEntity.getUniqueHorizontalDirection();
                if(to != null) {
                    renderUpFluidInCenter(to, fillPercent, poseStack, fluidStack, buffer, combinedOverlay, combinedLight);
                }
            }
            else if(castingChannelBlockEntity.isConnectTo(Direction.DOWN)){
                Direction from = castingChannelBlockEntity.getUniqueHorizontalDirection();
                if(from != null){
                    renderDownFluidInCenter(from, fillPercent, poseStack, fluidStack, buffer, combinedOverlay, combinedLight);
                }
            }
            else{
                Helper.renderFluidFace(poseStack, fluidStack, buffer, 5f/16, 5f/16, 11/16f, 11/16f, 5/16f + (2.5f/16) * fillPercent, combinedOverlay, combinedLight);
                for(int i = 2; i < 6; i++){
                    Direction d = Direction.from3DDataValue(i);
                    if(castingChannelBlockEntity.isConnectTo(d))
                        renderFluidOnSide(d, fillPercent, poseStack, fluidStack, buffer, combinedOverlay, combinedLight);
                }
            }

        }
        poseStack.popPose();

    }


    private static void renderFluidOnSide(Direction direction, float fillPercent, PoseStack poseStack, FluidStack fluidStack, MultiBufferSource buffer, int combinedOverlay, int combinedLight){
        renderFluidOnEdge(direction,fillPercent,poseStack,fluidStack,buffer,combinedOverlay,combinedLight);
        if(direction.equals(Direction.NORTH)){
            Helper.renderFluidFace(poseStack, fluidStack, buffer,
                    5f/16, 0, 11f/16, 5f/16, 5f/16 + (2.5f/16) * fillPercent, combinedOverlay, combinedLight);

        }
        else if(direction.equals(Direction.SOUTH)){
            Helper.renderFluidFace(poseStack, fluidStack, buffer,
                    5f/16, 11f/16, 11f/16, 1, 5f/16 + (2.5f/16) * fillPercent, combinedOverlay, combinedLight);
        }
        else if(direction.equals(Direction.WEST)){
            Helper.renderFluidFace(poseStack, fluidStack, buffer,
                    0, 5f/16, 5f/16, 11f/16, 5f/16 + (2.5f/16) * fillPercent, combinedOverlay, combinedLight);

        }
        else if(direction.equals(Direction.EAST)){
            Helper.renderFluidFace(poseStack, fluidStack, buffer,
                    11f/16, 5f/16, 1, 11f/16, 5f/16 + (2.5f/16) * fillPercent, combinedOverlay, combinedLight);
        }
    }

    public static void renderFluidOnEdge(Direction direction, float fillPercent, PoseStack poseStack, FluidStack fluidStack, MultiBufferSource buffer, int combinedOverlay, int combinedLight){
        if(direction.equals(Direction.NORTH)){
            Helper.renderFluidFace(poseStack, fluidStack, buffer,
                    5f/16, 5f/16, 0f,
                    11f/16, 5f/16 + (2.5f/16) * fillPercent, 0f,
                    true, combinedOverlay, combinedLight
            );
        }
        else if(direction.equals(Direction.SOUTH)){
            Helper.renderFluidFace(poseStack, fluidStack, buffer,
                    5f/16, 5f/16 + (2.5f/16) * fillPercent, 0.99f,
                    11f/16, 5f/16, 0.99f,
                    true, combinedOverlay, combinedLight
            );
        }
        else if(direction.equals(Direction.WEST)){
            Helper.renderFluidFace(poseStack, fluidStack, buffer,
                    0f, 5f/16, 5f/16,
                    0f, 5f/16 + (2.5f/16) * fillPercent, 11f/16,
                    false, combinedOverlay, combinedLight
            );
        }
        else if(direction.equals(Direction.EAST)){
            Helper.renderFluidFace(poseStack, fluidStack, buffer,
                    0.99f, 5f/16 + (2.5f/16) * fillPercent, 5f/16,
                    0.99f, 5f/16, 11f/16,
                    false, combinedOverlay, combinedLight
            );
        }
    }


    private static void renderUpFluidInCenter(Direction direction, float fillPercent, PoseStack poseStack, FluidStack fluidStack, MultiBufferSource buffer, int combinedOverlay, int combinedLight){
        renderFluidOnSide(direction, fillPercent, poseStack, fluidStack, buffer, combinedOverlay, combinedLight);
        if(direction.equals(Direction.SOUTH)){
            Helper.renderFluidFace(poseStack, fluidStack, buffer,
                    5f/16, 1 + (2.5f/16) * fillPercent, 5f/16,
                    11f/16, 5f/16 + (2.5f/16) * fillPercent, 11f/16,
                    true, combinedOverlay, combinedLight);
        }
        else if(direction.equals(Direction.NORTH)){
            Helper.renderFluidFace(poseStack, fluidStack, buffer,
                    5f/16, 5f/16 + (2.5f/16) * fillPercent, 5f/16,
                    11f/16, 1 + (2.5f/16) * fillPercent, 11f/16,
                    true, combinedOverlay, combinedLight);
        }
        else if(direction.equals(Direction.EAST)){
            Helper.renderFluidFace(poseStack, fluidStack, buffer,
                    5f/16, 1 + (2.5f/16) * fillPercent, 5f/16,
                    11f/16, 5f/16 + (2.5f/16) * fillPercent, 11f/16,
                    false, combinedOverlay, combinedLight);
        }
        else if(direction.equals(Direction.WEST)){
            Helper.renderFluidFace(poseStack, fluidStack, buffer,
                    5f/16, 5f/16 + (2.5f/16) * fillPercent, 5f/16,
                    11f/16, 1 + (2.5f/16) * fillPercent, 11f/16,
                    false, combinedOverlay, combinedLight);
        }
    }

    private static void renderDownFluidInCenter(Direction direction, float fillPercent, PoseStack poseStack, FluidStack fluidStack, MultiBufferSource buffer, int combinedOverlay, int combinedLight){
        renderFluidOnEdge(direction,fillPercent,poseStack,fluidStack,buffer,combinedOverlay,combinedLight);
        if(direction.equals(Direction.NORTH)){
            Helper.renderFluidFace(poseStack, fluidStack, buffer,
                    5f/16, 5f/16 + (2.5f/16) * fillPercent, 3f/16,
                    11f/16, 0 + (2.5f/16) * fillPercent, 5f/16,
                    true, combinedOverlay, combinedLight);

            Helper.renderFluidFace(poseStack, fluidStack, buffer,
                    5f/16, 0, 11f/16, 3f/16, 5f/16 + (2.5f/16) * fillPercent, combinedOverlay, combinedLight);


        }
        else if(direction.equals(Direction.SOUTH)){
            Helper.renderFluidFace(poseStack, fluidStack, buffer,
                    5f/16, 0 + (2.5f/16) * fillPercent, 11f/16,
                    11f/16, 5f/16 + (2.5f/16) * fillPercent, 13f/16,
                    true, combinedOverlay, combinedLight);

            Helper.renderFluidFace(poseStack, fluidStack, buffer,
                    5f/16, 13f/16, 11f/16, 1, 5f/16 + (2.5f/16) * fillPercent, combinedOverlay, combinedLight);


        }
        else if(direction.equals(Direction.WEST)){
            Helper.renderFluidFace(poseStack, fluidStack, buffer,
                    3f/16, 5f/16 + (2.5f/16) * fillPercent, 5f/16,
                    5f/16, 0 + (2.5f/16) * fillPercent, 11f/16,
                    false, combinedOverlay, combinedLight);

            Helper.renderFluidFace(poseStack, fluidStack, buffer,
                    0, 5f/16, 3f/16, 11f/16, 5f/16 + (2.5f/16) * fillPercent, combinedOverlay, combinedLight);


        }
        else if(direction.equals(Direction.EAST)){
            Helper.renderFluidFace(poseStack, fluidStack, buffer,
                    11f/16, 0 + (2.5f/16) * fillPercent, 5f/16,
                    13f/16, 5f/16 + (2.5f/16) * fillPercent, 11f/16,
                    false, combinedOverlay, combinedLight);

            Helper.renderFluidFace(poseStack, fluidStack, buffer,
                    13f/16, 5f/16, 1, 11f/16, 5f/16 + (2.5f/16) * fillPercent, combinedOverlay, combinedLight);


        }





    }

}
