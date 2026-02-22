package com.reiasu.reiparticlesapi.event.events.world;

import com.reiasu.reiparticlesapi.event.api.ReiEvent;
import net.minecraft.world.level.Level;

public abstract class WorldEvent extends ReiEvent {
    private final Level world;

    protected WorldEvent(Level world) {
        this.world = world;
    }

    public final Level getWorld() {
        return world;
    }
}

