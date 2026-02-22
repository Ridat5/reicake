package com.reiasu.reiparticlesapi.utils.helper.impl;

import com.reiasu.reiparticlesapi.network.buffer.ParticleControlerDataBuffer;
import com.reiasu.reiparticlesapi.network.particle.style.ParticleGroupStyle;
import com.reiasu.reiparticlesapi.particles.Controlable;
import com.reiasu.reiparticlesapi.utils.helper.StatusHelper;

import java.util.HashMap;
import java.util.Map;

public final class StyleStatusHelper extends StatusHelper {
    private ParticleGroupStyle group;
    private boolean init;

    public ParticleGroupStyle getGroup() {
        return group;
    }

    public void setGroup(ParticleGroupStyle group) {
        this.group = group;
    }

    @Override
    public void changeStatus(int status) {
        if (group == null || group.getClient()) {
            return;
        }
        Map<String, ParticleControlerDataBuffer<?>> args = new HashMap<>();
        for (ArgPair pair : toArgsPairs()) {
            args.put(pair.name(), pair.value());
        }
        group.change(args);
    }

    @Override
    public void initHelper() {
        if (group == null || init) {
            return;
        }
        init = true;
        group.addPreTickAction(style -> {
            if (getDisplayStatus() != Status.DISABLE.id()) {
                return;
            }
            setCurrent(getCurrent() + 1);
            if (getCurrent() >= getClosedInternal()) {
                style.remove();
            }
        });
    }

    @Override
    public void loadControler(Controlable<?> controler) {
        if (!(controler instanceof ParticleGroupStyle particleGroupStyle)) {
            return;
        }
        this.group = particleGroupStyle;
        initHelper();
    }
}
