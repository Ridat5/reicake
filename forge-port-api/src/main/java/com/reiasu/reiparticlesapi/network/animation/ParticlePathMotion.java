package com.reiasu.reiparticlesapi.network.animation;

import com.reiasu.reiparticlesapi.network.animation.api.AbstractPathMotion;
import com.reiasu.reiparticlesapi.particles.ControlableParticle;
import net.minecraft.world.phys.Vec3;

/**
 * Path motion targeting an individual particle (ControlableParticle).
 * <p>
 * Delegates teleport and validity checks through {@link ControlableParticle}.
 */
public abstract class ParticlePathMotion extends AbstractPathMotion {
    private final ControlableParticle particle;

    protected ParticlePathMotion(Vec3 origin, ControlableParticle particle) {
        super(origin);
        this.particle = particle;
    }

    public final ControlableParticle getParticle() {
        return particle;
    }

    @Override
    public void apply(Vec3 actualPos) {
        if (particle != null) {
            particle.teleportTo(actualPos);
        }
    }

    @Override
    public boolean checkValid() {
        return particle != null && !particle.getDeath();
    }
}
