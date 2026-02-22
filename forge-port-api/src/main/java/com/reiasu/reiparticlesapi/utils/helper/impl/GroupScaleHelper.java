package com.reiasu.reiparticlesapi.utils.helper.impl;

import com.reiasu.reiparticlesapi.network.particle.ServerParticleGroup;
import com.reiasu.reiparticlesapi.network.particle.style.ParticleGroupStyle;
import com.reiasu.reiparticlesapi.particles.Controlable;
import com.reiasu.reiparticlesapi.utils.helper.ScaleHelper;

/**
 * {@link ScaleHelper} implementation for particle groups (ControlableParticleGroup).
 * <p>
 * Delegates scale operations through {@link ServerParticleGroup} or
 * {@link ParticleGroupStyle} depending on the loaded controller type.
 */
public final class GroupScaleHelper extends ScaleHelper {
    private Controlable<?> group;

    public GroupScaleHelper(double minScale, double maxScale, int scaleTick) {
        super(minScale, maxScale, scaleTick);
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
