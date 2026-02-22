package com.reiasu.reiparticlesapi.network.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CameraShakeS2CPacketTest {
    @Test
    void shouldEncodeAndDecodeWithoutLosingData() {
        CameraShakeS2CPacket input = new CameraShakeS2CPacket(12.5, new Vec3(1.25, 80.0, -4.5), 0.85, 40);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        CameraShakeS2CPacket.encode(input, buf);
        CameraShakeS2CPacket output = CameraShakeS2CPacket.decode(buf);

        assertEquals(input.range(), output.range());
        assertEquals(input.origin().x, output.origin().x);
        assertEquals(input.origin().y, output.origin().y);
        assertEquals(input.origin().z, output.origin().z);
        assertEquals(input.amplitude(), output.amplitude());
        assertEquals(input.tick(), output.tick());
    }
}
