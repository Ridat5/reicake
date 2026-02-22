package com.reiasu.reiparticleskill.command.layout;

import com.reiasu.reiparticleskill.compat.geom.Math3DUtil;
import com.reiasu.reiparticleskill.compat.geom.RelativeLocation;

import java.util.Optional;

public final class DisplayCommandLayout {
    private DisplayCommandLayout() {
    }

    public static DisplayOrientation computeOrientation(RelativeLocation lookAt) {
        double yaw = Math3DUtil.INSTANCE.getYawFromLocation(lookAt) * 180.0 / Math.PI;
        double pitch = Math3DUtil.INSTANCE.getPitchFromLocation(lookAt) * 180.0 / Math.PI;
        return new DisplayOrientation((float) yaw, (float) pitch);
    }

    public static Optional<DisplaySpawnProfile> profileForIndex(int index) {
        DisplayOrientation upOrientation = computeOrientation(new RelativeLocation(0.0, 1.0, 0.0));

        return switch (index) {
            case 2, 3 -> Optional.of(new DisplaySpawnProfile(upOrientation, 1.0f, 1.0f));
            case 6 -> Optional.of(new DisplaySpawnProfile(upOrientation, 10.0f, 0.5f));
            default -> Optional.empty();
        };
    }
}