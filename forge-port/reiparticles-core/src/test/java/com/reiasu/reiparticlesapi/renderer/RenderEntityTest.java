/*
 * Copyright (C) 2025 Reiasu
 *
 * This file is part of ReiParticlesAPI.
 *
 * ReiParticlesAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * ReiParticlesAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ReiParticlesAPI. If not, see <https://www.gnu.org/licenses/>.
 */
// SPDX-License-Identifier: LGPL-3.0-only
package com.reiasu.reiparticlesapi.renderer;

import com.reiasu.reiparticlesapi.renderer.server.ServerRenderEntityManager;
import com.reiasu.reiparticlesapi.testutil.UnsafeAllocator;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RenderEntityTest {
    @AfterEach
    void cleanup() {
        ServerRenderEntityManager.INSTANCE.getEntities().clear();
        ServerRenderEntityManager.INSTANCE.getPlayerViewable().clear();
    }

    @Test
    void shouldBindWorldAndPositionWhenSpawningInWorld() {
        ServerLevel level = UnsafeAllocator.allocate(ServerLevel.class);
        Vec3 pos = new Vec3(-5.0, 72.0, 11.0);
        TestRenderEntity entity = new TestRenderEntity();

        entity.spawnInWorld(level, pos);

        assertSame(level, entity.getWorld());
        assertEquals(pos, entity.getPos());
        assertTrue(ServerRenderEntityManager.INSTANCE.getEntities().containsKey(entity.getUuid()));
    }

    @Test
    void shouldEncodeDecodeAndLoadBaseRenderState() {
        TestRenderEntity source = new TestRenderEntity();
        source.setPos(new Vec3(4.0, 5.0, 6.0));
        source.setRenderRange(96.0);
        source.setInit(true);
        source.setAlwaysToggle(true);
        source.setSyncOnce(true);
        source.setLastRenderPos(new Vec3(-1.0, 2.0, -3.0));
        source.setAge(14);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(source.encodeToBytes()));
        TestRenderEntity decoded = TestRenderEntity.decode(buf);
        TestRenderEntity updated = new TestRenderEntity();
        updated.loadProfileFromEntity(decoded);

        assertEquals(source.getPos(), decoded.getPos());
        assertEquals(source.getRenderRange(), decoded.getRenderRange());
        assertEquals(source.getInit(), decoded.getInit());
        assertEquals(source.getAlwaysToggle(), decoded.getAlwaysToggle());
        assertEquals(source.getSyncOnce(), decoded.getSyncOnce());
        assertEquals(source.getLastRenderPos(), decoded.getLastRenderPos());
        assertEquals(source.getAge(), decoded.getAge());
        assertEquals(source.getUuid(), decoded.getUuid());
        assertEquals(decoded.getPos(), updated.getPos());
        assertEquals(decoded.getLastRenderPos(), updated.getLastRenderPos());
        assertEquals(decoded.getSyncOnce(), updated.getSyncOnce());
    }

    private static final class TestRenderEntity extends RenderEntity {
        private static final ResourceLocation ID = new ResourceLocation("reiparticlesapi", "test_render_entity");

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