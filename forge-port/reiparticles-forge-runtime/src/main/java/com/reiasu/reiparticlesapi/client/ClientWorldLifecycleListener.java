// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.client;

import com.reiasu.reiparticlesapi.annotations.events.EventHandler;
import com.reiasu.reiparticlesapi.event.events.world.client.ClientWorldChangeEvent;

public final class ClientWorldLifecycleListener {
    @EventHandler
    public void onClientWorldChange(ClientWorldChangeEvent event) {
        ClientWorldStateReset.reset();
    }
}
