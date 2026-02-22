package com.reiasu.reiparticlesapi.network.particle.composition;

import com.reiasu.reiparticlesapi.particles.Controlable;
import com.reiasu.reiparticlesapi.particles.ControlableParticle;
import com.reiasu.reiparticlesapi.particles.ControlableParticleEffect;
import com.reiasu.reiparticlesapi.particles.ParticleDisplayer;
import com.reiasu.reiparticlesapi.particles.control.ParticleControler;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Data attached to each particle entry in a {@link ParticleComposition}.
 * <p>
 * Each instance carries a unique {@link UUID} that identifies it within
 * the composition, and an {@code order} value used for sequenced display.
 * <p>
 * Client-side fields:
 * <ul>
 *   <li>{@link #displayerBuilder} — factory that creates the {@link ParticleDisplayer}
 *       for this entry (e.g. single-particle, group, style).</li>
 *   <li>{@link #particleInit} — callback run once when the particle controller
 *       is first loaded (optional).</li>
 *   <li>{@link #controlable} — the {@link Controlable} handle returned by the
 *       displayer after spawning. Used for teleport/remove/rotate operations.</li>
 * </ul>
 */
public class CompositionData implements Comparable<CompositionData> {

    private final UUID uuid = UUID.randomUUID();
    private int order;

    private Supplier<ParticleDisplayer> displayerBuilder;
    private Consumer<ParticleControler> particleInit;
    @Nullable
    private Controlable<?> controlable;

    public UUID getUuid() {
        return uuid;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Supplier<ParticleDisplayer> getDisplayerBuilder() {
        return displayerBuilder;
    }

    public CompositionData setDisplayerBuilder(Supplier<ParticleDisplayer> displayerBuilder) {
        this.displayerBuilder = displayerBuilder;
        return this;
    }

    public CompositionData setDisplayerWithEffect(Supplier<ControlableParticleEffect> effectSupplier) {
        this.displayerBuilder = () -> ParticleDisplayer.withSingle(effectSupplier.get());
        return this;
    }

    public Consumer<ParticleControler> getParticleInit() {
        return particleInit;
    }

    public CompositionData setParticleInit(Consumer<ParticleControler> particleInit) {
        this.particleInit = particleInit;
        return this;
    }

    @Nullable
    public Controlable<?> getControlable() {
        return controlable;
    }

    public void setControlable(@Nullable Controlable<?> controlable) {
        this.controlable = controlable;
    }

    @Override
    public int compareTo(CompositionData other) {
        return this.order - other.order;
    }
}
