package com.reiasu.reiparticleskill.command.layout;

import com.reiasu.reiparticleskill.compat.geom.RelativeLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisplayCommandLayoutTest {
    @Test
    void shouldBuildFacingUpOrientation() {
        DisplayOrientation orientation = DisplayCommandLayout.computeOrientation(new RelativeLocation(0.0, 1.0, 0.0));
        assertEquals(0.0f, orientation.yawDegrees(), 0.0001f);
        assertEquals(-90.0f, orientation.pitchDegrees(), 0.0001f);
    }

    @Test
    void shouldProvideProfilesForDisplayIndexes() {
        assertTrue(DisplayCommandLayout.profileForIndex(2).isPresent());
        assertTrue(DisplayCommandLayout.profileForIndex(3).isPresent());
        assertTrue(DisplayCommandLayout.profileForIndex(6).isPresent());
        assertTrue(DisplayCommandLayout.profileForIndex(0).isEmpty());
    }

    @Test
    void shouldUseExpectedScaleAndSpeed() {
        DisplaySpawnProfile index2 = DisplayCommandLayout.profileForIndex(2).orElseThrow();
        DisplaySpawnProfile index6 = DisplayCommandLayout.profileForIndex(6).orElseThrow();

        assertEquals(1.0f, index2.targetScale());
        assertEquals(1.0f, index2.scaledSpeed());

        assertEquals(10.0f, index6.targetScale());
        assertEquals(0.5f, index6.scaledSpeed());
    }
}
