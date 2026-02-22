package com.reiasu.reiparticlesapi.particles.impl;

import com.reiasu.reiparticlesapi.particles.ControlableParticleEffect;
import com.reiasu.reiparticlesapi.particles.ReiModParticles;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class ControlableFallingDustEffect implements ControlableParticleEffect {
    private UUID uuid;
    private final BlockState blockState;
    private final boolean faceToPlayer;

    public ControlableFallingDustEffect(UUID uuid, BlockState blockState, boolean faceToPlayer) {
        this.uuid = uuid;
        this.blockState = blockState;
        this.faceToPlayer = faceToPlayer;
    }

    public ControlableFallingDustEffect(UUID uuid, BlockState blockState, boolean faceToPlayer, int ignored, Object ignored2) {
        this(uuid, blockState, faceToPlayer);
    }

    @Override
    public UUID getControlUUID() { return uuid; }

    @Override
    public void setControlUUID(UUID uuid) { this.uuid = uuid; }

    @Override
    public ControlableFallingDustEffect clone() {
        return new ControlableFallingDustEffect(UUID.randomUUID(), blockState, faceToPlayer);
    }

    public BlockState getBlockState() { return blockState; }
    @Override public boolean getFaceToPlayer() { return faceToPlayer; }

    @Override public ParticleType<?> getType() { return ReiModParticles.CONTROLABLE_FALLING_DUST.get(); }
    @Override public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeUUID(uuid); buf.writeBoolean(faceToPlayer);
    }
    @Override public String writeToString() { return "coo:falling_dust"; }
}
