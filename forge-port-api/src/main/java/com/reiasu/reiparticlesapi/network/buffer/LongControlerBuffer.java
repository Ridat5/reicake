package com.reiasu.reiparticlesapi.network.buffer;

import net.minecraft.resources.ResourceLocation;

import java.nio.ByteBuffer;

public final class LongControlerBuffer extends AbstractControlerBuffer<Long> {
    public static final ParticleControlerDataBuffer.Id ID =
            new ParticleControlerDataBuffer.Id(new ResourceLocation("reiparticlesapi", "long"));

    @Override
    public byte[] encode(Long value) {
        return ByteBuffer.allocate(8).putLong(value == null ? 0L : value).array();
    }

    @Override
    public Long decode(byte[] buf) {
        if (buf.length < 8) {
            return 0L;
        }
        return ByteBuffer.wrap(buf).getLong();
    }

    @Override
    public ParticleControlerDataBuffer.Id getBufferID() {
        return ID;
    }
}

