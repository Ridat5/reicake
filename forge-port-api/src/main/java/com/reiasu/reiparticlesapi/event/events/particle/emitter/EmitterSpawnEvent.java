package com.reiasu.reiparticlesapi.event.events.particle.emitter;

import com.reiasu.reiparticlesapi.event.api.ReiEvent;
import com.reiasu.reiparticlesapi.network.particle.emitters.ParticleEmitters;

public final class EmitterSpawnEvent extends ReiEvent {
    private final ParticleEmitters emitter;
    private final boolean clientSide;

    public EmitterSpawnEvent(ParticleEmitters emitter, boolean clientSide) {
        this.emitter = emitter;
        this.clientSide = clientSide;
    }

    public ParticleEmitters getEmitter() {
        return emitter;
    }

    public boolean isClientSide() {
        return clientSide;
    }
}

