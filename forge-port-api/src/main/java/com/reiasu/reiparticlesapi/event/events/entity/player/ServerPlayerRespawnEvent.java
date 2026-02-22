package com.reiasu.reiparticlesapi.event.events.entity.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class ServerPlayerRespawnEvent extends PlayerEvent {
    private final Level world;

    public ServerPlayerRespawnEvent(Player player, Level world) {
        super(player);
        this.world = world;
    }

    public Level getWorld() {
        return world;
    }
}

