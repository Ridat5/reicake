// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network.packet;

import com.reiasu.reiparticlesapi.network.packet.client.listener.ClientDisplayEntityPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record PacketDisplayEntityS2C(UUID uuid, String type, byte[] data) {
    public static void encode(PacketDisplayEntityS2C packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.type);
        buf.writeUUID(packet.uuid);
        buf.writeInt(packet.data.length);
        buf.writeBytes(packet.data);
    }

    public static PacketDisplayEntityS2C decode(FriendlyByteBuf buf) {
        String type = buf.readUtf();
        UUID uuid = buf.readUUID();
        int size = buf.readInt();
        byte[] data = new byte[size];
        buf.readBytes(data);
        return new PacketDisplayEntityS2C(uuid, type, data);
    }

    public static void handle(PacketDisplayEntityS2C packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientDisplayEntityPacketHandler.receive(packet)));
        context.setPacketHandled(true);
    }
}
