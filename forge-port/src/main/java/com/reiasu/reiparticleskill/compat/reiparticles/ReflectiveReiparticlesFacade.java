package com.reiasu.reiparticleskill.compat.reiparticles;

import com.reiasu.reiparticlesapi.ReiParticlesAPI;
import org.slf4j.Logger;

public final class ReflectiveReiparticlesFacade implements ReiparticlesFacade {
    @Override
    public boolean isOperational() {
        return true;
    }

    @Override
    public void bootstrap(Logger logger) {
        ReiParticlesAPI.init();
        ReiParticlesAPI.INSTANCE.loadScannerPackages();
        logger.info("reiparticlesapi bridge active: init=true, scanner=true");
    }

    @Override
    public void registerParticleStyles(Logger logger) {
        ReiParticlesAPI.INSTANCE.registerParticleStyles();
        logger.info("reiparticlesapi bridge: particle style hooks invoked");
    }

    @Override
    public void registerTestHooks(Logger logger) {
        ReiParticlesAPI.INSTANCE.registerTest();
        logger.info("reiparticlesapi bridge: test hooks invoked");
    }

    @Override
    public void registerKeyBindings(Logger logger) {
        ReiParticlesAPI.INSTANCE.registerKeyBindings();
        logger.info("reiparticlesapi bridge: keybinding hooks invoked");
    }
}
