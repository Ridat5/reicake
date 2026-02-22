package com.reiasu.reiparticlesapi.utils.helper.impl;

import com.reiasu.reiparticlesapi.network.particle.ServerParticleGroup;
import com.reiasu.reiparticlesapi.network.particle.style.ParticleGroupStyle;
import com.reiasu.reiparticlesapi.particles.Controlable;
import com.reiasu.reiparticlesapi.utils.RelativeLocation;
import com.reiasu.reiparticlesapi.utils.helper.BezierValueScaleHelper;

/**
 * {@link BezierValueScaleHelper} implementation for particle groups
 * (ControlableParticleGroup) using bezier-curved scale transitions.
 * <p>
 * Delegates scale operations through {@link ServerParticleGroup} or
 * {@link ParticleGroupStyle} depending on the loaded controller type.
 */
public final class GroupBezierValueScaleHelper extends BezierValueScaleHelper {
    private Controlable<?> group;

    public GroupBezierValueScaleHelper(
            int scaleTick,
            double minScale,
            double maxScale,
            RelativeLocation controlPoint1,
            RelativeLocation controlPoint2
    ) {
        super(scaleTick, minScale, maxScale, controlPoint1, controlPoint2);
    }

    public Controlable<?> getGroup() {
        return group;
    }

    public void setGroup(Controlable<?> group) {
        this.group = group;
    }

    @Override
    public void loadControler(Controlable<?> controler) {
        if (controler instanceof ServerParticleGroup || controler instanceof ParticleGroupStyle) {
            this.group = controler;
        }
    }

    @Override
    public Controlable<?> getLoadedGroup() {
        return group;
    }

    @Override
    public double getGroupScale() {
        if (group instanceof ParticleGroupStyle pgs) {
            return pgs.getScale();
        }
        return getMinScale();
    }

    @Override
    public void scale(double scale) {
        if (group == null) {
            return;
        }
        double clamped = Math.max(getMinScale(), Math.min(getMaxScale(), scale));
        if (group instanceof ParticleGroupStyle pgs) {
            pgs.scale(clamped);
        }
    }
}
