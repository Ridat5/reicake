package com.reiasu.reiparticlesapi.network.buffer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.nio.ByteBuffer;

public final class Vec3dControlerBuffer extends AbstractControlerBuffer<Vec3> {
    public static final ParticleControlerDataBuffer.Id ID =
            new ParticleControlerDataBuffer.Id(new ResourceLocation("reiparticlesapi", "vec3d"));

    @Override
    public byte[] encode(Vec3 value) {
        Vec3 safe = value == null ? Vec3.ZERO : value;
        return ByteBuffer.allocate(24)
                .putDouble(safe.x)
                .putDouble(safe.y)
                .putDouble(safe.z)
                .array();
    }

    @Override
    public Vec3 decode(byte[] buf) {
        if (buf.length < 24) {
            return Vec3.ZERO;
        }
        ByteBuffer buffer = ByteBuffer.wrap(buf);
        return new Vec3(buffer.getDouble(), buffer.getDouble(), buffer.getDouble());
    }

    @Override
    public ParticleControlerDataBuffer.Id getBufferID() {
        return ID;
    }
}

