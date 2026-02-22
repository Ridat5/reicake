package com.reiasu.reiparticlesapi.utils.helper.impl.composition;

import com.reiasu.reiparticlesapi.network.particle.composition.ParticleComposition;
import com.reiasu.reiparticlesapi.particles.Controlable;
import com.reiasu.reiparticlesapi.utils.helper.ScaleHelper;

/**
 * {@link ScaleHelper} implementation that delegates scale operations to a
 * {@link ParticleComposition}.
 */
public final class CompositionScaleHelper extends ScaleHelper {
    private ParticleComposition composition;

    public CompositionScaleHelper(double minScale, double maxScale, int scaleTick) {
        super(minScale, maxScale, scaleTick);
    }

    public ParticleComposition getComposition() {
        return composition;
    }

    public void setComposition(ParticleComposition composition) {
        this.composition = composition;
    }

    @Override
    public Controlable<?> getLoadedGroup() {
        return composition; // ParticleComposition now implements Controlable
    }

    @Override
    public double getGroupScale() {
        return composition != null ? composition.getScale() : 1.0;
    }

    @Override
    public void scale(double scale) {
        if (composition != null) {
            composition.scale(scale);
        }
    }

    @Override
    public void loadControler(Controlable<?> controler) {
        if (controler instanceof ParticleComposition pc) {
            loadComposition(pc);
        }
    }

    /**
     * Directly load a composition reference (for use when ParticleComposition
     * doesn't implement Controlable yet).
     */
    public void loadComposition(ParticleComposition composition) {
        this.composition = composition;
        if (composition != null) {
            composition.scale(getMinScale());
        }
    }
}
