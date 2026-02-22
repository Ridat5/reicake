package com.reiasu.reiparticlesapi;

import com.reiasu.reiparticlesapi.animation.AnimateManager;
import com.reiasu.reiparticlesapi.client.ClientTickEventForwarder;
import com.reiasu.reiparticlesapi.compat.version.ModLifecycleVersionBridge;
import com.reiasu.reiparticlesapi.compat.version.VersionBridgeRegistry;
import com.reiasu.reiparticlesapi.display.DisplayEntityManager;
import com.reiasu.reiparticlesapi.event.ReiEventBus;
import com.reiasu.reiparticlesapi.event.ForgeEventForwarder;
import com.reiasu.reiparticlesapi.event.events.server.ServerPostTickEvent;
import com.reiasu.reiparticlesapi.event.events.server.ServerPreTickEvent;
import com.reiasu.reiparticlesapi.network.ReiParticlesNetwork;
import com.reiasu.reiparticlesapi.network.particle.composition.manager.ParticleCompositionManager;
import com.reiasu.reiparticlesapi.network.particle.emitters.ParticleEmittersManager;
import com.reiasu.reiparticlesapi.network.particle.style.ParticleStyleManager;
import com.reiasu.reiparticlesapi.particles.control.group.ClientParticleGroupManager;
import com.reiasu.reiparticlesapi.renderer.client.ClientRenderEntityManager;
import com.reiasu.reiparticlesapi.renderer.server.ServerRenderEntityManager;
import com.reiasu.reiparticlesapi.test.TestManager;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(ReiParticlesAPIForge.MOD_ID)
public final class ReiParticlesAPIForge {
    public static final String MOD_ID = "reiparticlesapi";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ModLifecycleVersionBridge LIFECYCLE = VersionBridgeRegistry.lifecycle();

    public ReiParticlesAPIForge() {
        LIFECYCLE.registerClientSetup(this::onClientSetup);
        LIFECYCLE.registerClientStartTick(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientTickEventForwarder::onClientStartTick)
        );
        LIFECYCLE.registerClientEndTick(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientTickEventForwarder::onClientEndTick);
            AnimateManager.INSTANCE.tickClient();
            ParticleEmittersManager.tickClient();
            DisplayEntityManager.INSTANCE.tickClient();
            ParticleCompositionManager.INSTANCE.tickClient();
            ParticleStyleManager.doTickClient();
            ClientParticleGroupManager.INSTANCE.doClientTick();
            ClientRenderEntityManager.INSTANCE.doClientTick();
            com.reiasu.reiparticlesapi.network.animation.PathMotionManager.INSTANCE.tick();
            com.reiasu.reiparticlesapi.scheduler.ReiScheduler.INSTANCE.doTick();
            com.reiasu.reiparticlesapi.utils.ClientCameraUtil.INSTANCE.tick();
        });
        LIFECYCLE.registerServerStartTick(server -> ReiEventBus.call(new ServerPreTickEvent(server)));
        LIFECYCLE.registerServerEndTick(server -> {
            AnimateManager.INSTANCE.tickServer();
            ParticleEmittersManager.tickAll();
            DisplayEntityManager.INSTANCE.tickAll();
            ParticleCompositionManager.INSTANCE.tickAll();
            ParticleStyleManager.doTickServer();
            ServerRenderEntityManager.INSTANCE.tick();
            ServerRenderEntityManager.INSTANCE.upgrade(server);
            TestManager.INSTANCE.doTickServer();
            ReiEventBus.call(new ServerPostTickEvent(server));
        });

        var modBus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
        com.reiasu.reiparticlesapi.particles.ReiModParticles.register(modBus);
        modBus.addListener(this::onRegisterParticleProviders);

        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
                (net.minecraftforge.event.RegisterCommandsEvent event) ->
                        com.reiasu.reiparticlesapi.commands.APICommand.INSTANCE.register(event.getDispatcher())
        );

        ReiParticlesNetwork.init();
        ParticleEmittersManager.registerBuiltinCodecs();
        com.reiasu.reiparticlesapi.network.particle.emitters.type.EmittersShootTypes.INSTANCE.init();
        com.reiasu.reiparticlesapi.network.particle.emitters.environment.wind.WindDirections.INSTANCE.init();
        com.reiasu.reiparticlesapi.particles.ControlableParticleEffectManager.INSTANCE.init();
        ReiParticlesAPI.init();
        ForgeEventForwarder.init();
        ReiParticlesAPI.INSTANCE.loadScannerPackages();
        ReiParticlesAPI.INSTANCE.registerParticleStyles();
        ReiParticlesAPI.INSTANCE.registerTestHooks();

        LOGGER.info("ReiParticlesAPI Forge runtime initialized");
    }

    private void onClientSetup() {
        ReiParticlesAPI.INSTANCE.registerKeyBindings();
        LOGGER.info("ReiParticlesAPI client setup completed");
    }

    private void onRegisterParticleProviders(net.minecraftforge.client.event.RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(com.reiasu.reiparticlesapi.particles.ReiModParticles.CONTROLABLE_END_ROD.get(),
                com.reiasu.reiparticlesapi.particles.impl.particles.ControlableEndRodParticle.Factory::new);
        event.registerSpriteSet(com.reiasu.reiparticlesapi.particles.ReiModParticles.CONTROLABLE_ENCHANTMENT.get(),
                com.reiasu.reiparticlesapi.particles.impl.particles.ControlableEnchantmentParticle.Factory::new);
        event.registerSpriteSet(com.reiasu.reiparticlesapi.particles.ReiModParticles.CONTROLABLE_CLOUD.get(),
                com.reiasu.reiparticlesapi.particles.impl.particles.ControlableCloudParticle.Factory::new);
        event.registerSpriteSet(com.reiasu.reiparticlesapi.particles.ReiModParticles.CONTROLABLE_FLASH.get(),
                com.reiasu.reiparticlesapi.particles.impl.particles.ControlableFlashParticle.Factory::new);
        event.registerSpriteSet(com.reiasu.reiparticlesapi.particles.ReiModParticles.CONTROLABLE_FIREWORK.get(),
                com.reiasu.reiparticlesapi.particles.impl.particles.ControlableFireworkParticle.Factory::new);
        event.registerSpecial(com.reiasu.reiparticlesapi.particles.ReiModParticles.CONTROLABLE_FALLING_DUST.get(),
                new com.reiasu.reiparticlesapi.particles.impl.particles.ControlableFallingDustParticle.Factory());
        event.registerSpriteSet(com.reiasu.reiparticlesapi.particles.ReiModParticles.CONTROLABLE_SPLASH.get(),
                com.reiasu.reiparticlesapi.particles.impl.particles.ControlableSplashParticle.Factory::new);
        LOGGER.info("Registered ReiParticlesAPI particle providers (7 types)");
    }
}
