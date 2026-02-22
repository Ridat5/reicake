package com.reiasu.reiparticlesapi;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReiParticlesAPISchedulerTest {
    @Test
    void shouldExecuteScheduledTask() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean executed = new AtomicBoolean(false);

        ReiParticlesAPI.scheduler.runTask(1, () -> {
            executed.set(true);
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertTrue(executed.get());
    }

    @Test
    void shouldExecuteWhenTickIsZero() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        ReiParticlesAPI.scheduler.runTask(0, latch::countDown);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }
}
