package net.bauxite_ltk.immersive_metallurgy.block.multiblock.logic;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.IMMultiblockLogic;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.IMMultiblockProperties;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class AdvancedCokeOvenMultiblock extends IETemplateMultiblock {

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
        consumer.accept(new IMMultiblockProperties(this, 1.5,1.5,1.5));
    }
}
