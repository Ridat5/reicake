package com.reiasu.reiparticlesapi.event.events.entity.player;

import com.reiasu.reiparticlesapi.event.events.entity.EntityEvent;
import net.minecraft.world.entity.player.Player;

public abstract class PlayerEvent extends EntityEvent {
    private final Player player;

    protected PlayerEvent(Player player) {
        super(player);
        this.player = player;
    }

    public final Player getPlayer() {
        return player;
    }
}

