// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network.packet;

import com.reiasu.reiparticlesapi.renderer.RenderEntity;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PacketRenderEntityS2CTest {
    @Test
    void shouldRoundTripSerializedRenderEntityPayload() {
        TestRenderEntity entity = new TestRenderEntity();
        entity.setPos(new Vec3(1.0, 2.0, 3.0));
        entity.setRenderRange(80.0);
        entity.setAge(7);
        PacketRenderEntityS2C source = PacketRenderEntityS2C.ofSync(entity);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        PacketRenderEntityS2C.encode(source, buf);
        PacketRenderEntityS2C decoded = PacketRenderEntityS2C.decode(buf);

        assertEquals(PacketRenderEntityS2C.Method.TOGGLE, decoded.getMethod());
        assertEquals(source.getUuid(), decoded.getUuid());
        assertEquals(source.getId(), decoded.getId());
        assertTrue(decoded.getEntityData().length > 0);
        assertArrayEquals(source.getEntityData(), decoded.getEntityData());
    }

    private static final class TestRenderEntity extends RenderEntity {
        private static final ResourceLocation ID = new ResourceLocation("reiparticlesapi", "packet_render_entity");

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