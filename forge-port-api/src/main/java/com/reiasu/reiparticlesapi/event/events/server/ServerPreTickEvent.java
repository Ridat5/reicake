package com.reiasu.reiparticlesapi.event.events.server;

import net.minecraft.server.MinecraftServer;

public final class ServerPreTickEvent extends ServerEvent {
    public ServerPreTickEvent(MinecraftServer server) {
        super(server);
    }
}

