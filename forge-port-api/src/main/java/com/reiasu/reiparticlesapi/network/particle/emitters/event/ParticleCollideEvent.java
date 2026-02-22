package com.reiasu.reiparticlesapi.network.particle.emitters.event;

import com.reiasu.reiparticlesapi.network.particle.emitters.ControlableParticleData;
import com.reiasu.reiparticlesapi.particles.ControlableParticle;
import net.minecraft.world.phys.HitResult;

/**
 * Fired when a particle collides with any block or entity (general ray-cast hit).
 */
public final class ParticleCollideEvent implements ParticleEvent {

    public static final String EVENT_ID = "ParticleColliderEvent";

    private ControlableParticle particle;
    private ControlableParticleData particleData;
    private HitResult res;
    private boolean canceled;

    public ParticleCollideEvent(ControlableParticle particle, ControlableParticleData particleData, HitResult res) {
        this.particle = particle;
        this.particleData = particleData;
        this.res = res;
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

    public HitResult getRes() {
        return res;
    }

    public void setRes(HitResult res) {
        this.res = res;
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
