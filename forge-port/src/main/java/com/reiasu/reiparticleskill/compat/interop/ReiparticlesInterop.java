package com.reiasu.reiparticleskill.compat.interop;

import com.reiasu.reiparticlesapi.display.DebugDisplayEntity;
import com.reiasu.reiparticlesapi.display.DisplayEntityManager;
import com.reiasu.reiparticlesapi.network.particle.emitters.DebugParticleEmitters;
import com.reiasu.reiparticlesapi.network.particle.emitters.DebugRailgunEmitters;
import com.reiasu.reiparticlesapi.network.particle.emitters.ParticleEmittersManager;
import com.reiasu.reiparticlesapi.test.TestManager;
import com.reiasu.reiparticlesapi.test.api.TestGroup;
import com.reiasu.reiparticlesapi.test.api.TestOption;
import com.reiasu.reiparticleskill.command.layout.DisplaySpawnProfile;
import com.reiasu.reiparticleskill.compat.geom.RelativeLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class ReiparticlesInterop {
    public enum ApiTestState {
        STARTED,
        SKIPPED,
        UNAVAILABLE,
        FAILED
    }

    public record ApiTestResult(ApiTestState state, String detail) {
    }

    private ReiparticlesInterop() {
    }

    public static boolean isApiPresent() {
        return true;
    }

    public static int spawnGroupDisplays(ServerLevel level, double baseX, double baseY, double baseZ, List<RelativeLocation> offsets) {
        int spawned = 0;
        for (RelativeLocation offset : offsets) {
            DebugDisplayEntity display = new DebugDisplayEntity(
                    level,
                    baseX + offset.getX(),
                    baseY + offset.getY(),
                    baseZ + offset.getZ(),
                    "group"
            );
            DisplayEntityManager.INSTANCE.spawn(display, level);
            spawned++;
        }
        return spawned;
    }

    public static int spawnProfileEmitter(ServerLevel level, double x, double y, double z, DisplaySpawnProfile profile) {
        DebugParticleEmitters emitter = new DebugParticleEmitters(
                level, x, y, z, profile.targetScale(), profile.scaledSpeed()
        );
        ParticleEmittersManager.spawnEmitters(emitter, level, x, y, z);
        return 1;
    }

    public static int spawnRailgun(ServerLevel level, Vec3 from, Vec3 target) {
        DebugRailgunEmitters emitter = new DebugRailgunEmitters(level, from, target);
        ParticleEmittersManager.spawnEmitters(emitter);
        return 1;
    }

    public static ApiTestResult triggerApiTest(Object serverPlayer) {
        if (!(serverPlayer instanceof ServerPlayer player)) {
            return new ApiTestResult(ApiTestState.FAILED, "not a ServerPlayer");
        }
        TestGroup currentGroup = TestManager.INSTANCE.getTestFromServer(player);
        if (currentGroup == null) {
            TestGroup started = TestManager.INSTANCE.startTest("api-test-group-builder", player);
            if (started == null) {
                started = TestManager.INSTANCE.startTest("skill-test-builder", player);
            }
            if (started != null) {
                return new ApiTestResult(ApiTestState.STARTED, "started");
            }
            return new ApiTestResult(ApiTestState.UNAVAILABLE, "test builder missing");
        }

        TestOption option = currentGroup.skipCurrent();
        if (option == null) {
            return new ApiTestResult(ApiTestState.SKIPPED, "skipped current option: <none>");
        }
        return new ApiTestResult(ApiTestState.SKIPPED, "skipped current option: " + option.optionID());
    }
}
