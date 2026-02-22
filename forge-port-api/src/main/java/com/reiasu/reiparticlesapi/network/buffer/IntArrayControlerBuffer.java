package com.reiasu.reiparticlesapi.network.buffer;

import net.minecraft.resources.ResourceLocation;

import java.nio.ByteBuffer;

public final class IntArrayControlerBuffer extends AbstractControlerBuffer<int[]> {
    public static final ParticleControlerDataBuffer.Id ID =
            new ParticleControlerDataBuffer.Id(new ResourceLocation("reiparticlesapi", "int_array"));

    @Override
    public byte[] encode(int[] value) {
        int[] safe = value == null ? new int[0] : value;
        ByteBuffer buffer = ByteBuffer.allocate(4 + safe.length * 4);
        buffer.putInt(safe.length);
        for (int i : safe) {
            buffer.putInt(i);
        }
        return buffer.array();
    }

    @Override
    public int[] decode(byte[] buf) {
        if (buf.length < 4) {
            return new int[0];
        }
        ByteBuffer buffer = ByteBuffer.wrap(buf);
        int len = Math.max(0, buffer.getInt());
        int[] out = new int[Math.min(len, (buf.length - 4) / 4)];
        for (int i = 0; i < out.length; i++) {
            out[i] = buffer.getInt();
        }
        return out;
    }

    @Override
    public ParticleControlerDataBuffer.Id getBufferID() {
        return ID;
    }
}

