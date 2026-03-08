// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network.particle.emitters;

import com.reiasu.reiparticlesapi.config.APIConfig;
import com.reiasu.reiparticlesapi.network.ReiParticlesNetwork;
import com.reiasu.reiparticlesapi.network.ServerSyncPacketBudget;
import com.reiasu.reiparticlesapi.network.packet.PacketParticleEmittersS2C;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

final class ParticleEmitterVisibilityTracker {
    private static final int PLAYER_SHARD_COUNT = 4;

    private final Map<UUID, Set<UUID>> visible = new ConcurrentHashMap<>();
    private long visibilityTick;
    private int statSynced;
    private int statSkippedLod;
    private int statSkippedShard;
    private int statThrottled;
    private volatile int[] lastTickStats = new int[4];

    long beginTick() {
        lastTickStats = new int[]{statSynced, statSkippedLod, statSkippedShard, statThrottled};
        statSynced = 0;
        statSkippedLod = 0;
        statSkippedShard = 0;
        statThrottled = 0;
        return visibilityTick++;
    }

    int[] getLastTickStats() {
        return lastTickStats.clone();
    }

    int trackedPlayerCount() {
        return visible.size();
    }

    void updateClientVisible(ParticleEmitters emitters, long tick) {
        if (!(emitters.level() instanceof ServerLevel level)) {
            return;
        }
        beginSharedBudget(level);
        EncodedEmitterPackets packets = EncodedEmitterPackets.create(emitters);

        List<ServerPlayer> players = level.players();
        for (int i = 0; i < players.size(); i++) {
            if (i % PLAYER_SHARD_COUNT != (int) (tick % PLAYER_SHARD_COUNT)) {
                statSkippedShard++;
                continue;
            }
            ServerPlayer player = players.get(i);
            Set<UUID> visibleSet = visible.computeIfAbsent(player.getUUID(), ignored -> ConcurrentHashMap.newKeySet());
            boolean shouldView = canViewEmitter(emitters, player);
            boolean isViewing = visibleSet.contains(emitters.getUuid());

            if (shouldView && !isViewing) {
                addView(player, emitters, packets);
                continue;
            }
            if (!shouldView && isViewing) {
                removeView(player, emitters, packets);
                continue;
            }
            if (shouldView) {
                double dist = player.position().distanceTo(emitters.position());
                int lodInterval = computeLodInterval(dist, emitters.getVisibleRange());
                if (lodInterval > 1 && (emitters.getTick() % lodInterval) != 0) {
                    statSkippedLod++;
                    continue;
                }
                sendChange(packets, player);
            }
        }
    }

    static int computeLodInterval(double distance, double visibleRange) {
        double ratio = distance / Math.max(1.0, visibleRange);
        if (ratio < 0.25) {
            return 1;
        }
        if (ratio < 0.50) {
            return 3;
        }
        if (ratio < 0.75) {
            return 6;
        }
        return 12;
    }

    static boolean canViewEmitter(ParticleEmitters emitters, ServerPlayer player) {
        if (emitters.level() == null || player == null) {
            return false;
        }
        if (player.isRemoved() || player.isSpectator()) {
            return false;
        }
        if (emitters.level() != player.level()) {
            return false;
        }
        double range = Math.min(emitters.getVisibleRange(), APIConfig.INSTANCE.getMaxEmitterVisibleRange());
        return player.position().distanceTo(emitters.position()) <= range;
    }

    static boolean markVisibleAfterSuccessfulSend(Set<UUID> visibleSet, UUID emitterId, BooleanSupplier sendAction) {
        if (visibleSet.contains(emitterId)) {
            return false;
        }
        if (!sendAction.getAsBoolean()) {
            return false;
        }
        return visibleSet.add(emitterId);
    }

