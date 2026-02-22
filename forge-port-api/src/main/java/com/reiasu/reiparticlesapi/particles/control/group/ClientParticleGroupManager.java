package com.reiasu.reiparticlesapi.particles.control.group;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side manager for {@link ControlableParticleGroup} instances.
 * <p>
 * Tracks visible groups, ticks them, and provides factory registration
 * for creating groups from server-side packets.
 *
 * @deprecated Use ParticleGroupStyle instead.
 */
@Deprecated
public final class ClientParticleGroupManager {
    public static final ClientParticleGroupManager INSTANCE = new ClientParticleGroupManager();

    private final ConcurrentHashMap<UUID, ControlableParticleGroup> visibleControls = new ConcurrentHashMap<>();
    private final HashMap<Class<? extends ControlableParticleGroup>, ControlableParticleGroupProvider> registerBuilders = new HashMap<>();

    private ClientParticleGroupManager() {}

    public void register(Class<? extends ControlableParticleGroup> type, ControlableParticleGroupProvider provider) {
        registerBuilders.put(type, provider);
    }

    public ControlableParticleGroupProvider getBuilder(Class<? extends ControlableParticleGroup> type) {
        return registerBuilders.get(type);
    }

    public ControlableParticleGroup getControlGroup(UUID groupId) {
        return visibleControls.get(groupId);
    }

    public void addVisibleGroup(ControlableParticleGroup group) {
        visibleControls.put(group.getUuid(), group);
    }

    public void removeVisible(UUID id) {
        ControlableParticleGroup group = visibleControls.get(id);
        if (group != null) {
            group.remove();
        }
        visibleControls.remove(id);
    }

    public void clearAllVisible() {
        for (ControlableParticleGroup group : visibleControls.values()) {
            group.setCanceled(true);
        }
        visibleControls.clear();
    }

    /**
     * Called every client tick. Ticks all visible groups and clears if player is removed.
     */
    public void doClientTick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (player.isRemoved()) {
            visibleControls.clear();
            return;
        }
        for (ControlableParticleGroup group : visibleControls.values()) {
            group.tick();
        }
    }
}
