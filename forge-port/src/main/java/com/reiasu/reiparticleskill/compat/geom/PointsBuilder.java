package com.reiasu.reiparticleskill.compat.geom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class PointsBuilder {
    private final List<RelativeLocation> points = new ArrayList<>();
    private RelativeLocation axis = new RelativeLocation(0.0, 0.0, 1.0);

    public PointsBuilder axis(RelativeLocation axis) {
        this.axis = axis.copy();
        return this;
    }

    public PointsBuilder addLine(RelativeLocation from, RelativeLocation to, int steps) {
        if (steps <= 1) {
            points.add(from.copy());
            points.add(to.copy());
            return this;
        }

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            points.add(new RelativeLocation(
                    from.getX() + (to.getX() - from.getX()) * t,
                    from.getY() + (to.getY() - from.getY()) * t,
                    from.getZ() + (to.getZ() - from.getZ()) * t
            ));
        }
        return this;
    }

    public PointsBuilder addCircle(double radius, int samples) {
        if (samples <= 0) {
            return this;
        }
        for (int i = 0; i < samples; i++) {
            double t = (Math.PI * 2.0 * i) / samples;
            points.add(new RelativeLocation(Math.cos(t) * radius, 0.0, Math.sin(t) * radius));
        }
        return this;
    }

    public PointsBuilder addPolygonInCircle(int sides, int pointsPerEdge, double radius) {
        List<RelativeLocation> vertices = Math3DUtil.INSTANCE.getPolygonInCircleVertices(sides, radius);
        if (vertices.size() < 3) {
            return this;
        }
        for (int i = 0; i < vertices.size(); i++) {
            RelativeLocation from = vertices.get(i);
            RelativeLocation to = vertices.get((i + 1) % vertices.size());
            addLine(from, to, pointsPerEdge);
        }
        return this;
    }

    public PointsBuilder withBuilder(PointsBuilder other) {
        points.addAll(other.createWithoutClone());
        return this;
    }

    public PointsBuilder pointsOnEach(Consumer<RelativeLocation> action) {
        for (RelativeLocation point : points) {
            action.accept(point);
        }
        return this;
    }

    public PointsBuilder addWith(Function<Math3DUtil, Collection<RelativeLocation>> generator) {
        Collection<RelativeLocation> generated = generator.apply(Math3DUtil.INSTANCE);
        if (generated != null) {
            points.addAll(generated);
        }
        return this;
    }

    public PointsBuilder rotateAsAxis(double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        for (RelativeLocation point : points) {
            double x = point.getX();
            double z = point.getZ();
            point.setX(x * cos - z * sin);
            point.setZ(x * sin + z * cos);
        }
        return this;
    }

    public PointsBuilder rotateTo(RelativeLocation direction) {
        RelativeLocation dir = direction.copy().normalize();
        if (dir.length() == 0.0) {
            return this;
        }
        double yaw = Math3DUtil.INSTANCE.getYawFromLocation(dir);
        return rotateAsAxis(yaw);
    }

    public List<RelativeLocation> create() {
        List<RelativeLocation> copy = new ArrayList<>(points.size());
        for (RelativeLocation point : points) {
            copy.add(point.copy());
        }
        return copy;
    }

    public List<RelativeLocation> createWithoutClone() {
        return Collections.unmodifiableList(points);
    }

    public RelativeLocation getAxis() {
        return axis.copy();
    }
}
