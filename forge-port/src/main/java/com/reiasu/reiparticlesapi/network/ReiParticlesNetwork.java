// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network;

import com.reiasu.reiparticlesapi.ReiParticlesAPIForge;
import com.reiasu.reiparticlesapi.network.packet.CameraShakeS2CPacket;
import com.reiasu.reiparticlesapi.network.packet.PacketCameraShakeS2C;
import com.reiasu.reiparticlesapi.network.packet.PacketDisplayEntityS2C;
import com.reiasu.reiparticlesapi.network.packet.PacketKeyActionC2S;
import com.reiasu.reiparticlesapi.network.packet.PacketParticleCompositionS2C;
import com.reiasu.reiparticlesapi.network.packet.PacketParticleEmittersS2C;
import com.reiasu.reiparticlesapi.network.packet.PacketParticleGroupS2C;
import com.reiasu.reiparticlesapi.network.packet.PacketParticleS2C;
import com.reiasu.reiparticlesapi.network.packet.PacketParticleStyleS2C;
import com.reiasu.reiparticlesapi.network.packet.PacketRenderEntityS2C;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;

public final class ReiParticlesNetwork {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int PROTOCOL_VERSION = 1;
    private static int packetId = 0;

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(ReiParticlesAPIForge.MOD_ID, "main"))
            .networkProtocolVersion(() -> Integer.toString(PROTOCOL_VERSION))
            .clientAcceptedVersions(v -> true)
            .serverAcceptedVersions(v -> true)
            .simpleChannel();

    private ReiParticlesNetwork() {
    }

    public static void init() {
        CHANNEL.registerMessage(packetId++, CameraShakeS2CPacket.class, CameraShakeS2CPacket::encode, CameraShakeS2CPacket::decode, CameraShakeS2CPacket::handle);
        CHANNEL.registerMessage(packetId++, PacketCameraShakeS2C.class, PacketCameraShakeS2C::encode, PacketCameraShakeS2C::decode, PacketCameraShakeS2C::handle);
        CHANNEL.registerMessage(packetId++, PacketParticleS2C.class, PacketParticleS2C::encode, PacketParticleS2C::decode, PacketParticleS2C::handle);
        CHANNEL.registerMessage(packetId++, PacketParticleEmittersS2C.class, PacketParticleEmittersS2C::encode, PacketParticleEmittersS2C::decode, PacketParticleEmittersS2C::handle);
        CHANNEL.registerMessage(packetId++, PacketParticleCompositionS2C.class, PacketParticleCompositionS2C::encode, PacketParticleCompositionS2C::decode, PacketParticleCompositionS2C::handle);
        CHANNEL.registerMessage(packetId++, PacketDisplayEntityS2C.class, PacketDisplayEntityS2C::encode, PacketDisplayEntityS2C::decode, PacketDisplayEntityS2C::handle);
        CHANNEL.registerMessage(packetId++, PacketParticleStyleS2C.class, PacketParticleStyleS2C::encode, PacketParticleStyleS2C::decode, PacketParticleStyleS2C::handle);
        CHANNEL.registerMessage(packetId++, PacketParticleGroupS2C.class, PacketParticleGroupS2C::encode, PacketParticleGroupS2C::decode, PacketParticleGroupS2C::handle);
        CHANNEL.registerMessage(packetId++, PacketRenderEntityS2C.class, PacketRenderEntityS2C::encode, PacketRenderEntityS2C::decode, PacketRenderEntityS2C::handle);
        CHANNEL.registerMessage(packetId++, PacketKeyActionC2S.class, PacketKeyActionC2S::encode, PacketKeyActionC2S::decode, PacketKeyActionC2S::handle);
    }

    public static void sendTo(ServerPlayer player, Object packet) {
        try {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Player {} lacks channel — packet {} dropped",
                    player.getName().getString(), packet.getClass().getSimpleName());
        } catch (RuntimeException e) {
            LOGGER.debug("Failed to send packet {} to {}: {}", packet.getClass().getSimpleName(),
                    player.getName().getString(), e.getMessage());
        }
    }
}
