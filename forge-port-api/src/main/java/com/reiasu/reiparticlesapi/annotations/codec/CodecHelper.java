package com.reiasu.reiparticlesapi.annotations.codec;

import com.reiasu.reiparticlesapi.annotations.CodecField;
import com.reiasu.reiparticlesapi.barrages.HitBox;
import com.reiasu.reiparticlesapi.network.particle.data.DoubleRangeData;
import com.reiasu.reiparticlesapi.network.particle.data.FloatRangeData;
import com.reiasu.reiparticlesapi.network.particle.data.IntRangeData;
import com.reiasu.reiparticlesapi.utils.RelativeLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton codec registry for serializing annotated {@link CodecField} fields
 * to/from {@link FriendlyByteBuf}.
 * <p>
 * Replaces the original Fabric implementation that used {@code StreamCodec}
 * (which does not exist in Forge 1.20.1). Instead uses {@link BufferCodec}.
 */
public final class CodecHelper {

    public static final CodecHelper INSTANCE = new CodecHelper();

    private static final ConcurrentHashMap<String, BufferCodec<?>> supposedTypes = new ConcurrentHashMap<>();

    private CodecHelper() {
    }

    public ConcurrentHashMap<String, BufferCodec<?>> getSupposedTypes() {
        return supposedTypes;
    }

    /**
     * Registers a codec for the given type.
     */
    public <T> void register(Class<T> type, BufferCodec<T> codec) {
        supposedTypes.put(type.getName(), codec);
    }

    /**
     * Copies all {@link CodecField}-annotated, non-final fields from {@code other}
     * to {@code current} via reflection. Both objects must be the same class.
     */
    public void updateFields(Object current, Object other) {
        if (current == null || other == null) return;
        if (!current.getClass().equals(other.getClass())) return;

        Field[] fields = current.getClass().getDeclaredFields();
        List<Field> codecFields = new ArrayList<>();
        for (Field f : fields) {
            if (f.isAnnotationPresent(CodecField.class) && !Modifier.isFinal(f.getModifiers())) {
                codecFields.add(f);
            }
        }
        for (Field field : codecFields) {
            field.setAccessible(true);
            try {
                field.set(current, field.get(other));
            } catch (IllegalAccessException e) {
                // Shouldn't happen since we called setAccessible(true)
            }
        }
    }

    // ────────────────── Static registration of built-in types ──────────────────

    static {
        // short
        INSTANCE.register(Short.TYPE, BufferCodec.of(
                (buf, v) -> buf.writeShort(v.shortValue()),
                FriendlyByteBuf::readShort
        ));

        // int
        INSTANCE.register(Integer.TYPE, BufferCodec.of(
                (buf, v) -> buf.writeInt(v),
                FriendlyByteBuf::readInt
        ));

        // long
        INSTANCE.register(Long.TYPE, BufferCodec.of(
                (buf, v) -> buf.writeLong(v),
                FriendlyByteBuf::readLong
        ));

        // long[]
        INSTANCE.register(long[].class, BufferCodec.of(
                FriendlyByteBuf::writeLongArray,
                FriendlyByteBuf::readLongArray
        ));

        // float
        INSTANCE.register(Float.TYPE, BufferCodec.of(
                (buf, v) -> buf.writeFloat(v),
                FriendlyByteBuf::readFloat
        ));

        // double
        INSTANCE.register(Double.TYPE, BufferCodec.of(
                (buf, v) -> buf.writeDouble(v),
                FriendlyByteBuf::readDouble
        ));

        // String
        INSTANCE.register(String.class, BufferCodec.of(
                FriendlyByteBuf::writeUtf,
                FriendlyByteBuf::readUtf
        ));

        // byte
        INSTANCE.register(Byte.TYPE, BufferCodec.of(
                (buf, v) -> buf.writeByte(v.byteValue()),
                FriendlyByteBuf::readByte
        ));

        // boolean
        INSTANCE.register(Boolean.TYPE, BufferCodec.of(
                FriendlyByteBuf::writeBoolean,
                FriendlyByteBuf::readBoolean
        ));

        // byte[]
        INSTANCE.register(byte[].class, BufferCodec.of(
                FriendlyByteBuf::writeByteArray,
                FriendlyByteBuf::readByteArray
        ));

        // char
        INSTANCE.register(Character.TYPE, BufferCodec.of(
                (buf, v) -> buf.writeChar(v.charValue()),
                FriendlyByteBuf::readChar
        ));

        // UUID
        INSTANCE.register(UUID.class, BufferCodec.of(
                FriendlyByteBuf::writeUUID,
                FriendlyByteBuf::readUUID
        ));

        // Vector3f — manual serialization (no direct FriendlyByteBuf method in 1.20.1)
        INSTANCE.register(Vector3f.class, BufferCodec.of(
                (buf, v) -> {
                    buf.writeFloat(v.x());
                    buf.writeFloat(v.y());
                    buf.writeFloat(v.z());
                },
                buf -> new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat())
        ));

