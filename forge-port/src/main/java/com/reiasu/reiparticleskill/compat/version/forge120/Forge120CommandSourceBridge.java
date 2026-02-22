package com.reiasu.reiparticleskill.compat.version.forge120;

import com.reiasu.reiparticleskill.compat.version.CommandSourceVersionBridge;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class Forge120CommandSourceBridge implements CommandSourceVersionBridge {
    @Override
    public ServerLevel level(CommandSourceStack source) {
        return source.getLevel();
    }

    @Override
    public Vec3 position(CommandSourceStack source) {
        return source.getPosition();
    }

    @Override
    public ServerPlayer playerOrNull(CommandSourceStack source) {
        return source.getPlayer();
    }

    @Override
    public void sendSuccess(CommandSourceStack source, String message) {
        source.sendSuccess(() -> Component.literal(message), false);
    }

    @Override
    public void sendFailure(CommandSourceStack source, String message) {
        source.sendFailure(Component.literal(message));
    }
}
