package com.reiasu.reiparticleskill.listener;

import com.reiasu.reiparticlesapi.annotations.events.EventHandler;
import com.reiasu.reiparticlesapi.annotations.events.EventListener;
import com.reiasu.reiparticlesapi.event.events.key.KeyActionEvent;
import com.reiasu.reiparticlesapi.event.events.key.KeyActionType;
import com.reiasu.reiparticleskill.ReiParticleSkillForge;
import com.reiasu.reiparticleskill.enchantments.SkillEnchantments;
import com.reiasu.reiparticleskill.keys.SkillKeys;
import com.reiasu.reiparticleskill.util.SwordLightEnchantUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@EventListener(modId = ReiParticleSkillForge.MOD_ID)
@Mod.EventBusSubscriber(modid = ReiParticleSkillForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class KeyListener {
    private static final int USE_COOLDOWN_TICKS = 16;

    @EventHandler
    public void onKeyEvent(KeyActionEvent event) {
        if (event == null || event.getAction() != KeyActionType.SINGLE_CLICK || !event.getServerSide()) {
            return;
        }
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        handleSkillKey(player, event.getKeyId(), event.getPlayer().getMainHandItem());
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event == null || event.getEntity().level().isClientSide() || event.getEntity().isSpectator()) {
            return;
        }
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) {
            return;
        }
        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();
        triggerShoot(player, stack);
    }

    public static boolean handleSkillKey(Player player, ResourceLocation keyId, ItemStack stack) {
        if (player == null || keyId == null || !canUseSkill(player, stack)) {
            return false;
        }
        player.getCooldowns().addCooldown(stack.getItem(), USE_COOLDOWN_TICKS);
        if (SkillKeys.FORMATION_1.equals(keyId)) {
            SwordLightEnchantUtil.placeSwordFormation(player, stack);
            return true;
        }
        if (SkillKeys.FORMATION_2.equals(keyId)) {
            SwordLightEnchantUtil.placeSwordFormation2(player, stack);
            return true;
        }
        return false;
    }

    public static boolean triggerShoot(Player player, ItemStack stack) {
        if (!canUseSkill(player, stack)) {
            return false;
        }
        player.getCooldowns().addCooldown(stack.getItem(), USE_COOLDOWN_TICKS);
        SwordLightEnchantUtil.shoot(player, stack);
        return true;
    }

    private static boolean canUseSkill(Player player, ItemStack stack) {
        if (player == null || stack == null || stack.isEmpty()) {
            return false;
        }
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return false;
        }
        return SkillEnchantments.getSwordLightLevel(stack) > 0;
    }
}
