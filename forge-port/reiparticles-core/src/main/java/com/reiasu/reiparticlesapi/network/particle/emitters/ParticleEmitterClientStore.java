// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network.particle.emitters;

import com.mojang.logging.LogUtils;
import com.reiasu.reiparticlesapi.event.ReiEventBus;
import com.reiasu.reiparticlesapi.event.events.particle.emitter.EmitterRemoveEvent;
import com.reiasu.reiparticlesapi.event.events.particle.emitter.EmitterSpawnEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class ParticleEmitterClientStore {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<UUID, ParticleEmitters> clientEmitters = new ConcurrentHashMap<>();

    void createOrChange(ParticleEmitters emitters, Level viewWorld) {
        if (emitters == null || viewWorld == null) {
            return;
        }
        ParticleEmitters current = clientEmitters.get(emitters.getUuid());
        if (current == null) {
            Vec3 pos = emitters.position();
            emitters.bind(viewWorld, pos.x, pos.y, pos.z);
            clientEmitters.put(emitters.getUuid(), emitters);
            ReiEventBus.call(new EmitterSpawnEvent(emitters, true));
            return;
        }
        current.update(emitters);
        Vec3 pos = emitters.position();
        current.bind(viewWorld, pos.x, pos.y, pos.z);
        if (emitters.getCanceled()) {
            current.cancel();
        }
    }

    void tickClient() {
        Iterator<Map.Entry<UUID, ParticleEmitters>> iterator = clientEmitters.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ParticleEmitters> entry = iterator.next();
            ParticleEmitters emitters = entry.getValue();
            try {
                emitters.tick();
            } catch (Exception e) {
                LOGGER.warn("Emitter {} ({}) failed during client tick; removing emitter",
                        emitters.getUuid(), emitters.getClass().getName(), e);
                emitters.cancel();
            }
            if (!emitters.getCanceled()) {
                continue;
            }
            removeClientEmitter(emitters);
            iterator.remove();
        }
    }

    int size() {
        return clientEmitters.size();
    }

    Map<UUID, ParticleEmitters> snapshot() {
        return Collections.unmodifiableMap(clientEmitters);
    }

    void clear() {
        for (ParticleEmitters emitter : clientEmitters.values()) {
            removeClientEmitter(emitter);
        }
        clientEmitters.clear();
    }

    private void removeClientEmitter(ParticleEmitters emitters) {
        if (emitters == null) {
            return;
        }
        emitters.cancel();
        ReiEventBus.call(new EmitterRemoveEvent(emitters, true));
    }
}
