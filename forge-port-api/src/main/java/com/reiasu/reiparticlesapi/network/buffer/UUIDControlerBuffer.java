package com.reiasu.reiparticlesapi.network.buffer;

import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class UUIDControlerBuffer extends AbstractControlerBuffer<UUID> {
    public static final ParticleControlerDataBuffer.Id ID =
            new ParticleControlerDataBuffer.Id(new ResourceLocation("reiparticlesapi", "uuid"));

    @Override
    public byte[] encode(UUID value) {
        UUID safe = value == null ? new UUID(0L, 0L) : value;
        return safe.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public UUID decode(byte[] buf) {
        if (buf.length == 0) {
            return new UUID(0L, 0L);
        }
        return UUID.fromString(new String(buf, StandardCharsets.UTF_8));
    }

    @Override
    public ParticleControlerDataBuffer.Id getBufferID() {
        return ID;
    }
}

