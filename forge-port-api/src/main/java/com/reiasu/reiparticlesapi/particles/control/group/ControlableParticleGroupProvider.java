package com.reiasu.reiparticlesapi.particles.control.group;

import com.reiasu.reiparticlesapi.network.buffer.ParticleControlerDataBuffer;

import java.util.Map;
import java.util.UUID;

/**
 * Provider (factory) for creating and modifying {@link ControlableParticleGroup} instances.
 * <p>
 * Registered with {@link ClientParticleGroupManager} for client-side group creation
 * when receiving server-side packets.
 *
 * @deprecated Use ParticleGroupStyle instead.
 */
@Deprecated
public interface ControlableParticleGroupProvider {

    /**
     * Create a new particle group with the given UUID and initialization args.
     */
    ControlableParticleGroup createGroup(UUID uuid, Map<String, ? extends ParticleControlerDataBuffer<?>> args);

    /**
     * Apply changes to an existing group with the given args.
     */
    void changeGroup(ControlableParticleGroup group, Map<String, ? extends ParticleControlerDataBuffer<?>> args);
}
