package com.reiasu.reiparticleskill.command;

import com.reiasu.reiparticleskill.command.layout.DisplayIndexKind;
import com.reiasu.reiparticleskill.command.layout.DisplayIndexPlan;
import com.reiasu.reiparticleskill.command.layout.DisplayIndexRouting;
import com.reiasu.reiparticleskill.command.layout.DisplaySpawnProfile;
import com.reiasu.reiparticleskill.barrages.SkillBarrageManager;
import com.reiasu.reiparticleskill.compat.geom.RelativeLocation;
import com.reiasu.reiparticleskill.compat.version.CommandSourceVersionBridge;
import com.reiasu.reiparticleskill.compat.version.VersionBridgeRegistry;
import com.reiasu.reiparticleskill.display.group.layout.FormationLayerSpec;
import com.reiasu.reiparticleskill.display.group.layout.SimpleSwordFormationLayout;
import com.reiasu.reiparticleskill.display.group.layout.SwordFormationLayerPresets;
import com.reiasu.reiparticleskill.end.respawn.EndRespawnPhase;
import com.reiasu.reiparticleskill.end.respawn.EndRespawnSnapshot;
import com.reiasu.reiparticleskill.end.respawn.EndRespawnStateBridge;
import com.reiasu.reiparticleskill.end.respawn.EndRespawnWatcher;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

public final class ReiParticleSkillDebugCommand {
    private ReiParticleSkillDebugCommand() {
    }

