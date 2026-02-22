package com.reiasu.reiparticlesapi.network.particle.emitters.event;

import com.reiasu.reiparticlesapi.network.particle.emitters.ControlableParticleData;
import com.reiasu.reiparticlesapi.particles.ControlableParticle;

/**
 * Base interface for particle-level events fired during emitter simulation.
 */
public interface ParticleEvent {

    /**
     * Returns the unique string identifier for this event type.
     */
    String getEventID();

    /**
     * Gets the particle instance.
     */
    ControlableParticle getParticle();

    /**
     * Sets the particle instance.
     */
    void setParticle(ControlableParticle particle);

    /**
     * Gets the particle's mutable data.
     */
    ControlableParticleData getParticleData();

    /**
     * Sets the particle's mutable data.
     */
    void setParticleData(ControlableParticleData data);

    /**
     * Whether this event has been canceled by a handler.
     */
    boolean getCanceled();

    /**
     * Cancel or un-cancel this event.
     */
    void setCanceled(boolean canceled);
}
