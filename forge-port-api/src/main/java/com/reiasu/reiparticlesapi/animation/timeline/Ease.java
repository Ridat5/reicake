package com.reiasu.reiparticlesapi.animation.timeline;

/**
 * Easing function interface for timeline animations.
 * Takes a progress value t in [0,1] and returns the eased value.
 */
@FunctionalInterface
public interface Ease {
    double cal(double t);
}
