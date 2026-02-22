package com.reiasu.reiparticlesapi.event.events.server;

import net.minecraft.server.MinecraftServer;

public final class ServerPostTickEvent extends ServerEvent {
    public ServerPostTickEvent(MinecraftServer server) {
        super(server);
    }
}

