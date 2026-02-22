package com.reiasu.reiparticlesapi.event.events.entity;

import net.minecraft.world.entity.LivingEntity;

public abstract class LivingEntityEvent extends EntityEvent {
    protected LivingEntityEvent(LivingEntity entity) {
        super(entity);
    }
}

