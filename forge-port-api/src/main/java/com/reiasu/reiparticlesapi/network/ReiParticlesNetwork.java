package com.reiasu.reiparticlesapi.network;

import com.reiasu.reiparticlesapi.ReiParticlesAPIForge;
import com.reiasu.reiparticlesapi.compat.version.VersionBridgeRegistry;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ReiParticlesNetwork {
    private static final int PROTOCOL_VERSION = 1;
    private static int packetId = 0;

    public static final SimpleChannel CHANNEL = VersionBridgeRegistry.network()
            .createSimpleChannel(ReiParticlesAPIForge.MOD_ID, "main", PROTOCOL_VERSION);

    private ReiParticlesNetwork() {
    }

    public static void init() {
        VersionBridgeRegistry.network().registerClientboundMessage(
                CHANNEL,
                CameraShakeS2CPacket.class,
                packetId++,
                CameraShakeS2CPacket::encode,
                CameraShakeS2CPacket::decode,
                CameraShakeS2CPacket::handle
        );

        VersionBridgeRegistry.network().registerClientboundMessage(
                CHANNEL,
                PacketCameraShakeS2C.class,
                packetId++,
                PacketCameraShakeS2C::encode,
                PacketCameraShakeS2C::decode,
                PacketCameraShakeS2C::handle
        );
        VersionBridgeRegistry.network().registerClientboundMessage(
                CHANNEL,
                PacketParticleS2C.class,
                packetId++,
                PacketParticleS2C::encode,
                PacketParticleS2C::decode,
                PacketParticleS2C::handle
        );
        VersionBridgeRegistry.network().registerClientboundMessage(
                CHANNEL,
                PacketParticleEmittersS2C.class,
                packetId++,
                PacketParticleEmittersS2C::encode,
                PacketParticleEmittersS2C::decode,
                PacketParticleEmittersS2C::handle
        );
        VersionBridgeRegistry.network().registerClientboundMessage(
                CHANNEL,
                PacketParticleCompositionS2C.class,
                packetId++,
                PacketParticleCompositionS2C::encode,
                PacketParticleCompositionS2C::decode,
                PacketParticleCompositionS2C::handle
        );
        VersionBridgeRegistry.network().registerClientboundMessage(
                CHANNEL,
                PacketDisplayEntityS2C.class,
                packetId++,
                PacketDisplayEntityS2C::encode,
                PacketDisplayEntityS2C::decode,
                PacketDisplayEntityS2C::handle
        );
        VersionBridgeRegistry.network().registerClientboundMessage(
                CHANNEL,
                PacketParticleStyleS2C.class,
                packetId++,
                PacketParticleStyleS2C::encode,
                PacketParticleStyleS2C::decode,
                PacketParticleStyleS2C::handle
        );
        VersionBridgeRegistry.network().registerClientboundMessage(
                CHANNEL,
                PacketParticleGroupS2C.class,
                packetId++,
                PacketParticleGroupS2C::encode,
                PacketParticleGroupS2C::decode,
                PacketParticleGroupS2C::handle
        );
        VersionBridgeRegistry.network().registerClientboundMessage(
                CHANNEL,
                PacketRenderEntityS2C.class,
                packetId++,
                PacketRenderEntityS2C::encode,
                PacketRenderEntityS2C::decode,
                PacketRenderEntityS2C::handle
        );
        VersionBridgeRegistry.network().registerServerboundMessage(
                CHANNEL,
                PacketKeyActionC2S.class,
                packetId++,
                PacketKeyActionC2S::encode,
                PacketKeyActionC2S::decode,
                PacketKeyActionC2S::handle
        );
    }

    public static void sendTo(ServerPlayer player, Object packet) {
        try {
            VersionBridgeRegistry.network().sendToPlayer(CHANNEL, player, packet);
        } catch (RuntimeException ignored) {
            // Allow server-only install: skip optional client packet when remote lacks this channel.
        }
    }
}
