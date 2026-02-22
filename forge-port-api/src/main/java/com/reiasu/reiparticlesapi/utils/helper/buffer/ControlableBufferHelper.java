package com.reiasu.reiparticlesapi.utils.helper.buffer;

import com.reiasu.reiparticlesapi.network.buffer.ParticleControlerDataBuffer;
import com.reiasu.reiparticlesapi.network.buffer.ParticleControlerDataBuffers;
import com.reiasu.reiparticlesapi.particles.Controlable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reflection-based helper that reads/writes fields annotated with
 * {@link ControlableBuffer} on a {@link Controlable} as buffer pairs.
 */
public final class ControlableBufferHelper {
    public static final ControlableBufferHelper INSTANCE = new ControlableBufferHelper();
    private static final Logger LOGGER = LoggerFactory.getLogger("ReiParticlesAPI");

    private ControlableBufferHelper() {
    }

    /**
     * Collect all {@link ControlableBuffer}-annotated fields from the given
     * {@link Controlable} and convert their values into buffer entries.
     */
    public Map<String, ParticleControlerDataBuffer<?>> getPairs(Controlable<?> buf) {
        Map<String, ParticleControlerDataBuffer<?>> res = new LinkedHashMap<>();
        List<Field> annotatedFields = getAnnotatedFields(buf.getClass());
        for (Field field : annotatedFields) {
            field.setAccessible(true);
            ControlableBuffer anno = field.getAnnotation(ControlableBuffer.class);
            if (anno == null) {
                continue;
            }
            try {
                Object value = field.get(buf);
                if (value == null) {
                    continue;
                }
                ParticleControlerDataBuffer<?> buffer =
                        ParticleControlerDataBuffers.INSTANCE.fromBufferType(value, value.getClass());
                if (buffer != null) {
                    res.put(anno.name(), buffer);
                }
            } catch (IllegalAccessException e) {
                LOGGER.warn("Failed to read field {} on {}", field.getName(), buf.getClass().getSimpleName(), e);
            }
        }
        return res;
    }

    /**
     * Write buffer values back into the {@link ControlableBuffer}-annotated fields
     * of the given {@link Controlable}.
     */
    public void setPairs(Controlable<?> buf, Map<String, ? extends ParticleControlerDataBuffer<?>> args) {
        List<Field> annotatedFields = getAnnotatedFields(buf.getClass());
        for (Field field : annotatedFields) {
            field.setAccessible(true);
            ControlableBuffer anno = field.getAnnotation(ControlableBuffer.class);
            if (anno == null) {
                continue;
            }
            ParticleControlerDataBuffer<?> value = args.get(anno.name());
            if (value == null) {
                continue;
            }
            if (Modifier.isFinal(field.getModifiers())) {
                LOGGER.warn("Cannot set final field {} to {}", field.getName(), value);
                continue;
            }
            try {
                field.set(buf, value.getLoadedValue());
            } catch (IllegalAccessException e) {
                LOGGER.warn("Failed to set field {} on {}", field.getName(), buf.getClass().getSimpleName(), e);
            }
        }
    }

    private List<Field> getAnnotatedFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        List<Field> result = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ControlableBuffer.class)) {
                result.add(field);
            }
        }
        return result;
    }
}
