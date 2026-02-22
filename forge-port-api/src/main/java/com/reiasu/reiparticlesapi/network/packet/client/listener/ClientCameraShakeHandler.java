package com.reiasu.reiparticlesapi.network.packet.client.listener;

import com.reiasu.reiparticlesapi.client.CameraShakeClientState;
import com.reiasu.reiparticlesapi.network.packet.CameraShakeS2CPacket;
import com.reiasu.reiparticlesapi.network.packet.PacketCameraShakeS2C;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class ClientCameraShakeHandler {
    private ClientCameraShakeHandler() {
    }

    public static void receive(PacketCameraShakeS2C packet) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        double range = packet.range();
        if (range > 0.0 && player.position().distanceTo(packet.origin()) > range) {
            return;
        }
        CameraShakeClientState.start(new CameraShakeS2CPacket(packet.range(), packet.origin(), packet.amplitude(), packet.tick()));
    }
}

