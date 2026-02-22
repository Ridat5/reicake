package com.reiasu.reiparticleskill.enchantments;

import com.reiasu.reiparticleskill.ReiParticleSkillForge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SkillEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, ReiParticleSkillForge.MOD_ID);

    public static final RegistryObject<Enchantment> SWORD_LIGHT =
            ENCHANTMENTS.register("sword_light", SwordLightEnchantment::new);

    private SkillEnchantments() {
    }

    public static void register(IEventBus bus) {
        ENCHANTMENTS.register(bus);
    }

    public static int getSwordLightLevel(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !SWORD_LIGHT.isPresent()) {
            return 0;
        }
        return EnchantmentHelper.getItemEnchantmentLevel(SWORD_LIGHT.get(), stack);
    }
}
