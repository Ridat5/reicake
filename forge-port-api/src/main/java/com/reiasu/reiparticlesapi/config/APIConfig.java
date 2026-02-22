package com.reiasu.reiparticlesapi.config;

/**
 * API-level configuration for particle rendering behavior.
 */
public final class APIConfig {

    private boolean enabledParticleCountInject = true;
    private boolean enabledParticleAsync = true;
    private int particleCountLimit = 131072;
    private int calculateThreadCount = 16;

    public boolean isEnabledParticleCountInject() { return enabledParticleCountInject; }
    public void setEnabledParticleCountInject(boolean v) { this.enabledParticleCountInject = v; }

    public boolean isEnabledParticleAsync() { return enabledParticleAsync; }
    public void setEnabledParticleAsync(boolean v) { this.enabledParticleAsync = v; }

    /** Returns the particle count limit, always at least 1. */
    public int getParticleCountLimit() { return Math.max(particleCountLimit, 1); }
    public void setParticleCountLimit(int v) { this.particleCountLimit = v; }

    /** Returns the calculation thread count, always at least 1. */
    public int getCalculateThreadCount() { return Math.max(calculateThreadCount, 1); }
    public void setCalculateThreadCount(int v) { this.calculateThreadCount = v; }
}
