// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network.packet.client.listener;

import com.reiasu.reiparticlesapi.network.packet.PacketRenderEntityS2C;
import com.reiasu.reiparticlesapi.renderer.RenderEntity;
import com.reiasu.reiparticlesapi.renderer.client.ClientRenderEntityManager;
import net.minecraft.client.Minecraft;

import java.util.function.Function;

public final class ClientRenderEntityPacketHandler {
    private ClientRenderEntityPacketHandler() {
    }

    public static void receive(PacketRenderEntityS2C packet) {
        PacketRenderEntityS2C.Method method = packet.getMethod();
        if (method == PacketRenderEntityS2C.Method.REMOVE) {
            ClientRenderEntityManager.INSTANCE.remove(packet.getUuid());
            return;
        }

        Function<byte[], RenderEntity> codec = ClientRenderEntityManager.INSTANCE.getCodecFromID(packet.getId());
        if (codec == null) {
            return;
        }

        RenderEntity entity = codec.apply(packet.getEntityData());
        if (entity == null) {
            return;
        }
        entity.setUuid(packet.getUuid());

        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.level != null) {
            entity.setWorld(mc.level);
        }

        switch (method) {
            case CREATE -> ClientRenderEntityManager.INSTANCE.add(entity);
            case TOGGLE -> {
                RenderEntity existing = ClientRenderEntityManager.INSTANCE.getFrom(packet.getUuid());
                if (existing != null) {
                    existing.loadProfileFromEntity(entity);
                    if (mc != null && mc.level != null) {
                        existing.setWorld(mc.level);
                    }
                }
            }
            case REMOVE -> ClientRenderEntityManager.INSTANCE.remove(packet.getUuid());
        }
    }
}