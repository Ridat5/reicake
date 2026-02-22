package com.reiasu.reiparticlesapi.network.buffer;

import net.minecraft.resources.ResourceLocation;

import java.nio.ByteBuffer;

public final class DoubleControlerBuffer extends AbstractControlerBuffer<Double> {
    public static final ParticleControlerDataBuffer.Id ID =
            new ParticleControlerDataBuffer.Id(new ResourceLocation("reiparticlesapi", "double"));

    @Override
    public byte[] encode(Double value) {
        return ByteBuffer.allocate(8).putDouble(value == null ? 0.0 : value).array();
    }

    @Override
    public Double decode(byte[] buf) {
        if (buf.length < 8) {
            return 0.0;
        }
        return ByteBuffer.wrap(buf).getDouble();
    }

    @Override
    public ParticleControlerDataBuffer.Id getBufferID() {
        return ID;
    }
}

