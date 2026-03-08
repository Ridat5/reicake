// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network;

import com.reiasu.reiparticlesapi.config.APIConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerSyncPacketBudgetTest {
    @AfterEach
    void cleanup() {
        ServerSyncPacketBudget.reset();
        APIConfig.INSTANCE.setPacketsPerTickLimit(512);
    }

    @Test
    void shouldShareBudgetUntilServerTickAdvances() {
        APIConfig.INSTANCE.setPacketsPerTickLimit(16);

        ServerSyncPacketBudget.beginServerTick(100L);
        for (int i = 0; i < 16; i++) {
            assertTrue(ServerSyncPacketBudget.tryAcquire());
        }
        assertFalse(ServerSyncPacketBudget.tryAcquire());
        assertEquals(17, ServerSyncPacketBudget.usedPackets());

        ServerSyncPacketBudget.beginServerTick(100L);
        assertFalse(ServerSyncPacketBudget.tryAcquire());

        ServerSyncPacketBudget.beginServerTick(101L);
        assertTrue(ServerSyncPacketBudget.tryAcquire());
    }
}
