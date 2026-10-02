package net.bauxite_ltk.immersive_metallurgy;

import blusunrize.immersiveengineering.api.ManualHelper;
import net.bauxite_ltk.immersive_metallurgy.block.IMBlockEntities;
import net.bauxite_ltk.immersive_metallurgy.block.IMBlocks;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.IMMultiblockBuilder;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.IMMultiblockLogic;
import net.bauxite_ltk.immersive_metallurgy.block.multiblock.IMMultiblocks;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.global.GlobalCableConnectionData;
import net.bauxite_ltk.immersive_metallurgy.crafting.IMRecipeSerializers;
import net.bauxite_ltk.immersive_metallurgy.crafting.IMRecipeType;
import net.bauxite_ltk.immersive_metallurgy.event.IMClient;
import net.bauxite_ltk.immersive_metallurgy.event.IMListeners;
import net.bauxite_ltk.immersive_metallurgy.event.IMManualHelper;
import net.bauxite_ltk.immersive_metallurgy.fluid.IMFluids;
import net.bauxite_ltk.immersive_metallurgy.gui.IMMenuTypes;
import net.bauxite_ltk.immersive_metallurgy.item.IMItems;
import net.bauxite_ltk.immersive_metallurgy.particle.IMParticleTypes;
import net.bauxite_ltk.immersive_metallurgy.worldgen.tree.IMTreePlacerTypes;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ImmersiveMetallurgy.MOD_ID)
public class ImmersiveMetallurgy {
    public static final String MOD_ID = "immersive_metallurgy";
    public static final Logger LOGGER = LogUtils.getLogger();




    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ImmersiveMetallurgy(IEventBus modEventBus, Dist dist, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        IMRecipeSerializers.init(modEventBus);

        IMMultiblocks.init();
        IMMultiblockLogic.init(modEventBus);
        IMMultiblockBuilder.handleModBusRegistrations(modEventBus);

        IMRecipeType.init(modEventBus);
        IMMenuTypes.init(modEventBus);

        IMBlocks.init(modEventBus);
        IMBlockEntities.init(modEventBus);
        IMItems.init(modEventBus);
        IMFluids.init(modEventBus);
        IMCreativeModTabs.init(modEventBus);
        IMTreePlacerTypes.init(modEventBus);
        IMParticleTypes.init(modEventBus);

        if(dist.isClient()){
            IMClient.modConstruction();
        }

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ImmersiveMetallurgy) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new IMListeners());

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        populateAPI();

    }


    public static void populateAPI(){
        Config.MACHINES.populateAPI();
        IMManualHelper.addIMElements(ManualHelper.IE_MANUAL_INSTANCE.get());
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");

        MinecraftServer world = event.getServer();
        var result =  GlobalCableConnectionData.ofGlobal(world);
        GlobalCableConnectionData.setInstance(result);
    }
}
