package com.hellgauntlet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Sent from the client to the server when X is pressed, to switch laser mode on or off. */
public record TogglePayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TogglePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HellGauntletMod.MODID, "toggle_laser"));
    public static final StreamCodec<ByteBuf, TogglePayload> STREAM_CODEC = StreamCodec.unit(new TogglePayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TogglePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> GauntletItem.toggleLaser(context.player()));
    }
}
