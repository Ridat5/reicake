package com.reiasu.reiparticleskill.compat.version;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public interface CommandSourceVersionBridge {
    ServerLevel level(CommandSourceStack source);

    Vec3 position(CommandSourceStack source);

    ServerPlayer playerOrNull(CommandSourceStack source);

    void sendSuccess(CommandSourceStack source, String message);

    void sendFailure(CommandSourceStack source, String message);
}
