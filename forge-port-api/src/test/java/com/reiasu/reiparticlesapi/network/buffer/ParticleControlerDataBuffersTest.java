package com.reiasu.reiparticlesapi.network.buffer;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ParticleControlerDataBuffersTest {
    @Test
    void shouldRoundTripBufferWithEnvelopeEncoding() {
        StringControlerBuffer source = ParticleControlerDataBuffers.INSTANCE.string("forge-runtime");
        byte[] encoded = ParticleControlerDataBuffers.INSTANCE.encode(source);
        ParticleControlerDataBuffer<?> decoded = ParticleControlerDataBuffers.INSTANCE.decodeToBuffer(encoded);

        assertInstanceOf(StringControlerBuffer.class, decoded);
        assertEquals("forge-runtime", decoded.getLoadedValue());
    }

    @Test
    void shouldDecodeByBufferId() {
        UUID uuid = UUID.randomUUID();
        UUIDControlerBuffer source = ParticleControlerDataBuffers.INSTANCE.uuid(uuid);

        ParticleControlerDataBuffer<?> decoded =
                ParticleControlerDataBuffers.INSTANCE.withIdDecode(UUIDControlerBuffer.ID, source.encode());

        assertNotNull(decoded);
        assertEquals(uuid, decoded.getLoadedValue());
    }

    @Test
    void shouldResolveWrapperClassToPrimitiveRegistration() {
        ParticleControlerDataBuffer<?> buffer = ParticleControlerDataBuffers.INSTANCE.fromBufferType(42, Integer.class);

        assertNotNull(buffer);
        assertInstanceOf(IntControlerBuffer.class, buffer);
        assertEquals(42, buffer.getLoadedValue());
    }

    @Test
    void shouldRoundTripPrimitiveArrays() {
        int[] input = new int[]{2, 4, 6, 8};
        IntArrayControlerBuffer source = ParticleControlerDataBuffers.INSTANCE.intArray(input);

        ParticleControlerDataBuffer<?> decoded =
                ParticleControlerDataBuffers.INSTANCE.withIdDecode(IntArrayControlerBuffer.ID, source.encode());

        assertNotNull(decoded);
        assertArrayEquals(input, (int[]) decoded.getLoadedValue());
    }
}

