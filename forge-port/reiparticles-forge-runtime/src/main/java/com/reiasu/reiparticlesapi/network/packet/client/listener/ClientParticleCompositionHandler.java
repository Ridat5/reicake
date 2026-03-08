// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network.packet.client.listener;

import com.reiasu.reiparticlesapi.network.packet.PacketParticleCompositionS2C;
import com.reiasu.reiparticlesapi.network.particle.composition.ParticleComposition;
import com.reiasu.reiparticlesapi.network.particle.composition.SequencedParticleComposition;
import com.reiasu.reiparticlesapi.network.particle.composition.manager.ParticleCompositionManager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Function;

public final class ClientParticleCompositionHandler {
    private ClientParticleCompositionHandler() {
    }

    public static void receive(PacketParticleCompositionS2C packet) {
        ParticleComposition old = ParticleCompositionManager.INSTANCE.getClientView().get(packet.getUuid());
        if (packet.getDistanceRemove()) {
            if (old != null) {
                old.remove();
            }
            return;
        }

        Function<FriendlyByteBuf, ParticleComposition> decoder =
                ParticleCompositionManager.INSTANCE.getRegisteredTypes().get(packet.getType());
        if (decoder == null) {
            return;
        }
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(packet.getData()));
        ParticleComposition decoded = decoder.apply(buf);
        if (decoded == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        decoded.setWorld(minecraft.level);

        if (old == null) {
            ParticleCompositionManager.INSTANCE.addClient(decoded);
            return;
        }

        old.update(decoded);
        if (!(old instanceof SequencedParticleComposition)) {
            old.flush();
        }
    }
}
