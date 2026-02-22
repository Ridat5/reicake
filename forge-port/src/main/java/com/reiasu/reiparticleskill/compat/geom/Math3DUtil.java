package com.reiasu.reiparticleskill.compat.geom;

import java.util.ArrayList;
import java.util.List;

public final class Math3DUtil {
    public static final Math3DUtil INSTANCE = new Math3DUtil();

    private Math3DUtil() {
    }

    public double length(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public double lerp(double from, double to, double alpha) {
        return from + (to - from) * alpha;
    }

    public int colorOf(int r, int g, int b) {
        int rr = (int) clamp(r, 0, 255);
        int gg = (int) clamp(g, 0, 255);
        int bb = (int) clamp(b, 0, 255);
        return (rr << 16) | (gg << 8) | bb;
    }

    public double getYawFromLocation(RelativeLocation location) {
        return Math.atan2(-location.getX(), location.getZ());
    }

    public double getPitchFromLocation(RelativeLocation location) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        return Math.atan2(-y, Math.sqrt(x * x + z * z));
    }

    public List<RelativeLocation> getPolygonInCircleVertices(int sides, double radius) {
        List<RelativeLocation> vertices = new ArrayList<>();
        if (sides < 3) {
            return vertices;
        }

        for (int i = 0; i < sides; i++) {
            double angle = Math.PI * 2.0 * i / sides;
            vertices.add(new RelativeLocation(Math.cos(angle) * radius, 0.0, Math.sin(angle) * radius));
        }
        return vertices;
    }
}
