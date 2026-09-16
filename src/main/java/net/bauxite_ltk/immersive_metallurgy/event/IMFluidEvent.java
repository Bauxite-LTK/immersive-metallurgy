package net.bauxite_ltk.immersive_metallurgy.event;

import net.bauxite_ltk.immersive_metallurgy.ImmersiveMetallurgy;
import net.bauxite_ltk.immersive_metallurgy.block.liquid.CanSolidifyLiquidBlockEntity;
import net.bauxite_ltk.immersive_metallurgy.fluid.FluidRendererExtension;
import net.bauxite_ltk.immersive_metallurgy.fluid.IMFluids;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;

import java.util.Objects;

@EventBusSubscriber(modid = ImmersiveMetallurgy.MOD_ID, value = Dist.CLIENT)
public class IMFluidEvent {
    public static final ResourceLocation WATER_STILL = ResourceLocation.fromNamespaceAndPath("minecraft","block/water_still");
    public static final ResourceLocation WATER_FLOW = ResourceLocation.fromNamespaceAndPath("minecraft","block/water_flow");

    public static final ResourceLocation BUBBLE_STILL = IMUtils.modRL("block/bubble_still");

    public static final ResourceLocation THICKLY_WATER_STILL = IMUtils.modRL("block/thickly_water_still");
    public static final ResourceLocation THICKLY_WATER_FLOW = IMUtils.modRL("block/thickly_water_flow");


    private static final ResourceLocation MOLTEN_STILL = IMUtils.modRL("block/molten_still");
    private static final ResourceLocation MOLTEN_FLOW = IMUtils.modRL("block/molten_flow");

