// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network.packet;

import com.reiasu.reiparticlesapi.network.packet.client.listener.ClientCameraShakeHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PacketCameraShakeS2C(double range, Vec3 origin, double amplitude, int tick) {
    public static void encode(PacketCameraShakeS2C packet, FriendlyByteBuf buf) {
        buf.writeDouble(packet.range);
        buf.writeDouble(packet.origin.x);
        buf.writeDouble(packet.origin.y);
        buf.writeDouble(packet.origin.z);
        buf.writeDouble(packet.amplitude);
        buf.writeInt(packet.tick);
    }

    public static PacketCameraShakeS2C decode(FriendlyByteBuf buf) {
        double range = buf.readDouble();
        Vec3 origin = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        double amplitude = buf.readDouble();
        int tick = buf.readInt();
        return new PacketCameraShakeS2C(range, origin, amplitude, tick);
    }

    public static void handle(PacketCameraShakeS2C packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    ClientCameraShakeHandler.receive(packet);
                })
        );
        context.setPacketHandled(true);
    }
}
