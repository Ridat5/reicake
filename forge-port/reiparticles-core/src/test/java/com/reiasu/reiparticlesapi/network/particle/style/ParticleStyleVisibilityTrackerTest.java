// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network.particle.style;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ParticleStyleVisibilityTrackerTest {
    @Test
    void computeLodIntervalScalesWithViewerDistance() {
        assertEquals(1, ParticleStyleVisibilityTracker.computeLodInterval(5.0, 100.0));
        assertEquals(3, ParticleStyleVisibilityTracker.computeLodInterval(30.0, 100.0));
        assertEquals(6, ParticleStyleVisibilityTracker.computeLodInterval(60.0, 100.0));
        assertEquals(12, ParticleStyleVisibilityTracker.computeLodInterval(90.0, 100.0));
    }

    @Test
    void shouldShardPlayerProcessingByTick() {
        assertTrue(ParticleStyleVisibilityTracker.shouldProcessPlayerIndex(0, 0));
        assertFalse(ParticleStyleVisibilityTracker.shouldProcessPlayerIndex(1, 0));
        assertTrue(ParticleStyleVisibilityTracker.shouldProcessPlayerIndex(1, 1));
        assertFalse(ParticleStyleVisibilityTracker.shouldProcessPlayerIndex(3, 1));
    }
}
