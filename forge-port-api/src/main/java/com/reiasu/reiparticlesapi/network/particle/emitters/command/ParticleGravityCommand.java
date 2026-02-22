package com.reiasu.reiparticlesapi.network.particle.emitters.command;

import com.reiasu.reiparticlesapi.network.particle.emitters.ControlableParticleData;
import com.reiasu.reiparticlesapi.particles.ControlableParticle;
import net.minecraft.world.phys.Vec3;

/**
 * Applies downward gravity to a particle's velocity.
 */
public final class ParticleGravityCommand implements ParticleCommand {

    private final double gravity;

    public ParticleGravityCommand(double gravity) {
        this.gravity = gravity;
    }

    public double getGravity() {
        return gravity;
    }

    @Override
    public void execute(ControlableParticleData data, ControlableParticle particle) {
        Vec3 vel = data.getVelocity();
        data.setVelocity(vel.add(0.0, -gravity, 0.0));
    }
}
