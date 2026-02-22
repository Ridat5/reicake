package com.reiasu.reiparticlesapi.event.events.particle.emitter;

import com.reiasu.reiparticlesapi.event.api.ReiEvent;
import com.reiasu.reiparticlesapi.network.particle.emitters.ParticleEmitters;

public final class EmitterRemoveEvent extends ReiEvent {
    private final ParticleEmitters emitter;
    private final boolean clientSide;

    public EmitterRemoveEvent(ParticleEmitters emitter, boolean clientSide) {
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

