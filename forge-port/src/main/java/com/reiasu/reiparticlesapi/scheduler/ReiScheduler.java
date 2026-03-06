// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.scheduler;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

/**
 * Tick-based task scheduler. Supports one-shot, repeating (with interval),
 * and repeating-with-max-tick tasks. Thread-safe task queue.
 */
public final class ReiScheduler {

    public static final ReiScheduler INSTANCE = new ReiScheduler();

    private final ConcurrentLinkedQueue<TickRunnable> ticks = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<TickRunnable> taskQueue = new ConcurrentLinkedQueue<>();

    private ReiScheduler() {
    }

    /** Called once per server/client tick. */
    public void doTick() {
        // Merge newly queued tasks
        TickRunnable queued;
        while ((queued = taskQueue.poll()) != null) {
            ticks.add(queued);
        }
        // Tick all active tasks
        ticks.removeIf(tr -> {
            tr.doTick();
            return tr.isCancelled();
        });
    }

    /** Schedule a one-shot task after {@code delay} ticks. */
    public TickRunnable runTask(int delay, Runnable action) {
        TickRunnable tr = new TickRunnable(delay, action, false, 0);
        taskQueue.add(tr);
        return tr;
    }

    /** Schedule a repeating task that runs every {@code delay} ticks (indefinitely). */
    public TickRunnable runTaskTimer(int delay, Runnable action) {
        TickRunnable tr = new TickRunnable(delay, action, true, -1);
        taskQueue.add(tr);
        return tr;
    }

    /** Schedule a repeating task that runs every tick, up to {@code maxLoopTick} total ticks. */
    public TickRunnable runTaskTimerMaxTick(int maxLoopTick, Runnable action) {
        TickRunnable tr = new TickRunnable(1, action, true, maxLoopTick);
        taskQueue.add(tr);
        return tr;
    }

    /** Schedule a repeating task with a pre-delay, then runs every tick up to {@code maxLoopTick}. */
    public TickRunnable runTaskTimerMaxTick(int preDelay, int maxLoopTick, Runnable action) {
        TickRunnable tr = new TickRunnable(preDelay, action, true, maxLoopTick);
        tr.setPreDelay(preDelay);
        taskQueue.add(tr);
        return tr;
    }

    /**
     * A scheduled tick-driven task with support for one-shot, repeating,
     * and repeating-with-limit modes.
     */
    public static final class TickRunnable {
        private final int delay;
        private final Runnable action;
        private final boolean repeating;
        private final int maxTick; // -1 = infinite
        private int preDelay;
        private int currentTick;
        private int loopCount;
        private boolean cancelled;
        private Predicate<TickRunnable> cancelPredicate;
        private Runnable finishCallback;

        TickRunnable(int delay, Runnable action, boolean repeating, int maxTick) {
            this.delay = Math.max(delay, 1);
            this.action = action;
            this.repeating = repeating;
            this.maxTick = maxTick;
        }

        public void cancel() {
            this.cancelled = true;
            if (finishCallback != null) {
                finishCallback.run();
            }
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void setCancelPredicate(Predicate<TickRunnable> predicate) {
            this.cancelPredicate = predicate;
        }

        public void setFinishCallback(Runnable callback) {
            this.finishCallback = callback;
        }

        void setPreDelay(int preDelay) {
            this.preDelay = preDelay;
        }

        public int getLoopCount() {
            return loopCount;
        }

        void doTick() {
            if (cancelled) return;

            // Handle pre-delay
            if (preDelay > 0) {
                preDelay--;
                return;
            }

            currentTick++;

            // Check cancel predicate
            if (cancelPredicate != null && cancelPredicate.test(this)) {
                cancel();
                return;
            }

            if (!repeating) {
                // One-shot mode: fire after delay ticks
                if (currentTick >= delay) {
                    action.run();
                    cancel();
                }
            } else {
                // Repeating mode
                if (currentTick >= delay) {
                    action.run();
                    currentTick = 0;
                    loopCount++;

                    // Check max tick limit
                    if (maxTick > 0 && loopCount >= maxTick) {
                        cancel();
                    }
                }
            }
        }
    }
}
