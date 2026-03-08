// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network.particle.composition.manager;

import com.reiasu.reiparticlesapi.network.packet.PacketParticleCompositionS2C;
import com.reiasu.reiparticlesapi.network.particle.composition.CompositionData;
import com.reiasu.reiparticlesapi.network.particle.composition.ParticleComposition;
import com.reiasu.reiparticlesapi.particles.Controllable;
import com.reiasu.reiparticlesapi.testutil.UnsafeAllocator;
import com.reiasu.reiparticlesapi.utils.RelativeLocation;
import io.netty.buffer.Unpooled;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        TrackingComposition client = new TrackingComposition(allocateClientLevel());
        ParticleCompositionManager.INSTANCE.spawn(server);
        ParticleCompositionManager.INSTANCE.addClient(client);

        assertFalse(client.handle.removed);

        ParticleCompositionManager.INSTANCE.clearClient();

        assertEquals(1, ParticleCompositionManager.INSTANCE.activeCount());
        assertTrue(ParticleCompositionManager.INSTANCE.getClientView().isEmpty());
        assertSame(server, ParticleCompositionManager.INSTANCE.getServerView().get(server.getControlUUID()));
        assertTrue(client.handle.removed);
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

    private static final class TrackingComposition extends ParticleComposition {
        private final TrackingControllable handle = new TrackingControllable();
        private final CompositionData data = new CompositionData()
                .setDisplayerBuilder(() -> (loc, world) -> handle);

        private TrackingComposition(Level world) {
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
