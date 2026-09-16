package net.bauxite_ltk.immersive_metallurgy.gui.multiblock;

import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import com.google.common.collect.ImmutableList;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.logic.AdvancedCokeOvenLogic;
import net.bauxite_ltk.immersive_metallurgy.gui.info.IMEnergyInfoArea;
import net.bauxite_ltk.immersive_metallurgy.gui.info.IMFuelInfoArea;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class AdvancedCokeOvenScreen extends IEContainerScreen<AdvancedCokeOvenMenu> {
    private static final ResourceLocation TEXTURE = makeTextureLocation("advanced_coke_oven");
    private static final ResourceLocation OUTPUT_TANK = IMUtils.modRL("advanced_coke_oven/oven_tank");
    private static final ResourceLocation PROGRESS = IMUtils.modRL("advanced_coke_oven/fire");


    public AdvancedCokeOvenScreen(AdvancedCokeOvenMenu container, Inventory inventoryPlayer, Component title) {
        super(container, inventoryPlayer, title, TEXTURE);
    }

    @Nonnull
    @Override
    protected List<InfoArea> makeInfoAreas()
    {
        List<InfoArea> infoAreas = new ArrayList<>();
        for(int i = 0; i < AdvancedCokeOvenMenu.COKE_OVEN_THREAD_COUNT; i++){
            infoAreas.add(new FluidInfoArea(menu.tanks.get(i).outputOil(), new Rect2i(leftPos+13 + 40*i, topPos+14, 4, 52), 8, 58, OUTPUT_TANK));
            infoAreas.add(new FluidInfoArea(menu.tanks.get(i).outputGas(), new Rect2i(leftPos+39 + 40*i, topPos+14, 4, 52), 8, 58, OUTPUT_TANK));
        }

        return ImmutableList.copyOf(infoAreas);
    }

    public static ResourceLocation makeTextureLocation(String name) {
        return IMUtils.modRL( "textures/gui/"+name+".png");
    }

    @Override
    protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float f, int mx, int my)
    {
        for(int i = 0; i < AdvancedCokeOvenMenu.COKE_OVEN_THREAD_COUNT; i++){
            float process = menu.guiProgressList.get(i).get();
            //ImmersiveMetallurgy.LOGGER.info("progress:{}", process);
            if(process > 0)
            {
                int h = Math.clamp((int)(17*process), 0, 17);
                graphics.blitSprite(PROGRESS, 14, 17, 0, h, leftPos+21 + 40*i, topPos+30 + h, 14, 17 - h);
            }
        }

    }
}
