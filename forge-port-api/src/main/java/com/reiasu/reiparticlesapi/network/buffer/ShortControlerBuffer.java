package com.reiasu.reiparticlesapi.network.buffer;

import net.minecraft.resources.ResourceLocation;

import java.nio.ByteBuffer;

public final class ShortControlerBuffer extends AbstractControlerBuffer<Short> {
    public static final ParticleControlerDataBuffer.Id ID =
            new ParticleControlerDataBuffer.Id(new ResourceLocation("reiparticlesapi", "short"));

    @Override
    public byte[] encode(Short value) {
        return ByteBuffer.allocate(2).putShort(value == null ? 0 : value).array();
    }

    @Override
    public Short decode(byte[] buf) {
        if (buf.length < 2) {
            return 0;
        }
        return ByteBuffer.wrap(buf).getShort();
    }

    @Override
    public ParticleControlerDataBuffer.Id getBufferID() {
        return ID;
    }
}

