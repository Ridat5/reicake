package com.reiasu.reiparticlesapi.network.buffer;

import net.minecraft.resources.ResourceLocation;

import java.nio.ByteBuffer;

public final class LongArrayControlerBuffer extends AbstractControlerBuffer<long[]> {
    public static final ParticleControlerDataBuffer.Id ID =
            new ParticleControlerDataBuffer.Id(new ResourceLocation("reiparticlesapi", "long_array"));

    @Override
    public byte[] encode(long[] value) {
        long[] safe = value == null ? new long[0] : value;
        ByteBuffer buffer = ByteBuffer.allocate(4 + safe.length * 8);
        buffer.putInt(safe.length);
        for (long i : safe) {
            buffer.putLong(i);
        }
        return buffer.array();
    }

    @Override
    public long[] decode(byte[] buf) {
        if (buf.length < 4) {
            return new long[0];
        }
        ByteBuffer buffer = ByteBuffer.wrap(buf);
        int len = Math.max(0, buffer.getInt());
        long[] out = new long[Math.min(len, (buf.length - 4) / 8)];
        for (int i = 0; i < out.length; i++) {
            out[i] = buffer.getLong();
        }
        return out;
    }

    @Override
    public ParticleControlerDataBuffer.Id getBufferID() {
        return ID;
    }
}

