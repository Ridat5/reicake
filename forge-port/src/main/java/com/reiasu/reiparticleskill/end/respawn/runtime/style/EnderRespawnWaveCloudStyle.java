package com.reiasu.reiparticleskill.end.respawn.runtime.style;

import com.reiasu.reiparticleskill.util.ParticleHelper;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Rotating cloud ring wave with sinusoidal vertical undulation.
 * The ring breathes radially and particles ride a traveling sine wave
 * along the ring perimeter, creating an organic ripple effect.
 *
 * @author Reiasu
 */
public final class EnderRespawnWaveCloudStyle {
    private static final Vector3f MAIN_COLOR = new Vector3f(210f / 255f, 80f / 255f, 1.0f);
    private static final int SCALE_TICKS = 24;

    private final RandomSource random = RandomSource.create();

    public int emit(ServerLevel level, Vec3 center, long tick, Params params) {
        int emitted = 0;
        int countMin = Math.max(1, params.countMin);
        int countMaxExcl = Math.max(countMin + 1, params.countMax);
        int count = random.nextInt(countMin, countMaxExcl);
        double scale = easeScale(tick);
        double rotation = tick * params.rotateSpeed;
        // Breathing radius: sinusoidal expansion/contraction
        double breath = 1.0 + 0.05 * Math.sin(tick * 0.07);
        double radius = params.radius * scale * breath;
        double discrete = params.discrete * scale;
        double yOff = params.yOffset * scale;

        // Wave parameters: 4 lobes traveling around the ring
        double waveLobes = 4.0;
        double waveAmplitude = params.radius * 0.08 * scale;
        double wavePhase = tick * 0.12;

        float dustSize = (float) Math.max(0.4, Math.min(4.0,
                randomBetween(params.minSize, params.maxSize) * scale * 2.5));
        DustParticleOptions dust = new DustParticleOptions(MAIN_COLOR, dustSize);
        DustParticleOptions dustLg = new DustParticleOptions(MAIN_COLOR, Math.min(4.0f, dustSize * 1.5f));

        for (int i = 0; i < count; i++) {
            double angle = (TAU * i) / (double) count + rotation;

            // Sinusoidal radial wobble: each point has slightly different radius
            double radialWobble = 1.0 + 0.1 * Math.sin(angle * 3.0 + tick * 0.05);
            double localR = radius * radialWobble;

            // Traveling sine wave Y displacement
            double waveY = waveAmplitude * Math.sin(waveLobes * angle - wavePhase);

            // Jitter
            double jR = discrete > 0 ? random.nextDouble() * discrete : 0.0;
            double jA = random.nextDouble() * TAU;

            double px = Math.cos(angle) * localR + Math.cos(jA) * jR;
            double pz = Math.sin(angle) * localR + Math.sin(jA) * jR;

            double wx = center.x + px;
            double wy = center.y + yOff + waveY;
            double wz = center.z + pz;

            ParticleHelper.sendForce(level, dust, wx, wy, wz, 2, 0.1, 0.1, 0.1, 0.0);
            emitted += 2;

            if ((i & 1) == 0) {
                ParticleHelper.sendForce(level, dustLg, wx, wy, wz, 1, 0.0, 0.0, 0.0, 0.0);
                emitted++;
            }
        }
        return emitted;
    }

    public int emit(ServerLevel level, Vec3 center, long tick, double radius, int points, double yOffset) {
        Params p = new Params();
        p.radius = radius;
        p.countMin = points;
        p.countMax = points + 1;
        p.yOffset = yOffset;
        return emit(level, center, tick, p);
    }

    private static final double TAU = Math.PI * 2.0;

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
        public double radius = 1.0;
        public double discrete = 0.0;
        public int countMin = 80;
        public int countMax = 260;
        public double rotateSpeed = 0.05;
        public double minSize = 0.2;
        public double maxSize = 1.0;
        public boolean faceCamera = true;
        public boolean particleRandomAngle;
        public float particlePitchAngleSpeed = 0.098f;
        public float particleYawAngleSpeed = 0.098f;
        public float particleRollAngleSpeed = 0.098f;
        public double yOffset;
    }
}
