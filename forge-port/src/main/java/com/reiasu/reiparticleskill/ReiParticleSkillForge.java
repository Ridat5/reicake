package com.reiasu.reiparticleskill;

import com.reiasu.reiparticlesapi.ReiParticlesAPI;
import com.reiasu.reiparticleskill.command.ReiParticleSkillDebugCommand;
import com.reiasu.reiparticleskill.command.SkillActionCommand;
import com.reiasu.reiparticleskill.command.port.APITestCommandPort;
import com.reiasu.reiparticleskill.command.port.DisplayCommandPort;
import com.reiasu.reiparticleskill.command.port.RailgunCommandPort;
import com.reiasu.reiparticleskill.compat.reiparticles.ReiparticlesFacade;
import com.reiasu.reiparticleskill.compat.reiparticles.ReiparticlesFacadeProvider;
import com.reiasu.reiparticleskill.compat.version.ModLifecycleVersionBridge;
import com.reiasu.reiparticleskill.compat.version.VersionBridgeRegistry;
import com.reiasu.reiparticleskill.enchantments.SkillEnchantments;
import com.reiasu.reiparticleskill.entities.SkillEntityTypes;
import com.reiasu.reiparticleskill.end.respawn.EndRespawnStateBridge;
import com.reiasu.reiparticleskill.end.respawn.EndRespawnWatcher;
import com.reiasu.reiparticleskill.listener.KeyListener;
import com.reiasu.reiparticleskill.listener.ServerListener;
import com.reiasu.reiparticleskill.particles.core.emitters.p1.CollectEnderPowerEmitter;
import com.reiasu.reiparticleskill.particles.core.emitters.p1.CollectPillarsEmitters;
import com.reiasu.reiparticleskill.particles.core.emitters.p1.EndBeamExplosionEmitter;
import com.reiasu.reiparticleskill.particles.core.emitters.p1.EndCrystalEmitters;
import com.reiasu.reiparticleskill.particles.core.emitters.p1.EndLightBeamEmitter;
import com.reiasu.reiparticleskill.particles.core.emitters.p1.SummonExplosionEmitter;
import com.reiasu.reiparticleskill.particles.core.emitters.p2.LightEmitter;
import com.reiasu.reiparticleskill.particles.core.emitters.p2.SwordAuraEmitters;
import com.reiasu.reiparticleskill.particles.core.emitters.p2.SwordExplosionEmitters;
import com.reiasu.reiparticleskill.particles.core.emitters.p2.formation.SwordFormationEmitters;
import com.reiasu.reiparticleskill.particles.core.emitters.p2.formation.SwordFormationExplosionEmitter;
import com.reiasu.reiparticleskill.particles.core.emitters.p2.formation.SwordFormationWaveEmitter;
import com.reiasu.reiparticleskill.particles.core.styles.p1.EndCrystalStyle;
import com.reiasu.reiparticleskill.particles.core.styles.p1.EndDustStyle;
import com.reiasu.reiparticleskill.particles.core.styles.p1.EnderRespawnCenterStyle;
import com.reiasu.reiparticleskill.particles.core.styles.p1.EnderRespawnWaveCloudStyle;
import com.reiasu.reiparticleskill.particles.core.styles.p1.EnderRespawnWaveEnchantStyle;
import com.reiasu.reiparticleskill.particles.display.emitter.ParticleGroupEmitter;
import com.reiasu.reiparticleskill.particles.display.emitter.RailgunBeamEmitter;
import com.reiasu.reiparticleskill.particles.display.emitter.RailgunExplosionEmitter;
import com.reiasu.reiparticleskill.particles.display.style.LargeMagicCircleStyle;
import com.reiasu.reiparticleskill.particles.display.style.RailgunBeamStyle;
import com.reiasu.reiparticleskill.particles.display.style.RailgunChargingRingStyle;
import com.reiasu.reiparticleskill.sounds.SkillSoundEvents;
import com.mojang.logging.LogUtils;
import com.reiasu.reiparticlesapi.network.particle.emitters.ParticleEmittersManager;
import com.reiasu.reiparticlesapi.network.particle.style.ParticleStyleManager;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(ReiParticleSkillForge.MOD_ID)
public final class ReiParticleSkillForge {
    public static final String MOD_ID = "reiparticleskill";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ModLifecycleVersionBridge LIFECYCLE = VersionBridgeRegistry.lifecycle();
    private final ReiparticlesFacade reiparticles = ReiparticlesFacadeProvider.get();
    private final EndRespawnStateBridge endRespawnBridge = new EndRespawnStateBridge();

    public ReiParticleSkillForge() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        SkillEntityTypes.register(modBus);
        SkillEnchantments.register(modBus);
        SkillSoundEvents.register(modBus);

