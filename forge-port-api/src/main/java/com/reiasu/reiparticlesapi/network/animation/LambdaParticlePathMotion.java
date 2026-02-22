package com.reiasu.reiparticlesapi.network.animation;

import com.reiasu.reiparticlesapi.particles.ControlableParticle;
import net.minecraft.world.phys.Vec3;

import java.util.function.IntFunction;

/**
 * Concrete path motion that uses a lambda to compute the path offset.
 * The function takes the current tick index and returns a Vec3 offset from origin.
 * <p>
 * Inherits teleport and validity logic from {@link ParticlePathMotion}.
 */
public final class LambdaParticlePathMotion extends ParticlePathMotion {
    private final IntFunction<Vec3> path;

    /**
     * @param origin   the world origin position
     * @param particle the target particle
     * @param path     function from tick index to Vec3 offset
     */
    public LambdaParticlePathMotion(Vec3 origin, ControlableParticle particle, IntFunction<Vec3> path) {
        super(origin, particle);
        this.path = path;
    }

    public IntFunction<Vec3> getPath() {
        return path;
    }

    @Override
    public Vec3 pathFunction() {
        return path.apply(getCurrentTick());
    }
}
