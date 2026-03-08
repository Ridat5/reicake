// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network.packet.client.listener;

import com.mojang.logging.LogUtils;
import com.reiasu.reiparticlesapi.network.buffer.ParticleControllerDataBuffer;
import com.reiasu.reiparticlesapi.network.packet.PacketParticleStyleS2C;
import com.reiasu.reiparticlesapi.network.particle.style.ParticleGroupStyle;
import com.reiasu.reiparticlesapi.network.particle.style.ParticleStyleManager;
import com.reiasu.reiparticlesapi.network.particle.style.ParticleStyleProvider;
import com.reiasu.reiparticlesapi.particles.control.ControlType;
import com.reiasu.reiparticlesapi.utils.RelativeLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;

public final class ClientParticleStylePacketHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ClientParticleStylePacketHandler() {
    }

    public static void receive(PacketParticleStyleS2C packet) {
        UUID uuid = packet.uuid();
        ControlType type = packet.type();
        if (type == ControlType.CREATE) {
            handleCreate(uuid, packet.args(), LOGGER);
            return;
        }
        if (type == ControlType.CHANGE) {
            handleChange(uuid, packet.args());
            return;
        }
        handleRemove(uuid);
    }

    private static void handleRemove(UUID uuid) {
        ParticleGroupStyle style = ParticleStyleManager.getClientViewStyles().remove(uuid);
        if (style != null) {
            style.remove();
        }
    }

    private static void handleChange(UUID uuid, Map<String, ParticleControllerDataBuffer<?>> args) {
        ParticleGroupStyle style = ParticleStyleManager.getClientViewStyles().get(uuid);
        if (style == null) {
            return;
        }
        applyCommonArgs(style, args);
        style.readPacketArgs(args);
    }

    static void handleCreate(UUID uuid, Map<String, ParticleControllerDataBuffer<?>> args, Logger logger) {
        ResourceLocation styleKey = readResourceLocation(args, "style_registry_key");
        Integer styleTypeId = readInt(args, "style_type_id");
        try {
            ParticleStyleProvider<?> provider = resolveProvider(styleKey, styleTypeId);
            if (provider == null) {
                return;
            }
            ParticleGroupStyle style = provider.createStyle(uuid, args);
            if (style == null) {
                return;
            }
            style.setUuid(uuid);
            ResourceLocation resolvedKey = resolveStyleKey(styleKey, styleTypeId);
            if (resolvedKey != null) {
                style.setRegistryKey(resolvedKey);
            }
            applyCommonArgs(style, args);
            style.readPacketArgs(args);

            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) {
                return;
            }
            ParticleStyleManager.spawnStyle(minecraft.level, style.getPos(), style);
        } catch (Exception e) {
            logger.warn("Failed to spawn particle style {} (key {}, type {})", uuid, styleKey, styleTypeId, e);
        }
    }

    static ParticleStyleProvider<?> resolveProvider(ResourceLocation styleKey, Integer styleTypeId) {
        if (styleKey != null) {
            ParticleStyleProvider<?> provider = ParticleStyleManager.getProvider(styleKey);
            if (provider != null) {
                return provider;
            }
        }
        if (styleTypeId == null || styleTypeId < 0) {
            return null;
        }
        return ParticleStyleManager.getProviderByRawID(styleTypeId);
    }

    static ResourceLocation resolveStyleKey(ResourceLocation styleKey, Integer styleTypeId) {
        if (styleKey != null) {
            return styleKey;
        }
        if (styleTypeId == null || styleTypeId < 0) {
            return null;
        }
        return ParticleStyleManager.getStyleKey(styleTypeId);
    }

    private static void applyCommonArgs(ParticleGroupStyle style, Map<String, ? extends ParticleControllerDataBuffer<?>> args) {
        Vec3 pos = readVec3(args, "pos");
        if (pos != null) {
            style.teleportTo(pos);
        }
        Vec3 teleport = readVec3(args, "teleport");
        if (teleport != null) {
            style.teleportTo(teleport);
        }
        Double rotate = readDouble(args, "rotate");
        if (rotate != null) {
            style.setRotate(rotate);
        }
        RelativeLocation rotateTo = readRelative(args, "rotate_to");
        Double rotateAngle = readDouble(args, "rotate_angle");
        if (rotateTo != null && rotateAngle != null) {
            style.rotateToWithAngle(rotateTo, rotateAngle);
        } else {
            if (rotateAngle != null) {
                style.rotateAsAxis(rotateAngle);
            }
            if (rotateTo != null) {
                style.rotateToPoint(rotateTo);
            }
        }
        Vec3 axis = readVec3(args, "axis");
        if (axis != null) {
            style.setAxis(RelativeLocation.of(axis));
        }
        Double scale = readDouble(args, "scale");
        if (scale != null) {
            style.setScale(scale);
        }
        Long lastUpdatedGameTime = readLong(args, "lastUpdatedGameTime");
        if (lastUpdatedGameTime != null) {
            style.setLastUpdatedGameTime(lastUpdatedGameTime);
        }
        Long displayedTime = readLong(args, "displayedTime");
        if (displayedTime != null) {
            style.setDisplayedTime(displayedTime);
        }
        Double visibleRange = readDouble(args, "visibleRange");
        if (visibleRange != null) {
            style.setVisibleRange(visibleRange);
        }
        Boolean autoToggle = readBoolean(args, "autoToggle");
        if (autoToggle != null) {
            style.setAutoToggle(autoToggle);
        }
    }

    private static Vec3 readVec3(Map<String, ? extends ParticleControllerDataBuffer<?>> args, String key) {
        Object value = read(args, key);
        return value instanceof Vec3 vec3 ? vec3 : null;
    }

    private static RelativeLocation readRelative(Map<String, ? extends ParticleControllerDataBuffer<?>> args, String key) {
        Object value = read(args, key);
        if (value instanceof RelativeLocation relativeLocation) {
            return relativeLocation;
        }
        if (value instanceof Vec3 vec3) {
            return RelativeLocation.of(vec3);
        }
        return null;
    }

    private static Integer readInt(Map<String, ? extends ParticleControllerDataBuffer<?>> args, String key) {
        Object value = read(args, key);
        return value instanceof Number number ? number.intValue() : null;
    }

    private static Double readDouble(Map<String, ? extends ParticleControllerDataBuffer<?>> args, String key) {
        Object value = read(args, key);
        return value instanceof Number number ? number.doubleValue() : null;
    }

    private static Long readLong(Map<String, ? extends ParticleControllerDataBuffer<?>> args, String key) {
        Object value = read(args, key);
        return value instanceof Number number ? number.longValue() : null;
    }

    private static Boolean readBoolean(Map<String, ? extends ParticleControllerDataBuffer<?>> args, String key) {
        Object value = read(args, key);
        return value instanceof Boolean b ? b : null;
    }

    private static ResourceLocation readResourceLocation(Map<String, ? extends ParticleControllerDataBuffer<?>> args, String key) {
        Object value = read(args, key);
        if (value instanceof ResourceLocation resourceLocation) {
            return resourceLocation;
        }
        if (value instanceof String path && !path.isBlank()) {
            return ResourceLocation.tryParse(path);
        }
        return null;
    }

    private static Object read(Map<String, ? extends ParticleControllerDataBuffer<?>> args, String key) {
        ParticleControllerDataBuffer<?> buffer = args.get(key);
        return buffer == null ? null : buffer.getLoadedValue();
    }
}