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
 * instances that carry {@link ControlableParticleEffect} data (UUID + faceToPlayer).
 */
public final class ReiModParticles {
    public static final ReiModParticles INSTANCE = new ReiModParticles();

    private static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, "reiparticlesapi");

    public static final RegistryObject<ParticleType<ControlableEndRodEffect>> CONTROLABLE_END_ROD =
            PARTICLES.register("controlable_end_rod",
                    () -> createType(ControlableEndRodEffect::new));

    public static final RegistryObject<ParticleType<ControlableEnchantmentEffect>> CONTROLABLE_ENCHANTMENT =
            PARTICLES.register("controlable_enchantment",
                    () -> createType(ControlableEnchantmentEffect::new));

    public static final RegistryObject<ParticleType<ControlableCloudEffect>> CONTROLABLE_CLOUD =
            PARTICLES.register("controlable_cloud",
                    () -> createType(ControlableCloudEffect::new));

    public static final RegistryObject<ParticleType<ControlableFlashEffect>> CONTROLABLE_FLASH =
            PARTICLES.register("controlable_flash",
                    () -> createType(ControlableFlashEffect::new));

    public static final RegistryObject<ParticleType<ControlableFireworkEffect>> CONTROLABLE_FIREWORK =
            PARTICLES.register("controlable_firework",
                    () -> createType(ControlableFireworkEffect::new));

    public static final RegistryObject<ParticleType<ControlableFallingDustEffect>> CONTROLABLE_FALLING_DUST =
            PARTICLES.register("controlable_falling_dust",
                    () -> createType((uuid, face) -> new ControlableFallingDustEffect(
                            uuid, net.minecraft.world.level.block.Blocks.SAND.defaultBlockState(), face)));

    public static final RegistryObject<ParticleType<ControlableSplashEffect>> CONTROLABLE_SPLASH =
            PARTICLES.register("controlable_splash",
                    () -> createType(ControlableSplashEffect::new));

    private ReiModParticles() {}

    /**
     * Creates a {@link ParticleType} with a deserializer that reads UUID + boolean
     * from the network/command, matching all {@link ControlableParticleEffect} subtypes.
     */
    @SuppressWarnings("deprecation")
    private static <T extends ControlableParticleEffect> ParticleType<T> createType(
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
