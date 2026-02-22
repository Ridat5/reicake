package com.reiasu.reiparticlesapi.network.particle.emitters;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParticleEmittersManagerTest {
    @AfterEach
    void cleanup() {
        ParticleEmittersManager.clear();
    }

    @Test
    void shouldTickAndPruneEmittersAtMaxTick() {
        CountingEmitter emitter = new CountingEmitter();
        emitter.setMaxTick(3);
        ParticleEmittersManager.spawnEmitters(emitter);

        assertEquals(1, ParticleEmittersManager.activeCount());

        ParticleEmittersManager.tickAll();
        ParticleEmittersManager.tickAll();
        assertEquals(1, ParticleEmittersManager.activeCount());

        ParticleEmittersManager.tickAll();
        assertEquals(0, ParticleEmittersManager.activeCount());
        assertEquals(3, emitter.emittedTicks);
    }

    @Test
    void shouldIgnoreNonEmitterObjects() {
        ParticleEmittersManager.spawnEmitters("not-an-emitter");
        assertEquals(0, ParticleEmittersManager.activeCount());
    }

    private static final class CountingEmitter extends ParticleEmitters {
        private int emittedTicks;

        @Override
        protected void emitTick() {
            emittedTicks++;
        }
    }
}
