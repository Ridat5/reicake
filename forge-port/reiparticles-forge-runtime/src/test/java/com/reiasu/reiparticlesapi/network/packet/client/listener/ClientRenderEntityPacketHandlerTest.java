// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network.packet.client.listener;

import com.reiasu.reiparticlesapi.network.packet.PacketRenderEntityS2C;
import com.reiasu.reiparticlesapi.renderer.RenderEntity;
import com.reiasu.reiparticlesapi.renderer.client.ClientRenderEntityManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ClientRenderEntityPacketHandlerTest {
    @AfterEach
    void cleanup() {
        ClientRenderEntityManager.INSTANCE.clear();
    }

    @Test
    void shouldRemoveTrackedEntityWithoutCodec() {
        TestRenderEntity entity = new TestRenderEntity();
        ClientRenderEntityManager.INSTANCE.add(entity);

        ClientRenderEntityPacketHandler.receive(new PacketRenderEntityS2C(
                entity.getUuid(),
                new byte[0],
                new ResourceLocation("reiparticlesruntime", "unknown_render_codec"),
                PacketRenderEntityS2C.Method.REMOVE
        ));

        assertNull(ClientRenderEntityManager.INSTANCE.getFrom(entity.getUuid()));
    }

    @Test
    void shouldCreateEntityFromSerializedPacketData() {
        ClientRenderEntityManager.INSTANCE.registerCodec(TestRenderEntity.ID, data ->
                TestRenderEntity.decode(new FriendlyByteBuf(Unpooled.wrappedBuffer(data))));
        TestRenderEntity source = new TestRenderEntity();
        source.setPos(new Vec3(3.0, 4.0, 5.0));
        source.setRenderRange(72.0);
        source.setAge(9);

        ClientRenderEntityPacketHandler.receive(PacketRenderEntityS2C.ofSpawn(source));

        RenderEntity created = ClientRenderEntityManager.INSTANCE.getFrom(source.getUuid());
        assertNotNull(created);
        assertEquals(source.getPos(), created.getPos());
        assertEquals(source.getRenderRange(), created.getRenderRange());
        assertEquals(source.getAge(), created.getAge());
    }

    private static final class TestRenderEntity extends RenderEntity {
        private static final ResourceLocation ID = new ResourceLocation("reiparticlesruntime", "client_render_entity_test");

        public static TestRenderEntity decode(FriendlyByteBuf buf) {
            TestRenderEntity entity = new TestRenderEntity();
            RenderEntity.decodeBase(entity, buf);
            return entity;
        }

        @Override
        public void clientTick() {
        }

        @Override
        public void serverTick() {
        }

        @Override
        public ResourceLocation getRenderID() {
            return ID;
        }
    }
}