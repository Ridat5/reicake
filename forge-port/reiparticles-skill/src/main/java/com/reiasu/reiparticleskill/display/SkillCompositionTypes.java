// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticleskill.display;

import com.reiasu.reiparticlesapi.network.particle.composition.manager.ParticleCompositionManager;
import com.reiasu.reiparticleskill.particles.preview.display.ChangingComposition;
import com.reiasu.reiparticleskill.particles.preview.display.Formation;

public final class SkillCompositionTypes {
    private SkillCompositionTypes() {
    }

    public static void register() {
        ParticleCompositionManager.INSTANCE.registerAutoType(ChangingComposition.class);
        ParticleCompositionManager.INSTANCE.registerAutoType(Formation.class);
    }
}