        LIFECYCLE.registerClientSetup(this::onClientSetup);
        LIFECYCLE.registerCommandRegistration(this::onRegisterCommands);
        LIFECYCLE.registerServerEndTick(server -> {
            EndRespawnWatcher.tickServer(server, endRespawnBridge, LOGGER);
            ServerListener.onServerPostTick(server);
        });

        reiparticles.bootstrap(LOGGER);
        registerApiListeners();
        registerRuntimePorts();
        reiparticles.registerParticleStyles(LOGGER);
        reiparticles.registerTestHooks(LOGGER);

        LOGGER.info("ReiParticleSkill Forge runtime initialized");
    }

    private void onClientSetup() {
        reiparticles.registerKeyBindings(LOGGER);
        LOGGER.info("ReiParticleSkill client setup completed");
    }

    private void onRegisterCommands(com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher) {
        ReiParticleSkillDebugCommand.register(dispatcher, endRespawnBridge, LOGGER);
        SkillActionCommand.register(dispatcher);
        DisplayCommandPort.register(dispatcher);
        RailgunCommandPort.register(dispatcher);
        APITestCommandPort.register(dispatcher);
        LOGGER.info("Registered reiparticleskill debug commands");
    }

    private void registerApiListeners() {
        if (!reiparticles.isOperational()) {
            return;
        }
        try {
            ReiParticlesAPI.INSTANCE.registerEventListener(MOD_ID, new KeyListener());
            LOGGER.info("Registered ReiParticleSkill API listeners");
        } catch (Throwable t) {
            LOGGER.warn("Failed to register ReiParticleSkill API listeners", t);
        }
    }

    private void registerRuntimePorts() {
        try {
            // Emitter codecs — p1
            ParticleEmittersManager.registerCodec(CollectEnderPowerEmitter.CODEC_ID, CollectEnderPowerEmitter::decode);
            ParticleEmittersManager.registerCodec(CollectPillarsEmitters.CODEC_ID, CollectPillarsEmitters::decode);
            ParticleEmittersManager.registerCodec(EndBeamExplosionEmitter.CODEC_ID, EndBeamExplosionEmitter::decode);
            ParticleEmittersManager.registerCodec(EndCrystalEmitters.CODEC_ID, EndCrystalEmitters::decode);
            ParticleEmittersManager.registerCodec(EndLightBeamEmitter.CODEC_ID, EndLightBeamEmitter::decode);
            ParticleEmittersManager.registerCodec(SummonExplosionEmitter.CODEC_ID, SummonExplosionEmitter::decode);
            // Emitter codecs — p2
            ParticleEmittersManager.registerCodec(LightEmitter.CODEC_ID, LightEmitter::decode);
            ParticleEmittersManager.registerCodec(SwordAuraEmitters.CODEC_ID, SwordAuraEmitters::decode);
            ParticleEmittersManager.registerCodec(SwordExplosionEmitters.CODEC_ID, SwordExplosionEmitters::decode);
            ParticleEmittersManager.registerCodec(SwordFormationEmitters.CODEC_ID, SwordFormationEmitters::decode);
            ParticleEmittersManager.registerCodec(SwordFormationExplosionEmitter.CODEC_ID, SwordFormationExplosionEmitter::decode);
            ParticleEmittersManager.registerCodec(SwordFormationWaveEmitter.CODEC_ID, SwordFormationWaveEmitter::decode);
            // Emitter codecs — display
            ParticleEmittersManager.registerCodec(ParticleGroupEmitter.CODEC_ID, ParticleGroupEmitter::decode);
            ParticleEmittersManager.registerCodec(RailgunBeamEmitter.CODEC_ID, RailgunBeamEmitter::decode);
            ParticleEmittersManager.registerCodec(RailgunExplosionEmitter.CODEC_ID, RailgunExplosionEmitter::decode);

            // Particle styles — p1
            ParticleStyleManager.register(EndCrystalStyle.class, new EndCrystalStyle.Provider());
            ParticleStyleManager.register(EndDustStyle.class, new EndDustStyle.Provider());
            ParticleStyleManager.register(EnderRespawnCenterStyle.class, new EnderRespawnCenterStyle.Provider());
            ParticleStyleManager.register(EnderRespawnWaveCloudStyle.class, new EnderRespawnWaveCloudStyle.Provider());
            ParticleStyleManager.register(EnderRespawnWaveEnchantStyle.class, new EnderRespawnWaveEnchantStyle.Provider());
            // Particle styles — display
            ParticleStyleManager.register(LargeMagicCircleStyle.class, new LargeMagicCircleStyle.Provider());
            ParticleStyleManager.register(RailgunBeamStyle.class, new RailgunBeamStyle.Provider());
            ParticleStyleManager.register(RailgunChargingRingStyle.class, new RailgunChargingRingStyle.Provider());

            LOGGER.info("Registered ReiParticleSkill runtime ports (15 emitters, 8 styles)");
        } catch (Throwable t) {
            LOGGER.warn("Failed to register ReiParticleSkill runtime ports", t);
        }
    }
}
