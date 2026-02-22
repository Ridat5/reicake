package com.reiasu.reiparticlesapi.network.buffer;

import com.reiasu.reiparticlesapi.utils.RelativeLocation;
import net.minecraft.resources.ResourceLocation;

import java.nio.ByteBuffer;

public final class RelativeLocationControlerBuffer extends AbstractControlerBuffer<RelativeLocation> {
    public static final ParticleControlerDataBuffer.Id ID =
            new ParticleControlerDataBuffer.Id(new ResourceLocation("reiparticlesapi", "relative_location"));

    @Override
    public byte[] encode(RelativeLocation value) {
        RelativeLocation safe = value == null ? new RelativeLocation() : value;
        return ByteBuffer.allocate(24)
                .putDouble(safe.getX())
                .putDouble(safe.getY())
                .putDouble(safe.getZ())
                .array();
    }

    @Override
    public RelativeLocation decode(byte[] buf) {
        if (buf.length < 24) {
            return new RelativeLocation();
        }
        ByteBuffer buffer = ByteBuffer.wrap(buf);
        return new RelativeLocation(buffer.getDouble(), buffer.getDouble(), buffer.getDouble());
    }

    @Override
    public ParticleControlerDataBuffer.Id getBufferID() {
        return ID;
    }
}

