package com.reiasu.reiparticlesapi.utils.helper.impl.composition;

import com.reiasu.reiparticlesapi.network.particle.composition.ParticleComposition;
import com.reiasu.reiparticlesapi.particles.Controlable;
import com.reiasu.reiparticlesapi.utils.RelativeLocation;
import com.reiasu.reiparticlesapi.utils.helper.BezierValueScaleHelper;

/**
 * {@link BezierValueScaleHelper} implementation that delegates scale operations
 * to a {@link ParticleComposition} using bezier-curved transitions.
 */
public final class CompositionBezierScaleHelper extends BezierValueScaleHelper {
    private ParticleComposition composition;

    public CompositionBezierScaleHelper(
            int scaleTick,
            double minScale,
            double maxScale,
            RelativeLocation controlPoint1,
            RelativeLocation controlPoint2
    ) {
        super(scaleTick, minScale, maxScale, controlPoint1, controlPoint2);
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
     * Directly load a composition reference.
     */
    public void loadComposition(ParticleComposition composition) {
        this.composition = composition;
        if (composition != null) {
            composition.scale(getMinScale());
        }
    }
}
