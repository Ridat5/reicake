// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network.packet;

import com.reiasu.reiparticlesapi.network.packet.client.listener.ClientParticlePacketHandler;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PacketParticleS2C(ParticleOptions type, Vec3 pos, Vec3 velocity) {
    public static void encode(PacketParticleS2C packet, FriendlyByteBuf buf) {
        ResourceLocation id = BuiltInRegistries.PARTICLE_TYPE.getKey(packet.type.getType());
        buf.writeResourceLocation(id);
        packet.type.writeToNetwork(buf);
        buf.writeDouble(packet.pos.x);
        buf.writeDouble(packet.pos.y);
        buf.writeDouble(packet.pos.z);
        buf.writeDouble(packet.velocity.x);
        buf.writeDouble(packet.velocity.y);
        buf.writeDouble(packet.velocity.z);
    }

    public static PacketParticleS2C decode(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        ParticleType<?> particleType = BuiltInRegistries.PARTICLE_TYPE.get(id);
        if (particleType == null) {
            particleType = ParticleTypes.END_ROD;
        }
        ParticleOptions options = readParticleOptions(particleType, buf);
        Vec3 pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        Vec3 velocity = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        return new PacketParticleS2C(options, pos, velocity);
    }

    public static void handle(PacketParticleS2C packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientParticlePacketHandler.receive(packet)));
        context.setPacketHandled(true);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static ParticleOptions readParticleOptions(ParticleType<?> type, FriendlyByteBuf buf) {
        return ((ParticleOptions.Deserializer) type.getDeserializer()).fromNetwork((ParticleType) type, buf);
    }
}
