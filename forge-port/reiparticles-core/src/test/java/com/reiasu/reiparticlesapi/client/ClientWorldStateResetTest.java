// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.client;

import com.reiasu.reiparticlesapi.display.DisplayEntity;
import com.reiasu.reiparticlesapi.display.DisplayEntityManager;
import com.reiasu.reiparticlesapi.network.particle.composition.CompositionData;
import com.reiasu.reiparticlesapi.network.particle.composition.ParticleComposition;
import com.reiasu.reiparticlesapi.network.particle.composition.manager.ParticleCompositionManager;
import com.reiasu.reiparticlesapi.network.particle.style.ParticleGroupStyle;
import com.reiasu.reiparticlesapi.network.particle.style.ParticleStyleManager;
import com.reiasu.reiparticlesapi.particles.control.group.ClientParticleGroupManager;
import com.reiasu.reiparticlesapi.particles.control.group.ControllableParticleGroup;
import com.reiasu.reiparticlesapi.renderer.RenderEntity;
import com.reiasu.reiparticlesapi.renderer.client.ClientRenderEntityManager;
import com.reiasu.reiparticlesapi.utils.RelativeLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientWorldStateResetTest {
    @AfterEach
    void cleanup() {
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
        DummyComposition composition = new DummyComposition(Vec3.ZERO, null);

        DisplayEntityManager.INSTANCE.addClient(display);
        ParticleStyleManager.getClientViewStyles().put(style.getUuid(), style);
        ClientRenderEntityManager.INSTANCE.add(renderEntity);
        ClientParticleGroupManager.INSTANCE.addVisibleGroup(group);
        ParticleCompositionManager.INSTANCE.addClient(composition);

        ClientWorldStateReset.reset();

        assertTrue(DisplayEntityManager.INSTANCE.getClientView().isEmpty());
        assertTrue(ParticleStyleManager.getClientViewStyles().isEmpty());
        assertTrue(ClientRenderEntityManager.INSTANCE.getEntities().isEmpty());
        assertNull(ClientParticleGroupManager.INSTANCE.getControlGroup(group.getUuid()));
        assertTrue(ParticleCompositionManager.INSTANCE.getClientView().isEmpty());
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

    private static final class DummyComposition extends ParticleComposition {
        DummyComposition(Vec3 position, Level world) {
            super(position, world);
        }

        @Override
        public Map<CompositionData, RelativeLocation> getParticles() {
            return Map.of();
        }

        @Override
        public void onDisplay() {
        }
    }
}
