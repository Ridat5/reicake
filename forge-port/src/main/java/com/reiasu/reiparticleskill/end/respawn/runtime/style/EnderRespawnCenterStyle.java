package com.reiasu.reiparticleskill.end.respawn.runtime.style;

import com.reiasu.reiparticleskill.util.ParticleHelper;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Central geometric pattern for the ender dragon respawn ritual.
 * <p>
 * Three rotating layers (all Dust-only, ender purple):
 * <ul>
 *   <li>Layer 1: Rhodonea (rose) curve r=cos(5/3·θ) + dual pentagram + breathing inner ring</li>
 *   <li>Layer 2: Three Lissajous figures pitched at 0°, 40°, 80° — counter-rotates</li>
 *   <li>Layer 3: Large pulsing enchant ring with sinusoidal radial wobble</li>
 * </ul>
 *
 * @author Reiasu
 */
public final class EnderRespawnCenterStyle {
    private static final Vector3f MAIN_COLOR = new Vector3f(210f / 255f, 80f / 255f, 1.0f);
    private static final double TAU = Math.PI * 2.0;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0 - Math.sqrt(5.0));
    private static final int SCALE_TICKS = 24;

    private static final int LAYER1_ROSE_POINTS = 200;
    private static final int LAYER1_STAR_POINTS = 50;
    private static final int LAYER1_RING_POINTS = 36;
    private static final int LAYER2_LISSAJOUS_POINTS = 80;
    private static final int LAYER2_REPEAT = 3;
    private static final int LAYER3_RING_POINTS = 48;

    private final RandomSource random = RandomSource.create();

    public int emit(ServerLevel level, Vec3 center, long tick, double yOffset) {
        int emitted = 0;
        double scale = easeScale(tick);
        double breath = 1.0 + 0.06 * Math.sin(tick * 0.08);
        double rotA = tick * 0.015;
        double rotB = tick * -0.022;
        double rotC = tick * -0.011;

        DustParticleOptions dustMed = new DustParticleOptions(MAIN_COLOR, 1.8f);
        DustParticleOptions dustSm = new DustParticleOptions(MAIN_COLOR, 1.0f);
        DustParticleOptions dustLg = new DustParticleOptions(MAIN_COLOR, 3.0f);

        // ---- Layer 1: Rhodonea curve r = cos(5/3 · θ) ----
        // Period = 3·TAU for k=5/3, produces a 5-petal rose that traces 3 full loops
        for (int i = 0; i < LAYER1_ROSE_POINTS; i++) {
            double theta = 3.0 * TAU * i / (double) LAYER1_ROSE_POINTS;
            double r = Math.cos(5.0 / 3.0 * theta) * 42.0 * scale * breath;
            double rx = r * Math.cos(theta);
            double rz = r * Math.sin(theta);
            Vec3 rel = rotateY(new Vec3(rx, 0.0, rz), rotA).add(0.0, yOffset, 0.0);
            sendDust(level, center, rel, dustMed);
            emitted++;
        }

        // ---- Layer 1: Dual pentagram (5-pointed star, inner + outer) ----
        emitted += emitStar(level, center, yOffset, 5, 48.0 * scale * breath, 0.0, LAYER1_STAR_POINTS, rotA, dustMed);
        emitted += emitStar(level, center, yOffset, 5, 36.0 * scale * breath, Math.PI / 5.0, LAYER1_STAR_POINTS, rotA, dustMed);

        // ---- Layer 1: Breathing inner ring with enchant accents ----
        double ringR = 28.0 * scale * breath;
        for (int i = 0; i < LAYER1_RING_POINTS; i++) {
            double a = rotA + TAU * i / (double) LAYER1_RING_POINTS;
            double wobble = 1.0 + 0.12 * Math.sin(a * 6.0 + tick * 0.1);
            Vec3 rel = new Vec3(Math.cos(a) * ringR * wobble, yOffset, Math.sin(a) * ringR * wobble);
            sendDust(level, center, rel, dustMed);
            emitted++;
            if (i % 4 == 0) {
                sendEnchant(level, center, rel);
                emitted++;
            }
        }

        // ---- Layer 2: Three Lissajous figures pitched at different angles ----
        // x = A·sin(a·t + δ), z = B·sin(b·t)  with a=3, b=4, δ=π/4
        for (int ring = 0; ring < LAYER2_REPEAT; ring++) {
            double pitch = (TAU / 9.0) * ring;
            double phase = ring * Math.PI / 3.0;
            for (int i = 0; i < LAYER2_LISSAJOUS_POINTS; i++) {
                double t = TAU * i / (double) LAYER2_LISSAJOUS_POINTS;
                double lx = Math.sin(3.0 * t + Math.PI / 4.0 + phase) * 14.0 * scale;
                double lz = Math.sin(4.0 * t) * 14.0 * scale;
                Vec3 rel = new Vec3(lx, 0.0, lz);
                rel = rotateX(rel, pitch);
                rel = rotateY(rel, rotB).add(0.0, yOffset, 0.0);
                sendDust(level, center, rel, dustSm);
                emitted++;
            }
        }

        // ---- Layer 3: Large pulsing enchant ring with radial wobble ----
        double outerR = 72.0 * scale * breath;
        for (int i = 0; i < LAYER3_RING_POINTS; i++) {
            double a = rotC + TAU * i / (double) LAYER3_RING_POINTS;
            double wobble = 1.0 + 0.08 * Math.sin(a * 5.0 - tick * 0.06);
            Vec3 rel = new Vec3(Math.cos(a) * outerR * wobble, yOffset, Math.sin(a) * outerR * wobble);
            sendDust(level, center, rel, dustLg);
            emitted++;
            if (i % 2 == 0) {
                sendEnchant(level, center, rel);
                emitted++;
            }
        }
        return emitted;
    }

    private int emitStar(
            ServerLevel level, Vec3 center, double yOffset,
            int points, double radius, double angleOffset,
            int samples, double rotateY, DustParticleOptions dust
    ) {
        int emitted = 0;
        // Star polygon {points/2} — connect every 2nd vertex to form a star
        int skip = 2;
        for (int i = 0; i < samples; i++) {
            double u = (i / (double) samples) * points;
            int seg = (int) Math.floor(u);
            double frac = u - seg;
            int v0 = (seg * skip) % points;
            int v1 = ((seg + 1) * skip) % points;
            double a0 = angleOffset + TAU * v0 / (double) points;
            double a1 = angleOffset + TAU * v1 / (double) points;
            double x = Math.cos(a0) * radius + (Math.cos(a1) - Math.cos(a0)) * radius * frac;
            double z = Math.sin(a0) * radius + (Math.sin(a1) - Math.sin(a0)) * radius * frac;
            Vec3 rel = rotateY(new Vec3(x, 0.0, z), rotateY).add(0.0, yOffset, 0.0);
            sendDust(level, center, rel, dust);
            emitted++;
        }
        return emitted;
    }

    private void sendDust(ServerLevel level, Vec3 center, Vec3 rel, DustParticleOptions dust) {
        ParticleHelper.sendForce(level, dust,
                center.x + rel.x, center.y + rel.y, center.z + rel.z,
                1, 0.0, 0.0, 0.0, 0.0);
    }

    private void sendEnchant(ServerLevel level, Vec3 center, Vec3 rel) {
        ParticleHelper.sendForce(level, ParticleTypes.ENCHANT,
                center.x + rel.x, center.y + rel.y, center.z + rel.z,
                0, random.nextGaussian() * 0.02, random.nextGaussian() * 0.02, random.nextGaussian() * 0.02, 1.0);
    }

    private Vec3 rotateX(Vec3 p, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(p.x, p.y * cos - p.z * sin, p.y * sin + p.z * cos);
    }

    private Vec3 rotateY(Vec3 p, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(p.x * cos - p.z * sin, p.y, p.x * sin + p.z * cos);
    }

    private double easeScale(long tick) {
        if (tick <= 0L) return 0.01;
        if (tick >= SCALE_TICKS) return 1.0;
        double t = tick / (double) SCALE_TICKS;
        // Quintic ease-out: 1 - (1-t)^5
        double inv = 1.0 - t;
        return 0.01 + 0.99 * (1.0 - inv * inv * inv * inv * inv);
    }
}
