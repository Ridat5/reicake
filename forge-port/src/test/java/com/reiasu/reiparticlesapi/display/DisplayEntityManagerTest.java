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
package com.reiasu.reiparticlesapi.display;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DisplayEntityManagerTest {
    @AfterEach
    void cleanup() {
        DisplayEntityManager.INSTANCE.clear();
    }

    @Test
    void shouldTickAndRemoveCanceledDisplays() {
        DisplayEntityManager.INSTANCE.spawn(new DebugDisplayEntity(0.0, 64.0, 0.0, "group"));
        assertEquals(1, DisplayEntityManager.INSTANCE.activeCount());

        DisplayEntity entity = DisplayEntityManager.INSTANCE.getDisplays().get(0);
        entity.cancel();
        DisplayEntityManager.INSTANCE.tickAll();

        assertEquals(0, DisplayEntityManager.INSTANCE.activeCount());
    }

    @Test
    void shouldIgnoreUnknownDisplayObjects() {
        DisplayEntityManager.INSTANCE.spawn("invalid");
        assertEquals(0, DisplayEntityManager.INSTANCE.activeCount());
    }
}
