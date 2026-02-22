package com.reiasu.reiparticleskill.display.group.layout;

import com.reiasu.reiparticleskill.compat.geom.RelativeLocation;

import java.util.List;

public final class SwordFormationLayerPresets {
    private SwordFormationLayerPresets() {
    }

    public static List<FormationLayerSpec> defaultLayerSpecs() {
        return List.of(
                new FormationLayerSpec(8.0, 12, 0.09817477042468103),
                new FormationLayerSpec(16.0, 12, -0.09817477042468103),
                new FormationLayerSpec(32.0, 36, 0.04908738521234052),
                new FormationLayerSpec(48.0, 12, -0.02454369260617026)
        );
    }

    public static List<SimpleSwordFormationLayout> createDefaultLayouts(RelativeLocation direction) {
        return defaultLayerSpecs().stream()
                .map(spec -> SimpleSwordFormationLayout.fromSpec(direction, spec))
                .toList();
    }
}