package com.reiasu.reiparticlesapi.utils.helper.impl;

import com.reiasu.reiparticlesapi.network.particle.style.ParticleGroupStyle;
import com.reiasu.reiparticlesapi.particles.Controlable;
import com.reiasu.reiparticlesapi.particles.control.ParticleControler;
import com.reiasu.reiparticlesapi.utils.helper.AlphaHelper;

/**
 * {@link AlphaHelper} implementation for {@link ParticleGroupStyle}.
 * Propagates alpha changes recursively to all particles in the style's hierarchy.
 * <p>
 * Propagates alpha changes to all particles in the style via
 * {@link ParticleControler} instances found in the style's particle map.
 */
public final class StyleAlphaHelper extends AlphaHelper {
    private ParticleGroupStyle style;
    private float currentAlpha = 1.0f;

    public StyleAlphaHelper(double minAlpha, double maxAlpha, int alphaTick) {
        super(minAlpha, maxAlpha, alphaTick);
    }

    public ParticleGroupStyle getStyle() {
        return style;
    }

    public void setStyle(ParticleGroupStyle style) {
        this.style = style;
    }

    public float getCurrentAlphaFloat() {
        return currentAlpha;
    }

    public void setCurrentAlpha(float currentAlpha) {
        this.currentAlpha = currentAlpha;
    }

    @Override
    public Controlable<?> getLoadedGroup() {
        return style;
    }

    @Override
    public double getCurrentAlpha() {
        return currentAlpha;
    }

    @Override
    public void setAlpha(double alpha) {
        this.currentAlpha = (float) alpha;
        if (style == null) return;
        for (Controlable<?> c : style.getParticles().values()) {
            if (c instanceof ParticleControler pc) {
                try {
                    pc.getParticle().setParticleAlpha(currentAlpha);
                } catch (IllegalStateException e) {
                    // Particle not loaded yet
                }
            }
        }
    }

    @Override
    public void loadControler(Controlable<?> controler) {
        if (!(controler instanceof ParticleGroupStyle pgs)) {
            return;
        }
        this.style = pgs;
        setAlpha(getMinAlpha());
    }
}
