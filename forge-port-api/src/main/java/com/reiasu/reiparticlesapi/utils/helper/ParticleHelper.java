package com.reiasu.reiparticlesapi.utils.helper;

import com.reiasu.reiparticlesapi.particles.Controlable;

public interface ParticleHelper {
    default void loadControler(Controlable<?> controler) {
    }

    default void initHelper() {
    }
}
