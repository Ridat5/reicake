// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network;

import com.reiasu.reiparticlesapi.config.APIConfig;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Shared packet budget for server-side visibility sync work.
 */
public final class ServerSyncPacketBudget {
    private static final long UNINITIALIZED_TICK = Long.MIN_VALUE;
    private static final AtomicLong currentTick = new AtomicLong(UNINITIALIZED_TICK);
    private static final AtomicInteger packetsThisTick = new AtomicInteger(0);

    private ServerSyncPacketBudget() {
    }

    public static void beginServerTick(long tickId) {
        if (currentTick.getAndSet(tickId) != tickId) {
            packetsThisTick.set(0);
        }
    }

    public static boolean tryAcquire() {
        return packetsThisTick.incrementAndGet() <= APIConfig.INSTANCE.getPacketsPerTickLimit();
    }

    static int usedPackets() {
        return packetsThisTick.get();
    }

    static void reset() {
        currentTick.set(UNINITIALIZED_TICK);
        packetsThisTick.set(0);
    }
}
