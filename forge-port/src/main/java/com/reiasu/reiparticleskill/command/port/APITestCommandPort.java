package com.reiasu.reiparticleskill.command.port;

import com.reiasu.reiparticleskill.compat.interop.ReiparticlesInterop;
import com.reiasu.reiparticleskill.compat.version.CommandSourceVersionBridge;
import com.reiasu.reiparticleskill.compat.version.VersionBridgeRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public final class APITestCommandPort {
    private APITestCommandPort() {
    }

    private static final CommandSourceVersionBridge BRIDGE = VersionBridgeRegistry.commandSource();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("apitest")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("index", IntegerArgumentType.integer())
                                .executes(ctx -> run(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "index"))))
        );
    }

    private static int run(CommandSourceStack source, int index) {
        ServerPlayer player = BRIDGE.playerOrNull(source);
        if (player == null) {
            BRIDGE.sendFailure(source, "apitest must be run by a player");
            return 0;
        }
        if (!ReiparticlesInterop.isApiPresent()) {
            BRIDGE.sendFailure(source, "reiparticlesapi not present");
            return 0;
        }

        ReiparticlesInterop.ApiTestResult result = ReiparticlesInterop.triggerApiTest(player);
        final String detail = result.detail();
        final var state = result.state();
        if (state == ReiparticlesInterop.ApiTestState.FAILED || state == ReiparticlesInterop.ApiTestState.UNAVAILABLE) {
            BRIDGE.sendFailure(source, "apitest index=" + index + " " + detail);
            return 0;
        }

        BRIDGE.sendSuccess(source, "apitest index=" + index + " " + detail);
        return 1;
    }
}
