// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network.particle.style;

import com.reiasu.reiparticlesapi.network.buffer.ParticleControllerDataBuffer;
import com.reiasu.reiparticlesapi.network.buffer.ParticleControllerDataBuffers;
import com.reiasu.reiparticlesapi.particles.Controllable;
import com.reiasu.reiparticlesapi.testutil.UnsafeAllocator;
import com.reiasu.reiparticlesapi.utils.RelativeLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParticleGroupStyleTest {
    @Test
    void shouldDisplayOnServerWithoutClientFlush() {
        ServerLevel level = UnsafeAllocator.allocate(ServerLevel.class);
        TrackingStyle style = new TrackingStyle();
        Vec3 pos = new Vec3(2.0, 4.0, 6.0);

        style.display(pos, level);

        assertTrue(style.getDisplayed());
        assertSame(level, style.getWorld());
        assertEquals(pos, style.getPos());
        assertEquals(1, style.onDisplayCalls);
        assertTrue(style.getParticles().isEmpty());
    }

    @Test
    void shouldScaleDisplayedParticlesAndTeleportRelativeToStyleOrigin() {
        TrackingStyle style = new TrackingStyle();
        DummyControllable particle = new DummyControllable();
        RelativeLocation relative = new RelativeLocation(2.0, 0.0, 0.0);
        style.setDisplayed(true);
        style.getParticleLocations().put(particle, relative);
        style.getParticleDefaultLength().put(particle.controlUUID(), relative.length());

        style.scale(2.0);
        style.teleportTo(new Vec3(10.0, 20.0, 30.0));

        assertEquals(4.0, relative.length(), 1.0E-6);
        assertEquals(new Vec3(14.0, 20.0, 30.0), particle.lastTeleport);
    }

    @Test
    void shouldApplyPacketArgsAndPretickActions() {
        TrackingStyle style = new TrackingStyle();
        AtomicInteger preTicks = new AtomicInteger();
        style.addPreTickAction(ignored -> preTicks.incrementAndGet());
        style.clearDirty();

        style.tick();
        style.change(s -> s.setVisibleRange(48.0), Map.of(
                "custom", ParticleControllerDataBuffers.INSTANCE.intValue(9)
        ));

        assertEquals(1, preTicks.get());
        assertEquals(1, style.getDisplayedTime());
        assertEquals(9, style.customValue);
        assertEquals(48.0, style.getVisibleRange());
        assertTrue(style.consumeDirty());
    }

    private static final class TrackingStyle extends ParticleGroupStyle {
        private final Map<StyleData, RelativeLocation> frames = new LinkedHashMap<>();
        private int onDisplayCalls;
        private int customValue;

        @Override
        public Map<StyleData, RelativeLocation> getCurrentFrames() {
            return frames;
        }

        @Override
        public void onDisplay() {
            onDisplayCalls++;
        }

        @Override
        public void readPacketArgs(Map<String, ? extends ParticleControllerDataBuffer<?>> args) {
            ParticleControllerDataBuffer<?> buffer = args.get("custom");
            if (buffer != null && buffer.getLoadedValue() instanceof Number number) {
                customValue = number.intValue();
            }
        }
    }

    private static final class DummyControllable implements Controllable<DummyControllable> {
        private final UUID uuid = UUID.randomUUID();
        private Vec3 lastTeleport = Vec3.ZERO;

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
            lastTeleport = pos;
        }

        @Override
        public void teleportTo(double x, double y, double z) {
            teleportTo(new Vec3(x, y, z));
        }

        @Override
        public void remove() {
        }

        @Override
        public DummyControllable getControlObject() {
            return this;
        }
    }
}