package com.reiasu.reiparticleskill.end.respawn.runtime.style;

import com.reiasu.reiparticleskill.util.ParticleHelper;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Ambient dust cloud filling the ritual area.
 * Distributes particles on a toroidal (donut-shaped) volume around the center,
 * with density concentrated near the torus surface and falling off toward edges.
 * The torus major radius orbits around the center; minor radius defines tube thickness.
 *
 * @author Reiasu
 */
public final class EndDustStyle {
    private static final Vector3f MAIN_COLOR = new Vector3f(210f / 255f, 80f / 255f, 1.0f);
    private static final int SCALE_TICKS = 24;

    private final RandomSource random = RandomSource.create();

    public int emit(ServerLevel level, Vec3 center, long tick, Params params) {
        int emitted = 0;
        int count = Math.max(1, params.count);
        double scale = easeScale(tick);
        double rotation = tick * params.rotateSpeed;
        // Torus: major radius = 60% of maxRadius, minor radius = 40%
        double majorR = params.maxRadius * 0.6 * scale;
        double minorR = params.maxRadius * 0.4 * scale;

        for (int i = 0; i < count; i++) {
            // Random angle around the torus tube (poloidal)
            double poloidalAngle = random.nextDouble() * Math.PI * 2.0;
            // Random angle around the torus center (toroidal)
            double toroidalAngle = random.nextDouble() * Math.PI * 2.0;
            // Density gradient: cubic falloff from tube center
            double tubeDist = random.nextDouble();
            tubeDist = tubeDist * tubeDist * tubeDist; // concentrate near surface
            double tubeR = minorR * (0.3 + 0.7 * tubeDist);

            double ringX = (majorR + tubeR * Math.cos(poloidalAngle)) * Math.cos(toroidalAngle);
            double ringZ = (majorR + tubeR * Math.cos(poloidalAngle)) * Math.sin(toroidalAngle);
            double ringY = tubeR * Math.sin(poloidalAngle);

            // Apply slow rotation
            double cos = Math.cos(rotation);
            double sin = Math.sin(rotation);
            double px = ringX * cos - ringZ * sin;
            double pz = ringX * sin + ringZ * cos;

            float size = Mth.clamp((float) randomBetween(params.sizeMin, params.sizeMax) * 2.0f, 0.2f, 4.0f);
            ParticleHelper.sendForce(level,
                    new DustParticleOptions(MAIN_COLOR, size),
                    center.x + px,
                    center.y + params.yOffset + ringY,
                    center.z + pz,
                    1, 0.0, 0.0, 0.0, 0.0);
            emitted++;
        }
        return emitted;
    }

    private double randomBetween(double min, double max) {
        double lo = Math.min(min, max);
        double hi = Math.max(min, max);
        return (Math.abs(hi - lo) < 1.0E-6) ? lo : lo + random.nextDouble() * (hi - lo);
    }

    private double easeScale(long tick) {
        if (tick <= 0L) return 0.01;
        if (tick >= SCALE_TICKS) return 1.0;
        double t = tick / (double) SCALE_TICKS;
        double inv = 1.0 - t;
        return 0.01 + 0.99 * (1.0 - inv * inv * inv * inv * inv);
    }

    public static final class Params {
        public int count = 160;
        public double maxRadius = 128.0;
        public double sizeMax = 1.0;
        public double sizeMin = 0.2;
        public double rotateSpeed = 0.007;
        public double yOffset;
    }
}
