// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.renderer.server;

import com.reiasu.reiparticlesapi.renderer.RenderEntity;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerRenderEntityManagerTest {
    @AfterEach
    void cleanup() {
        ServerRenderEntityManager.INSTANCE.getEntities().clear();
        ServerRenderEntityManager.INSTANCE.getPlayerViewable().clear();
    }

    @Test
    void shouldContinueServerTickAfterRenderEntityFailure() {
        FailingRenderEntity failing = new FailingRenderEntity();
        CountingRenderEntity healthy = new CountingRenderEntity();
        ServerRenderEntityManager.INSTANCE.spawn(failing);
        ServerRenderEntityManager.INSTANCE.spawn(healthy);

        ServerRenderEntityManager.INSTANCE.tick();

        assertEquals(1, healthy.ticks);
        assertEquals(1, ServerRenderEntityManager.INSTANCE.getEntities().size());
        assertSame(healthy, ServerRenderEntityManager.INSTANCE.getEntities().get(healthy.getUuid()));
    }

    @Test
    void shouldShardVisibilityChecksByTick() {
        assertTrue(ServerRenderEntityManager.shouldProcessPlayerIndex(0, 0));
        assertFalse(ServerRenderEntityManager.shouldProcessPlayerIndex(1, 0));
        assertTrue(ServerRenderEntityManager.shouldProcessPlayerIndex(1, 1));
    }

    @Test
    void shouldScaleLodIntervalWithViewerDistance() {
        assertEquals(1, ServerRenderEntityManager.computeLodInterval(5.0, 100.0));
        assertEquals(3, ServerRenderEntityManager.computeLodInterval(30.0, 100.0));
        assertEquals(6, ServerRenderEntityManager.computeLodInterval(60.0, 100.0));
        assertEquals(12, ServerRenderEntityManager.computeLodInterval(90.0, 100.0));
    }

    private static final class CountingRenderEntity extends RenderEntity {
        private int ticks;

        @Override
        public void tick() {
            ticks++;
        }

        @Override
        public void clientTick() {
        }

        @Override
        public void serverTick() {
        }

        @Override
        public ResourceLocation getRenderID() {
            return new ResourceLocation("reiparticlesapi", "counting_render_entity_server");
        }
    }

    private static final class FailingRenderEntity extends RenderEntity {
        @Override
        public void tick() {
            throw new IllegalStateException("boom");
        }

        @Override
        public void clientTick() {
        }

        @Override
        public void serverTick() {
        }

        @Override
        public ResourceLocation getRenderID() {
            return new ResourceLocation("reiparticlesapi", "failing_render_entity_server");
        }
    }
}
