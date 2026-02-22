package com.reiasu.reiparticlesapi.event.events.server;

import com.reiasu.reiparticlesapi.event.api.ReiEvent;
import net.minecraft.server.MinecraftServer;

import java.util.Objects;

public abstract class ServerEvent extends ReiEvent {
    private final MinecraftServer server;

    protected ServerEvent(MinecraftServer server) {
        this.server = Objects.requireNonNull(server, "server");
    }

    public MinecraftServer getServer() {
        return server;
    }
}

