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
package com.reiasu.reiparticlesapi;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReiParticlesAPISchedulerTest {

    private ReiParticlesAPI.Scheduler createScheduler() {
        return new ReiParticlesAPI.Scheduler();
    }

    @Test
    void shouldExecuteScheduledTask() {
        ReiParticlesAPI.Scheduler scheduler = createScheduler();
        AtomicBoolean executed = new AtomicBoolean(false);

        scheduler.runTask(1, () -> executed.set(true));

        // tick 1: currentTick becomes 1, executionTick is 1 → should fire
        scheduler.tick();
        assertTrue(executed.get());
    }

    @Test
    void shouldExecuteWhenTickIsZero() {
        ReiParticlesAPI.Scheduler scheduler = createScheduler();
        AtomicBoolean executed = new AtomicBoolean(false);

        // runTask(0) clamps to 1, so executionTick = 0 + 1 = 1
        scheduler.runTask(0, () -> executed.set(true));

        // Before tick: not yet executed
        assertFalse(executed.get());
        // tick 1: should fire
        scheduler.tick();
        assertTrue(executed.get());
    }
}
