package net.bauxite_ltk.immersive_metallurgy.block.multiblock;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.logic.*;
import net.bauxite_ltk.immersive_metallurgy.gui.IMMenuTypes;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IMMultiblockLogic {
    public static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(
            BuiltInRegistries.BLOCK, IMUtils.MOD_ID
    );
    private static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(
            BuiltInRegistries.ITEM, IMUtils.MOD_ID
    );
    private static final DeferredRegister<BlockEntityType<?>> BE_REGISTER = DeferredRegister.create(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, IMUtils.MOD_ID
    );

    public static final MultiblockRegistration<BallMillLogic.State> BALL_MILL =
            metal(new BallMillLogic(), "ball_mill")
            .structure(() -> IMMultiblocks.BALL_MILL)
            .gui(IMMenuTypes.BALL_MILL)
            .redstone(s -> s.rsState, BallMillLogic.REDSTONE_POS)
            .comparator(BallMillLogic.makeComparator())
            .build();

    public static final MultiblockRegistration<FlotationCellLogic.State> FLOTATION_CELL =
            metal(new FlotationCellLogic(), "flotation_cell")
            .structure(() -> IMMultiblocks.FLOTATION_CELL)
            .gui(IMMenuTypes.FLOTATION_CELL)
            .redstone(s -> s.rsState, FlotationCellLogic.REDSTONE_POS)
            .build();

    public static final MultiblockRegistration<HydrocycloneLogic.State> HYDROCYCLONE =
            metal(new HydrocycloneLogic(), "hydrocyclone")
                    .structure(() -> IMMultiblocks.HYDROCYCLONE)
                    .gui(IMMenuTypes.HYDROCYCLONE)
                    .redstone(s -> s.rsState, HydrocycloneLogic.REDSTONE_POS)
                    .build();

    public static final MultiblockRegistration<ThickenerLogic.State> THICKENER =
            metal(new ThickenerLogic(), "thickener")
                    .structure(() -> IMMultiblocks.THICKENER)
                    .gui(IMMenuTypes.THICKENER)
                    .redstone(s -> s.rsState, ThickenerLogic.REDSTONE_POS)
                    .build();

    public static final MultiblockRegistration<EliteBlastFurnaceLogic.State> ELITE_BLAST_FURNACE =
            metal(new EliteBlastFurnaceLogic(), "elite_blast_furnace")
                    .structure(() -> IMMultiblocks.ELITE_BLAST_FURNACE)
                    .gui(IMMenuTypes.ELITE_BLAST_FURNACE)
                    .redstone(s -> s.rsState, EliteBlastFurnaceLogic.REDSTONE_POS)
                    .build();

    public static final MultiblockRegistration<HotAirFurnaceLogic.State> HOT_AIR_FURNACE =
            metal(new HotAirFurnaceLogic(), "hot_air_furnace")
                    .structure(() -> IMMultiblocks.HOT_AIR_FURNACE)
                    .gui(IMMenuTypes.HOT_AIR_FURNACE)
                    .redstone(s -> s.rsState, HotAirFurnaceLogic.REDSTONE_POS)
                    .build();

    public static final MultiblockRegistration<ContinuousCastingMachineLogic.State> CONTINUOUS_CASTING_MACHINE =
            metal(new ContinuousCastingMachineLogic(), "continuous_casting_machine")
                    .structure(() -> IMMultiblocks.CONTINUOUS_CASTING_MACHINE)
                    .gui(IMMenuTypes.CONTINUOUS_CASTING_MACHINE)
                    .redstone(s -> s.rsState, ContinuousCastingMachineLogic.REDSTONE_POS)
                    .build();

    public static final MultiblockRegistration<AdvancedCokeOvenLogic.State> ADVANCED_COKE_OVEN =
            metal(new AdvancedCokeOvenLogic(), "advanced_coke_oven")
                    .structure(() -> IMMultiblocks.ADVANCED_COKE_OVEN)
                    .redstoneAware()
                    .gui(IMMenuTypes.ADVANCED_COKE_OVEN)
                    .build();

    private static <S extends IMultiblockState>
    IMMultiblockBuilder<S> metal(IMultiblockLogic<S> logic, String name)
    {
        return new IMMultiblockBuilder<>(logic, name)
                .defaultBEs(BE_REGISTER)
                .defaultBlock(BLOCK_REGISTER, ITEM_REGISTER, IEBlocks.METAL_PROPERTIES_NO_OCCLUSION.get());
    }

    public static void init(IEventBus bus)
    {
        BLOCK_REGISTER.register(bus);
        ITEM_REGISTER.register(bus);
        BE_REGISTER.register(bus);
        IMMultiblockBuilder.handleModBusRegistrations(bus);
    }
}
