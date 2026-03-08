// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network.packet.client.listener;

import com.reiasu.reiparticlesapi.network.buffer.ParticleControllerDataBuffer;
import com.reiasu.reiparticlesapi.network.buffer.ParticleControllerDataBuffers;
import com.reiasu.reiparticlesapi.network.particle.style.ParticleGroupStyle;
import com.reiasu.reiparticlesapi.network.particle.style.ParticleStyleManager;
import com.reiasu.reiparticlesapi.network.particle.style.ParticleStyleProvider;
import com.reiasu.reiparticlesapi.testutil.RecordingLogger;
import com.reiasu.reiparticlesapi.utils.RelativeLocation;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientParticleStylePacketHandlerTest {
    @AfterEach
    void cleanup() {
        ParticleStyleManager.getClientViewStyles().clear();
    }

    @Test
    void shouldWarnWhenStyleCreationFails() {
        RecordingLogger recorder = new RecordingLogger();
        ResourceLocation key = new ResourceLocation("reiparticlesruntime", "broken_style_" + System.nanoTime());
        int styleTypeId = ParticleStyleManager.register(key, new ThrowingProvider());
        Map<String, ParticleControllerDataBuffer<?>> args = Map.of(
                "style_type_id", ParticleControllerDataBuffers.INSTANCE.intValue(styleTypeId)
        );

        ClientParticleStylePacketHandler.handleCreate(UUID.randomUUID(), args, recorder.logger());

        assertTrue(recorder.hasEvent("warn", "Failed to spawn particle style"));
    }

    @Test
    void shouldPreferRegistryKeyOverRawTypeIdWhenResolvingStyleProvider() {
        ResourceLocation firstKey = new ResourceLocation("reiparticlesruntime", "ordered_style_a_" + System.nanoTime());
        ResourceLocation secondKey = new ResourceLocation("reiparticlesruntime", "ordered_style_b_" + System.nanoTime());
        AtomicInteger firstHits = new AtomicInteger();
        AtomicInteger secondHits = new AtomicInteger();
        int firstId = ParticleStyleManager.register(firstKey, new CountingProvider(firstHits));
        ParticleStyleManager.register(secondKey, new CountingProvider(secondHits));

        Map<String, ParticleControllerDataBuffer<?>> args = Map.of(
                "style_type_id", ParticleControllerDataBuffers.INSTANCE.intValue(firstId),
                "style_registry_key", ParticleControllerDataBuffers.INSTANCE.string(secondKey.toString())
        );

        ClientParticleStylePacketHandler.handleCreate(UUID.randomUUID(), args, new RecordingLogger().logger());

        assertEquals(0, firstHits.get());
        assertEquals(1, secondHits.get());
    }

    private static final class ThrowingProvider implements ParticleStyleProvider<ThrowingStyle> {
        @Override
        public ThrowingStyle create() {
            return new ThrowingStyle();
        }

        @Override
        public ThrowingStyle createStyle(UUID uuid, Map<String, ? extends ParticleControllerDataBuffer<?>> args) {
            throw new IllegalStateException("boom");
        }
    }

    private static final class ThrowingStyle extends ParticleGroupStyle {
        @Override
        public Map<StyleData, RelativeLocation> getCurrentFrames() {
            return Map.of();
        }

        @Override
        public void onDisplay() {
        }
    }

    private static final class CountingProvider implements ParticleStyleProvider<CountingStyle> {
        private final AtomicInteger hits;

        private CountingProvider(AtomicInteger hits) {
            this.hits = hits;
        }

        @Override
        public CountingStyle create() {
            return new CountingStyle();
        }

        @Override
        public CountingStyle createStyle(UUID uuid, Map<String, ? extends ParticleControllerDataBuffer<?>> args) {
            hits.incrementAndGet();
            CountingStyle style = new CountingStyle();
            style.setUuid(uuid);
            return style;
        }
    }

    private static final class CountingStyle extends ParticleGroupStyle {
        @Override
        public Map<StyleData, RelativeLocation> getCurrentFrames() {
            return Map.of();
        }

        @Override
        public void onDisplay() {
        }
    }
}