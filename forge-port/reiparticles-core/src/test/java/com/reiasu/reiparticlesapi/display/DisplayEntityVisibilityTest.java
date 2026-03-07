// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.display;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisplayEntityVisibilityTest {
    @Test
    void shouldRequireViewerToBeWithinDisplayRange() {
        assertTrue(DisplayEntityManager.isWithinVisibleRange(new Vec3(0.0, 64.0, 0.0), new Vec3(3.0, 64.0, 4.0), 5.0));
        assertFalse(DisplayEntityManager.isWithinVisibleRange(new Vec3(0.0, 64.0, 0.0), new Vec3(6.0, 64.0, 0.0), 5.0));
    }
}
