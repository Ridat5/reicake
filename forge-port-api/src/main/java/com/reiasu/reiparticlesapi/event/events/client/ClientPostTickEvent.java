package com.reiasu.reiparticlesapi.event.events.client;

import net.minecraft.client.Minecraft;

public final class ClientPostTickEvent extends ClientEvent {
    public ClientPostTickEvent(Minecraft client) {
        super(client);
    }
}

