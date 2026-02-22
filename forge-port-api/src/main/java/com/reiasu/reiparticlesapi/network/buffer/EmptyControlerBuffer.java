package com.reiasu.reiparticlesapi.network.buffer;

import net.minecraft.resources.ResourceLocation;

public final class EmptyControlerBuffer extends AbstractControlerBuffer<Void> {
    public static final ParticleControlerDataBuffer.Id ID =
            new ParticleControlerDataBuffer.Id(new ResourceLocation("reiparticlesapi", "empty"));

    @Override
    public byte[] encode(Void value) {
        return new byte[0];
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    @Override
    public Void decode(byte[] buf) {
        return null;
    }

    @Override
    public ParticleControlerDataBuffer.Id getBufferID() {
        return ID;
    }
}

