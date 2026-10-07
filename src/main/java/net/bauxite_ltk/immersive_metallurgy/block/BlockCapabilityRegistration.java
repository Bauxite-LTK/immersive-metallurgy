package net.bauxite_ltk.immersive_metallurgy.block;


import net.bauxite_ltk.immersive_metallurgy.ImmersiveMetallurgy;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.ElectricCableBlockEntity;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.RFCableBlockEntity;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.casting_channel.CastingChannelBlockEntity;
import net.bauxite_ltk.immersive_metallurgy.block.sapCollector.SapCollectorBlockEntity;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@EventBusSubscriber(modid = IMUtils.MOD_ID)
public class BlockCapabilityRegistration {
    @SubscribeEvent
    public static void registerBlockCapabilities(RegisterCapabilitiesEvent event){
        RFCableBlockEntity.registerCapabilities(forType(event,IMBlockEntities.ELECTRIC_CABLE_LV));
        RFCableBlockEntity.registerCapabilities(forType(event,IMBlockEntities.ELECTRIC_CABLE_MV));
        RFCableBlockEntity.registerCapabilities(forType(event,IMBlockEntities.ELECTRIC_CABLE_HV));
        SapCollectorBlockEntity.registerCapabilities(forType(event,IMBlockEntities.SAP_COLLECTOR));
        CastingChannelBlockEntity.registerCapabilities(forType(event,IMBlockEntities.CASTING_CHANNEL));
        ImmersiveMetallurgy.LOGGER.info("BlockCapabilityRegistration: Called registerBlockCapabilities");
    }

    private static <BE extends BlockEntity> BlockCapabilityRegistration.BECapabilityRegistrar<BE> forType(
            RegisterCapabilitiesEvent ev, Supplier<BlockEntityType<BE>> type
    )
    {
        return new BlockCapabilityRegistration.BECapabilityRegistrar<>()
        {
            @Override
            public <C, T> void register(BlockCapability<T, C> capability, ICapabilityProvider<? super BE, C, T> provider)
            {
                ev.registerBlockEntity(capability, type.get(), provider);
            }
        };
    }

    public interface BECapabilityRegistrar<BE>
    {
        <C, T> void register(BlockCapability<T, C> capability, ICapabilityProvider<? super BE, C, T> provider);

        default <C, T> void registerOnContext(
                BlockCapability<T, C> capability, Function<? super BE, T> getValue, C onContext
        )
        {
            register(capability, (be, ctx) -> Objects.equals(onContext, ctx)?getValue.apply(be): null);
        }

        default <T> void registerAllContexts(BlockCapability<T, ?> capability, Function<? super BE, T> getValue)
        {
            register(capability, (be, ctx) -> getValue.apply(be));
        }
    }
}
