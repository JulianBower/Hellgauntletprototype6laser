package com.hellgauntlet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = HellGauntletMod.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (ClientSetup.TOGGLE_LASER.consumeClick()) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && (player.getMainHandItem().getItem() instanceof GauntletItem
                    || player.getOffhandItem().getItem() instanceof GauntletItem)) {
                PacketDistributor.sendToServer(new TogglePayload());
            }
        }
    }
}