    void removeAllViews(ParticleEmitters emitters) {
        ServerLevel level = emitters.level() instanceof ServerLevel serverLevel ? serverLevel : null;
        EncodedEmitterPackets packets = EncodedEmitterPackets.create(emitters);
        for (Map.Entry<UUID, Set<UUID>> entry : visible.entrySet()) {
            Set<UUID> visibleSet = entry.getValue();
            if (!visibleSet.remove(emitters.getUuid()) || level == null || packets == null) {
                continue;
            }
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player != null) {
                ReiParticlesNetwork.sendTo(player, packets.removePacket());
            }
        }
    }

    void pruneDisconnectedPlayers(List<ParticleEmitters> emitters) {
        if (emitters.isEmpty()) {
            visible.clear();
            return;
        }
        net.minecraft.server.MinecraftServer server = null;
        for (ParticleEmitters emitter : emitters) {
            if (emitter.level() instanceof ServerLevel serverLevel) {
                server = serverLevel.getServer();
                break;
            }
        }
        if (server == null) {
            return;
        }
        net.minecraft.server.MinecraftServer runtime = server;
        visible.entrySet().removeIf(entry -> runtime.getPlayerList().getPlayer(entry.getKey()) == null);
    }

    void clear() {
        visible.clear();
        visibilityTick = 0;
        statSynced = 0;
        statSkippedLod = 0;
        statSkippedShard = 0;
        statThrottled = 0;
        lastTickStats = new int[4];
    }

    private boolean sendChange(EncodedEmitterPackets packets, ServerPlayer player) {
        if (packets == null) {
            return false;
        }
        if (!ServerSyncPacketBudget.tryAcquire()) {
            statThrottled++;
            return false;
        }
        statSynced++;
        ReiParticlesNetwork.sendTo(player, packets.changePacket());
        return true;
    }

    private void addView(ServerPlayer player, ParticleEmitters emitters, EncodedEmitterPackets packets) {
        Set<UUID> visibleSet = visible.computeIfAbsent(player.getUUID(), ignored -> ConcurrentHashMap.newKeySet());
        markVisibleAfterSuccessfulSend(visibleSet, emitters.getUuid(), () -> sendChange(packets, player));
    }

    private void removeView(ServerPlayer player, ParticleEmitters emitters, EncodedEmitterPackets packets) {
        Set<UUID> visibleSet = visible.computeIfAbsent(player.getUUID(), ignored -> ConcurrentHashMap.newKeySet());
        visibleSet.remove(emitters.getUuid());
        if (packets != null) {
            ReiParticlesNetwork.sendTo(player, packets.removePacket());
        }
    }

    private static void beginSharedBudget(ServerLevel level) {
        if (level.getServer() != null) {
            ServerSyncPacketBudget.beginServerTick(level.getServer().getTickCount());
            return;
        }
        ServerSyncPacketBudget.beginServerTick(level.getGameTime());
    }

    private static final class EncodedEmitterPackets {
        private final ParticleEmitters emitters;
        private final ResourceLocation emitterKey;
        private byte[] payload;
        private PacketParticleEmittersS2C changePacket;
        private PacketParticleEmittersS2C removePacket;

        private EncodedEmitterPackets(ParticleEmitters emitters, ResourceLocation emitterKey) {
            this.emitters = emitters;
            this.emitterKey = emitterKey;
        }

        private static EncodedEmitterPackets create(ParticleEmitters emitters) {
            ResourceLocation key = emitters.getEmittersID();
            if (key == null || EmitterRegistry.INSTANCE.getDecoder(key) == null) {
                return null;
            }
            return new EncodedEmitterPackets(emitters, key);
        }

        private PacketParticleEmittersS2C changePacket() {
            if (changePacket == null) {
                changePacket = new PacketParticleEmittersS2C(
                        emitterKey,
                        payload(),
                        PacketParticleEmittersS2C.PacketType.CHANGE_OR_CREATE
                );
            }
            return changePacket;
        }

        private PacketParticleEmittersS2C removePacket() {
            if (removePacket == null) {
                removePacket = new PacketParticleEmittersS2C(
                        emitterKey,
                        payload(),
                        PacketParticleEmittersS2C.PacketType.REMOVE
                );
            }
            return removePacket;
        }

        private byte[] payload() {
            if (payload == null) {
                payload = emitters.encodeToBytes();
            }
            return payload;
        }
    }
}
