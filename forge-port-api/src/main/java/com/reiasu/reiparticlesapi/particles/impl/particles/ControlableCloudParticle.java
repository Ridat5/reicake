package com.reiasu.reiparticlesapi.particles.impl.particles;

import com.reiasu.reiparticlesapi.particles.ControlableParticle;
import com.reiasu.reiparticlesapi.particles.impl.ControlableCloudEffect;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class ControlableCloudParticle extends ControlableParticle {
    private final SpriteSet provider;

    public ControlableCloudParticle(ClientLevel world, Vec3 pos, Vec3 velocity,
                                    UUID controlUUID, boolean faceToCamera, SpriteSet provider) {
        super(world, pos, velocity, controlUUID, faceToCamera);
        this.provider = provider;
        this.pickSprite(provider);
        this.getControler().addPreTickAction(p -> p.setSpriteFromAge(this.provider));
    }

    public SpriteSet getProvider() { return provider; }

    public static class Factory implements ParticleProvider<ControlableCloudEffect> {
        private final SpriteSet provider;

        public Factory(SpriteSet provider) {
            this.provider = provider;
        }

        public SpriteSet getProvider() { return provider; }

        @Override
        public Particle createParticle(ControlableCloudEffect parameters, ClientLevel world,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            return new ControlableCloudParticle(world,
                    new Vec3(x, y, z), new Vec3(velocityX, velocityY, velocityZ),
                    parameters.getControlUUID(), parameters.getFaceToPlayer(), this.provider);
        }
    }
}
