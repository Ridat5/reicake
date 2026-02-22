package com.reiasu.reiparticlesapi.network.particle.style;

import com.reiasu.reiparticlesapi.network.buffer.ParticleControlerDataBuffer;

import java.util.Map;
import java.util.UUID;

public interface ParticleStyleProvider<T extends ParticleGroupStyle> {
    T create();

    default T createStyle(UUID uuid, Map<String, ? extends ParticleControlerDataBuffer<?>> args) {
        return create();
    }

    default void changeStyle(T style, Map<String, ? extends ParticleControlerDataBuffer<?>> args) {
    }
}
