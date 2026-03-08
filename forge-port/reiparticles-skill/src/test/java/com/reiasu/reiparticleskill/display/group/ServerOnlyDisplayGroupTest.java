// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticleskill.display.group;

import com.reiasu.reiparticlesapi.display.DisplayEntity;
import com.reiasu.reiparticlesapi.display.DisplayEntityManager;
import com.reiasu.reiparticlesapi.network.particle.composition.CompositionData;
import com.reiasu.reiparticlesapi.network.particle.composition.ParticleComposition;
import com.reiasu.reiparticlesapi.network.particle.composition.manager.ParticleCompositionManager;
import com.reiasu.reiparticlesapi.network.particle.style.ParticleGroupStyle;
import com.reiasu.reiparticlesapi.utils.RelativeLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerOnlyDisplayGroupTest {
    @AfterEach
    void cleanup() {
        ServerDisplayGroupManager.INSTANCE.clear();
        ParticleCompositionManager.INSTANCE.clear();
        DisplayEntityManager.INSTANCE.clear();
    }

    @Test
    void shouldSpawnCompositionAndDisplayEntityAtGroupOffsets() {
        TrackingComposition composition = new TrackingComposition();
        TrackingDisplayEntity displayEntity = new TrackingDisplayEntity();
        TestGroup group = new TestGroup(
                new Vec3(2.0, 3.0, 4.0),
                Map.of(
                        supplierOf(composition), new RelativeLocation(1.0, 0.0, 0.0),
                        supplierOf(displayEntity), new RelativeLocation(0.0, 2.0, 0.0)
                )
        );

        group.display();

        assertTrue(group.getStart());
        assertEquals(new Vec3(3.0, 3.0, 4.0), composition.getPosition());
        assertEquals(new Vec3(2.0, 5.0, 4.0), displayEntity.getPos());
        assertSame(composition, ParticleCompositionManager.INSTANCE.getServerView().get(composition.getControlUUID()));
        assertSame(displayEntity, DisplayEntityManager.INSTANCE.getServerView().get(displayEntity.getControlUUID()));
    }

    @Test
    void shouldMoveAndClearManagedControls() {
        TrackingComposition composition = new TrackingComposition();
        TrackingDisplayEntity displayEntity = new TrackingDisplayEntity();
        TrackingStyle style = new TrackingStyle();
        TestGroup group = new TestGroup(
                Vec3.ZERO,
                Map.of(
                        supplierOf(composition), new RelativeLocation(1.0, 0.0, 0.0),
                        supplierOf(displayEntity), new RelativeLocation(0.0, 1.0, 0.0),
                        supplierOf(style), new RelativeLocation(0.0, 0.0, 1.0)
                )
        );

        group.display();
        group.teleportTo(new Vec3(5.0, 6.0, 7.0));
        group.clear();

        assertEquals(new Vec3(6.0, 6.0, 7.0), composition.getPosition());
        assertEquals(new Vec3(5.0, 7.0, 7.0), displayEntity.getPos());
        assertEquals(new Vec3(5.0, 6.0, 8.0), style.getPos());
        assertTrue(composition.getCanceled());
        assertTrue(displayEntity.getCanceled());
        assertTrue(style.getCanceled());
    }

    private static <T> Supplier<Object> supplierOf(T value) {
        return () -> value;
    }

    private static final class TestGroup extends ServerOnlyDisplayGroup {
        private final Map<Supplier<Object>, RelativeLocation> displayers;

        private TestGroup(Vec3 pos, Map<Supplier<Object>, RelativeLocation> displayers) {
            super(pos, null);
            this.displayers = new LinkedHashMap<>(displayers);
        }

        @Override
        public Map<Supplier<Object>, RelativeLocation> getDisplayers() {
            return displayers;
        }

        @Override
        public void tick() {
        }

        @Override
        public void onDisplay() {
        }
    }

    private static final class TrackingComposition extends ParticleComposition {
        @Override
        public Map<CompositionData, RelativeLocation> getParticles() {
            return Map.of();
        }

        @Override
        public void onDisplay() {
        }
    }

    private static final class TrackingStyle extends ParticleGroupStyle {
        @Override
        public Map<StyleData, RelativeLocation> getCurrentFrames() {
            return Map.of();
        }

        @Override
        public void onDisplay() {
        }
    }

    private static final class TrackingDisplayEntity extends DisplayEntity {
    }
}