    private static final CommandSourceVersionBridge BRIDGE = VersionBridgeRegistry.commandSource();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, EndRespawnStateBridge bridge, Logger logger) {
        dispatcher.register(
                Commands.literal("reiparticleskill")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("display")
                                .then(Commands.argument("index", IntegerArgumentType.integer())
                                        .executes(ctx -> runDisplayIndex(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "index")
                                        ))))
                        .then(Commands.literal("formation")
                                .executes(ctx -> runFormationPreview(ctx.getSource())))
                        .then(Commands.literal("barrage")
                                .then(Commands.literal("status")
                                        .executes(ctx -> runBarrageStatus(ctx.getSource())))
                                .then(Commands.literal("clear")
                                        .executes(ctx -> runBarrageClear(ctx.getSource()))))
                        .then(Commands.literal("respawn")
                                .then(Commands.literal("phase")
                                        .then(Commands.argument("id", StringArgumentType.word())
                                                .executes(ctx -> runRespawnPhase(
                                                        ctx.getSource(),
                                                        bridge,
                                                        logger,
                                                        StringArgumentType.getString(ctx, "id")
                                                ))))
                                .then(Commands.literal("cancel")
                                        .executes(ctx -> runRespawnCancel(ctx.getSource(), bridge, logger)))
                                .then(Commands.literal("status")
                                        .executes(ctx -> runRespawnStatus(ctx.getSource(), bridge))))
        );
    }

    private static int runDisplayIndex(CommandSourceStack source, int index) {
        ServerLevel level = BRIDGE.level(source);
        var pos = BRIDGE.position(source);

        Optional<DisplayIndexPlan> planOpt = DisplayIndexRouting.plan(index);
        if (planOpt.isEmpty()) {
            BRIDGE.sendFailure(source, "index=" + index + " is not supported in migrated debug routing");
            return 0;
        }

        DisplayIndexPlan plan = planOpt.get();

        if (plan.kind() == DisplayIndexKind.GROUP) {
            List<FormationLayerSpec> specs = SwordFormationLayerPresets.defaultLayerSpecs();
            int totalCount = specs.stream().mapToInt(FormationLayerSpec::count).sum();
            int emitted = DisplayDebugRuntime.previewByPlan(level, pos.x, pos.y, pos.z, plan);
            BRIDGE.sendSuccess(source, "index=4 group layout ready, layers=" + specs.size()
                    + ", total points=" + totalCount
                    + ", preview particles=" + emitted);
            return 1;
        }

        Optional<DisplaySpawnProfile> profile = DisplayIndexRouting.spawnProfile(index);
        if (profile.isPresent()) {
            DisplaySpawnProfile p = profile.get();
            int emitted = DisplayDebugRuntime.previewByPlan(level, pos.x, pos.y, pos.z, plan);
            BRIDGE.sendSuccess(source, "index=" + index
                    + " kind=" + plan.kind()
                    + " yaw=" + p.orientation().yawDegrees()
                    + " pitch=" + p.orientation().pitchDegrees()
                    + " scale=" + p.targetScale()
                    + " speed=" + p.scaledSpeed()
                    + " preview particles=" + emitted);
            return 1;
        }

        int emitted = DisplayDebugRuntime.previewByPlan(level, pos.x, pos.y, pos.z, plan);
        BRIDGE.sendSuccess(source, "index=" + index + " kind=" + plan.kind() + " preview particles=" + emitted);
        return 1;
    }

    private static int runFormationPreview(CommandSourceStack source) {
        List<SimpleSwordFormationLayout> layouts =
                SwordFormationLayerPresets.createDefaultLayouts(new RelativeLocation(0.0, 0.0, 1.0));

        int totalPoints = 0;
        for (SimpleSwordFormationLayout layout : layouts) {
            totalPoints += layout.currentOffsets().size();
        }
        final int points = totalPoints;

        var pos = BRIDGE.position(source);
        int emitted = DisplayDebugRuntime.previewFormation(BRIDGE.level(source), pos.x, pos.y, pos.z);

        BRIDGE.sendSuccess(source, "formation preview layouts=" + layouts.size()
                + ", points=" + points
                + ", preview particles=" + emitted);
        return 1;
    }

    private static int runBarrageStatus(CommandSourceStack source) {
        BRIDGE.sendSuccess(source, "barrage active=" + SkillBarrageManager.INSTANCE.activeCount());
        return 1;
    }

    private static int runBarrageClear(CommandSourceStack source) {
        SkillBarrageManager.INSTANCE.clear();
        BRIDGE.sendSuccess(source, "barrage cleared");
        return 1;
    }

    private static int runRespawnPhase(CommandSourceStack source, EndRespawnStateBridge bridge, Logger logger, String id) {
        var phaseOpt = EndRespawnPhase.fromId(id);
        if (phaseOpt.isEmpty()) {
            BRIDGE.sendFailure(source, "unknown phase id=" + id);
            return 0;
        }

        var pos = BRIDGE.position(source);
        bridge.setup(BRIDGE.level(source), pos);
        bridge.next(BRIDGE.level(source), pos, phaseOpt.get(), logger);
        BRIDGE.sendSuccess(source, "respawn phase -> " + phaseOpt.get().id());
        return 1;
    }

    private static int runRespawnCancel(CommandSourceStack source, EndRespawnStateBridge bridge, Logger logger) {
        bridge.cancel(logger);
        BRIDGE.sendSuccess(source, "respawn bridge canceled");
        return 1;
    }

    private static int runRespawnStatus(CommandSourceStack source, EndRespawnStateBridge bridge) {
        EndRespawnSnapshot snapshot = bridge.current();
        String bridgeState;
        if (snapshot == null) {
            bridgeState = "inactive";
        } else {
            bridgeState = "level=" + snapshot.levelId()
                    + " phase=" + snapshot.phase().id()
                    + " phaseTick=" + snapshot.phaseTick()
                    + " center=" + snapshot.center();
        }

        String probeState = EndRespawnWatcher.probeServer(source.getServer())
                .map(probe -> "level=" + probe.levelId()
                        + " direct_phase=" + probe.directPhase()
                        + " crystals(fight/portal/resolved)="
                        + probe.fightCrystals() + "/" + probe.portalAreaCrystals() + "/" + probe.resolvedCrystals()
                        + " center=" + probe.center())
                .orElse("none");

        String directorState = bridge.directorDebugState();
        BRIDGE.sendSuccess(source, "respawn status: bridge={" + bridgeState + "} probe={" + probeState
                + "} director={" + directorState + "}");
        return 1;
    }
}
