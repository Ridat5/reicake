// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network.particle.composition.manager;

import com.reiasu.reiparticlesapi.network.particle.composition.CompositionData;
import com.reiasu.reiparticlesapi.network.particle.composition.ParticleComposition;
import com.reiasu.reiparticlesapi.utils.RelativeLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ParticleCompositionManagerTest {
    @AfterEach
    void cleanup() {
        ParticleCompositionManager.INSTANCE.clear();
    }

    @Test
    void shouldContinueServerTickAfterCompositionFailure() {
        FailingComposition failing = new FailingComposition();
        CountingComposition healthy = new CountingComposition();
        ParticleCompositionManager.INSTANCE.spawn(failing);
        ParticleCompositionManager.INSTANCE.spawn(healthy);

        ParticleCompositionManager.INSTANCE.tickAll();

        assertEquals(1, healthy.ticks);
        assertEquals(1, ParticleCompositionManager.INSTANCE.activeCount());
        assertSame(healthy, ParticleCompositionManager.INSTANCE.getCompositions().get(0));
    }

    @Test
    void shouldContinueClientTickAfterCompositionFailure() {
        FailingComposition failing = new FailingComposition();
        CountingComposition healthy = new CountingComposition();
        ParticleCompositionManager.INSTANCE.getClientView().put(failing.getControlUUID(), failing);
        ParticleCompositionManager.INSTANCE.getClientView().put(healthy.getControlUUID(), healthy);

        ParticleCompositionManager.INSTANCE.tickClient();

        assertEquals(1, healthy.ticks);
        assertEquals(1, ParticleCompositionManager.INSTANCE.getClientView().size());
        assertSame(healthy, ParticleCompositionManager.INSTANCE.getClientView().get(healthy.getControlUUID()));
    }

    private static final class CountingComposition extends ParticleComposition {
        private int ticks;

        @Override
        public Map<CompositionData, RelativeLocation> getParticles() {
            return Map.of();
        }

        @Override
        public void onDisplay() {
        }

        @Override
        public void tick() {
            ticks++;
        }
    }

    private static final class FailingComposition extends ParticleComposition {
        @Override
        public Map<CompositionData, RelativeLocation> getParticles() {
            return Map.of();
        }

        @Override
        public void onDisplay() {
        }

        @Override
        public void tick() {
            throw new IllegalStateException("boom");
        }
    }
}
