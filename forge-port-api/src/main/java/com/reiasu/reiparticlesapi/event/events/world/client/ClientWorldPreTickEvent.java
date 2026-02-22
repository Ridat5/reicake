package com.reiasu.reiparticlesapi.event.events.world.client;

import com.reiasu.reiparticlesapi.event.events.world.WorldEvent;
import net.minecraft.client.multiplayer.ClientLevel;

public final class ClientWorldPreTickEvent extends WorldEvent {
    public ClientWorldPreTickEvent(ClientLevel world) {
        super(world);
    }
}

