package com.reiasu.reiparticlesapi.network.particle.emitters;

import com.reiasu.reiparticlesapi.event.ReiEventBus;
import com.reiasu.reiparticlesapi.event.events.particle.emitter.EmitterRemoveEvent;
import com.reiasu.reiparticlesapi.event.events.particle.emitter.EmitterSpawnEvent;
import com.reiasu.reiparticlesapi.network.ReiParticlesNetwork;
import com.reiasu.reiparticlesapi.network.packet.PacketParticleEmittersS2C;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class ParticleEmittersManager {
    private static final List<ParticleEmitters> EMITTERS = new ArrayList<>();
    private static final Map<String, Function<FriendlyByteBuf, ParticleEmitters>> EMITTER_DECODERS = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<UUID>> VISIBLE = new ConcurrentHashMap<>();
    private static final Map<UUID, ParticleEmitters> CLIENT_EMITTERS = new ConcurrentHashMap<>();
    private static boolean builtinsRegistered;

    private ParticleEmittersManager() {
    }

    public static void registerBuiltinCodecs() {
        if (builtinsRegistered) {
            return;
        }
        builtinsRegistered = true;
        registerCodec(DebugParticleEmitters.CODEC_ID, DebugParticleEmitters::decode);
        registerCodec(DebugRailgunEmitters.CODEC_ID, DebugRailgunEmitters::decode);
    }

    public static void registerCodec(String id, Function<FriendlyByteBuf, ParticleEmitters> decoder) {
        if (id == null || id.isBlank() || decoder == null) {
            return;
        }
        EMITTER_DECODERS.put(id, decoder);
    }

    public static Function<FriendlyByteBuf, ParticleEmitters> getCodecFromID(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return EMITTER_DECODERS.get(id);
    }

    public static void spawnEmitters(Object emitter) {
        spawnEmitters(emitter, null, 0.0, 0.0, 0.0);
    }

    public static void spawnEmitters(Object emitter, ServerLevel level, double x, double y, double z) {
        if (!(emitter instanceof ParticleEmitters particleEmitters)) {
            return;
        }
        if (level != null) {
            particleEmitters.bind(level, x, y, z);
        }
        synchronized (EMITTERS) {
            EMITTERS.add(particleEmitters);
        }
        ReiEventBus.call(new EmitterSpawnEvent(particleEmitters, false));
    }

    public static void createOrChangeClient(ParticleEmitters emitters, Level viewWorld) {
        if (emitters == null || viewWorld == null) {
            return;
        }
        ParticleEmitters old = CLIENT_EMITTERS.get(emitters.getUuid());
        if (old == null) {
            Vec3 pos = emitters.position();
            emitters.bind(viewWorld, pos.x, pos.y, pos.z);
            CLIENT_EMITTERS.put(emitters.getUuid(), emitters);
            ReiEventBus.call(new EmitterSpawnEvent(emitters, true));
            return;
        }
        old.update(emitters);
        Vec3 pos = emitters.position();
        old.bind(viewWorld, pos.x, pos.y, pos.z);
        if (emitters.getCanceled()) {
            old.cancel();
        }
    }

    public static void tickAll() {
        synchronized (EMITTERS) {
            EMITTERS.removeIf(emitters -> {
                updateClientVisible(emitters);
                emitters.tick();
                if (!emitters.getCanceled()) {
                    return false;
                }
                removeAllViews(emitters);
                ReiEventBus.call(new EmitterRemoveEvent(emitters, false));
                return true;
            });
        }
        pruneDisconnectedPlayers();
    }

    private static void updateClientVisible(ParticleEmitters emitters) {
        if (!(emitters.level() instanceof ServerLevel level)) {
            return;
        }

        for (ServerPlayer player : level.players()) {
            Set<UUID> visibleSet = VISIBLE.computeIfAbsent(player.getUUID(), ignored -> ConcurrentHashMap.newKeySet());
            boolean shouldView = canViewEmitter(emitters, player);
            boolean isViewing = visibleSet.contains(emitters.getUuid());

            if (shouldView && !isViewing) {
                addView(player, emitters);
                continue;
            }

            if (!shouldView && isViewing) {
                removeView(player, emitters);
                continue;
            }

            if (shouldView) {
                sendChange(emitters, player);
            }
        }
    }

    private static boolean canViewEmitter(ParticleEmitters emitters, ServerPlayer player) {
        if (emitters.level() == null || player == null) {
            return false;
        }
        if (player.isRemoved() || player.isSpectator()) {
            return false;
        }
        return emitters.level() == player.level();
    }

    private static void sendChange(ParticleEmitters emitters, ServerPlayer player) {
        PacketParticleEmittersS2C packet = new PacketParticleEmittersS2C(
                emitters.getEmittersID(),
                emitters.encodeToBytes(),
                PacketParticleEmittersS2C.PacketType.CHANGE_OR_CREATE
        );
        ReiParticlesNetwork.sendTo(player, packet);
    }

    private static void addView(ServerPlayer player, ParticleEmitters emitters) {
        Set<UUID> visibleSet = VISIBLE.computeIfAbsent(player.getUUID(), ignored -> ConcurrentHashMap.newKeySet());
        if (!visibleSet.add(emitters.getUuid())) {
            return;
        }
        sendChange(emitters, player);
    }

    private static void removeView(ServerPlayer player, ParticleEmitters emitters) {
        Set<UUID> visibleSet = VISIBLE.computeIfAbsent(player.getUUID(), ignored -> ConcurrentHashMap.newKeySet());
        visibleSet.remove(emitters.getUuid());
        PacketParticleEmittersS2C packet = new PacketParticleEmittersS2C(
                emitters.getEmittersID(),
                emitters.encodeToBytes(),
                PacketParticleEmittersS2C.PacketType.REMOVE
        );
        ReiParticlesNetwork.sendTo(player, packet);
    }

    private static void removeAllViews(ParticleEmitters emitters) {
        ServerLevel level = emitters.level() instanceof ServerLevel sl ? sl : null;
        for (Map.Entry<UUID, Set<UUID>> entry : VISIBLE.entrySet()) {
            UUID playerId = entry.getKey();
            Set<UUID> visibleSet = entry.getValue();
            if (!visibleSet.remove(emitters.getUuid())) {
                continue;
            }
            if (level == null) {
                continue;
            }
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
            if (player != null) {
                PacketParticleEmittersS2C packet = new PacketParticleEmittersS2C(
                        emitters.getEmittersID(),
                        emitters.encodeToBytes(),
                        PacketParticleEmittersS2C.PacketType.REMOVE
                );
                ReiParticlesNetwork.sendTo(player, packet);
            }
        }
    }

    private static void pruneDisconnectedPlayers() {
        if (EMITTERS.isEmpty()) {
            VISIBLE.clear();
            return;
        }
        VISIBLE.entrySet().removeIf(entry -> {
            UUID playerId = entry.getKey();
            for (ParticleEmitters emitter : EMITTERS) {
                if (emitter.level() instanceof ServerLevel level) {
                    if (level.getServer().getPlayerList().getPlayer(playerId) != null) {
                        return false;
                    }
                }
            }
            return true;
        });
    }

    public static void tickClient() {
        CLIENT_EMITTERS.entrySet().removeIf(entry -> {
            ParticleEmitters emitters = entry.getValue();
            emitters.tick();
            if (emitters.getCanceled()) {
                ReiEventBus.call(new EmitterRemoveEvent(emitters, true));
                return true;
            }
            return false;
        });
    }

    public static int activeCount() {
        synchronized (EMITTERS) {
            return EMITTERS.size();
        }
    }

    public static void clear() {
        synchronized (EMITTERS) {
            for (ParticleEmitters emitters : EMITTERS) {
                emitters.cancel();
            }
            EMITTERS.clear();
        }
        VISIBLE.clear();
        for (ParticleEmitters emitters : CLIENT_EMITTERS.values()) {
            emitters.cancel();
        }
        CLIENT_EMITTERS.clear();
    }

    public static List<ParticleEmitters> getEmitters() {
        synchronized (EMITTERS) {
            return Collections.unmodifiableList(new ArrayList<>(EMITTERS));
        }
    }

    public static Map<UUID, ParticleEmitters> getClientEmitters() {
        return CLIENT_EMITTERS;
    }
}
