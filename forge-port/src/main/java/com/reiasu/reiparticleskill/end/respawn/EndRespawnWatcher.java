package com.reiasu.reiparticleskill.end.respawn;

import com.reiasu.reiparticleskill.compat.version.EndRespawnVersionBridge;
import com.reiasu.reiparticleskill.compat.version.VersionBridgeRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class EndRespawnWatcher {
    private static final EndRespawnVersionBridge BRIDGE = VersionBridgeRegistry.endRespawn();
    private static final double PORTAL_RADIUS = 14.0;
    private static final Map<String, SyntheticRespawnTracker> SYNTHETIC_TRACKERS = new HashMap<>();

    private EndRespawnWatcher() {
    }

    public static void tickServer(MinecraftServer server, EndRespawnStateBridge bridge, Logger logger) {
        boolean foundRespawning = false;

        for (ServerLevel level : server.getAllLevels()) {
            if (!Level.END.equals(level.dimension())) {
                continue;
            }

            EndDragonFight fight = level.getDragonFight();
            if (fight == null) {
                continue;
            }

            Vec3 center = BRIDGE.portalCenter(fight);
            String levelId = level.dimension().location().toString();
            SyntheticRespawnTracker tracker = SYNTHETIC_TRACKERS.computeIfAbsent(levelId, ignored -> new SyntheticRespawnTracker());

            int crystalCount = resolveCrystalCount(fight, level, center);
            Optional<EndRespawnPhase> phase = BRIDGE.detectPhase(fight);
            if (phase.isPresent()) {
                tracker.observeDirectPhase(crystalCount);
            } else {
                phase = tracker.update(crystalCount);
            }
            if (phase.isEmpty()) {
                continue;
            }

            foundRespawning = true;
            bridge.setup(level, center);
            bridge.next(level, center, phase.get(), logger);
            break;
        }

        if (!foundRespawning && bridge.isActive()) {
            bridge.cancel(logger);
        }
    }

    public static Optional<RespawnProbe> probeServer(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            if (!Level.END.equals(level.dimension())) {
                continue;
            }

            EndDragonFight fight = level.getDragonFight();
            if (fight == null) {
                continue;
            }

            Vec3 center = BRIDGE.portalCenter(fight);
            int fightCrystals = Math.max(0, fight.getCrystalsAlive());
            int portalAreaCrystals = Math.max(0, countPortalCrystals(level, center));
            int resolvedCrystals = Math.max(fightCrystals, portalAreaCrystals);
            Optional<EndRespawnPhase> directPhase = BRIDGE.detectPhase(fight);
            return Optional.of(new RespawnProbe(
                    level.dimension().location().toString(),
                    center,
                    directPhase.map(EndRespawnPhase::id).orElse("none"),
                    fightCrystals,
                    portalAreaCrystals,
                    resolvedCrystals
            ));
        }
        return Optional.empty();
    }

    private static int countPortalCrystals(ServerLevel level, Vec3 center) {
        AABB box = new AABB(
                center.x - PORTAL_RADIUS,
                center.y - 24.0,
                center.z - PORTAL_RADIUS,
                center.x + PORTAL_RADIUS,
                center.y + 48.0,
                center.z + PORTAL_RADIUS
        );
        return level.getEntitiesOfClass(EndCrystal.class, box, crystal -> crystal != null && crystal.isAlive()).size();
    }

    private static int resolveCrystalCount(EndDragonFight fight, ServerLevel level, Vec3 center) {
        int fromFight = Math.max(0, fight.getCrystalsAlive());
        int fromPortalArea = Math.max(0, countPortalCrystals(level, center));
        return Math.max(fromFight, fromPortalArea);
    }

    static EndRespawnPhase syntheticPhaseForTick(long tick) {
        // Align synthetic fallback with Fabric mixin timing semantics:
        // START ~= 150, SUMMON_PILLARS ~= 500, SUMMONING_DRAGON ~= 100,
        // BEFORE_END_WAITING ~= 30.
        if (tick < 150L) {
            return EndRespawnPhase.START;
        }
        if (tick < 650L) {
            return EndRespawnPhase.SUMMON_PILLARS;
        }
        if (tick < 750L) {
            return EndRespawnPhase.SUMMONING_DRAGON;
        }
        if (tick < 780L) {
            return EndRespawnPhase.BEFORE_END_WAITING;
        }
        return EndRespawnPhase.END;
    }

    public record RespawnProbe(
            String levelId,
            Vec3 center,
            String directPhase,
            int fightCrystals,
            int portalAreaCrystals,
            int resolvedCrystals
    ) {
    }

    private static final class SyntheticRespawnTracker {
        private long tick;
        private int quietTicks;
        private boolean active;

        void observeDirectPhase(int crystalCount) {
            if (!active) {
                active = true;
                tick = 0L;
                quietTicks = 0;
                return;
            }

            tick++;
            if (crystalCount >= 4) {
                quietTicks = 0;
            }
        }

        Optional<EndRespawnPhase> update(int crystalCount) {
            if (crystalCount >= 4) {
                if (!active) {
                    active = true;
                    tick = 0L;
                    quietTicks = 0;
                } else {
                    tick++;
                }
                return Optional.of(syntheticPhaseForTick(tick));
            }

            if (!active) {
                return Optional.empty();
            }

            // Keep a short tail after crystals disappear so END effects can finish.
            quietTicks++;
            tick++;
            if (quietTicks <= 50) {
                return Optional.of(EndRespawnPhase.END);
            }

            reset();
            return Optional.empty();
        }

        void reset() {
            tick = 0L;
            quietTicks = 0;
            active = false;
        }

    }
}
