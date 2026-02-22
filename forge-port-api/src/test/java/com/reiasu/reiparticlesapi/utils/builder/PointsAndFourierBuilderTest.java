package com.reiasu.reiparticlesapi.utils.builder;

import com.reiasu.reiparticlesapi.utils.RelativeLocation;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PointsAndFourierBuilderTest {
    @Test
    void fourierBuilderKeepsRequestedCount() {
        FourierSeriesBuilder builder = new FourierSeriesBuilder()
                .count(314)
                .scale(0.2857142857142857)
                .addFourier(2.0, 4.0)
                .addFourier(-5.0, -3.0);
        assertEquals(314, builder.build().size());
    }

    @Test
    void discreteCircleProducesExpectedSamples() {
        PointsBuilder builder = new PointsBuilder()
                .addDiscreteCircleXZ(48.0, 200, 8.0);
        assertEquals(200, builder.create().size());
    }

    @Test
    void createWithStyleDataMapsEachPoint() {
        PointsBuilder builder = new PointsBuilder().addCircle(10.0, 32);
        Map<String, RelativeLocation> mapped = builder.createWithStyleData(point -> point.getX() > 0 ? "A" + point.getX() : "B" + point.getZ());
        assertFalse(mapped.isEmpty());
        assertTrue(mapped.values().stream().allMatch(point -> point != null));
    }
}
