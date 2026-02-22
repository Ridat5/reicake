package com.reiasu.reiparticleskill.compat.geom;

public final class PhysicsUtil {
    private PhysicsUtil() {
    }

    public static RelativeLocation applyDrag(RelativeLocation velocity, double dragFactor) {
        return velocity.scale(dragFactor);
    }

    public static RelativeLocation steer(RelativeLocation velocity, RelativeLocation desired, double blend) {
        double clampedBlend = Math3DUtil.INSTANCE.clamp(blend, 0.0, 1.0);
        velocity.setX(Math3DUtil.INSTANCE.lerp(velocity.getX(), desired.getX(), clampedBlend));
        velocity.setY(Math3DUtil.INSTANCE.lerp(velocity.getY(), desired.getY(), clampedBlend));
        velocity.setZ(Math3DUtil.INSTANCE.lerp(velocity.getZ(), desired.getZ(), clampedBlend));
        return velocity;
    }

    public static RelativeLocation capSpeed(RelativeLocation velocity, double maxSpeed) {
        double len = velocity.length();
        if (len > maxSpeed && len > 0.0) {
            velocity.scale(maxSpeed / len);
        }
        return velocity;
    }
}
