package com.reiasu.reiparticlesapi.network.buffer;

import com.reiasu.reiparticlesapi.utils.RelativeLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class ParticleControlerDataBuffers {
    public static final ParticleControlerDataBuffers INSTANCE = new ParticleControlerDataBuffers();

    private final Map<Class<?>, Class<?>> wrapperToPrimitive = new ConcurrentHashMap<>();
    private final Map<ParticleControlerDataBuffer.Id, Supplier<? extends ParticleControlerDataBuffer<?>>> registerBuilder =
            new ConcurrentHashMap<>();
    private final Map<Class<?>, ParticleControlerDataBuffer.Id> registerTypes = new ConcurrentHashMap<>();

    private ParticleControlerDataBuffers() {
        wrapperToPrimitive.put(Integer.class, int.class);
        wrapperToPrimitive.put(Double.class, double.class);
        wrapperToPrimitive.put(Long.class, long.class);
        wrapperToPrimitive.put(Float.class, float.class);
        wrapperToPrimitive.put(Boolean.class, boolean.class);
        wrapperToPrimitive.put(Short.class, short.class);

        register(boolean.class, BooleanControlerBuffer.ID, BooleanControlerBuffer::new);
        register(long.class, LongControlerBuffer.ID, LongControlerBuffer::new);
        register(int.class, IntControlerBuffer.ID, IntControlerBuffer::new);
        register(double.class, DoubleControlerBuffer.ID, DoubleControlerBuffer::new);
        register(float.class, FloatControlerBuffer.ID, FloatControlerBuffer::new);
        register(String.class, StringControlerBuffer.ID, StringControlerBuffer::new);
        register(int[].class, IntArrayControlerBuffer.ID, IntArrayControlerBuffer::new);
        register(long[].class, LongArrayControlerBuffer.ID, LongArrayControlerBuffer::new);
        register(UUID.class, UUIDControlerBuffer.ID, UUIDControlerBuffer::new);
        register(Vec3.class, Vec3dControlerBuffer.ID, Vec3dControlerBuffer::new);
        register(RelativeLocation.class, RelativeLocationControlerBuffer.ID, RelativeLocationControlerBuffer::new);
        register(short.class, ShortControlerBuffer.ID, ShortControlerBuffer::new);
        register(Void.class, EmptyControlerBuffer.ID, EmptyControlerBuffer::new);
    }

    public Map<ParticleControlerDataBuffer.Id, Supplier<? extends ParticleControlerDataBuffer<?>>> getRegisterBuilder() {
        return registerBuilder;
    }

    public Map<Class<?>, ParticleControlerDataBuffer.Id> getRegisterTypes() {
        return registerTypes;
    }

    public BooleanControlerBuffer bool(boolean value) {
        BooleanControlerBuffer buffer = new BooleanControlerBuffer();
        buffer.setLoadedValue(value);
        return buffer;
    }

    public StringControlerBuffer string(String value) {
        StringControlerBuffer buffer = new StringControlerBuffer();
        buffer.setLoadedValue(value);
        return buffer;
    }

    public IntControlerBuffer intValue(int value) {
        IntControlerBuffer buffer = new IntControlerBuffer();
        buffer.setLoadedValue(value);
        return buffer;
    }

    public DoubleControlerBuffer doubleValue(double value) {
        DoubleControlerBuffer buffer = new DoubleControlerBuffer();
        buffer.setLoadedValue(value);
        return buffer;
    }

    public FloatControlerBuffer floatValue(float value) {
        FloatControlerBuffer buffer = new FloatControlerBuffer();
        buffer.setLoadedValue(value);
        return buffer;
    }

    public LongControlerBuffer longValue(long value) {
        LongControlerBuffer buffer = new LongControlerBuffer();
        buffer.setLoadedValue(value);
        return buffer;
    }

    public ShortControlerBuffer shortValue(short value) {
        ShortControlerBuffer buffer = new ShortControlerBuffer();
        buffer.setLoadedValue(value);
        return buffer;
    }

    public IntArrayControlerBuffer intArray(int[] value) {
        IntArrayControlerBuffer buffer = new IntArrayControlerBuffer();
        buffer.setLoadedValue(value);
        return buffer;
    }

    public LongArrayControlerBuffer longArray(long[] value) {
        LongArrayControlerBuffer buffer = new LongArrayControlerBuffer();
        buffer.setLoadedValue(value);
        return buffer;
    }

    public UUIDControlerBuffer uuid(UUID value) {
        UUIDControlerBuffer buffer = new UUIDControlerBuffer();
        buffer.setLoadedValue(value);
        return buffer;
    }

    public Vec3dControlerBuffer vec3d(Vec3 value) {
        Vec3dControlerBuffer buffer = new Vec3dControlerBuffer();
        buffer.setLoadedValue(value);
        return buffer;
    }

    public RelativeLocationControlerBuffer relative(RelativeLocation value) {
        RelativeLocationControlerBuffer buffer = new RelativeLocationControlerBuffer();
        buffer.setLoadedValue(value);
        return buffer;
    }

    public EmptyControlerBuffer empty() {
        return new EmptyControlerBuffer();
    }

    public void register(
            Class<?> bufferType,
            ParticleControlerDataBuffer.Id id,
            Supplier<? extends ParticleControlerDataBuffer<?>> supplier
    ) {
        registerBuilder.put(id, supplier);
        registerTypes.put(bufferType, id);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ParticleControlerDataBuffer<?> withType(Object value, Class<? extends ParticleControlerDataBuffer<?>> clazz) {
        ParticleControlerDataBuffer<?> instance = newInstance(clazz);
        ((ParticleControlerDataBuffer) instance).setLoadedValue(value);
        return instance;
    }

    public ParticleControlerDataBuffer<?> fromBufferType(Object value, Class<?> clazz) {
        ParticleControlerDataBuffer.Id id = registerTypes.get(clazz);
        if (id == null) {
            Class<?> primitiveType = wrapperToPrimitive.get(clazz);
            if (primitiveType != null) {
                id = registerTypes.get(primitiveType);
            }
        }
        if (id == null) {
            return null;
        }
        return withId(id, value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ParticleControlerDataBuffer<?> withId(ParticleControlerDataBuffer.Id id, Object value) {
        Supplier<? extends ParticleControlerDataBuffer<?>> supplier = registerBuilder.get(id);
        if (supplier == null) {
            return null;
        }
        ParticleControlerDataBuffer<?> buffer = supplier.get();
        ((ParticleControlerDataBuffer) buffer).setLoadedValue(value);
        return buffer;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ParticleControlerDataBuffer<?> withIdDecode(ParticleControlerDataBuffer.Id id, byte[] array) {
        Supplier<? extends ParticleControlerDataBuffer<?>> supplier = registerBuilder.get(id);
        if (supplier == null) {
            return null;
        }
        ParticleControlerDataBuffer<?> buffer = supplier.get();
        Object decoded = buffer.decode(array);
        ((ParticleControlerDataBuffer) buffer).setLoadedValue(decoded);
        return buffer;
    }

    public ParticleControlerDataBuffer<?> withId(ResourceLocation id, Object value) {
        return withId(new ParticleControlerDataBuffer.Id(id), value);
    }

    public ParticleControlerDataBuffer<?> withIdDecode(ResourceLocation id, byte[] array) {
        return withIdDecode(new ParticleControlerDataBuffer.Id(id), array);
    }

    public <T> byte[] encode(ParticleControlerDataBuffer<T> buffer) {
        byte[] payload = buffer.encode();
        String className = buffer.getClass().getName();
        return wrapPayload(className, payload);
    }

    public <T> byte[] encode(T value, ParticleControlerDataBuffer<T> buffer) {
        byte[] payload = buffer.encode(value);
        String className = buffer.getClass().getName();
        return wrapPayload(className, payload);
    }

    @SuppressWarnings("unchecked")
    public <T> T decode(byte[] bytes) {
        return (T) decodeToBuffer(bytes).getLoadedValue();
    }

    @SuppressWarnings("unchecked")
    public <T> ParticleControlerDataBuffer<T> decodeToBuffer(byte[] bytes) {
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes))) {
            int classLen = input.readInt();
            byte[] classBytes = input.readNBytes(classLen);
            String className = new String(classBytes, StandardCharsets.UTF_8);
            int payloadLen = input.readInt();
            byte[] payload = input.readNBytes(payloadLen);

            Class<?> clazz = Class.forName(className);
            if (!ParticleControlerDataBuffer.class.isAssignableFrom(clazz)) {
                throw new IllegalStateException("Not a buffer class: " + className);
            }
            ParticleControlerDataBuffer<?> instance = newInstance((Class<? extends ParticleControlerDataBuffer<?>>) clazz);
            Object decoded = instance.decode(payload);
            //noinspection rawtypes,unchecked
            ((ParticleControlerDataBuffer) instance).setLoadedValue(decoded);
            return (ParticleControlerDataBuffer<T>) instance;
        } catch (IOException | ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to decode buffer payload", e);
        }
    }

    private static byte[] wrapPayload(String className, byte[] payload) {
        try (ByteArrayOutputStream raw = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(raw)) {
            byte[] classBytes = className.getBytes(StandardCharsets.UTF_8);
            output.writeInt(classBytes.length);
            output.write(classBytes);
            output.writeInt(payload.length);
            output.write(payload);
            output.flush();
            return raw.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to encode buffer payload", e);
        }
    }

    private static <T extends ParticleControlerDataBuffer<?>> T newInstance(Class<T> clazz) {
        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to instantiate buffer " + clazz.getName(), e);
        }
    }
}
