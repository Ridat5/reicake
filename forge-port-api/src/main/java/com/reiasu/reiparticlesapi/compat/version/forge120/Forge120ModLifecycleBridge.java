package com.reiasu.reiparticlesapi.compat.version.forge120;

import com.reiasu.reiparticlesapi.compat.version.ModLifecycleVersionBridge;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.function.Consumer;

public final class Forge120ModLifecycleBridge implements ModLifecycleVersionBridge {
    @Override
    public void registerClientSetup(Runnable callback) {
        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLClientSetupEvent event) -> callback.run());
    }

    @Override
    public void registerClientStartTick(Runnable callback) {
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.START) {
                callback.run();
            }
        });
    }

    @Override
    public void registerClientEndTick(Runnable callback) {
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                callback.run();
            }
        });
    }

    @Override
    public void registerServerEndTick(Consumer<MinecraftServer> callback) {
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END && event.getServer() != null) {
                callback.accept(event.getServer());
            }
        });
    }

    @Override
    public void registerServerStartTick(Consumer<MinecraftServer> callback) {
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.START && event.getServer() != null) {
                callback.accept(event.getServer());
            }
        });
    }
}
