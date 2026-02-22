package com.reiasu.reiparticleskill.barrages;

import com.reiasu.reiparticlesapi.barrages.Barrage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public enum SkillBarrageManager {
    INSTANCE;

    private final CopyOnWriteArrayList<Barrage> activeBarrages = new CopyOnWriteArrayList<>();

    public void spawn(Barrage barrage) {
        if (barrage == null || !barrage.getValid()) {
            return;
        }
        activeBarrages.add(barrage);
    }

    public void tickAll() {
        if (activeBarrages.isEmpty()) {
            return;
        }
        activeBarrages.removeIf(barrage -> {
            if (barrage == null || !barrage.getValid()) {
                return true;
            }
            barrage.tick();
            return !barrage.getValid();
        });
    }

    public void clear() {
        for (Barrage barrage : activeBarrages) {
            if (barrage == null) {
                continue;
            }
            try {
                barrage.getBindControl().cancel();
            } catch (Throwable ignored) {
                // best effort cleanup
            }
        }
        activeBarrages.clear();
    }

    public int activeCount() {
        return activeBarrages.size();
    }

    public List<Barrage> snapshot() {
        return List.copyOf(activeBarrages);
    }
}
