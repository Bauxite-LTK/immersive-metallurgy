package net.bauxite_ltk.immersive_metallurgy.event;


import blusunrize.immersiveengineering.api.client.ieobj.IEOBJCallbacks;
import net.bauxite_ltk.immersive_metallurgy.Config;
import net.bauxite_ltk.immersive_metallurgy.ImmersiveMetallurgy;
import net.bauxite_ltk.immersive_metallurgy.block.IMBlockEntities;
import net.bauxite_ltk.immersive_metallurgy.block.liquid.CanSolidifyLiquidBlockEntity;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.IMMultiblockLogic;
import net.bauxite_ltk.immersive_metallurgy.callback.CastingChannelCallbacks;
import net.bauxite_ltk.immersive_metallurgy.callback.ElectricCableCallbacks;
import net.bauxite_ltk.immersive_metallurgy.fluid.FluidRendererExtension;
import net.bauxite_ltk.immersive_metallurgy.fluid.IMFluids;
import net.bauxite_ltk.immersive_metallurgy.gui.IMMenuTypes;
import net.bauxite_ltk.immersive_metallurgy.gui.multiblock.*;
import net.bauxite_ltk.immersive_metallurgy.particle.DripSapParticles;
import net.bauxite_ltk.immersive_metallurgy.particle.IMParticleTypes;
import net.bauxite_ltk.immersive_metallurgy.render.*;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;

import java.util.Objects;
import java.util.function.Supplier;

@EventBusSubscriber(modid = ImmersiveMetallurgy.MOD_ID, value = Dist.CLIENT)
public class IMClient {
    public static void modConstruction(){
        IEOBJCallbacks.register(IMUtils.modRL("electric_cable"), ElectricCableCallbacks.INSTANCE);
        IEOBJCallbacks.register(IMUtils.modRL("casting_channel"), CastingChannelCallbacks.INSTANCE);
        //IEOBJCallbacks.register(IMUtils.modRL("electric_cable_mv"), ElectricCableCallbacks.INSTANCE);
        ImmersiveMetallurgy.LOGGER.info("ImmersiveMetallurgy register callbacks");
    }





    @SubscribeEvent
    public static void registerRenders(EntityRenderersEvent.RegisterRenderers event)
    {
        registerBERenderNoContext(event, IMMultiblockLogic.BALL_MILL.masterBE(), BallMillRender::new);
        registerBERenderNoContext(event, IMMultiblockLogic.FLOTATION_CELL.masterBE(), FlotationCellRender::new);
        registerBERenderNoContext(event, IMMultiblockLogic.THICKENER.masterBE(), ThickenerRender::new);
        registerBERenderNoContext(event, IMBlockEntities.SAP_COLLECTOR.get(), SapCollectorRender::new);
        registerBERenderNoContext(event, IMBlockEntities.CASTING_CHANNEL.get(), CastingChannelBlockEntityRender::new);
        registerBERenderNoContext(event, IMMultiblockLogic.CONTINUOUS_CASTING_MACHINE.masterBE(), ContinuousCastingMachineRenderer::new);
        registerBERenderNoContext(event, IMMultiblockLogic.ADVANCED_COKE_OVEN.masterBE(), AdvancedCokeOvenRenderer::new);

        //registerBERenderNoContext(event, IMBlockEntities.ELECTRIC_CABLE.get(), ElectricCableSelectionRenderer::new);
    }


    private static <T extends BlockEntity>
    void registerBERenderNoContext(
            EntityRenderersEvent.RegisterRenderers event, Supplier<BlockEntityType<? extends T>> type, Supplier<BlockEntityRenderer<T>> render
    )
    {
        registerBERenderNoContext(event, type.get(), render);
    }

    private static <T extends BlockEntity>
    void registerBERenderNoContext(
            EntityRenderersEvent.RegisterRenderers event, BlockEntityType<? extends T> type, Supplier<BlockEntityRenderer<T>> render
    )
    {
        event.registerBlockEntityRenderer(type, $ -> render.get());
    }

    @SubscribeEvent
    public static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event)
    {
        BallMillRender.BARREL = new IMDynamicModel(BallMillRender.NAME);
        FlotationCellRender.BLADE = new IMDynamicModel(FlotationCellRender.NAME);
        ThickenerRender.AGITATOR = new IMDynamicModel(ThickenerRender.NAME);
        ContinuousCastingMachineRenderer.METAL = new IMDynamicModel(ContinuousCastingMachineRenderer.NAME);
        AdvancedCokeOvenRenderer.DOOR = new IMDynamicModel(AdvancedCokeOvenRenderer.DOOR_NAME);

    }

    @SubscribeEvent
    public static void registerContainersAndScreens(RegisterMenuScreensEvent event)
    {
        event.register(IMMenuTypes.BALL_MILL.getType(), BallMillScreen::new);
        event.register(IMMenuTypes.FLOTATION_CELL.getType(), FlotationCellScreen::new);
        event.register(IMMenuTypes.HYDROCYCLONE.getType(), HydrocycloneScreen::new);
        event.register(IMMenuTypes.THICKENER.getType(), ThickenerScreen::new);
        event.register(IMMenuTypes.ELITE_BLAST_FURNACE.getType(), EliteBlastFurnaceScreen::new);
        event.register(IMMenuTypes.HOT_AIR_FURNACE.getType(), HotAirFurnaceScreen::new);
        event.register(IMMenuTypes.CONTINUOUS_CASTING_MACHINE.getType(), ContinuousCastingMachineScreen::new);
        event.register(IMMenuTypes.ADVANCED_COKE_OVEN.getType(), AdvancedCokeOvenScreen::new);
    }

    @SubscribeEvent
    public static void registerProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(
                IMParticleTypes.DRIPPING_SAP.get(),
                DripSapParticles.Provider::new
        );
    }

    @SubscribeEvent
    public static void onConfigReloading(ModConfigEvent.Reloading event){
        Config.MACHINES.populateAPI();
    }
}
