package com.reiasu.reiparticlesapi.network.buffer;

import net.minecraft.resources.ResourceLocation;

import java.nio.ByteBuffer;

public final class IntControlerBuffer extends AbstractControlerBuffer<Integer> {
    public static final ParticleControlerDataBuffer.Id ID =
            new ParticleControlerDataBuffer.Id(new ResourceLocation("reiparticlesapi", "int"));

    @Override
    public byte[] encode(Integer value) {
        return ByteBuffer.allocate(4).putInt(value == null ? 0 : value).array();
    }

    @Override
    public Integer decode(byte[] buf) {
        if (buf.length < 4) {
            return 0;
        }
        return ByteBuffer.wrap(buf).getInt();
    }

    @Override
    public ParticleControlerDataBuffer.Id getBufferID() {
        return ID;
    }
}

