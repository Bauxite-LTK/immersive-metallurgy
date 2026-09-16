package net.bauxite_ltk.immersive_metallurgy.gui.multiblock;

import blusunrize.immersiveengineering.common.gui.IEContainerMenu;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.logic.AdvancedCokeOvenLogic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdvancedCokeOvenMenu extends IEContainerMenu {
    public final List<AdvancedCokeOvenLogic.AdvancedCokeOvenTanks> tanks;
    public final List<GetterAndSetter<Float>> guiProgressList;
    public static final int COKE_OVEN_THREAD_COUNT = AdvancedCokeOvenLogic.THREAD_COUNT;
    public static final int THREAD_SLOTS_COUNT = AdvancedCokeOvenLogic.CokeOvenThread.NUM_SLOTS;

    public static AdvancedCokeOvenMenu makeServer(
            MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<AdvancedCokeOvenLogic.State> ctx
    )
    {
        final AdvancedCokeOvenLogic.State state = ctx.mbContext().getState();
        return new AdvancedCokeOvenMenu(
                multiblockCtx(type, id, ctx),
                invPlayer,
                state.getAllOf(AdvancedCokeOvenLogic.CokeOvenThread::getInventory),
                state.getAllOf(AdvancedCokeOvenLogic.CokeOvenThread::getTanks),
                state.getAllOf(t -> GetterAndSetter.getterOnly(t::getRecipeProgress))

        );
    }

    public static AdvancedCokeOvenMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
    {
        List<AdvancedCokeOvenLogic.AdvancedCokeOvenTanks> tanksList = new ArrayList<>();
        List<IItemHandler> itemHandlerList = new ArrayList<>();
        List<GetterAndSetter<Float>> progresslist = new ArrayList<>();
        for(int i = 0; i < COKE_OVEN_THREAD_COUNT; i++){
            tanksList.add(new AdvancedCokeOvenLogic.AdvancedCokeOvenTanks());
            itemHandlerList.add(new ItemStackHandler(THREAD_SLOTS_COUNT));
            progresslist.add(GetterAndSetter.standalone(0f));
        }
        return new AdvancedCokeOvenMenu(
                clientCtx(type, id),
                invPlayer,
                itemHandlerList,
                tanksList,
                progresslist
        );
    }


    protected AdvancedCokeOvenMenu(
            MenuContext ctx,
            Inventory inventoryPlayer,
            List<IItemHandler> invList,
            List<AdvancedCokeOvenLogic.AdvancedCokeOvenTanks> tanks,
            List<GetterAndSetter<Float>> guiProgressList
    ) {
        super(ctx);
        this.tanks = tanks;
        this.guiProgressList = guiProgressList;

        for(int i = 0; i < COKE_OVEN_THREAD_COUNT; i++){
            IItemHandler inv = invList.get(i);
            this.addSlot(new SlotItemHandler(inv, 0, 20 + 40*i, 10));
            this.addSlot(new IESlot.NewOutput(inv, 1, 20 + 40*i, 54));
        }
        ownSlotCount = 8;
        for(int i = 0; i < 3; i++)
            for(int j = 0; j < 9; j++)
                addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
        for(int i = 0; i < 9; i++)
            addSlot(new Slot(inventoryPlayer, i, 8+i*18, 143));

        for(int i = 0; i < COKE_OVEN_THREAD_COUNT; i++){
            addGenericData(GenericContainerData.fluid(tanks.get(i).outputOil()));
            addGenericData(GenericContainerData.fluid(tanks.get(i).outputGas()));
            addGenericData(new GenericContainerData<>(GenericDataSerializers.FLOAT, guiProgressList.get(i)));
        }



    }
}
