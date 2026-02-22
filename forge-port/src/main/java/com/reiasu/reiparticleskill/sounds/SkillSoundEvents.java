package com.reiasu.reiparticleskill.sounds;

import com.reiasu.reiparticleskill.ReiParticleSkillForge;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SkillSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ReiParticleSkillForge.MOD_ID);

    public static final RegistryObject<SoundEvent> SWORD_FORMATION =
            SOUND_EVENTS.register("sword_formation",
                    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ReiParticleSkillForge.MOD_ID, "sword_formation")));

    private SkillSoundEvents() {
    }

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
