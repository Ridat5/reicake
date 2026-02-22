package com.reiasu.reiparticlesapi.network.buffer;

import net.minecraft.resources.ResourceLocation;

public final class BooleanControlerBuffer extends AbstractControlerBuffer<Boolean> {
    public static final ParticleControlerDataBuffer.Id ID =
            new ParticleControlerDataBuffer.Id(new ResourceLocation("reiparticlesapi", "boolean"));

    @Override
    public byte[] encode(Boolean value) {
        return new byte[]{(byte) (Boolean.TRUE.equals(value) ? 1 : 0)};
    }

    @Override
    public Boolean decode(byte[] buf) {
        return buf.length > 0 && buf[0] != 0;
    }

    @Override
    public ParticleControlerDataBuffer.Id getBufferID() {
        return ID;
    }
}

