// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.display;

import com.reiasu.reiparticlesapi.network.particle.ServerController;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Base class for display entities (non-particle visual elements managed
 * by the ReiParticles system).
 * <p>
 * Carries position, rotation (yaw/pitch/roll), scale, and validity state.
 * Provides static {@link #encodeBase}/{@link #decodeBase} for network
 * serialization of the base fields.
 */
public abstract class DisplayEntity implements ServerController<DisplayEntity> {

    // ─── Static encode/decode ────────────────────────────────────────────

    public static void encodeBase(DisplayEntity data, FriendlyByteBuf buf) {
        buf.writeDouble(data.pos.x());
        buf.writeDouble(data.pos.y());
        buf.writeDouble(data.pos.z());
        buf.writeFloat(data.yaw);
        buf.writeFloat(data.pitch);
        buf.writeFloat(data.roll);
        buf.writeFloat(data.scale);
        buf.writeBoolean(data.valid);
        buf.writeUUID(data.controlUUID);
    }

    public static void decodeBase(DisplayEntity instance, FriendlyByteBuf buf) {
        instance.pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        instance.yaw = buf.readFloat();
        instance.pitch = buf.readFloat();
        instance.roll = buf.readFloat();
        instance.scale = buf.readFloat();
        instance.valid = buf.readBoolean();
        instance.controlUUID = buf.readUUID();
    }

    // ─── Fields ──────────────────────────────────────────────────────────

    private UUID controlUUID = UUID.randomUUID();
    private boolean valid = true;
    private ServerLevel level;
    private Vec3 pos = Vec3.ZERO;
    private float yaw;
    private float pitch;
    private float roll;
    private float scale = 1.0f;

    @Override
    public void spawnInWorld(ServerLevel world, Vec3 pos) {
        DisplayEntityManager.INSTANCE.spawn(this);
    }

    // ─── Accessors ───────────────────────────────────────────────────────

    public UUID getControlUUID() {
        return controlUUID;
    }

    public void setControlUUID(UUID controlUUID) {
        this.controlUUID = controlUUID;
    }

    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Vec3 getPos() {
        return pos;
    }

    public void setPos(Vec3 pos) {
        this.pos = pos;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    // ─── Level binding ───────────────────────────────────────────────────

    public DisplayEntity bindLevel(ServerLevel level) {
        this.level = level;
        return this;
    }

    protected ServerLevel level() {
        return level;
    }

    // ─── Lifecycle ───────────────────────────────────────────────────────

    public void update(DisplayEntity other) {
        this.valid = other.valid;
        this.pos = other.pos;
        this.yaw = other.yaw;
        this.pitch = other.pitch;
        this.roll = other.roll;
        this.scale = other.scale;
    }

    @Override
    public boolean getCanceled() {
        return !valid;
    }

    @Override
    public void cancel() {
        this.valid = false;
    }
}
