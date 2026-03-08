// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network.particle.composition.manager;

import com.reiasu.reiparticlesapi.network.packet.PacketParticleCompositionS2C;
import com.reiasu.reiparticlesapi.network.particle.composition.CompositionData;
import com.reiasu.reiparticlesapi.network.particle.composition.ParticleComposition;
import com.reiasu.reiparticlesapi.utils.RelativeLocation;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParticleCompositionManagerTest {
    @AfterEach
    void cleanup() {
        ParticleCompositionManager.INSTANCE.clear();
    }

    @Test
    void shouldContinueServerTickAfterCompositionFailure() {
        FailingComposition failing = new FailingComposition();
        CountingComposition healthy = new CountingComposition();
        ParticleCompositionManager.INSTANCE.spawn(failing);
        ParticleCompositionManager.INSTANCE.spawn(healthy);

        ParticleCompositionManager.INSTANCE.tickAll();

        assertEquals(1, healthy.ticks);
        assertEquals(1, ParticleCompositionManager.INSTANCE.activeCount());
        assertSame(healthy, ParticleCompositionManager.INSTANCE.getCompositions().get(0));
    }

    @Test
    void shouldContinueClientTickAfterCompositionFailure() {
        FailingComposition failing = new FailingComposition();
        CountingComposition healthy = new CountingComposition();
        ParticleCompositionManager.INSTANCE.getClientView().put(failing.getControlUUID(), failing);
        ParticleCompositionManager.INSTANCE.getClientView().put(healthy.getControlUUID(), healthy);

        ParticleCompositionManager.INSTANCE.tickClient();

        assertEquals(1, healthy.ticks);
        assertEquals(1, ParticleCompositionManager.INSTANCE.getClientView().size());
        assertSame(healthy, ParticleCompositionManager.INSTANCE.getClientView().get(healthy.getControlUUID()));
    }

    @Test
    void shouldBuildPacketsForAutoRegisteredCompositionTypes() {
        ParticleCompositionManager.INSTANCE.registerAutoType(CodecComposition.class);

        CodecComposition composition = new CodecComposition(new Vec3(1.0, 2.0, 3.0), null);
        UUID uuid = UUID.randomUUID();
        composition.setControlUUID(uuid);
        composition.setVisibleRange(42.0);

        PacketParticleCompositionS2C packet = ParticleCompositionManager.INSTANCE.buildPacket(composition, false);

        assertNotNull(packet);
        assertEquals(CodecComposition.class.getName(), packet.getType());
        ParticleComposition decoded = ParticleCompositionManager.INSTANCE.getRegisteredTypes()
                .get(packet.getType())
                .apply(new FriendlyByteBuf(Unpooled.wrappedBuffer(packet.getData())));
        assertInstanceOf(CodecComposition.class, decoded);
        assertEquals(uuid, decoded.getControlUUID());
        assertEquals(composition.getPosition(), decoded.getPosition());
        assertEquals(42.0, decoded.getVisibleRange());
    }

    @Test
    void clearClientShouldPreserveServerState() {
        CountingComposition server = new CountingComposition();
        CountingComposition client = new CountingComposition();
        ParticleCompositionManager.INSTANCE.spawn(server);
        ParticleCompositionManager.INSTANCE.addClient(client);

        ParticleCompositionManager.INSTANCE.clearClient();

        assertEquals(1, ParticleCompositionManager.INSTANCE.activeCount());
        assertTrue(ParticleCompositionManager.INSTANCE.getClientView().isEmpty());
        assertSame(server, ParticleCompositionManager.INSTANCE.getServerView().get(server.getControlUUID()));
    }

    private static final class CountingComposition extends ParticleComposition {
        private int ticks;

        @Override
        public Map<CompositionData, RelativeLocation> getParticles() {
            return Map.of();
        }

        @Override
        public void onDisplay() {
        }

        @Override
        public void tick() {
            ticks++;
        }
    }

    private static final class FailingComposition extends ParticleComposition {
        @Override
        public Map<CompositionData, RelativeLocation> getParticles() {
            return Map.of();
        }

        @Override
        public void onDisplay() {
        }

        @Override
        public void tick() {
            throw new IllegalStateException("boom");
        }
    }

    public static final class CodecComposition extends ParticleComposition {
        public CodecComposition(Vec3 position, Level world) {
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
