// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.client;

import com.reiasu.reiparticlesapi.display.DisplayEntity;
import com.reiasu.reiparticlesapi.display.DisplayEntityManager;
import com.reiasu.reiparticlesapi.network.particle.composition.CompositionData;
import com.reiasu.reiparticlesapi.network.particle.composition.ParticleComposition;
import com.reiasu.reiparticlesapi.network.particle.composition.manager.ParticleCompositionManager;
import com.reiasu.reiparticlesapi.network.particle.emitters.ParticleEmitters;
import com.reiasu.reiparticlesapi.network.particle.emitters.ParticleEmittersManager;
import com.reiasu.reiparticlesapi.network.particle.style.ParticleGroupStyle;
import com.reiasu.reiparticlesapi.network.particle.style.ParticleStyleManager;
import com.reiasu.reiparticlesapi.particles.Controllable;
import com.reiasu.reiparticlesapi.particles.control.group.ClientParticleGroupManager;
import com.reiasu.reiparticlesapi.particles.control.group.ControllableParticleGroup;
import com.reiasu.reiparticlesapi.renderer.RenderEntity;
import com.reiasu.reiparticlesapi.renderer.client.ClientRenderEntityManager;
import com.reiasu.reiparticlesapi.testutil.UnsafeAllocator;
import com.reiasu.reiparticlesapi.utils.RelativeLocation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientWorldStateResetTest {
    @AfterEach
    void cleanup() {
        ParticleEmittersManager.clear();
        DisplayEntityManager.INSTANCE.clearClient();
        ParticleStyleManager.clearAllVisible();
        ClientRenderEntityManager.INSTANCE.clear();
        ClientParticleGroupManager.INSTANCE.clearAllVisible();
        ParticleCompositionManager.INSTANCE.clear();
    }

    @Test
    void resetShouldClearClientSingletonState() {
        DummyDisplay display = new DummyDisplay();
        DummyStyle style = new DummyStyle();
        DummyRenderEntity renderEntity = new DummyRenderEntity();
        DummyParticleGroup group = new DummyParticleGroup();
        TrackingComposition composition = new TrackingComposition(allocateClientLevel());
        DummyEmitter emitter = new DummyEmitter();
        emitter.setUuid(UUID.randomUUID());
        ClientLevel clientWorld = allocateClientLevel();

        DisplayEntityManager.INSTANCE.addClient(display);
        ParticleStyleManager.getClientViewStyles().put(style.getUuid(), style);
        ClientRenderEntityManager.INSTANCE.add(renderEntity);
        ClientParticleGroupManager.INSTANCE.addVisibleGroup(group);
        ParticleCompositionManager.INSTANCE.addClient(composition);
        ParticleEmittersManager.createOrChangeClient(emitter, clientWorld);

        assertFalse(emitter.getCanceled());
        assertFalse(composition.handle.removed);

        ClientWorldStateReset.reset();

        assertTrue(ParticleEmittersManager.getClientEmitters().isEmpty());
        assertTrue(emitter.getCanceled());
        assertTrue(DisplayEntityManager.INSTANCE.getClientView().isEmpty());
        assertTrue(ParticleStyleManager.getClientViewStyles().isEmpty());
        assertTrue(ClientRenderEntityManager.INSTANCE.getEntities().isEmpty());
        assertNull(ClientParticleGroupManager.INSTANCE.getControlGroup(group.getUuid()));
        assertTrue(ParticleCompositionManager.INSTANCE.getClientView().isEmpty());
        assertTrue(composition.handle.removed);
    }

    private static ClientLevel allocateClientLevel() {
        ClientLevel clientLevel = UnsafeAllocator.allocate(ClientLevel.class);
        setBooleanField(Level.class, clientLevel, "isClientSide", true);
        return clientLevel;
    }

    private static void setBooleanField(Class<?> owner, Object target, String fieldName, boolean value) {
        try {
            Field field = owner.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setBoolean(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to set field " + fieldName, e);
        }
    }

    private static final class DummyDisplay extends DisplayEntity {
        @Override
        public String typeId() {
            return "test:display";
        }
    }

    private static final class DummyStyle extends ParticleGroupStyle {
        @Override
        public Map<StyleData, RelativeLocation> getCurrentFrames() {
            return Map.of();
        }

        @Override
        public void onDisplay() {
        }
    }

    private static final class DummyRenderEntity extends RenderEntity {
        @Override
        public void clientTick() {
        }

        @Override
        public void serverTick() {
        }

        @Override
        public ResourceLocation getRenderID() {
            return new ResourceLocation("test", "render");
        }
    }

    private static final class DummyParticleGroup extends ControllableParticleGroup {
        DummyParticleGroup() {
            super(UUID.randomUUID());
        }

        @Override
        public Map<ParticleRelativeData, RelativeLocation> loadParticleLocations() {
            return Map.of();
        }

        @Override
        public void onGroupDisplay() {
        }
    }

    private static final class TrackingComposition extends ParticleComposition {
        private final TrackingControllable handle = new TrackingControllable();
        private final CompositionData data = new CompositionData()
                .setDisplayerBuilder(() -> (loc, world) -> handle);

        TrackingComposition(Level world) {
            super(Vec3.ZERO, world);
        }

        @Override
        public Map<CompositionData, RelativeLocation> getParticles() {
            return Map.of(data, new RelativeLocation(0.0, 0.0, 0.0));
        }

        @Override
        public void onDisplay() {
        }
    }

    private static final class TrackingControllable implements Controllable<TrackingControllable> {
        private final UUID uuid = UUID.randomUUID();
        private boolean removed;

        @Override
        public UUID controlUUID() {
            return uuid;
        }

        @Override
        public void rotateToPoint(RelativeLocation to) {
        }

        @Override
        public void rotateToWithAngle(RelativeLocation to, double radian) {
        }

        @Override
        public void rotateAsAxis(double radian) {
        }

        @Override
        public void teleportTo(Vec3 pos) {
        }

        @Override
        public void teleportTo(double x, double y, double z) {
        }

        @Override
        public void remove() {
            removed = true;
        }

        @Override
        public TrackingControllable getControlObject() {
            return this;
        }
    }

    private static final class DummyEmitter extends ParticleEmitters {
    }
}
