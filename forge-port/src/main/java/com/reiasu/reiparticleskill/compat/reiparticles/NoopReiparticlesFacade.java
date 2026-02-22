package com.reiasu.reiparticleskill.compat.reiparticles;

import org.slf4j.Logger;

public final class NoopReiparticlesFacade implements ReiparticlesFacade {
    @Override
    public boolean isOperational() {
        return false;
    }

    @Override
    public void bootstrap(Logger logger) {
        logger.warn("reiparticlesapi facade is running in NOOP mode; gameplay features are disabled until ported.");
    }

    @Override
    public void registerParticleStyles(Logger logger) {
        logger.warn("Skipping particle style registration (NOOP facade)");
    }

    @Override
    public void registerTestHooks(Logger logger) {
        logger.warn("Skipping test hook registration (NOOP facade)");
    }

    @Override
    public void registerKeyBindings(Logger logger) {
        logger.warn("Skipping keybinding bridge registration (NOOP facade)");
    }
}