        // Vec3
        INSTANCE.register(Vec3.class, BufferCodec.of(
                (buf, v) -> {
                    buf.writeDouble(v.x());
                    buf.writeDouble(v.y());
                    buf.writeDouble(v.z());
                },
                buf -> new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())
        ));

        // Quaternionf — manual serialization
        INSTANCE.register(Quaternionf.class, BufferCodec.of(
                (buf, q) -> {
                    buf.writeFloat(q.x());
                    buf.writeFloat(q.y());
                    buf.writeFloat(q.z());
                    buf.writeFloat(q.w());
                },
                buf -> new Quaternionf(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat())
        ));

        // AABB
        INSTANCE.register(AABB.class, BufferCodec.of(
                (buf, a) -> {
                    buf.writeDouble(a.minX);
                    buf.writeDouble(a.minY);
                    buf.writeDouble(a.minZ);
                    buf.writeDouble(a.maxX);
                    buf.writeDouble(a.maxY);
                    buf.writeDouble(a.maxZ);
                },
                buf -> new AABB(
                        buf.readDouble(), buf.readDouble(), buf.readDouble(),
                        buf.readDouble(), buf.readDouble(), buf.readDouble()
                )
        ));

        // HitBox
        INSTANCE.register(HitBox.class, BufferCodec.of(
                (buf, h) -> {
                    buf.writeDouble(h.getX1());
                    buf.writeDouble(h.getY1());
                    buf.writeDouble(h.getZ1());
                    buf.writeDouble(h.getX2());
                    buf.writeDouble(h.getY2());
                    buf.writeDouble(h.getZ2());
                },
                buf -> new HitBox(
                        buf.readDouble(), buf.readDouble(), buf.readDouble(),
                        buf.readDouble(), buf.readDouble(), buf.readDouble()
                )
        ));

        // RelativeLocation
        INSTANCE.register(RelativeLocation.class, BufferCodec.of(
                (buf, r) -> {
                    buf.writeDouble(r.getX());
                    buf.writeDouble(r.getY());
                    buf.writeDouble(r.getZ());
                },
                buf -> new RelativeLocation(buf.readDouble(), buf.readDouble(), buf.readDouble())
        ));

        // DoubleRangeData
        INSTANCE.register(DoubleRangeData.class, BufferCodec.of(
                (buf, d) -> {
                    buf.writeDouble(d.min());
                    buf.writeDouble(d.max());
                },
                buf -> new DoubleRangeData(buf.readDouble(), buf.readDouble())
        ));

        // IntRangeData
        INSTANCE.register(IntRangeData.class, BufferCodec.of(
                (buf, d) -> {
                    buf.writeInt(d.getMin());
                    buf.writeInt(d.getMax());
                },
                buf -> new IntRangeData(buf.readInt(), buf.readInt())
        ));

        // FloatRangeData
        INSTANCE.register(FloatRangeData.class, BufferCodec.of(
                (buf, d) -> {
                    buf.writeFloat(d.getMin());
                    buf.writeFloat(d.getMax());
                },
                buf -> new FloatRangeData(buf.readFloat(), buf.readFloat())
        ));

        // NOTE: ControlableParticleData, SimpleRandomParticleData, ItemStack,
        // and all InterpolatorXxx types should register themselves via
        //   CodecHelper.INSTANCE.register(MyClass.class, myCodec)
        // in their own static initializers or during mod init, since their
        // codecs are not yet available as static fields in Forge 1.20.1.
    }
}
