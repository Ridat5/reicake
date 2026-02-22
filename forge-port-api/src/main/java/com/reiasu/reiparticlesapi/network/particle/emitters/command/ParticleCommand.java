package com.reiasu.reiparticlesapi.network.particle.emitters.command;

import com.reiasu.reiparticlesapi.network.particle.emitters.ControlableParticleData;
import com.reiasu.reiparticlesapi.particles.ControlableParticle;

/**
 * A command that mutates particle state during the emitter tick loop.
 */
@FunctionalInterface
public interface ParticleCommand {

    /**
     * Execute this command against the given particle data and particle instance.
     *
     * @param data     the mutable particle data
     * @param particle the particle instance
     */
    void execute(ControlableParticleData data, ControlableParticle particle);
}
