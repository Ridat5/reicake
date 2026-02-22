package com.reiasu.reiparticlesapi.compat.version;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface NetworkVersionBridge {
    SimpleChannel createSimpleChannel(String modId, String channelName, int protocolVersion);

    <M> void registerClientboundMessage(
            SimpleChannel channel,
            Class<M> messageType,
            int messageId,
            BiConsumer<M, FriendlyByteBuf> encoder,
            Function<FriendlyByteBuf, M> decoder,
            BiConsumer<M, Supplier<NetworkEvent.Context>> handler
    );

    default <M> void registerServerboundMessage(
            SimpleChannel channel,
            Class<M> messageType,
            int messageId,
            BiConsumer<M, FriendlyByteBuf> encoder,
            Function<FriendlyByteBuf, M> decoder,
            BiConsumer<M, Supplier<NetworkEvent.Context>> handler
    ) {
        registerClientboundMessage(channel, messageType, messageId, encoder, decoder, handler);
    }

    void sendToPlayer(SimpleChannel channel, ServerPlayer player, Object packet);
}
