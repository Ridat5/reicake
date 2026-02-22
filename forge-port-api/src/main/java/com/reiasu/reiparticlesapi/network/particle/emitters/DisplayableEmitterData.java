package com.reiasu.reiparticlesapi.network.particle.emitters;

import com.reiasu.reiparticlesapi.network.particle.data.SerializableData;
import com.reiasu.reiparticlesapi.particles.ParticleDisplayer;

/**
 * A {@link SerializableData} implementation for emitter display data.
 * <p>
 * All methods currently throw {@link UnsupportedOperationException} matching
 * the original Fabric source which also had "Not yet implemented" stubs.
 * This class exists as a placeholder type for emitter data factories.
 */
public final class DisplayableEmitterData implements SerializableData {

    @Override
    public SerializableData clone() {
        throw new UnsupportedOperationException("DisplayableEmitterData.clone() not yet implemented");
    }

    @Override
    public ParticleDisplayer createDisplayer() {
        throw new UnsupportedOperationException("DisplayableEmitterData.createDisplayer() not yet implemented");
    }
}
