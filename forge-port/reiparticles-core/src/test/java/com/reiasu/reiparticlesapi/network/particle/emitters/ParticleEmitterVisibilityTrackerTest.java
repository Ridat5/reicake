// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network.particle.emitters;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ParticleEmitterVisibilityTrackerTest {
    @Test
    void computeLodIntervalScalesWithViewerDistance() {
        assertEquals(1, ParticleEmitterVisibilityTracker.computeLodInterval(5.0, 100.0));
        assertEquals(3, ParticleEmitterVisibilityTracker.computeLodInterval(30.0, 100.0));
        assertEquals(6, ParticleEmitterVisibilityTracker.computeLodInterval(60.0, 100.0));
        assertEquals(12, ParticleEmitterVisibilityTracker.computeLodInterval(90.0, 100.0));
    }

    @Test
    void shouldOnlyTrackEmitterAfterSuccessfulSend() {
        Set<UUID> visible = new HashSet<>();
        UUID emitterId = UUID.randomUUID();

        assertFalse(ParticleEmitterVisibilityTracker.markVisibleAfterSuccessfulSend(visible, emitterId, () -> false));
        assertFalse(visible.contains(emitterId));
        assertTrue(ParticleEmitterVisibilityTracker.markVisibleAfterSuccessfulSend(visible, emitterId, () -> true));
        assertTrue(visible.contains(emitterId));

        AtomicInteger sendAttempts = new AtomicInteger();
        assertFalse(ParticleEmitterVisibilityTracker.markVisibleAfterSuccessfulSend(visible, emitterId, () -> {
            sendAttempts.incrementAndGet();
            return true;
        }));
        assertEquals(0, sendAttempts.get());
    }
}
