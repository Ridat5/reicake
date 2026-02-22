package com.reiasu.reiparticlesapi.network.particle.emitters.event;

import com.reiasu.reiparticlesapi.network.particle.emitters.ControlableParticleData;
import com.reiasu.reiparticlesapi.particles.ControlableParticle;
import net.minecraft.world.entity.Entity;

/**
 * Fired when a particle's trajectory intersects with a living entity.
 */
public final class ParticleHitEntityEvent implements ParticleEvent {

    public static final String EVENT_ID = "ParticleHitEntityEvent";

    private ControlableParticle particle;
    private ControlableParticleData particleData;
    private Entity hit;
    private boolean canceled;

    public ParticleHitEntityEvent(ControlableParticle particle, ControlableParticleData particleData, Entity hit) {
        this.particle = particle;
        this.particleData = particleData;
        this.hit = hit;
    }

    @Override
    public String getEventID() {
        return EVENT_ID;
    }

    @Override
    public ControlableParticle getParticle() {
        return particle;
    }

    @Override
    public void setParticle(ControlableParticle particle) {
        this.particle = particle;
    }

    @Override
    public ControlableParticleData getParticleData() {
        return particleData;
    }

    @Override
    public void setParticleData(ControlableParticleData data) {
        this.particleData = data;
    }

    public Entity getHit() {
        return hit;
    }

    public void setHit(Entity hit) {
        this.hit = hit;
    }

    @Override
    public boolean getCanceled() {
        return canceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
