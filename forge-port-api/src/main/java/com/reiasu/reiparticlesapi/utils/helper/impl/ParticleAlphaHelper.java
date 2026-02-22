package com.reiasu.reiparticlesapi.utils.helper.impl;

import com.reiasu.reiparticlesapi.particles.Controlable;
import com.reiasu.reiparticlesapi.particles.control.ParticleControler;
import com.reiasu.reiparticlesapi.utils.helper.AlphaHelper;

/**
 * {@link AlphaHelper} implementation for a single particle controller.
 * <p>
 * Delegates alpha operations through {@link ParticleControler} to the
 * underlying {@link com.reiasu.reiparticlesapi.particles.ControlableParticle}.
 */
public final class ParticleAlphaHelper extends AlphaHelper {
    private Controlable<?> controler;
    private float currentAlpha = 1.0f;

    public ParticleAlphaHelper(double minAlpha, double maxAlpha, int alphaTick) {
        super(minAlpha, maxAlpha, alphaTick);
    }

    public Controlable<?> getControler() {
        return controler;
    }

    public void setControler(Controlable<?> controler) {
        this.controler = controler;
    }

    @Override
    public Controlable<?> getLoadedGroup() {
        return controler;
    }

    @Override
    public double getCurrentAlpha() {
        if (controler instanceof ParticleControler pc) {
            try {
                return pc.getParticle().getParticleAlpha();
            } catch (IllegalStateException e) {
                // Particle not loaded yet
            }
        }
        return currentAlpha;
    }

    @Override
    public void setAlpha(double alpha) {
        this.currentAlpha = (float) alpha;
        if (controler instanceof ParticleControler pc) {
            try {
                pc.getParticle().setParticleAlpha((float) alpha);
            } catch (IllegalStateException e) {
                // Particle not loaded yet
            }
        }
    }

    @Override
    public void loadControler(Controlable<?> controler) {
        if (!(controler instanceof ParticleControler)) return;
        this.controler = controler;
    }
}
