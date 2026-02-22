package com.reiasu.reiparticlesapi.event.events.entity.player;

import net.minecraft.world.entity.player.Player;

public final class PlayerLoggedInEvent extends PlayerEvent {
    public PlayerLoggedInEvent(Player player) {
        super(player);
    }
}

