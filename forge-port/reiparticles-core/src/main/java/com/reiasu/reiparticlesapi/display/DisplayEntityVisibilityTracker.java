// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.display;

import com.reiasu.reiparticlesapi.network.ReiParticlesNetwork;
import com.reiasu.reiparticlesapi.network.ServerSyncPacketBudget;
import com.reiasu.reiparticlesapi.network.packet.PacketDisplayEntityS2C;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

final class DisplayEntityVisibilityTracker {
    private static final int PLAYER_SHARD_COUNT = 4;

    private final Map<UUID, Set<UUID>> visible;
    private long visibilityTick;
    private int statSynced;
    private int statSkippedShard;
    private int statThrottled;
    private volatile int[] lastTickStats = new int[3];

    DisplayEntityVisibilityTracker(Map<UUID, Set<UUID>> visible) {
        this.visible = visible;
    }

    long beginTick() {
        lastTickStats = new int[]{statSynced, statSkippedShard, statThrottled};
        statSynced = 0;
        statSkippedShard = 0;
        statThrottled = 0;
        return visibilityTick++;
    }

    int[] getLastTickStats() {
        return lastTickStats.clone();
    }

    boolean updateClientVisible(DisplayEntity entity,
                                ServerLevel level,
                                long tick,
                                PacketDisplayEntityS2C dirtyPacket) {
        beginSharedBudget(level);
        boolean dirtyPacketFullySynced = true;
        List<ServerPlayer> players = level.players();
        for (int i = 0; i < players.size(); i++) {
            if (!shouldProcessPlayerIndex(i, tick)) {
                statSkippedShard++;
                continue;
            }
            ServerPlayer player = players.get(i);
            Set<UUID> visibleSet = visible.computeIfAbsent(player.getUUID(), ignored -> ConcurrentHashMap.newKeySet());
            boolean shouldView = canViewDisplay(entity, player);
            boolean alreadyVisible = visibleSet.contains(entity.getControlUUID());

            if (shouldView && !alreadyVisible) {
                addView(player, entity, visibleSet);
                continue;
            }
            if (!shouldView && alreadyVisible) {
                removeView(player, entity, visibleSet);
                continue;
            }
            if (shouldView && dirtyPacket != null) {
                dirtyPacketFullySynced &= sendPacket(player, dirtyPacket);
            }
        }
        return dirtyPacketFullySynced;
    }

    void removeAllViews(DisplayEntity entity, ServerLevel level) {
        UUID displayId = entity.getControlUUID();
        PacketDisplayEntityS2C removePacket = hasSyncType(entity) ? PacketDisplayEntityS2C.ofRemove(entity) : null;
        for (Map.Entry<UUID, Set<UUID>> entry : visible.entrySet()) {
            if (!entry.getValue().remove(displayId) || level == null || removePacket == null) {
                continue;
            }
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player != null) {
                ReiParticlesNetwork.sendTo(player, removePacket);
            }
        }
    }

    void pruneDisconnectedPlayers(Collection<DisplayEntity> displays) {
        if (displays.isEmpty()) {
            clear();
            return;
        }
        net.minecraft.server.MinecraftServer server = null;
        for (DisplayEntity display : displays) {
            if (display.level() instanceof ServerLevel serverLevel) {
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
        visibilityTick = 0L;
        statSynced = 0;
        statSkippedShard = 0;
        statThrottled = 0;
        lastTickStats = new int[3];
    }

    static int playerShardCount() {
        return PLAYER_SHARD_COUNT;
    }

    static boolean shouldProcessPlayerIndex(int playerIndex, long tick) {
        return playerIndex % PLAYER_SHARD_COUNT == (int) (tick % PLAYER_SHARD_COUNT);
    }

    static boolean canViewDisplay(DisplayEntity entity, ServerPlayer player) {
        if (entity == null || player == null || entity.level() == null) {
            return false;
        }
        if (player.isRemoved() || player.isSpectator()) {
            return false;
        }
        if (entity.level() != player.level()) {
            return false;
        }
        return DisplayEntityManager.isWithinVisibleRange(entity.getPos(), player.position(), entity.getVisibleRange());
    }

    static boolean markVisibleAfterSuccessfulSend(Set<UUID> visibleSet, UUID displayId, BooleanSupplier sendAction) {
        if (visibleSet.contains(displayId)) {
            return false;
        }
        if (!sendAction.getAsBoolean()) {
            return false;
        }
        return visibleSet.add(displayId);
    }

    private void addView(ServerPlayer player, DisplayEntity entity, Set<UUID> visibleSet) {
        markVisibleAfterSuccessfulSend(visibleSet, entity.getControlUUID(),
                () -> sendPacket(player, PacketDisplayEntityS2C.ofCreate(entity)));
    }

    private void removeView(ServerPlayer player, DisplayEntity entity, Set<UUID> visibleSet) {
        visibleSet.remove(entity.getControlUUID());
        if (hasSyncType(entity)) {
            ReiParticlesNetwork.sendTo(player, PacketDisplayEntityS2C.ofRemove(entity));
        }
    }

    private boolean sendPacket(ServerPlayer player, PacketDisplayEntityS2C packet) {
        if (packet == null) {
            return false;
        }
        if (!ServerSyncPacketBudget.tryAcquire()) {
            statThrottled++;
            return false;
        }
        statSynced++;
        ReiParticlesNetwork.sendTo(player, packet);
        return true;
    }

    private static boolean hasSyncType(DisplayEntity entity) {
        return entity != null && entity.typeId() != null && !entity.typeId().isBlank();
    }

    private static void beginSharedBudget(ServerLevel level) {
        if (level.getServer() != null) {
            ServerSyncPacketBudget.beginServerTick(level.getServer().getTickCount());
            return;
        }
        ServerSyncPacketBudget.beginServerTick(level.getGameTime());
    }
}
