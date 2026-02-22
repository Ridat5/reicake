package com.reiasu.reiparticlesapi.event.events.client;

import net.minecraft.client.Minecraft;

public final class ClientPreTickEvent extends ClientEvent {
    public ClientPreTickEvent(Minecraft client) {
        super(client);
    }
}

