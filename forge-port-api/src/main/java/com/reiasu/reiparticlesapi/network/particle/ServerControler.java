package com.reiasu.reiparticlesapi.network.particle;

public interface ServerControler<T> {
    default void tick() {
    }

    default boolean getCanceled() {
        return false;
    }

    default void teleportTo(net.minecraft.world.phys.Vec3 pos) {
    }

    default void cancel() {
    }
}