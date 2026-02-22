package com.reiasu.reiparticleskill.command.port;

import com.reiasu.reiparticlesapi.network.particle.emitters.DebugRailgunEmitters;
import com.reiasu.reiparticlesapi.network.particle.emitters.ParticleEmittersManager;
import com.reiasu.reiparticleskill.compat.version.CommandSourceVersionBridge;
import com.reiasu.reiparticleskill.compat.version.VersionBridgeRegistry;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;

public final class RailgunCommandPort {
    private RailgunCommandPort() {
    }

    private static final CommandSourceVersionBridge BRIDGE = VersionBridgeRegistry.commandSource();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("railgun")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                .executes(ctx -> run(ctx.getSource(), Vec3Argument.getVec3(ctx, "pos"))))
        );
    }

    private static int run(CommandSourceStack source, net.minecraft.world.phys.Vec3 target) {
        var level = BRIDGE.level(source);
        net.minecraft.world.phys.Vec3 from = BRIDGE.position(source);
        int runtimeSpawned = 0;
        try {
            ParticleEmittersManager.spawnEmitters(new DebugRailgunEmitters(level, from, target));
            runtimeSpawned = 1;
        } catch (Throwable ignored) {
            runtimeSpawned = 0;
        }

        int emitted = 0;
        if (runtimeSpawned <= 0) {
            net.minecraft.world.phys.Vec3 diff = target.subtract(from);
            int steps = Math.max(12, (int) (diff.length() * 2.0));
            DustParticleOptions color = new DustParticleOptions(new Vector3f(1.0f, 0.55f, 0.62f), 1.2f);
            for (int i = 0; i <= steps; i++) {
                double t = (double) i / steps;
                double x = from.x + diff.x * t;
                double y = from.y + diff.y * t;
                double z = from.z + diff.z * t;
                level.sendParticles(color, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
                emitted++;
            }
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION, target.x, target.y, target.z, 6, 0.2, 0.2, 0.2, 0.01);
            emitted += 6;
        }

        BRIDGE.sendSuccess(source, "railgun runtime_spawned=" + runtimeSpawned
                + " preview_fallback=" + emitted);
        return 1;
    }
}
