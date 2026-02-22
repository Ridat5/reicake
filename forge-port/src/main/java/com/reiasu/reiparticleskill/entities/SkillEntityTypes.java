package com.reiasu.reiparticleskill.entities;

import com.reiasu.reiparticleskill.ReiParticleSkillForge;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SkillEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ReiParticleSkillForge.MOD_ID);

    public static final RegistryObject<EntityType<BarrageItemEntity>> BARRAGE_ITEM =
            ENTITIES.register("barrage_item", () ->
                    EntityType.Builder.<BarrageItemEntity>of(BarrageItemEntity::new, MobCategory.MISC)
                            .sized(0.4F, 0.4F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(ReiParticleSkillForge.MOD_ID + ":barrage_item"));

    private SkillEntityTypes() {
    }

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}
