package com.reiasu.reiparticleskill.util;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Utility for sending particles with force=true so they are visible
 * at long range (up to 512 blocks) instead of the default 32 blocks.
 */
public final class ParticleHelper {

    private ParticleHelper() {
    }

    public static <T extends ParticleOptions> void sendForce(
            ServerLevel level, T type,
            double x, double y, double z,
            int count,
            double xDist, double yDist, double zDist,
            double speed
    ) {
        ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(
                type, true, x, y, z,
                (float) xDist, (float) yDist, (float) zDist,
                (float) speed, count
        );
        Vec3 pos = new Vec3(x, y, z);
        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().closerToCenterThan(pos, 512.0)) {
                player.connection.send(packet);
            }
        }
    }
}
