package com.reiasu.reiparticlesapi.utils;

import com.reiasu.reiparticlesapi.network.ReiParticlesNetwork;
import com.reiasu.reiparticlesapi.network.packet.CameraShakeS2CPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class ServerCameraUtil {
    public static final ServerCameraUtil INSTANCE = new ServerCameraUtil();

    private ServerCameraUtil() {
    }

    public void sendShake(Object world, Object target, double range, double power, int durationTicks) {
        if (!(world instanceof ServerLevel serverLevel)) {
            throw new IllegalArgumentException("world must be ServerLevel");
        }
        if (!(target instanceof Vec3 origin)) {
            throw new IllegalArgumentException("target must be Vec3");
        }
        sendShake(serverLevel, origin, range, power, durationTicks);
    }

    public void sendShake(ServerLevel world, double power, int durationTicks) {
        if (world == null) {
            throw new IllegalArgumentException("world must not be null");
        }
        if (power <= 0.0) {
            throw new IllegalArgumentException("power must be > 0");
        }
        if (durationTicks <= 0) {
            throw new IllegalArgumentException("durationTicks must be > 0");
        }

        Vec3 origin = Vec3.ZERO;
        CameraShakeS2CPacket packet = new CameraShakeS2CPacket(-1.0, origin, power, durationTicks);
        for (ServerPlayer player : world.players()) {
            ReiParticlesNetwork.sendTo(player, packet);
        }
    }

    public void sendShake(ServerLevel world, Vec3 origin, double range, double power, int durationTicks) {
        if (world == null) {
            throw new IllegalArgumentException("world must not be null");
        }
        if (origin == null) {
            throw new IllegalArgumentException("origin must not be null");
        }
        if (range <= 0.0) {
            throw new IllegalArgumentException("range must be > 0");
        }
        if (power <= 0.0) {
            throw new IllegalArgumentException("power must be > 0");
        }
        if (durationTicks <= 0) {
            throw new IllegalArgumentException("durationTicks must be > 0");
        }

        CameraShakeS2CPacket packet = new CameraShakeS2CPacket(range, origin, power, durationTicks);
        for (ServerPlayer player : world.players()) {
            if (player.position().distanceTo(origin) <= range) {
                ReiParticlesNetwork.sendTo(player, packet);
            }
        }
    }
}
