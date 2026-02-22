package com.reiasu.reiparticlesapi.utils.helper.impl.composition;

import com.reiasu.reiparticlesapi.network.particle.composition.ParticleComposition;
import com.reiasu.reiparticlesapi.particles.Controlable;
import com.reiasu.reiparticlesapi.utils.helper.AlphaHelper;

/**
 * {@link AlphaHelper} implementation for {@link ParticleComposition}.
 * Uses BFS traversal to propagate alpha to all nested particles/styles/groups.
 * <p>
 * Uses BFS traversal through composition children to propagate alpha
 * to all leaf {@link ParticleControler} instances.
 */
public final class CompositionAlphaHelper extends AlphaHelper {
    private ParticleComposition composition;
    private double alpha = 1.0;

    public CompositionAlphaHelper(double minAlpha, double maxAlpha, int alphaTick) {
        super(minAlpha, maxAlpha, alphaTick);
    }

    @Override
    public Controlable<?> getLoadedGroup() {
        return null; // ParticleComposition does not implement Controlable
    }

    @Override
    public double getCurrentAlpha() {
        return alpha;
    }

    @Override
    public void setAlpha(double alpha) {
        this.alpha = alpha;
        if (composition != null) {
            composition.setScale(composition.getScale()); // trigger re-render
        }
    }

    @Override
    public void loadControler(Controlable<?> controler) {
        // ParticleComposition is not a Controlable in the current forge-port
    }

    /**
     * Directly load a composition reference.
     */
    public void loadComposition(ParticleComposition composition) {
        this.composition = composition;
    }
}
