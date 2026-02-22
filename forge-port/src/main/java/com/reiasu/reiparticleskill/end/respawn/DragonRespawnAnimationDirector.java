package com.reiasu.reiparticleskill.end.respawn;

import com.reiasu.reiparticleskill.end.respawn.runtime.emitter.CollectEnderPowerEmitter;
import com.reiasu.reiparticleskill.end.respawn.runtime.emitter.CollectPillarsEmitter;
import com.reiasu.reiparticleskill.end.respawn.runtime.emitter.EndBeamExplosionEmitter;
import com.reiasu.reiparticleskill.end.respawn.runtime.emitter.EndCrystalEmitter;
import com.reiasu.reiparticleskill.end.respawn.runtime.emitter.EndCrystalStyleEmitter;
import com.reiasu.reiparticleskill.end.respawn.runtime.emitter.RespawnEmitter;
import com.reiasu.reiparticleskill.end.respawn.runtime.style.EndDustStyle;
import com.reiasu.reiparticleskill.end.respawn.runtime.style.EnderRespawnCenterStyle;
import com.reiasu.reiparticleskill.end.respawn.runtime.style.EnderRespawnWaveCloudStyle;
import com.reiasu.reiparticleskill.end.respawn.runtime.style.EnderRespawnWaveEnchantStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class DragonRespawnAnimationDirector {
    private static final int PILLAR_EVENT_DELAY_TICKS = 39;

    private final EnderRespawnCenterStyle centerStyle = new EnderRespawnCenterStyle();
    private final EndDustStyle dustStyle = new EndDustStyle();
    private final EnderRespawnWaveCloudStyle cloudStyle = new EnderRespawnWaveCloudStyle();
    private final EnderRespawnWaveEnchantStyle enchantStyle = new EnderRespawnWaveEnchantStyle();
    private final List<RespawnEmitter> activeEmitters = new ArrayList<>();
    private final Set<RespawnEmitter> pulseScopedEmitters = Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<PillarCrafter> activePillarCrafters = new ArrayList<>();
    private final RandomSource random = RandomSource.create();
    private final Map<UUID, BlockPos> crystalBeamTargets = new HashMap<>();
    private final Map<BlockPos, PendingPillarPulse> pendingPillarPulseTicks = new HashMap<>();
    private final Map<UUID, Long> dragonGravityRestoreTicks = new HashMap<>();

    private EndRespawnPhase activePhase;
    private ServerLevel activeLevel;
    private boolean active;
    private int pillarPulseIndex;
    private long lastPillarPulseTick = Long.MIN_VALUE;

    public void setup(ServerLevel level, Vec3 center) {
        if (active) {
            return;
        }
        active = true;
        activeLevel = level;
        activePhase = null;
        activeEmitters.clear();
        pulseScopedEmitters.clear();
        activePillarCrafters.clear();
        pillarPulseIndex = 0;
        crystalBeamTargets.clear();
        pendingPillarPulseTicks.clear();
        dragonGravityRestoreTicks.clear();
        lastPillarPulseTick = Long.MIN_VALUE;
    }

    public int next(ServerLevel level, Vec3 center, EndRespawnPhase phase, long phaseTick) {
        if (!active) {
            setup(level, center);
        }
        activeLevel = level;
        if (activePhase != phase) {
            if (!shouldPreserveMidChain(activePhase, phase)) {
                reconfigureEmittersForPhase(level, center, phase);
            }
            activePhase = phase;
            if (phase != EndRespawnPhase.SUMMON_PILLARS) {
                crystalBeamTargets.clear();
                pendingPillarPulseTicks.clear();
                lastPillarPulseTick = Long.MIN_VALUE;
            }
            if (phase == EndRespawnPhase.BEFORE_END_WAITING) {
                activeEmitters.removeIf(pulseScopedEmitters::contains);
                pulseScopedEmitters.clear();
            }
            if (phase == EndRespawnPhase.BEFORE_END_WAITING
                    || phase == EndRespawnPhase.END
                    || phase == EndRespawnPhase.START) {
                activePillarCrafters.clear();
            }
        }

        if (phase == EndRespawnPhase.SUMMON_PILLARS) {
            maybeTriggerPillarPulse(level, center, phaseTick);
        }
        if (phase == EndRespawnPhase.END && phaseTick == 0) {
            handleEnd(level, center);
        }
        tickDragonGravityRestore(level);
        int emitted = emitPhaseStyles(level, center, phase, phaseTick);
        emitted += tickEmitters(level, center);
        return emitted;
    }

    public void tick(ServerLevel level, Vec3 center) {
        if (!active) {
            return;
        }
        tickDragonGravityRestore(level);
        tickEmitters(level, center);
    }

    public void cancel() {
        if (activeLevel != null) {
            restoreTrackedDragons(activeLevel);
        }
        active = false;
        activeLevel = null;
        activePhase = null;
        activeEmitters.clear();
        pulseScopedEmitters.clear();
        activePillarCrafters.clear();
        pillarPulseIndex = 0;
        crystalBeamTargets.clear();
        pendingPillarPulseTicks.clear();
        dragonGravityRestoreTicks.clear();
        lastPillarPulseTick = Long.MIN_VALUE;
    }

    public String debugState() {
        String phaseId = activePhase == null ? "none" : activePhase.id();
        return "active=" + active
                + ", phase=" + phaseId
                + ", emitters=" + activeEmitters.size()
                + ", pulse_emitters=" + pulseScopedEmitters.size()
                + ", pillar_crafters=" + activePillarCrafters.size()
                + ", pending_pulses=" + pendingPillarPulseTicks.size()
                + ", no_gravity_dragons=" + dragonGravityRestoreTicks.size();
    }

    public void handleOncePillars(ServerLevel level, Vec3 center, Vec3 pillarCenter) {
        handleOncePillars(level, center, pillarCenter, null);
    }

    private void handleOncePillars(ServerLevel level, Vec3 center, Vec3 pillarCenter, UUID preferredCrystalId) {
        Vec3 anchor = pillarCenter == null ? center : pillarCenter;
        EndCrystal crystal = resolvePulseCrystal(level, center, anchor, preferredCrystalId);
        Vec3 start = anchor;
        Vec3 target = Vec3.ZERO;
        if (crystal != null) {
            BlockPos beamTarget = crystal.getBeamTarget();
            if (beamTarget != null) {
                start = Vec3.atCenterOf(beamTarget);
            }
            target = crystal.position().add(0.0, 1.7, 0.0);
        }

        EndCrystalStyleEmitter styleEmitter = new EndCrystalStyleEmitter(start, target, 500)
                .setRotateSpeed(0.02454369260617026);
        activeEmitters.add(styleEmitter);
        pulseScopedEmitters.add(styleEmitter);

        activePillarCrafters.add(new PillarCrafter(start, target, crystal == null ? null : crystal.getUUID()));

        CollectPillarsEmitter burst = new CollectPillarsEmitter(500)
                .setAnchorOffset(anchor.subtract(center).add(0.0, -0.5, 0.0))
                .setDiscrete(2.0)
                .setRadiusMin(3.0)
                .setRadiusMax(8.0)
                .setCountMin(70)
                .setCountMax(90)
                .setVerticalMaxSpeedMultiplier(3.0)
                .setVerticalMinSpeedMultiplier(0.1)
                .setHorizontalMaxSpeedMultiplier(1.0)
                .setHorizontalMinSpeedMultiplier(0.08)
                .setParticleMinAge(20)
                .setParticleMaxAge(40)
                .setSizeMin(0.2f)
                .setSizeMax(0.5f)
                .setSpeed(1.5);
        activeEmitters.add(burst);
        pulseScopedEmitters.add(burst);
        pillarPulseIndex++;
    }

    public void handleEnd(ServerLevel level, Vec3 center) {
        activeEmitters.removeIf(emitter -> emitter instanceof EndBeamExplosionEmitter);
        EndBeamExplosionEmitter emitter = new EndBeamExplosionEmitter(160)
                .setAnchorOffset(new Vec3(0.0, 130.5, 0.0))
                .setParticleMaxAge(130)
                .setParticleMinAge(50)
                .setCountMin(2048)
                .setCountMax(3072)
                .setDiscrete(0.5)
                .setMinSpeed(0.8)
                .setMaxSpeed(6.0)
                .setSizeMin(0.4)
                .setSizeMax(0.9)
                .setDrag(0.96);
        activeEmitters.add(emitter);
        setNearestDragonNoGravity(level, center.add(0.0, 130.5, 0.0), 32.0, 20L);
    }

    private void reconfigureEmittersForPhase(ServerLevel level, Vec3 center, EndRespawnPhase phase) {
        activeEmitters.clear();
        pulseScopedEmitters.clear();
        activePillarCrafters.clear();
        crystalBeamTargets.clear();
        pendingPillarPulseTicks.clear();
        switch (phase) {
            case START -> {
                CollectEnderPowerEmitter emitter = new CollectEnderPowerEmitter(600)
                        .setR(60.0)
                        .setRadiusOffset(40.0)
                        .setCountMin(400)
                        .setCountMax(800)
                        .setSpeed(1.9)
                        .setOriginOffset(new Vec3(0.0, 4.0, 0.0))
                        .setTargetOffset(new Vec3(0.0, 4.0, 0.0));
                activeEmitters.add(emitter);
            }
            case SUMMON_PILLARS, SUMMONING_DRAGON, BEFORE_END_WAITING -> {
                CollectPillarsEmitter emitter = new CollectPillarsEmitter(500)
                        .setAnchorOffset(new Vec3(0.0, -1.0, 0.0))
                        .setDiscrete(2.0)
                        .setRadiusMin(16.0)
                        .setRadiusMax(18.0)
                        .setCountMin(120)
                        .setCountMax(200)
                        .setVerticalMaxSpeedMultiplier(4.5)
                        .setVerticalMinSpeedMultiplier(0.1)
                        .setHorizontalMaxSpeedMultiplier(1.5)
                        .setHorizontalMinSpeedMultiplier(0.04)
                        .setParticleMinAge(40)
                        .setParticleMaxAge(60)
                        .setSizeMin(0.2f)
                        .setSizeMax(0.7f)
                        .setSpeed(1.5);
                activeEmitters.add(emitter);
            }
            case END -> handleEnd(level, center);
        }
    }

    private void maybeTriggerPillarPulse(ServerLevel level, Vec3 center, long phaseTick) {
        List<EndCrystal> crystals = level.getEntitiesOfClass(
                EndCrystal.class,
                new AABB(center, center).inflate(96.0),
                EndCrystal::isAlive
        );
        Set<UUID> seen = new HashSet<>();
        for (EndCrystal crystal : crystals) {
            UUID uuid = crystal.getUUID();
            seen.add(uuid);
            BlockPos beam = crystal.getBeamTarget();
            if (beam == null) {
                continue;
            }
            BlockPos prev = crystalBeamTargets.put(uuid, beam.immutable());
            if (prev == null || !prev.equals(beam)) {
                pendingPillarPulseTicks.put(beam.immutable(), new PendingPillarPulse(phaseTick + PILLAR_EVENT_DELAY_TICKS, uuid));
            }
        }
        crystalBeamTargets.keySet().removeIf(uuid -> !seen.contains(uuid));

        boolean fired = false;
        Iterator<Map.Entry<BlockPos, PendingPillarPulse>> pendingIterator = pendingPillarPulseTicks.entrySet().iterator();
        while (pendingIterator.hasNext()) {
            Map.Entry<BlockPos, PendingPillarPulse> pending = pendingIterator.next();
            PendingPillarPulse pulse = pending.getValue();
            if (pulse.triggerTick() > phaseTick) {
                continue;
            }
            handleOncePillars(level, center, Vec3.atCenterOf(pending.getKey()), pulse.crystalId());
            pendingIterator.remove();
            fired = true;
        }
        if (fired) {
            lastPillarPulseTick = phaseTick;
            return;
        }

        // Prefer strict "beam-target changed" triggering. Fallback is only used once
        // when no beam targets are visible yet to avoid missing the first pulse.
        if (!pendingPillarPulseTicks.isEmpty()) {
            return;
        }
        if (!crystalBeamTargets.isEmpty()) {
            return;
        }
        if (lastPillarPulseTick != Long.MIN_VALUE) {
            return;
        }
        Vec3 fallback = chooseFallbackPillarTarget(crystals, center);
        handleOncePillars(level, center, fallback);
        lastPillarPulseTick = phaseTick;
    }

    private Vec3 chooseFallbackPillarTarget(List<EndCrystal> crystals, Vec3 center) {
        if (!crystals.isEmpty()) {
            EndCrystal crystal = crystals.get(Math.floorMod(pillarPulseIndex, crystals.size()));
            BlockPos beam = crystal.getBeamTarget();
            if (beam != null) {
                return Vec3.atCenterOf(beam);
            }
            return crystal.position();
        }
        double angle = (Math.PI * 2.0 * (pillarPulseIndex % 10)) / 10.0;
        return center.add(Math.cos(angle) * 56.0, 80.0, Math.sin(angle) * 56.0);
    }

    private EndCrystal resolvePulseCrystal(ServerLevel level, Vec3 center, Vec3 anchor, UUID preferredCrystalId) {
        if (preferredCrystalId != null) {
            net.minecraft.world.entity.Entity entity = level.getEntity(preferredCrystalId);
            if (entity instanceof EndCrystal preferred && preferred.isAlive()) {
                return preferred;
            }
        }

        List<EndCrystal> nearAnchor = level.getEntitiesOfClass(
                EndCrystal.class,
                new AABB(anchor, anchor).inflate(8.0),
                EndCrystal::isAlive
        );
        EndCrystal closestNearAnchor = nearestCrystalTo(anchor, nearAnchor);
        if (closestNearAnchor != null) {
            return closestNearAnchor;
        }

        List<EndCrystal> aroundPortal = level.getEntitiesOfClass(
                EndCrystal.class,
                new AABB(center, center).inflate(96.0),
                EndCrystal::isAlive
        );
        return nearestCrystalTo(anchor, aroundPortal);
    }

    private EndCrystal nearestCrystalTo(Vec3 pos, List<EndCrystal> crystals) {
        EndCrystal best = null;
        double bestDistance = Double.MAX_VALUE;
        for (EndCrystal candidate : crystals) {
            double d = candidate.position().distanceToSqr(pos);
            if (d < bestDistance) {
                bestDistance = d;
                best = candidate;
            }
        }
        return best;
    }

    private void setNearestDragonNoGravity(ServerLevel level, Vec3 origin, double radius, long restoreAfterTicks) {
        List<EnderDragon> dragons = level.getEntitiesOfClass(
                EnderDragon.class,
                new AABB(origin, origin).inflate(radius),
                EnderDragon::isAlive
        );
        EnderDragon nearest = null;
        double bestDistance = Double.MAX_VALUE;
        for (EnderDragon dragon : dragons) {
            double d = dragon.position().distanceToSqr(origin);
            if (d < bestDistance) {
                bestDistance = d;
                nearest = dragon;
            }
        }
        if (nearest == null) {
            return;
        }
        nearest.setNoGravity(true);
        dragonGravityRestoreTicks.put(nearest.getUUID(), level.getGameTime() + Math.max(1L, restoreAfterTicks));
    }

    private void tickDragonGravityRestore(ServerLevel level) {
        if (dragonGravityRestoreTicks.isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        Iterator<Map.Entry<UUID, Long>> iterator = dragonGravityRestoreTicks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            if (now < entry.getValue()) {
                continue;
            }
            net.minecraft.world.entity.Entity entity = level.getEntity(entry.getKey());
            if (entity instanceof EnderDragon dragon && dragon.isAlive()) {
                dragon.setNoGravity(false);
            }
            iterator.remove();
        }
    }

    private void restoreTrackedDragons(ServerLevel level) {
        if (dragonGravityRestoreTicks.isEmpty()) {
            return;
        }
        for (UUID uuid : dragonGravityRestoreTicks.keySet()) {
            net.minecraft.world.entity.Entity entity = level.getEntity(uuid);
            if (entity instanceof EnderDragon dragon && dragon.isAlive()) {
                dragon.setNoGravity(false);
            }
        }
    }

    private int emitPhaseStyles(ServerLevel level, Vec3 center, EndRespawnPhase phase, long phaseTick) {
        return switch (phase) {
            case START -> emitStartAmbient(level, center, phaseTick);
            case END -> 0;
            case SUMMON_PILLARS, SUMMONING_DRAGON, BEFORE_END_WAITING -> emitSummonStack(level, center, phaseTick);
        };
    }

    private int emitStartAmbient(ServerLevel level, Vec3 center, long tick) {
        int emitted = 0;

        // Center geometric pattern (same as summon phase)
        emitted += centerStyle.emit(level, center, tick, 0.0);

        // Dense ambient dust
        EndDustStyle.Params dust = new EndDustStyle.Params();
        dust.count = 200;
        dust.maxRadius = 120.0;
        dust.rotateSpeed = 0.006135923151542565;
        dust.sizeMax = 1.2;
        dust.sizeMin = 0.3;
        emitted += dustStyle.emit(level, center, tick, dust);

        // Cloud ring A at y=20
        EnderRespawnWaveCloudStyle.Params cloudA = new EnderRespawnWaveCloudStyle.Params();
        cloudA.radius = 28.0;
        cloudA.countMin = 60;
        cloudA.countMax = 90;
        cloudA.discrete = 3.0;
        cloudA.rotateSpeed = Math.PI / 180.0;
        cloudA.faceCamera = false;
        cloudA.particleRandomAngle = true;
        cloudA.minSize = 1.0;
        cloudA.maxSize = 2.5;
        cloudA.yOffset = 20.0;
        emitted += cloudStyle.emit(level, center, tick, cloudA);

        // Cloud ring B at y=40
        EnderRespawnWaveCloudStyle.Params cloudB = new EnderRespawnWaveCloudStyle.Params();
        cloudB.radius = 20.0;
        cloudB.countMin = 40;
        cloudB.countMax = 60;
        cloudB.discrete = 2.0;
        cloudB.rotateSpeed = -Math.PI / 180.0;
        cloudB.faceCamera = false;
        cloudB.particleRandomAngle = true;
        cloudB.minSize = 1.2;
        cloudB.maxSize = 3.0;
        cloudB.yOffset = 40.0;
        emitted += cloudStyle.emit(level, center, tick, cloudB);

        // Enchant ring at y=55
        EnderRespawnWaveEnchantStyle.Params enchant = new EnderRespawnWaveEnchantStyle.Params();
        enchant.radius = 50.0;
        enchant.countMin = 80;
        enchant.countMax = 120;
        enchant.discrete = 8.0;
        enchant.rotateSpeed = -0.01227184630308513;
        enchant.faceCamera = false;
        enchant.minSize = 1.5;
        enchant.maxSize = 3.5;
        enchant.yOffset = 55.0;
        emitted += enchantStyle.emit(level, center, tick, enchant);
        return emitted;
    }

    private int emitSummonStack(ServerLevel level, Vec3 center, long tick) {
        int emitted = 0;
        // Center style: ~600 packets
        emitted += centerStyle.emit(level, center, tick, 0.0);

        // Dust layer A: ambient fill — original 512, reduced heavily
        EndDustStyle.Params dustA = new EndDustStyle.Params();
        dustA.count = 80;
        dustA.maxRadius = 156.0;
        dustA.rotateSpeed = 0.006135923151542565;
        dustA.sizeMax = 0.6;
        dustA.sizeMin = 0.15;
        emitted += dustStyle.emit(level, center, tick, dustA);

        // Dust layer B: inner fill — original 256
        EndDustStyle.Params dustB = new EndDustStyle.Params();
        dustB.count = 50;
        dustB.maxRadius = 128.0;
        dustB.rotateSpeed = 0.0030679615757712823;
        dustB.sizeMax = 0.7;
        dustB.sizeMin = 0.25;
        emitted += dustStyle.emit(level, center, tick, dustB);

        // Cloud ring A (y=30) — original 270-360
        EnderRespawnWaveCloudStyle.Params cloudA = new EnderRespawnWaveCloudStyle.Params();
        cloudA.radius = 32.0;
        cloudA.countMin = 60;
        cloudA.countMax = 90;
        cloudA.discrete = 3.0;
        cloudA.rotateSpeed = Math.PI / 180.0;
        cloudA.faceCamera = false;
        cloudA.particleRandomAngle = true;
        cloudA.yOffset = 30.0;
        emitted += cloudStyle.emit(level, center, tick, cloudA);

        // Cloud ring B (y=50) — original 180-240
        EnderRespawnWaveCloudStyle.Params cloudB = new EnderRespawnWaveCloudStyle.Params();
        cloudB.radius = 16.0;
        cloudB.countMin = 40;
        cloudB.countMax = 60;
        cloudB.discrete = 2.0;
        cloudB.rotateSpeed = -0.01227184630308513;
        cloudB.faceCamera = false;
        cloudB.particleRandomAngle = true;
        cloudB.yOffset = 50.0;
        emitted += cloudStyle.emit(level, center, tick, cloudB);

        // Cloud ring C (y=16) — original 180-240
        EnderRespawnWaveCloudStyle.Params cloudC = new EnderRespawnWaveCloudStyle.Params();
        cloudC.radius = 24.0;
        cloudC.countMin = 40;
        cloudC.countMax = 60;
        cloudC.discrete = 2.0;
        cloudC.rotateSpeed = -Math.PI / 180.0;
        cloudC.faceCamera = false;
        cloudC.particleRandomAngle = true;
        cloudC.yOffset = 16.0;
        emitted += cloudStyle.emit(level, center, tick, cloudC);

        // Enchant ring (y=65, large radius) — original 1400-1800
        EnderRespawnWaveEnchantStyle.Params enchant = new EnderRespawnWaveEnchantStyle.Params();
        enchant.radius = 108.0;
        enchant.rotateSpeed = 0.006135923151542565;
        enchant.countMin = 200;
        enchant.countMax = 300;
        enchant.discrete = 18.0;
        enchant.faceCamera = false;
        enchant.minSize = 1.5;
        enchant.maxSize = 3.5;
        enchant.particleRollAngleSpeed = (float) (Math.PI / 180.0);
        enchant.particleRandomAngle = true;
        enchant.particleYawAngleSpeed = -0.024543693f;
        enchant.yOffset = 65.0;
        emitted += enchantStyle.emit(level, center, tick, enchant);

        // Cloud ring D (y=120, top cap) — original 500-600
        EnderRespawnWaveCloudStyle.Params cloudD = new EnderRespawnWaveCloudStyle.Params();
        cloudD.radius = 48.0;
        cloudD.rotateSpeed = 0.006135923151542565;
        cloudD.countMin = 80;
        cloudD.countMax = 120;
        cloudD.discrete = 8.0;
        cloudD.faceCamera = false;
        cloudD.minSize = 1.5;
        cloudD.maxSize = 3.5;
        cloudD.particleRollAngleSpeed = (float) (Math.PI / 180.0);
        cloudD.particleRandomAngle = true;
        cloudD.particleYawAngleSpeed = -0.024543693f;
        cloudD.yOffset = 120.0;
        emitted += cloudStyle.emit(level, center, tick, cloudD);
        return emitted;
    }

    private int tickEmitters(ServerLevel level, Vec3 center) {
        int emitted = 0;
        emitted += tickPillarCrafters(level);
        Iterator<RespawnEmitter> iterator = activeEmitters.iterator();
        while (iterator.hasNext()) {
            RespawnEmitter emitter = iterator.next();
            emitted += emitter.tick(level, center);
            if (emitter.done()) {
                pulseScopedEmitters.remove(emitter);
                iterator.remove();
            }
        }
        return emitted;
    }

    private int tickPillarCrafters(ServerLevel level) {
        if (activePillarCrafters.isEmpty()) {
            return 0;
        }
        List<RespawnEmitter> crafted = new ArrayList<>();
        Iterator<PillarCrafter> iterator = activePillarCrafters.iterator();
        while (iterator.hasNext()) {
            PillarCrafter crafter = iterator.next();
            if (crafter.shouldCancel(level, activePhase)) {
                iterator.remove();
                continue;
            }
            if (crafter.shouldCraftNow()) {
                Vec3 target = crafter.resolveTarget(level);
                EndCrystalEmitter emitter = new EndCrystalEmitter(crafter.start(), target, 60)
                        .setRotationSpeed(0.09817477042468103)
                        .setMovementSpeed(randomBetween(1.5, 2.0))
                        .setCurrentRotation(randomBetween(-Math.PI, Math.PI))
                        .setCountMin(3)
                        .setCountMax(5)
                        .setMaxRadius(4.0)
                        .setParticleMinAge(10)
                        .setParticleMaxAge(20)
                        .setRefiner(6.0);
                crafted.add(emitter);
                pulseScopedEmitters.add(emitter);
            }
            crafter.advanceTick();
            if (crafter.expired()) {
                iterator.remove();
            }
        }
        activeEmitters.addAll(crafted);
        return 0;
    }

    private boolean shouldPreserveMidChain(EndRespawnPhase from, EndRespawnPhase to) {
        return isMidPhase(from) && isMidPhase(to);
    }

    private boolean isMidPhase(EndRespawnPhase phase) {
        return phase == EndRespawnPhase.SUMMON_PILLARS
                || phase == EndRespawnPhase.SUMMONING_DRAGON
                || phase == EndRespawnPhase.BEFORE_END_WAITING;
    }

    private double randomBetween(double min, double max) {
        double lo = Math.min(min, max);
        double hi = Math.max(min, max);
        if (Math.abs(hi - lo) < 1.0E-6) {
            return lo;
        }
        return lo + random.nextDouble() * (hi - lo);
    }

    private record PendingPillarPulse(long triggerTick, UUID crystalId) {
    }

    private static final class PillarCrafter {
        private static final int CRAFT_INTERVAL = 15;
        private static final int MAX_TICKS = 500;

        private final Vec3 start;
        private Vec3 target;
        private final UUID crystalId;
        private int tick;

        private PillarCrafter(Vec3 start, Vec3 target, UUID crystalId) {
            this.start = start;
            this.target = target;
            this.crystalId = crystalId;
        }

        private Vec3 start() {
            return start;
        }

        private boolean shouldCraftNow() {
            return tick % CRAFT_INTERVAL == 0;
        }

        private void advanceTick() {
            tick++;
        }

        private boolean expired() {
            return tick > MAX_TICKS;
        }

        private boolean shouldCancel(ServerLevel level, EndRespawnPhase phase) {
            if (phase == EndRespawnPhase.BEFORE_END_WAITING || phase == EndRespawnPhase.END) {
                return true;
            }
            if (crystalId == null) {
                return true;
            }
            net.minecraft.world.entity.Entity entity = level.getEntity(crystalId);
            if (!(entity instanceof EndCrystal crystal) || !crystal.isAlive()) {
                return true;
            }
            return crystal.getBeamTarget() == null;
        }

        private Vec3 resolveTarget(ServerLevel level) {
            if (crystalId == null) {
                return target;
            }
            net.minecraft.world.entity.Entity entity = level.getEntity(crystalId);
            if (entity instanceof EndCrystal crystal && crystal.isAlive()) {
                target = crystal.position().add(0.0, 1.7, 0.0);
            }
            return target;
        }
    }
}
