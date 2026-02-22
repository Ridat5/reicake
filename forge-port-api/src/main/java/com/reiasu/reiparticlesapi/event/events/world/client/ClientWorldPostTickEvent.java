package com.reiasu.reiparticlesapi.event.events.world.client;

import com.reiasu.reiparticlesapi.event.events.world.WorldEvent;
import net.minecraft.client.multiplayer.ClientLevel;

public final class ClientWorldPostTickEvent extends WorldEvent {
    public ClientWorldPostTickEvent(ClientLevel world) {
        super(world);
    }
}

