package com.reiasu.reiparticlesapi.network.particle.emitters.event;

import com.reiasu.reiparticlesapi.network.particle.emitters.ControlableParticleData;
import com.reiasu.reiparticlesapi.particles.ControlableParticle;
import net.minecraft.core.BlockPos;

/**
 * Fired when a particle enters a liquid block (water, lava, etc.).
 */
public final class ParticleOnLiquidEvent implements ParticleEvent {

    public static final String EVENT_ID = "ParticleOnLiquidEvent";

    private ControlableParticle particle;
    private ControlableParticleData particleData;
    private BlockPos hit;
    private boolean canceled;

    public ParticleOnLiquidEvent(ControlableParticle particle, ControlableParticleData particleData, BlockPos hit) {
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

    public BlockPos getHit() {
        return hit;
    }

    public void setHit(BlockPos hit) {
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
