// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.particles;

import com.reiasu.reiparticlesapi.particles.impl.*;
import com.mojang.brigadier.StringReader;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.UUID;
import java.util.function.BiFunction;

/**
 * Registry for all ReiParticlesAPI custom particle types.
 * <p>
 * Forge port: uses {@link DeferredRegister} with custom {@link ParticleType}
 * instances that carry {@link ControllableParticleEffect} data (UUID + faceToPlayer).
 */
public final class ReiModParticles {
    public static final ReiModParticles INSTANCE = new ReiModParticles();

    private static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, "reiparticlesapi");

    public static final RegistryObject<ParticleType<ControllableEndRodEffect>> CONTROLLABLE_END_ROD =
            PARTICLES.register("controllable_end_rod",
                    () -> createType(ControllableEndRodEffect::new));

    public static final RegistryObject<ParticleType<ControllableEnchantmentEffect>> CONTROLLABLE_ENCHANTMENT =
            PARTICLES.register("controllable_enchantment",
                    () -> createType(ControllableEnchantmentEffect::new));

    public static final RegistryObject<ParticleType<ControllableCloudEffect>> CONTROLLABLE_CLOUD =
            PARTICLES.register("controllable_cloud",
                    () -> createType(ControllableCloudEffect::new));

    public static final RegistryObject<ParticleType<ControllableFlashEffect>> CONTROLLABLE_FLASH =
            PARTICLES.register("controllable_flash",
                    () -> createType(ControllableFlashEffect::new));

    public static final RegistryObject<ParticleType<ControllableFireworkEffect>> CONTROLLABLE_FIREWORK =
            PARTICLES.register("controllable_firework",
                    () -> createType(ControllableFireworkEffect::new));

    public static final RegistryObject<ParticleType<ControllableFallingDustEffect>> CONTROLLABLE_FALLING_DUST =
            PARTICLES.register("controllable_falling_dust",
                    () -> createType((uuid, face) -> new ControllableFallingDustEffect(
                            uuid, net.minecraft.world.level.block.Blocks.SAND.defaultBlockState(), face)));

    public static final RegistryObject<ParticleType<ControllableSplashEffect>> CONTROLLABLE_SPLASH =
            PARTICLES.register("controllable_splash",
                    () -> createType(ControllableSplashEffect::new));

    private ReiModParticles() {}

    /**
     * Creates a {@link ParticleType} with a deserializer that reads UUID + boolean
     * from the network/command, matching all {@link ControllableParticleEffect} subtypes.
     */
    @SuppressWarnings("deprecation")
    private static <T extends ControllableParticleEffect> ParticleType<T> createType(
            BiFunction<UUID, Boolean, T> factory) {
        ParticleOptions.Deserializer<T> deserializer = new ParticleOptions.Deserializer<T>() {
            @Override
            public T fromCommand(ParticleType<T> type, StringReader reader) {
                return factory.apply(UUID.randomUUID(), false);
            }

            @Override
            public T fromNetwork(ParticleType<T> type, FriendlyByteBuf buf) {
                return factory.apply(buf.readUUID(), buf.readBoolean());
            }
        };
        return new ParticleType<T>(true, deserializer) {
            @Override
            public com.mojang.serialization.Codec<T> codec() {
                return com.mojang.serialization.Codec.unit(() -> factory.apply(UUID.randomUUID(), false));
            }
        };
    }

    /**
     * Register the particle types with the Forge event bus.
     * Call this during mod construction.
     */
    public static void register(IEventBus modBus) {
        PARTICLES.register(modBus);
    }

    public void reg() {
        // No-op, registration is handled by DeferredRegister
    }
}
