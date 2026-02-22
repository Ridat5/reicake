package com.reiasu.reiparticleskill.animats;

import com.reiasu.reiparticlesapi.display.DisplayEntity;
import com.reiasu.reiparticlesapi.display.DisplayEntityManager;
import com.reiasu.reiparticlesapi.network.particle.emitters.ParticleEmittersManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnimatsActionTest {

    @BeforeEach
    void setUp() {
        ParticleEmittersManager.clear();
        DisplayEntityManager.INSTANCE.clear();
    }

    @AfterEach
    void tearDown() {
        ParticleEmittersManager.clear();
        DisplayEntityManager.INSTANCE.clear();
    }

    @Test
    void tickingActionStopsAtMaxTick() {
        AtomicInteger calls = new AtomicInteger();
        TickingAction action = new TickingAction(3, it -> calls.incrementAndGet());
        action.onStart();

        for (int i = 0; i < 8; i++) {
            if (action.check()) {
                break;
            }
            action.doTick();
            if (action.check()) {
                action.onDone();
                break;
            }
        }

        assertTrue(action.getCanceled());
        assertTrue(action.getDone());
        assertEquals(4, calls.get());
        assertTrue(action.getFirstTick());
    }

    @Test
    void emitterCrafterActionSpawnsAtInterval() {
        AtomicInteger built = new AtomicInteger();
        EmitterCrafterAction action = new EmitterCrafterAction(
                () -> {
                    built.incrementAndGet();
                    return null;
                },
                3,
                it -> it.getCount() >= 8
        );
        action.onStart();

        while (!action.checkDone()) {
            action.tick();
        }

        assertEquals(8, action.getCount());
        assertEquals(3, built.get());
        assertEquals(0, ParticleEmittersManager.activeCount());
    }

    @Test
    void displayEntityActionSpawnsOnlyOnce() {
        DummyDisplay display = new DummyDisplay();
        DisplayEntityAction action = new DisplayEntityAction(display, it -> {
        });
        action.onStart();

        action.tick();
        action.tick();

        assertEquals(1, DisplayEntityManager.INSTANCE.activeCount());
        assertFalse(action.checkDone());
        action.onDone();
        assertTrue(action.checkDone());
    }

    private static final class DummyDisplay extends DisplayEntity {
        @Override
        public void tick() {
            // no-op for unit test
        }
    }
}
