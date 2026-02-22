package com.reiasu.reiparticleskill.compat.geom;

public final class GraphMathHelper {
    private GraphMathHelper() {
    }

    public static float lerp(float alpha, float from, float to) {
        return from + (to - from) * alpha;
    }

    public static double lerp(double alpha, double from, double to) {
        return from + (to - from) * alpha;
    }

    public static double inverseLerp(double value, double min, double max) {
        if (max == min) {
            return 0.0;
        }
        return (value - min) / (max - min);
    }

    public static double smoothstep(double edge0, double edge1, double x) {
        double t = Math3DUtil.INSTANCE.clamp(inverseLerp(x, edge0, edge1), 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }
}
