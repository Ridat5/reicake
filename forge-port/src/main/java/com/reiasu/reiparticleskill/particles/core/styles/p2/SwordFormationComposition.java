package com.reiasu.reiparticleskill.particles.core.styles.p2;

import com.reiasu.reiparticlesapi.network.particle.composition.AutoParticleComposition;
import com.reiasu.reiparticlesapi.network.particle.composition.CompositionData;
import com.reiasu.reiparticlesapi.utils.RelativeLocation;
import com.reiasu.reiparticlesapi.utils.builder.PointsBuilder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sword formation composition that renders a rotating sword shape with
 * inner circle glyphs and outer enchantment rings.
 * Server-side port of the Fabric original's complex client-side composition.
 */
public final class SwordFormationComposition extends AutoParticleComposition {
    private static final DustParticleOptions SWORD_COLOR =
            new DustParticleOptions(new Vector3f(253f / 255f, 253f / 255f, 222f / 255f), 0.65f);
    private static final DustParticleOptions CIRCLE_COLOR =
            new DustParticleOptions(new Vector3f(0.62f, 0.88f, 1.0f), 0.55f);
    private static final DustParticleOptions GLYPH_COLOR =
            new DustParticleOptions(new Vector3f(160f / 255f, 1.0f, 221f / 255f), 0.45f);

    private static final List<RelativeLocation> SWORD_POINTS = buildSwordShape();
    private static final List<RelativeLocation> INNER_CIRCLE = new PointsBuilder()
            .addCircle(8.0, 480)
            .create();
    private static final List<RelativeLocation> OUTER_CIRCLE = new PointsBuilder()
            .addCircle(10.0, 480)
            .create();

    private Vec3 direction = new Vec3(0.0, 0.0, -1.0);
    private int tick;
    private double rotateAngle;
    private double scaleProgress;

    public SwordFormationComposition(Vec3 position, Level world) {
        setPosition(position == null ? Vec3.ZERO : position);
        setWorld(world);
        setVisibleRange(512.0);
    }

    public SwordFormationComposition setDirection(Vec3 direction) {
        this.direction = direction;
        return this;
    }

    @Override
    public Map<CompositionData, RelativeLocation> getParticles() {
        Map<CompositionData, RelativeLocation> result = new LinkedHashMap<>();
        result.put(new CompositionData(), new RelativeLocation());
        return result;
    }

    @Override
    public void onDisplay() {
        tick = 0;
        rotateAngle = 0.0;
        scaleProgress = 0.01;
    }

    @Override
    public void tick() {
        super.tick();
        if (getCanceled()) {
            return;
        }

        tick++;
        rotateAngle += 0.04908738521234052;

        // Scale in with bezier-like ease
        if (tick <= 18) {
            double t = (double) tick / 18.0;
            scaleProgress = 0.01 + (4.0 - 0.01) * easeOutCubic(t);
        } else {
            scaleProgress = 4.0;
        }

        if (tick > 300) {
            remove();
            return;
        }

        Level world = getWorld();
        if (!(world instanceof ServerLevel level)) {
            return;
        }

        Vec3 center = getPosition();

        // Build rotation basis from direction
        Vec3 dir = direction.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, -1.0) : direction.normalize();
        Vec3 basisA = dir.cross(new Vec3(0.0, 1.0, 0.0));
        if (basisA.lengthSqr() < 1.0E-6) {
            basisA = dir.cross(new Vec3(1.0, 0.0, 0.0));
        }
        basisA = basisA.normalize();
        Vec3 basisB = dir.cross(basisA).normalize();

        double cos = Math.cos(rotateAngle);
        double sin = Math.sin(rotateAngle);
        double scale = scaleProgress * 1.25 * 2.0;

        // Render sword shape
        int swordStep = tick < 25 ? 3 : 2;
        for (int i = 0; i < SWORD_POINTS.size(); i += swordStep) {
            RelativeLocation p = SWORD_POINTS.get(i);
            Vec3 rotated = rotateAndProject(p, cos, sin, scale, basisA, basisB, center);
            level.sendParticles(SWORD_COLOR,
                    rotated.x, rotated.y, rotated.z,
                    1, 0.0, 0.0, 0.0, 0.0);
        }

        // Render inner circle
        double circleScale = scaleProgress;
        double circleCos = Math.cos(-rotateAngle * 0.5);
        double circleSin = Math.sin(-rotateAngle * 0.5);
        int circleStep = tick < 30 ? 4 : 3;
        for (int i = 0; i < INNER_CIRCLE.size(); i += circleStep) {
            RelativeLocation p = INNER_CIRCLE.get(i);
            Vec3 rotated = rotateAndProject(p, circleCos, circleSin, circleScale, basisA, basisB, center);
            level.sendParticles(CIRCLE_COLOR,
                    rotated.x, rotated.y, rotated.z,
                    1, 0.0, 0.0, 0.0, 0.0);
        }

        // Render outer circle
        for (int i = 0; i < OUTER_CIRCLE.size(); i += circleStep) {
            RelativeLocation p = OUTER_CIRCLE.get(i);
            Vec3 rotated = rotateAndProject(p, circleCos, circleSin, circleScale, basisA, basisB, center);
            level.sendParticles(CIRCLE_COLOR,
                    rotated.x, rotated.y, rotated.z,
                    1, 0.0, 0.0, 0.0, 0.0);
        }

        // Ambient enchantment particles
        if (tick % 3 == 0) {
            level.sendParticles(ParticleTypes.ENCHANT,
                    center.x, center.y + 0.1, center.z,
                    8, 0.4, 0.1, 0.4, 0.0);
        }
        if (tick % 5 == 0) {
            level.sendParticles(ParticleTypes.END_ROD,
                    center.x, center.y + 0.12, center.z,
                    4, 0.3, 0.08, 0.3, 0.0);
        }
    }

    private static Vec3 rotateAndProject(RelativeLocation point,
                                          double cos, double sin, double scale,
                                          Vec3 basisA, Vec3 basisB, Vec3 center) {
        double px = point.getX() * scale;
        double pz = point.getZ() * scale;
        double rx = px * cos - pz * sin;
        double rz = px * sin + pz * cos;
        return center
                .add(basisA.scale(rx))
                .add(basisB.scale(rz))
                .add(0.0, point.getY() * scale * 0.1, 0.0);
    }

    private static double easeOutCubic(double t) {
        double inv = 1.0 - t;
        return 1.0 - inv * inv * inv;
    }

    private static List<RelativeLocation> buildSwordShape() {
        PointsBuilder builder = new PointsBuilder();
        // Blade
        builder.addLine(
                new RelativeLocation(0.0, 0.0, -4.0),
                new RelativeLocation(0.0, 0.0, 4.0),
                80);
        // Cross-guard
        builder.addLine(
                new RelativeLocation(-1.2, 0.0, -0.3),
                new RelativeLocation(1.2, 0.0, -0.3),
                30);
        // Pommel accent
        builder.addLine(
                new RelativeLocation(-0.4, 0.0, -4.2),
                new RelativeLocation(0.4, 0.0, -4.2),
                10);
        return builder.create();
    }
}
