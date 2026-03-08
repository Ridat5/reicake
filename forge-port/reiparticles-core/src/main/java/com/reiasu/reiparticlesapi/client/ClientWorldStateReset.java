// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.client;

import com.reiasu.reiparticlesapi.display.DisplayEntityManager;
import com.reiasu.reiparticlesapi.network.particle.composition.manager.ParticleCompositionManager;
import com.reiasu.reiparticlesapi.network.particle.style.ParticleStyleManager;
import com.reiasu.reiparticlesapi.particles.control.group.ClientParticleGroupManager;
import com.reiasu.reiparticlesapi.renderer.client.ClientRenderEntityManager;

public final class ClientWorldStateReset {
    private ClientWorldStateReset() {
    }

    public static void reset() {
        ParticleCompositionManager.INSTANCE.clearClient();
        DisplayEntityManager.INSTANCE.clearClient();
        ParticleStyleManager.clearAllVisible();
        ClientRenderEntityManager.INSTANCE.clear();
        ClientParticleGroupManager.INSTANCE.clearAllVisible();
    }
}
