package com.reiasu.reiparticlesapi.event.events.entity.player;

import net.minecraft.world.entity.player.Player;

public final class PlayerDisconnectEvent extends PlayerEvent {
    public PlayerDisconnectEvent(Player player) {
        super(player);
    }
}

