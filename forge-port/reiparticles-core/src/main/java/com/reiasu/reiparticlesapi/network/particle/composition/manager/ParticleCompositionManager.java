// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network.particle.composition.manager;

import com.mojang.logging.LogUtils;
import com.reiasu.reiparticlesapi.network.particle.composition.ParticleComposition;
import net.minecraft.network.FriendlyByteBuf;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class ParticleCompositionManager {
    public static final ParticleCompositionManager INSTANCE = new ParticleCompositionManager();
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<ParticleComposition> compositions = new ArrayList<>();
    private final Map<UUID, ParticleComposition> clientView = new ConcurrentHashMap<>();
    private final Map<UUID, ParticleComposition> serverView = new ConcurrentHashMap<>();
    private final Map<String, Function<FriendlyByteBuf, ParticleComposition>> registeredTypes = new ConcurrentHashMap<>();

    private ParticleCompositionManager() {
    }

    public Map<UUID, ParticleComposition> getClientView() {
        return clientView;
    }

    public Map<UUID, ParticleComposition> getServerView() {
        return serverView;
    }

    public Map<String, Function<FriendlyByteBuf, ParticleComposition>> getRegisteredTypes() {
        return registeredTypes;
    }

    public void registerType(String type, Function<FriendlyByteBuf, ParticleComposition> decoder) {
        if (type == null || type.isBlank() || decoder == null) {
            return;
        }
        registeredTypes.put(type, decoder);
    }

    public void spawn(ParticleComposition composition) {
        synchronized (compositions) {
            compositions.add(composition);
            serverView.put(composition.getControlUUID(), composition);
        }
    }

    public void addClient(ParticleComposition composition) {
        clientView.put(composition.getControlUUID(), composition);
        composition.display();
    }

    public void tickAll() {
        synchronized (compositions) {
            Iterator<ParticleComposition> iterator = compositions.iterator();
            while (iterator.hasNext()) {
                ParticleComposition composition = iterator.next();
                try {
                    composition.tick();
                } catch (Exception e) {
                    LOGGER.warn("Particle composition {} ({}) failed during server tick; removing composition",
                            composition.getControlUUID(), composition.getClass().getName(), e);
                    composition.cancel();
                }
                if (composition.getCanceled()) {
                    iterator.remove();
                    serverView.remove(composition.getControlUUID());
                }
            }
        }
    }

    public void tickClient() {
        Iterator<Map.Entry<UUID, ParticleComposition>> iterator = clientView.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ParticleComposition> entry = iterator.next();
            ParticleComposition composition = entry.getValue();
            try {
                composition.tick();
            } catch (Exception e) {
                LOGGER.warn("Particle composition {} ({}) failed during client tick; removing composition",
                        composition.getControlUUID(), composition.getClass().getName(), e);
                composition.cancel();
            }
            if (composition.getCanceled()) {
                iterator.remove();
            }
        }
    }

    public int activeCount() {
        synchronized (compositions) {
            return compositions.size();
        }
    }

    public void clear() {
        synchronized (compositions) {
            for (ParticleComposition composition : compositions) {
                composition.cancel();
            }
            compositions.clear();
        }
        serverView.clear();
        for (ParticleComposition composition : clientView.values()) {
            composition.cancel();
        }
        clientView.clear();
    }

    public List<ParticleComposition> getCompositions() {
        synchronized (compositions) {
            return Collections.unmodifiableList(new ArrayList<>(compositions));
        }
    }
}
