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
 * Center-focused sword formation composition that renders a polygon of swords
 * radiating from the center with rune accents.
 * Server-side port of the Fabric original's complex client-side composition.
 */
public final class SwordCenterFormationComposition extends AutoParticleComposition {
    private static final DustParticleOptions SWORD_COLOR =
            new DustParticleOptions(new Vector3f(253f / 255f, 253f / 255f, 222f / 255f), 0.6f);
    private static final DustParticleOptions RUNE_COLOR =
            new DustParticleOptions(new Vector3f(173f / 255f, 216f / 255f, 174f / 255f), 0.5f);
    private static final DustParticleOptions CIRCLE_COLOR =
            new DustParticleOptions(new Vector3f(0.62f, 0.88f, 1.0f), 0.5f);

    private static final List<RelativeLocation> SWORD_RUNE = buildSwordRune();
    private static final int POLYGON_SIDES = 9;
    private static final double POLYGON_RADIUS = 17.0;

    private RelativeLocation direction = new RelativeLocation(0.0, 0.0, -1.0);
    private int tick;
    private double rotateAngle;
    private double scaleProgress;

    public SwordCenterFormationComposition(Vec3 position, Level world) {
        setPosition(position == null ? Vec3.ZERO : position);
        setWorld(world);
        setVisibleRange(512.0);
    }

    public SwordCenterFormationComposition setDirection(RelativeLocation direction) {
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
        rotateAngle += 0.015707963267948967;

        // Scale in with bezier-like ease
        if (tick <= 10) {
            double t = (double) tick / 10.0;
            scaleProgress = 0.01 + (1.0 - 0.01) * easeOutCubic(t);
        } else {
            scaleProgress = 1.0;
        }

        if (tick > 400) {
            remove();
            return;
        }

        Level world = getWorld();
        if (!(world instanceof ServerLevel level)) {
            return;
        }

        Vec3 center = getPosition();
        double cos = Math.cos(rotateAngle);
        double sin = Math.sin(rotateAngle);

        // Generate polygon vertices for sword placement
        List<RelativeLocation> vertices = getPolygonVertices(POLYGON_SIDES, POLYGON_RADIUS);

        // Render swords at each polygon vertex
        int swordStep = tick < 30 ? 3 : 2;
        for (RelativeLocation vertex : vertices) {
            // Each sword is rotated to point toward vertex and scaled
            double angle = Math.atan2(vertex.getZ(), vertex.getX());
            double swordCos = Math.cos(angle);
            double swordSin = Math.sin(angle);

            for (int i = 0; i < SWORD_RUNE.size(); i += swordStep) {
                RelativeLocation p = SWORD_RUNE.get(i);
                double px = p.getX() * scaleProgress * 1.5;
                double pz = p.getZ() * scaleProgress * 1.5;
                // Rotate sword to face outward
                double rx = px * swordCos - pz * swordSin;
                double rz = px * swordSin + pz * swordCos;
                // Add vertex offset
                double fx = vertex.getX() * scaleProgress + rx;
                double fz = vertex.getZ() * scaleProgress + rz;
                // Apply global rotation
                double gx = fx * cos - fz * sin;
                double gz = fx * sin + fz * cos;

                level.sendParticles(SWORD_COLOR,
                        center.x + gx,
                        center.y + p.getY() * scaleProgress * 0.1,
                        center.z + gz,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        // Inner rune circle
        int circlePoints = 240;
        double circleRadius = 5.0 * scaleProgress;
        int circleStep = tick < 25 ? 4 : 3;
        for (int i = 0; i < circlePoints; i += circleStep) {
            double a = rotateAngle * 2 + (Math.PI * 2.0 * i) / circlePoints;
            double cx = Math.cos(a) * circleRadius;
            double cz = Math.sin(a) * circleRadius;
            level.sendParticles(RUNE_COLOR,
                    center.x + cx, center.y + 0.05, center.z + cz,
                    1, 0.0, 0.0, 0.0, 0.0);
        }

        // Outer boundary circle
        double outerRadius = POLYGON_RADIUS * scaleProgress * 1.1;
        int outerPoints = 360;
        for (int i = 0; i < outerPoints; i += circleStep) {
            double a = -rotateAngle + (Math.PI * 2.0 * i) / outerPoints;
            double cx = Math.cos(a) * outerRadius;
            double cz = Math.sin(a) * outerRadius;
            level.sendParticles(CIRCLE_COLOR,
                    center.x + cx, center.y + 0.03, center.z + cz,
                    1, 0.0, 0.0, 0.0, 0.0);
        }

        // Ambient
        if (tick % 4 == 0) {
            level.sendParticles(ParticleTypes.ENCHANT,
                    center.x, center.y + 0.1, center.z,
                    10, 0.5, 0.1, 0.5, 0.0);
        }
        if (tick % 6 == 0) {
            level.sendParticles(ParticleTypes.END_ROD,
                    center.x, center.y + 0.12, center.z,
                    5, 0.4, 0.08, 0.4, 0.0);
        }
    }

    private static List<RelativeLocation> getPolygonVertices(int sides, double radius) {
        List<RelativeLocation> vertices = new ArrayList<>();
        for (int i = 0; i < sides; i++) {
            double angle = Math.PI * 2.0 * i / sides;
            vertices.add(new RelativeLocation(
                    Math.cos(angle) * radius,
                    0.0,
                    Math.sin(angle) * radius
            ));
        }
        return vertices;
    }

    private static double easeOutCubic(double t) {
        double inv = 1.0 - t;
        return 1.0 - inv * inv * inv;
    }

    private static List<RelativeLocation> buildSwordRune() {
        PointsBuilder builder = new PointsBuilder();
        // Main blade shifted +0.9
        builder.addLine(
                new RelativeLocation(0.9, 0.0, -2.0),
                new RelativeLocation(0.9, 0.0, 2.0),
                40);
        // Cross-guard shifted -0.1
        builder.addLine(
                new RelativeLocation(-0.1 - 0.6, 0.0, 0.0),
                new RelativeLocation(-0.1 + 0.6, 0.0, 0.0),
                15);
        // Blade accent shifted -0.9
        builder.addLine(
                new RelativeLocation(-0.9, 0.0, -1.5),
                new RelativeLocation(-0.9, 0.0, 1.5),
                30);
        // Pommel shifted +0.1
        builder.addLine(
                new RelativeLocation(0.1 - 0.3, 0.0, -2.2),
                new RelativeLocation(0.1 + 0.3, 0.0, -2.2),
                8);
        return builder.create();
    }
}
