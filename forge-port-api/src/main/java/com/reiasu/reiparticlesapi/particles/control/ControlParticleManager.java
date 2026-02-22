package com.reiasu.reiparticlesapi.particles.control;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the mapping between control UUIDs and their {@link ParticleControler} instances.
 * <p>
 * Each controlled particle gets a UUID; the manager stores and retrieves the
 * corresponding controller so the particle can be mutated from server-driven
 * commands.
 */
public final class ControlParticleManager {

    public static final ControlParticleManager INSTANCE = new ControlParticleManager();

    private final ConcurrentHashMap<UUID, ParticleControler> controls = new ConcurrentHashMap<>();

    private ControlParticleManager() {
    }

    /**
     * Look up a controller by UUID. Returns {@code null} if none is registered.
     */
    public ParticleControler getControl(UUID uuid) {
        return controls.get(uuid);
    }

    /**
     * Remove a controller by UUID.
     */
    public void removeControl(UUID uuid) {
        controls.remove(uuid);
    }

    /**
     * Create a new controller for the given UUID and register it.
     *
     * @param uuid the control UUID
     * @return the newly created controller
     */
    public ParticleControler createControl(UUID uuid) {
        ParticleControler controler = new ParticleControler(uuid);
        controls.put(uuid, controler);
        return controler;
    }
}
