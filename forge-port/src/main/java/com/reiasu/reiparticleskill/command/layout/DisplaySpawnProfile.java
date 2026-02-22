package com.reiasu.reiparticleskill.command.layout;

public record DisplaySpawnProfile(DisplayOrientation orientation, float targetScale, float scaledSpeed) {
    public static DisplaySpawnProfile defaultFacingUp() {
        return new DisplaySpawnProfile(new DisplayOrientation(-180.0f, -90.0f), 1.0f, 1.0f);
    }
}