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
