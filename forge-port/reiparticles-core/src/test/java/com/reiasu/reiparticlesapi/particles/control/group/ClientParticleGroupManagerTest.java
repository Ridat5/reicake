// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.particles.control.group;

import com.reiasu.reiparticlesapi.utils.RelativeLocation;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientParticleGroupManagerTest {
    @Test
    void shouldContinueTickingOtherGroupsAfterGroupFailure() {
        ConcurrentHashMap<UUID, ControllableParticleGroup> visibleControls = new ConcurrentHashMap<>();
        FailingGroup failing = new FailingGroup();
        CountingGroup healthy = new CountingGroup();
        visibleControls.put(failing.getUuid(), failing);
        visibleControls.put(healthy.getUuid(), healthy);

        ClientParticleGroupManager.tickVisibleGroups(visibleControls, LoggerFactory.getLogger(ClientParticleGroupManagerTest.class));

        assertEquals(1, healthy.ticks);
        assertEquals(1, visibleControls.size());
        assertSame(healthy, visibleControls.get(healthy.getUuid()));
        assertTrue(failing.removed);
    }

    @Test
    void shouldRemoveCanceledGroupsAfterTick() {
        ConcurrentHashMap<UUID, ControllableParticleGroup> visibleControls = new ConcurrentHashMap<>();
        CountingGroup canceled = new CountingGroup();
        canceled.setCanceled(true);
        visibleControls.put(canceled.getUuid(), canceled);

        ClientParticleGroupManager.tickVisibleGroups(visibleControls, LoggerFactory.getLogger(ClientParticleGroupManagerTest.class));

        assertTrue(visibleControls.isEmpty());
        assertTrue(canceled.removed);
    }

    private static final class CountingGroup extends ControllableParticleGroup {
        private int ticks;
        private boolean removed;

        private CountingGroup() {
            super(UUID.randomUUID());
        }

        @Override
        public Map<ParticleRelativeData, RelativeLocation> loadParticleLocations() {
            return Map.of();
        }

        @Override
        public void onGroupDisplay() {
        }

        @Override
        public void tick() {
            ticks++;
        }

        @Override
        public void remove() {
            removed = true;
            setCanceled(true);
        }
    }

    private static final class FailingGroup extends ControllableParticleGroup {
        private boolean removed;

        private FailingGroup() {
            super(UUID.randomUUID());
        }

        @Override
        public Map<ParticleRelativeData, RelativeLocation> loadParticleLocations() {
            return Map.of();
        }

        @Override
        public void onGroupDisplay() {
        }

        @Override
        public void tick() {
            throw new IllegalStateException("boom");
        }

        @Override
        public void remove() {
            removed = true;
            setCanceled(true);
        }
    }
}
