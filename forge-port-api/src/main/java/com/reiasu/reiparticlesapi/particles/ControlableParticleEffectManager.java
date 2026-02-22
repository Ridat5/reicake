package com.reiasu.reiparticlesapi.particles;

import com.reiasu.reiparticlesapi.particles.impl.*;
import net.minecraft.world.level.block.Blocks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Registry and factory for {@link ControlableParticleEffect} instances.
 * Each effect type is registered with a prototype that is cloned on demand.
 */
public final class ControlableParticleEffectManager {
    public static final ControlableParticleEffectManager INSTANCE = new ControlableParticleEffectManager();

    private static final Map<Class<? extends ControlableParticleEffect>, ControlableParticleEffect> buffer =
            new LinkedHashMap<>();

    private ControlableParticleEffectManager() {}

    public void register(ControlableParticleEffect effect) {
        buffer.put(effect.getClass(), effect.clone());
    }

    public ControlableParticleEffect createWithUUID(UUID uuid, Class<? extends ControlableParticleEffect> type) {
        ControlableParticleEffect prototype = buffer.get(type);
        if (prototype == null) {
            throw new IllegalArgumentException("No registered effect for type: " + type.getName());
        }
        ControlableParticleEffect instance = prototype.clone();
        instance.setControlUUID(uuid);
        return instance;
    }

    public void init() {
        // Called during mod initialization to ensure static block has run
    }

    static {
        INSTANCE.register(new ControlableCloudEffect(UUID.randomUUID(), false));
        INSTANCE.register(new ControlableEnchantmentEffect(UUID.randomUUID(), false));
        INSTANCE.register(new ControlableFireworkEffect(UUID.randomUUID(), false));
        INSTANCE.register(new ControlableFlashEffect(UUID.randomUUID(), false));
        INSTANCE.register(new ControlableEndRodEffect(UUID.randomUUID(), false));
        INSTANCE.register(new ControlableFallingDustEffect(UUID.randomUUID(),
                Blocks.SAND.defaultBlockState(), false));
        INSTANCE.register(new ControlableSplashEffect(UUID.randomUUID(), false));
    }
}
