package com.reiasu.reiparticleskill.command.layout;

import java.util.Optional;

public record DisplayIndexPlan(
        int index,
        DisplayIndexKind kind,
        Optional<DisplaySpawnProfile> profile
) {
}
