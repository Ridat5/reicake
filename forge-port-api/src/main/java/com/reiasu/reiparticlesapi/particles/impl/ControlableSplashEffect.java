package com.reiasu.reiparticlesapi.particles.impl;

import com.reiasu.reiparticlesapi.particles.ControlableParticleEffect;
import com.reiasu.reiparticlesapi.particles.ReiModParticles;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class ControlableSplashEffect implements ControlableParticleEffect {
    private UUID uuid;
    private final boolean faceToPlayer;

    public ControlableSplashEffect(UUID uuid, boolean faceToPlayer) {
        this.uuid = uuid;
        this.faceToPlayer = faceToPlayer;
    }

    public ControlableSplashEffect(UUID uuid, boolean faceToPlayer, int ignored, Object ignored2) {
        this(uuid, faceToPlayer);
    }

    @Override
    public UUID getControlUUID() { return uuid; }

    @Override
    public void setControlUUID(UUID uuid) { this.uuid = uuid; }

    @Override
    public ControlableSplashEffect clone() {
        return new ControlableSplashEffect(UUID.randomUUID(), faceToPlayer);
    }

    @Override public boolean getFaceToPlayer() { return faceToPlayer; }

    @Override public ParticleType<?> getType() { return ReiModParticles.CONTROLABLE_SPLASH.get(); }
    @Override public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeUUID(uuid); buf.writeBoolean(faceToPlayer);
    }
    @Override public String writeToString() { return "coo:splash"; }
}
