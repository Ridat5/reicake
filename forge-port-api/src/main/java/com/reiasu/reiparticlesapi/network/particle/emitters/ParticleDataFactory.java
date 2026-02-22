package com.reiasu.reiparticlesapi.network.particle.emitters;

/**
 * Factory interface for creating {@link ControlableParticleData} instances.
 * <p>
 * Used by emitter implementations to produce the initial per-particle data
 * when spawning new particles each tick.
 */
@FunctionalInterface
public interface ParticleDataFactory {

    /**
     * Creates a new {@link ControlableParticleData} with default values.
     */
    ControlableParticleData create();
}