    private static final ResourceLocation GAS_STILL = IMUtils.modRL("block/gas_still");
    private static final ResourceLocation GAS_FLOW = IMUtils.modRL("block/gas_flow");

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(IMFluids.MASON_PINE_SAP.getSource(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(IMFluids.MASON_PINE_SAP.getFlowing(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(IMFluids.TURPENTINE_OIL.getSource(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(IMFluids.TURPENTINE_OIL.getFlowing(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(IMFluids.TERPENIC_OIL.getSource(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(IMFluids.TERPENIC_OIL.getFlowing(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(IMFluids.HOT_AIR.getSource(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(IMFluids.HOT_AIR.getFlowing(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(IMFluids.WATER_GAS.getSource(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(IMFluids.WATER_GAS.getFlowing(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(IMFluids.BLAST_FURNACE_GAS.getSource(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(IMFluids.BLAST_FURNACE_GAS.getFlowing(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(IMFluids.COKE_OVEN_GAS.getSource(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(IMFluids.COKE_OVEN_GAS.getFlowing(), RenderType.translucent());
    }


    @SubscribeEvent
    public static void registerExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(
                new FluidRendererExtension(0xFF4c3d3d, CanSolidifyLiquidBlockEntity::getColorFromTickRemain, MOLTEN_STILL, MOLTEN_FLOW, null, null),
                IMFluids.MOLTEN_PIG_IRON.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFffd241, CanSolidifyLiquidBlockEntity::getColorFromTickRemain, MOLTEN_STILL, MOLTEN_FLOW, null, null),
                IMFluids.MOLTEN_GOLD.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFb44e3b, CanSolidifyLiquidBlockEntity::getColorFromTickRemain, MOLTEN_STILL, MOLTEN_FLOW, null, null),
                IMFluids.MOLTEN_COPPER.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFd4d2cb, CanSolidifyLiquidBlockEntity::getColorFromTickRemain, MOLTEN_STILL, MOLTEN_FLOW, null, null),
                IMFluids.MOLTEN_SILVER.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFF413d50, CanSolidifyLiquidBlockEntity::getColorFromTickRemain, MOLTEN_STILL, MOLTEN_FLOW, null, null),
                IMFluids.MOLTEN_LEAD.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFa3a092, CanSolidifyLiquidBlockEntity::getColorFromTickRemain, MOLTEN_STILL, MOLTEN_FLOW, null, null),
                IMFluids.MOLTEN_NICKEL.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFF3c482f, CanSolidifyLiquidBlockEntity::getColorFromTickRemain, MOLTEN_STILL, MOLTEN_FLOW, null, null),
                IMFluids.MOLTEN_URANIUM.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFbdc4c7, CanSolidifyLiquidBlockEntity::getColorFromTickRemain, MOLTEN_STILL, MOLTEN_FLOW, null, null),
                IMFluids.MOLTEN_ALUMINUM.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFffcaca, GAS_STILL, GAS_FLOW, null, null),
                IMFluids.HOT_AIR.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFF4b5051, GAS_STILL, GAS_FLOW, null, null),
                IMFluids.WATER_GAS.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFF4f3333, GAS_STILL, GAS_FLOW, null, null),
                IMFluids.BLAST_FURNACE_GAS.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFF444333, GAS_STILL, GAS_FLOW, null, null),
                IMFluids.COKE_OVEN_GAS.getType());


        event.registerFluidType(
                new FluidRendererExtension(0xFFb98051, WATER_STILL, WATER_FLOW, null, null),
                IMFluids.MASON_PINE_SAP.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFf3c56c, WATER_STILL, WATER_FLOW, null, null),
                IMFluids.TURPENTINE_OIL.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFffcb00, WATER_STILL, WATER_FLOW, null, null),
                IMFluids.TERPENIC_OIL.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFaf8a63, MOLTEN_STILL, MOLTEN_FLOW, null, null),
                IMFluids.RAW_IRON_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFaf8a63, WATER_STILL, WATER_FLOW, null, null),
                IMFluids.RAW_IRON_PROCESSED_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFaf8a63, BUBBLE_STILL, WATER_FLOW, null, null),
                IMFluids.RAW_IRON_CONCENTRATE_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFaf8a63, THICKLY_WATER_STILL, THICKLY_WATER_FLOW, null, null),
                IMFluids.RAW_IRON_TAILING_SLURRY.getType());


        event.registerFluidType(
                new FluidRendererExtension(0xFFffc62f, MOLTEN_STILL, MOLTEN_FLOW, null, null),
                IMFluids.RAW_GOLD_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFffc62f, WATER_STILL, WATER_FLOW, null, null),
                IMFluids.RAW_GOLD_PROCESSED_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFffc62f, BUBBLE_STILL, WATER_FLOW, null, null),
                IMFluids.RAW_GOLD_CONCENTRATE_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFffc62f, THICKLY_WATER_STILL, THICKLY_WATER_FLOW, null, null),
                IMFluids.RAW_GOLD_TAILING_SLURRY.getType());



        event.registerFluidType(
                new FluidRendererExtension(0xFFc16348, MOLTEN_STILL, MOLTEN_FLOW, null, null),
                IMFluids.RAW_COPPER_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFc16348, WATER_STILL, WATER_FLOW, null, null),
                IMFluids.RAW_COPPER_PROCESSED_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFc16348, BUBBLE_STILL, WATER_FLOW, null, null),
                IMFluids.RAW_COPPER_CONCENTRATE_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFc16348, THICKLY_WATER_STILL, THICKLY_WATER_FLOW, null, null),
                IMFluids.RAW_COPPER_TAILING_SLURRY.getType());


        event.registerFluidType(
                new FluidRendererExtension(0xFFd4d2cb, MOLTEN_STILL, MOLTEN_FLOW, null, null),
                IMFluids.RAW_SILVER_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFd4d2cb, WATER_STILL, WATER_FLOW, null, null),
                IMFluids.RAW_SILVER_PROCESSED_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFd4d2cb, BUBBLE_STILL, WATER_FLOW, null, null),
                IMFluids.RAW_SILVER_CONCENTRATE_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFd4d2cb, THICKLY_WATER_STILL, THICKLY_WATER_FLOW, null, null),
                IMFluids.RAW_SILVER_TAILING_SLURRY.getType());


        event.registerFluidType(
                new FluidRendererExtension(0xFF413d50, MOLTEN_STILL, MOLTEN_FLOW, null, null),
                IMFluids.RAW_LEAD_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFF413d50, WATER_STILL, WATER_FLOW, null, null),
                IMFluids.RAW_LEAD_PROCESSED_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFF413d50, BUBBLE_STILL, WATER_FLOW, null, null),
                IMFluids.RAW_LEAD_CONCENTRATE_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFF413d50, THICKLY_WATER_STILL, THICKLY_WATER_FLOW, null, null),
                IMFluids.RAW_LEAD_TAILING_SLURRY.getType());


        event.registerFluidType(
                new FluidRendererExtension(0xFF8a8854, MOLTEN_STILL, MOLTEN_FLOW, null, null),
                IMFluids.RAW_NICKEL_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFF8a8854, WATER_STILL, WATER_FLOW, null, null),
                IMFluids.RAW_NICKEL_PROCESSED_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFF8a8854, BUBBLE_STILL, WATER_FLOW, null, null),
                IMFluids.RAW_NICKEL_CONCENTRATE_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFF8a8854, THICKLY_WATER_STILL, THICKLY_WATER_FLOW, null, null),
                IMFluids.RAW_NICKEL_TAILING_SLURRY.getType());


        event.registerFluidType(
                new FluidRendererExtension(0xFF3c482f, MOLTEN_STILL, MOLTEN_FLOW, null, null),
                IMFluids.RAW_URANIUM_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFF3c482f, WATER_STILL, WATER_FLOW, null, null),
                IMFluids.RAW_URANIUM_PROCESSED_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFF3c482f, BUBBLE_STILL, WATER_FLOW, null, null),
                IMFluids.RAW_URANIUM_CONCENTRATE_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFF3c482f, THICKLY_WATER_STILL, THICKLY_WATER_FLOW, null, null),
                IMFluids.RAW_URANIUM_TAILING_SLURRY.getType());
        


        event.registerFluidType(
                new FluidRendererExtension(0xFFcf8e88, MOLTEN_STILL, MOLTEN_FLOW, null, null),
                IMFluids.RAW_ALUMINUM_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFcf8e88, WATER_STILL, WATER_FLOW, null, null),
                IMFluids.RAW_ALUMINUM_PROCESSED_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFcf8e88, BUBBLE_STILL, WATER_FLOW, null, null),
                IMFluids.RAW_ALUMINUM_CONCENTRATE_SLURRY.getType());

        event.registerFluidType(
                new FluidRendererExtension(0xFFcf8e88, THICKLY_WATER_STILL, THICKLY_WATER_FLOW, null, null),
                IMFluids.RAW_ALUMINUM_TAILING_SLURRY.getType());

    }

    @SubscribeEvent
    public static void registerColorHandlerItems(RegisterColorHandlersEvent.Item event){
        for (Fluid fluid : BuiltInRegistries.FLUID)
        {
            if (Objects.requireNonNull(BuiltInRegistries.FLUID.getKey(fluid)).getNamespace().equals(IMUtils.MOD_ID))
            {
                event.register(new DynamicFluidContainerModel.Colors(), fluid.getBucket());
            }
        }
    }
}
