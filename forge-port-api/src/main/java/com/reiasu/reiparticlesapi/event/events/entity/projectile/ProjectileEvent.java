package com.reiasu.reiparticlesapi.event.events.entity.projectile;

import com.reiasu.reiparticlesapi.event.events.entity.EntityEvent;
import net.minecraft.world.entity.projectile.Projectile;

public abstract class ProjectileEvent extends EntityEvent {
    protected ProjectileEvent(Projectile entity) {
        super(entity);
    }
}

