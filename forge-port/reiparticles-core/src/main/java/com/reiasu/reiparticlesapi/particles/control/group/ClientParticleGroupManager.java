// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.particles.control.group;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side manager for {@link ControllableParticleGroup} instances.
 * <p>
 * Tracks visible groups, ticks them, and provides factory registration
 * for creating groups from server-side packets.
 *
 * @deprecated Use ParticleGroupStyle instead.
 */
@Deprecated
public final class ClientParticleGroupManager {
    public static final ClientParticleGroupManager INSTANCE = new ClientParticleGroupManager();
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ConcurrentHashMap<UUID, ControllableParticleGroup> visibleControls = new ConcurrentHashMap<>();
    private final Map<Class<? extends ControllableParticleGroup>, ControllableParticleGroupProvider> registerBuilders = new ConcurrentHashMap<>();
    private final Map<String, ControllableParticleGroupProvider> registerBuildersByName = new ConcurrentHashMap<>();

    private ClientParticleGroupManager() {
    }

    public void register(Class<? extends ControllableParticleGroup> type, ControllableParticleGroupProvider provider) {
        registerBuilders.put(type, provider);
        registerBuildersByName.put(type.getName(), provider);
    }

    public ControllableParticleGroupProvider getBuilder(Class<? extends ControllableParticleGroup> type) {
        return registerBuilders.get(type);
    }

    public ControllableParticleGroupProvider getBuilder(String typeName) {
        return registerBuildersByName.get(typeName);
    }

    public ControllableParticleGroup getControlGroup(UUID groupId) {
        return visibleControls.get(groupId);
    }

    public void addVisibleGroup(ControllableParticleGroup group) {
        visibleControls.put(group.getUuid(), group);
    }

    public void removeVisible(UUID id) {
        ControllableParticleGroup group = visibleControls.get(id);
        if (group != null) {
            group.remove();
        }
        visibleControls.remove(id);
    }

    public void clearAllVisible() {
        for (ControllableParticleGroup group : visibleControls.values()) {
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
        tickVisibleGroups(visibleControls, LOGGER);
    }

    static void tickVisibleGroups(Map<UUID, ControllableParticleGroup> visibleControls, Logger logger) {
        Iterator<Map.Entry<UUID, ControllableParticleGroup>> iterator = visibleControls.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ControllableParticleGroup> entry = iterator.next();
            ControllableParticleGroup group = entry.getValue();
            try {
                group.tick();
            } catch (Exception e) {
                logger.warn("Deprecated particle group {} ({}) failed during client tick; removing group",
                        group.getUuid(), group.getClass().getName(), e);
                group.setCanceled(true);
            }
            if (group.getCanceled() || !group.getValid()) {
                removeGroup(group, logger);
                iterator.remove();
            }
        }
    }

    private static void removeGroup(ControllableParticleGroup group, Logger logger) {
        try {
            group.remove();
        } catch (Exception e) {
            logger.warn("Deprecated particle group {} ({}) failed during client cleanup",
                    group.getUuid(), group.getClass().getName(), e);
        }
    }
}
