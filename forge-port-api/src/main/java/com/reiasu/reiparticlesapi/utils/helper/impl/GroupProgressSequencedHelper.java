package com.reiasu.reiparticlesapi.utils.helper.impl;

import com.reiasu.reiparticlesapi.network.particle.SequencedServerParticleGroup;
import com.reiasu.reiparticlesapi.particles.Controlable;
import com.reiasu.reiparticlesapi.utils.helper.ProgressSequencedHelper;

/**
 * {@link ProgressSequencedHelper} implementation for sequenced particle groups
 * (SequencedParticleGroup).
 * <p>
 * Delegates add/remove operations through {@link SequencedServerParticleGroup}.
 */
public final class GroupProgressSequencedHelper extends ProgressSequencedHelper {
    private Controlable<?> linkedStyle;

    public GroupProgressSequencedHelper(int maxCount, int progressMaxTick) {
        super(maxCount, progressMaxTick);
    }

    @Override
    public void addMultiple(int count) {
        if (linkedStyle instanceof SequencedServerParticleGroup sg) {
            sg.addMultiple(count);
        }
    }

    @Override
    public void removeMultiple(int count) {
        if (linkedStyle instanceof SequencedServerParticleGroup sg) {
            sg.removeMultiple(count);
        }
    }

    @Override
    public Controlable<?> getLoadedStyle() {
        return linkedStyle;
    }

    @Override
    protected void changeStatusBatch(int[] indexes, boolean status) {
        if (linkedStyle instanceof SequencedServerParticleGroup sg) {
            for (int index : indexes) {
                sg.changeSingle(index, status);
            }
        }
    }

    /**
     * Sync progress from server-side current tick to client-side display.
     */
    public void syncProgressFromServer(int current) {
        setCurrent(Math.max(0, Math.min(getProgressMaxTick(), current)));
        int targetCount = (int) Math.round(
                (double) current / (double) getProgressMaxTick() * (double) getMaxCount()
        );
        if (linkedStyle instanceof SequencedServerParticleGroup sg) {
            int current_count = sg.getServerSequencedParticleCount();
            if (targetCount > current_count) {
                sg.addMultiple(targetCount - current_count);
            } else if (targetCount < current_count) {
                sg.removeMultiple(current_count - targetCount);
            }
        }
    }

    @Override
    public void loadControler(Controlable<?> controler) {
        if (controler instanceof SequencedServerParticleGroup) {
            this.linkedStyle = controler;
        }
    }
}
