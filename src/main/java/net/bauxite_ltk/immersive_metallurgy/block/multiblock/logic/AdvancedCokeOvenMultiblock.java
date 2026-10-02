package net.bauxite_ltk.immersive_metallurgy.block.multiblock.logic;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.IMMultiblockLogic;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.IMMultiblockProperties;
import net.bauxite_ltk.immersive_metallurgy.render.AdvancedCokeOvenRenderer;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

import static net.bauxite_ltk.immersive_metallurgy.render.AdvancedCokeOvenRenderer.DOOR;
import static net.bauxite_ltk.immersive_metallurgy.render.BallMillRender.BARREL;

public class AdvancedCokeOvenMultiblock extends IETemplateMultiblock {

    public static final AdvancedCokeOvenMultiblock INSTANCE = new AdvancedCokeOvenMultiblock();

    public AdvancedCokeOvenMultiblock() {
        super(IMUtils.modRL("multiblocks/advanced_coke_oven"),
                AdvancedCokeOvenLogic.MASTER_OFFSET, new BlockPos(1, 1, 3), new BlockPos(4, 4, 4),
                IMMultiblockLogic.ADVANCED_COKE_OVEN);
    }

    @Override
    public float getManualScale() {
        return 14;
    }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer){
        consumer.accept(new AdvancedCokeOvenMultiblockProperties());
    }

    public static class AdvancedCokeOvenMultiblockProperties extends IMMultiblockProperties {
        public AdvancedCokeOvenMultiblockProperties(){
            super(INSTANCE, 1.5, 1.5, 1.5);
        }

        @Override
        public void renderExtras(PoseStack matrix, MultiBufferSource buffer){
            for(int i = 0; i < AdvancedCokeOvenLogic.THREAD_COUNT; i++){
                matrix.pushPose();
                matrix.translate(i-1, 0, 0);
                renderObj(DOOR.getModelResourceLocation(), buffer, matrix);
                matrix.popPose();
            }

        }

        private static void renderObj(ModelResourceLocation modelRL, @Nonnull MultiBufferSource bufferIn, @Nonnull PoseStack matrix){
            final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
            BakedModel model = DOOR.get();
            PoseStack.Pose last = matrix.last();
            VertexConsumer solid = bufferIn.getBuffer(RenderType.solid());

            blockRenderer.getModelRenderer().renderModel(
                    last, solid, null, model,
                    1, 1, 1,
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY , ModelData.EMPTY, RenderType.solid()
            );

        }
    }
}
