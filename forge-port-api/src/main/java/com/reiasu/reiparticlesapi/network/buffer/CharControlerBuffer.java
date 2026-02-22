package com.reiasu.reiparticlesapi.network.buffer;

import net.minecraft.resources.ResourceLocation;

import java.nio.ByteBuffer;

/**
 * Buffer for single char (2-byte) values.
 */
public final class CharControlerBuffer extends AbstractControlerBuffer<Character> {
    public static final ParticleControlerDataBuffer.Id ID =
            new ParticleControlerDataBuffer.Id(new ResourceLocation("reiparticlesapi", "char"));

    @Override
    public byte[] encode(Character value) {
        return ByteBuffer.allocate(2).putChar(value == null ? 0 : value).array();
    }

    @Override
    public Character decode(byte[] buf) {
        if (buf.length < 2) {
            return (char) 0;
        }
        return ByteBuffer.wrap(buf).getChar();
    }

    @Override
    public ParticleControlerDataBuffer.Id getBufferID() {
        return ID;
    }
}
