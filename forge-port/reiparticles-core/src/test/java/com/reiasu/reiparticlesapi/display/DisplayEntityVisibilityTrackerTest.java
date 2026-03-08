// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.display;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisplayEntityVisibilityTrackerTest {
    @Test
    void shouldShardPlayerProcessingByTick() {
        assertTrue(DisplayEntityVisibilityTracker.shouldProcessPlayerIndex(0, 0));
        assertFalse(DisplayEntityVisibilityTracker.shouldProcessPlayerIndex(1, 0));
        assertTrue(DisplayEntityVisibilityTracker.shouldProcessPlayerIndex(1, 1));
        assertFalse(DisplayEntityVisibilityTracker.shouldProcessPlayerIndex(3, 1));
    }

    @Test
    void shouldOnlyTrackDisplayAfterSuccessfulSend() {
        Set<UUID> visible = new HashSet<>();
        UUID displayId = UUID.randomUUID();

        assertFalse(DisplayEntityVisibilityTracker.markVisibleAfterSuccessfulSend(visible, displayId, () -> false));
        assertFalse(visible.contains(displayId));
        assertTrue(DisplayEntityVisibilityTracker.markVisibleAfterSuccessfulSend(visible, displayId, () -> true));
        assertTrue(visible.contains(displayId));

        AtomicInteger sendAttempts = new AtomicInteger();
        assertFalse(DisplayEntityVisibilityTracker.markVisibleAfterSuccessfulSend(visible, displayId, () -> {
            sendAttempts.incrementAndGet();
            return true;
        }));
        assertEquals(0, sendAttempts.get());
    }
}