// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network.packet.client.listener;

import com.reiasu.reiparticlesapi.display.DisplayEntity;
import com.reiasu.reiparticlesapi.display.DisplayEntityManager;
import com.reiasu.reiparticlesapi.network.packet.PacketDisplayEntityS2C;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Function;

public final class ClientDisplayEntityPacketHandler {
    private ClientDisplayEntityPacketHandler() {
    }

    public static void receive(PacketDisplayEntityS2C packet) {
        Function<FriendlyByteBuf, DisplayEntity> decoder =
                DisplayEntityManager.INSTANCE.getRegisteredTypes().get(packet.type());
        if (decoder == null) {
            return;
        }
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(packet.data()));
        DisplayEntity display = decoder.apply(buf);
        if (display == null) {
            return;
        }

        DisplayEntity old = DisplayEntityManager.INSTANCE.getClientView().get(packet.uuid());
        if (old == null) {
            DisplayEntityManager.INSTANCE.addClient(display);
            return;
        }
        old.update(display);
    }
}

