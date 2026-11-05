package com.hellgauntlet;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(HellGauntletMod.MODID)
public class HellGauntletMod {
    public static final String MODID = "hellgauntlet";

    public HellGauntletMod(IEventBus modBus) {
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        modBus.addListener(this::addCreative);
        modBus.addListener(this::registerPayloads);
        NeoForge.EVENT_BUS.addListener(VoidBlasts::onLevelTick);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.GAUNTLET);
        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(TogglePayload.TYPE, TogglePayload.STREAM_CODEC, TogglePayload::handle);
    }
}
