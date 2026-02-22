package com.reiasu.reiparticlesapi.network.packet;

import com.reiasu.reiparticlesapi.network.packet.client.listener.ClientParticleEmittersPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PacketParticleEmittersS2C(String emitterID, byte[] emitterData, PacketType type) {
    public enum PacketType {
        CHANGE_OR_CREATE(0),
        REMOVE(1);

        private final int id;

        PacketType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static PacketType fromID(int id) {
            return switch (id) {
                case 0 -> CHANGE_OR_CREATE;
                case 1 -> REMOVE;
                default -> CHANGE_OR_CREATE;
            };
        }
    }

    public static void encode(PacketParticleEmittersS2C packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.type.getId());
        buf.writeUtf(packet.emitterID);
        buf.writeInt(packet.emitterData.length);
        buf.writeBytes(packet.emitterData);
    }

    public static PacketParticleEmittersS2C decode(FriendlyByteBuf buf) {
        PacketType packetType = PacketType.fromID(buf.readInt());
        String emitterID = buf.readUtf();
        int size = buf.readInt();
        byte[] data = new byte[size];
        buf.readBytes(data);
        return new PacketParticleEmittersS2C(emitterID, data, packetType);
    }

    public static void handle(PacketParticleEmittersS2C packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientParticleEmittersPacketHandler.receive(packet)));
        context.setPacketHandled(true);
    }
}
