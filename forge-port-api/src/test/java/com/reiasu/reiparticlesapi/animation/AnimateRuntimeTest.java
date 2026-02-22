package com.reiasu.reiparticlesapi.animation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnimateRuntimeTest {
    @AfterEach
    void cleanup() {
        AnimateManager.INSTANCE.clear();
    }

    @Test
    void shouldDriveAnimateToCompletionByServerTicks() {
        CountingAction action = new CountingAction(3);
        Animate animate = new Animate().addNode(new AnimateNode().addAction(action));

        AnimateManager.INSTANCE.displayAnimateServer(animate);
        assertEquals(1, AnimateManager.INSTANCE.activeCount());

        AnimateManager.INSTANCE.tickServer();
        AnimateManager.INSTANCE.tickServer();
        assertEquals(1, AnimateManager.INSTANCE.activeCount());

        AnimateManager.INSTANCE.tickServer();
        assertEquals(1, AnimateManager.INSTANCE.activeCount());
        AnimateManager.INSTANCE.tickServer();
        assertEquals(0, AnimateManager.INSTANCE.activeCount());
        assertTrue(animate.getDone());
        assertEquals(3, action.ticks);
    }

    @Test
    void shouldCancelWhenPredicateIsSatisfied() {
        CountingAction action = new CountingAction(20);
        Animate animate = new Animate()
                .addNode(new AnimateNode().addAction(action))
                .addCancelPredicate(a -> action.ticks >= 2);

        AnimateManager.INSTANCE.displayAnimateServer(animate);
        AnimateManager.INSTANCE.tickServer();
        AnimateManager.INSTANCE.tickServer();
        AnimateManager.INSTANCE.tickServer();

        assertTrue(animate.getDone());
        assertEquals(0, AnimateManager.INSTANCE.activeCount());
        assertTrue(action.ticks >= 2);
    }

    private static final class CountingAction extends AnimateAction {
        private final int maxTicks;
        private int ticks;

        private CountingAction(int maxTicks) {
            this.maxTicks = maxTicks;
        }

        @Override
        public boolean checkDone() {
            return ticks >= maxTicks;
        }

        @Override
        public void tick() {
            ticks++;
        }

        @Override
        public void onStart() {
            ticks = 0;
        }

        @Override
        public void onDone() {
        }
    }
}
