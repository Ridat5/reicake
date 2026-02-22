package com.reiasu.reiparticlesapi.network.particle.data;

public final class DoubleRangeDataKt {
    private DoubleRangeDataKt() {
    }

    public static DoubleRangeData minRangeTo(double min, double max) {
        return new DoubleRangeData(min, max);
    }
}