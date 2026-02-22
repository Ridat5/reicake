package com.reiasu.reiparticlesapi.network.particle.emitters;

import com.reiasu.reiparticlesapi.network.particle.ServerControler;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class ParticleEmitters implements ServerControler<ParticleEmitters> {
    private final String emittersID = getClass().getName();
    private UUID uuid = UUID.randomUUID();
    private boolean canceled;
    private int maxTick = 1;
    private int tick;
    private final List<Runnable> tickHandlers = new ArrayList<>();
    private Level level;
    private Vec3 position = Vec3.ZERO;

    public String getEmittersID() {
        return emittersID;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getMaxTick() {
        return maxTick;
    }

    public void setMaxTick(int maxTick) {
        this.maxTick = maxTick;
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = Math.max(0, tick);
    }

    public ParticleEmitters bind(Level level, double x, double y, double z) {
        this.level = level;
        this.position = new Vec3(x, y, z);
        return this;
    }

    public Level level() {
        return level;
    }

    public Vec3 position() {
        return position;
    }

    public void teleportTo(Vec3 pos) {
        this.position = pos;
    }

    public ParticleEmitters addTickHandler(Runnable handler) {
        if (handler != null) {
            tickHandlers.add(handler);
        }
        return this;
    }

    public void update(ParticleEmitters emitter) {
        this.canceled = emitter.canceled;
        this.maxTick = emitter.maxTick;
        this.tick = emitter.tick;
        this.level = emitter.level;
        this.position = emitter.position;
    }

    protected void writePayload(FriendlyByteBuf buf) {
    }

    protected void readPayload(FriendlyByteBuf buf) {
    }

    public byte[] encodeToBytes() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUUID(uuid);
        buf.writeInt(maxTick);
        buf.writeInt(tick);
        buf.writeBoolean(canceled);
        buf.writeDouble(position.x);
        buf.writeDouble(position.y);
        buf.writeDouble(position.z);
        writePayload(buf);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }

    public void decodeFromBuffer(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.maxTick = buf.readInt();
        this.tick = buf.readInt();
        this.canceled = buf.readBoolean();
        this.position = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        readPayload(buf);
    }

    protected void emitTick() {
    }

    @Override
    public void tick() {
        if (canceled) {
            return;
        }
        emitTick();
        for (Runnable tickHandler : tickHandlers) {
            tickHandler.run();
        }
        tick++;
        if (maxTick > 0 && tick >= maxTick) {
            canceled = true;
        }
    }

    @Override
    public boolean getCanceled() {
        return canceled;
    }

    @Override
    public void cancel() {
        canceled = true;
    }
}
