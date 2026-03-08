// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network.particle.composition.manager;

import com.mojang.logging.LogUtils;
import com.reiasu.reiparticlesapi.annotations.codec.BufferCodec;
import com.reiasu.reiparticlesapi.annotations.composition.handler.ParticleCompositionHelper;
import com.reiasu.reiparticlesapi.network.ReiParticlesNetwork;
import com.reiasu.reiparticlesapi.network.packet.PacketParticleCompositionS2C;
import com.reiasu.reiparticlesapi.network.particle.composition.ParticleComposition;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class ParticleCompositionManager {
    public static final ParticleCompositionManager INSTANCE = new ParticleCompositionManager();
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<ParticleComposition> compositions = new ArrayList<>();
    private final Map<UUID, ParticleComposition> clientView = new ConcurrentHashMap<>();
    private final Map<UUID, ParticleComposition> serverView = new ConcurrentHashMap<>();
    private final Map<String, Function<FriendlyByteBuf, ParticleComposition>> registeredTypes = new ConcurrentHashMap<>();
    private final Map<String, BufferCodec<ParticleComposition>> registeredCodecs = new ConcurrentHashMap<>();
    private final Map<Class<? extends ParticleComposition>, String> typeIdsByClass = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> visibleByPlayer = new ConcurrentHashMap<>();
    private final Set<String> warnedUnregisteredTypes = ConcurrentHashMap.newKeySet();

    private ParticleCompositionManager() {
    }

    public Map<UUID, ParticleComposition> getClientView() {
        return clientView;
    }

    public Map<UUID, ParticleComposition> getServerView() {
        return serverView;
    }

    public Map<String, Function<FriendlyByteBuf, ParticleComposition>> getRegisteredTypes() {
        return registeredTypes;
    }

    public void registerType(String type, Function<FriendlyByteBuf, ParticleComposition> decoder) {
        if (type == null || type.isBlank() || decoder == null) {
            return;
        }
        registeredTypes.put(type, decoder);
    }

    public void registerType(String type,
                             BufferCodec<ParticleComposition> codec,
                             Class<? extends ParticleComposition> compositionClass) {
        if (type == null || type.isBlank() || codec == null || compositionClass == null) {
            return;
        }
        registerType(type, codec::decode);
        registeredCodecs.put(type, codec);
        typeIdsByClass.put(compositionClass, type);
        warnedUnregisteredTypes.remove(compositionClass.getName());
    }

    public void registerAutoType(Class<? extends ParticleComposition> compositionClass) {
        if (compositionClass == null) {
            return;
        }
        try {
            Constructor<? extends ParticleComposition> constructor =
                    compositionClass.getDeclaredConstructor(Vec3.class, Level.class);
            constructor.setAccessible(true);
            ParticleComposition sample = constructor.newInstance(Vec3.ZERO, null);
            BufferCodec<ParticleComposition> codec = ParticleCompositionHelper.INSTANCE.generateCodec(sample);
            registerType(compositionClass.getName(), codec, compositionClass);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to auto-register particle composition " + compositionClass.getName(), e);
        }
    }

    public void spawn(ParticleComposition composition) {
        if (composition == null) {
            return;
        }
        composition.display();
        synchronized (compositions) {
            compositions.add(composition);
            serverView.put(composition.getControlUUID(), composition);
        }
        syncVisible(composition,
                composition.getWorld() instanceof ServerLevel level ? level : null,
                null);
        composition.clearDirty();
    }

    public void addClient(ParticleComposition composition) {
        clientView.put(composition.getControlUUID(), composition);
        composition.display();
    }

    public void tickAll() {
        synchronized (compositions) {
            Iterator<ParticleComposition> iterator = compositions.iterator();
            while (iterator.hasNext()) {
                ParticleComposition composition = iterator.next();
                ServerLevel serverLevel = composition.getWorld() instanceof ServerLevel level ? level : null;
                try {
                    composition.tick();
                } catch (Exception e) {
                    LOGGER.warn("Particle composition {} ({}) failed during server tick; removing composition",
                            composition.getControlUUID(), composition.getClass().getName(), e);
                    composition.cancel();
                }
                if (composition.getCanceled()) {
                    iterator.remove();
                    serverView.remove(composition.getControlUUID());
                    removeTrackedVisibility(composition, serverLevel);
                    continue;
                }
                PacketParticleCompositionS2C dirtyPacket = composition.consumeDirty()
                        ? buildPacket(composition, false)
                        : null;
                syncVisible(composition, serverLevel, dirtyPacket);
            }
        }
        pruneDisconnectedPlayers();
    }

    public void tickClient() {
        Iterator<Map.Entry<UUID, ParticleComposition>> iterator = clientView.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ParticleComposition> entry = iterator.next();
            ParticleComposition composition = entry.getValue();
            try {
                composition.tick();
            } catch (Exception e) {
                LOGGER.warn("Particle composition {} ({}) failed during client tick; removing composition",
                        composition.getControlUUID(), composition.getClass().getName(), e);
                composition.cancel();
            }
            if (composition.getCanceled()) {
                iterator.remove();
            }
        }
    }

    public int activeCount() {
        synchronized (compositions) {
            return compositions.size();
        }
    }

    public void clearClient() {
        for (ParticleComposition composition : clientView.values()) {
            composition.cancel();
        }
        clientView.clear();
    }

    public void clear() {
        synchronized (compositions) {
            for (ParticleComposition composition : compositions) {
                composition.cancel();
            }
            compositions.clear();
        }
        serverView.clear();
        visibleByPlayer.clear();
        clearClient();
    }

    public List<ParticleComposition> getCompositions() {
        synchronized (compositions) {
            return Collections.unmodifiableList(new ArrayList<>(compositions));
        }
    }

    PacketParticleCompositionS2C buildPacket(ParticleComposition composition, boolean distanceRemove) {
        String typeId = resolveTypeId(composition);
        if (typeId == null) {
            return null;
        }
        BufferCodec<ParticleComposition> codec = registeredCodecs.get(typeId);
        if (codec == null) {
            warnUnregisteredType(composition.getClass());
            return null;
        }

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        codec.encode(buf, composition);
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        PacketParticleCompositionS2C packet =
                new PacketParticleCompositionS2C(composition.getControlUUID(), typeId, data);
        packet.setDistanceRemove(distanceRemove);
        return packet;
    }

    private void syncVisible(ParticleComposition composition,
                             ServerLevel level,
                             PacketParticleCompositionS2C dirtyPacket) {
        if (composition == null) {
            return;
        }
        if (level == null) {
            removeTrackedVisibility(composition, null);
            return;
        }

        UUID compositionId = composition.getControlUUID();
        PacketParticleCompositionS2C packetForVisiblePlayers = dirtyPacket;
        for (ServerPlayer player : level.players()) {
            Set<UUID> visibleSet = visibleByPlayer.computeIfAbsent(player.getUUID(), ignored -> ConcurrentHashMap.newKeySet());
            boolean shouldView = canView(composition, player);
            boolean alreadyVisible = visibleSet.contains(compositionId);

            if (!shouldView && alreadyVisible) {
                visibleSet.remove(compositionId);
                PacketParticleCompositionS2C removePacket = buildPacket(composition, true);
                if (removePacket != null) {
                    ReiParticlesNetwork.sendTo(player, removePacket);
                }
                continue;
            }
            if (shouldView && !alreadyVisible) {
                if (packetForVisiblePlayers == null) {
                    packetForVisiblePlayers = buildPacket(composition, false);
                }
                if (packetForVisiblePlayers == null) {
                    continue;
                }
                visibleSet.add(compositionId);
                ReiParticlesNetwork.sendTo(player, packetForVisiblePlayers);
                continue;
            }
            if (shouldView && packetForVisiblePlayers != null) {
                ReiParticlesNetwork.sendTo(player, packetForVisiblePlayers);
            }
        }
    }

    private void removeTrackedVisibility(ParticleComposition composition, ServerLevel level) {
        PacketParticleCompositionS2C removePacket = buildPacket(composition, true);
        UUID compositionId = composition.getControlUUID();
        for (Map.Entry<UUID, Set<UUID>> entry : visibleByPlayer.entrySet()) {
            if (!entry.getValue().remove(compositionId) || level == null || removePacket == null) {
                continue;
            }
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player != null) {
                ReiParticlesNetwork.sendTo(player, removePacket);
            }
        }
    }

    private void pruneDisconnectedPlayers() {
        if (serverView.isEmpty()) {
            visibleByPlayer.clear();
            return;
        }
        net.minecraft.server.MinecraftServer server = null;
        for (ParticleComposition composition : serverView.values()) {
            if (composition.getWorld() instanceof ServerLevel level) {
                server = level.getServer();
                break;
            }
        }
        if (server == null) {
            return;
        }
        net.minecraft.server.MinecraftServer runtime = server;
        visibleByPlayer.entrySet().removeIf(entry -> runtime.getPlayerList().getPlayer(entry.getKey()) == null);
    }

    private String resolveTypeId(ParticleComposition composition) {
        if (composition == null) {
            return null;
        }
        String typeId = typeIdsByClass.get(composition.getClass());
        if (typeId == null) {
            warnUnregisteredType(composition.getClass());
        }
        return typeId;
    }

    private void warnUnregisteredType(Class<? extends ParticleComposition> compositionClass) {
        String className = compositionClass.getName();
        if (warnedUnregisteredTypes.add(className)) {
            LOGGER.warn("Particle composition {} is not registered for network sync; skipping client synchronization", className);
        }
    }

    private static boolean canView(ParticleComposition composition, ServerPlayer player) {
        if (composition == null || player == null || composition.getWorld() == null) {
            return false;
        }
        if (player.isRemoved() || player.isSpectator()) {
            return false;
        }
        if (composition.getWorld() != player.level()) {
            return false;
        }
        return composition.getPosition().distanceTo(player.position()) <= Math.max(0.0, composition.getVisibleRange());
    }
}
