// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.renderer.client;

import com.reiasu.reiparticlesapi.renderer.RenderEntity;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ClientRenderEntityManagerTest {
    @AfterEach
    void cleanup() {
        ClientRenderEntityManager.INSTANCE.clear();
    }

    @Test
    void shouldContinueClientTickAfterRenderEntityFailure() {
        FailingRenderEntity failing = new FailingRenderEntity();
        CountingRenderEntity healthy = new CountingRenderEntity();
        ClientRenderEntityManager.INSTANCE.add(failing);
        ClientRenderEntityManager.INSTANCE.add(healthy);

        ClientRenderEntityManager.INSTANCE.doClientTick();

        assertEquals(1, healthy.ticks);
        assertEquals(1, ClientRenderEntityManager.INSTANCE.getEntities().size());
        assertSame(healthy, ClientRenderEntityManager.INSTANCE.getEntities().get(healthy.getUuid()));
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
            return new ResourceLocation("reiparticlesapi", "counting_render_entity");
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
            return new ResourceLocation("reiparticlesapi", "failing_render_entity");
        }
    }
}
