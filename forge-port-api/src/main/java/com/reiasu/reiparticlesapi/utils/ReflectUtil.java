package com.reiasu.reiparticlesapi.utils;

import com.reiasu.reiparticlesapi.ReiParticlesConstants;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

/**
 * Reflection and timing utilities.
 * <p>
 * Provides class references for common Minecraft types (useful for reflection-based
 * annotation processing) and timed execution helpers that log elapsed time
 * to the mod logger.
 */
public final class ReflectUtil {

    public static final ReflectUtil INSTANCE = new ReflectUtil();

    private ReflectUtil() {
    }

    /**
     * Returns the {@link Vec3} class reference.
     */
    public static Class<Vec3> getVec3Class() {
        return Vec3.class;
    }

    /**
     * Returns the {@link Level} class reference.
     */
    public static Class<Level> getLevelClass() {
        return Level.class;
    }

    /**
     * Runs a {@link Runnable} and logs the elapsed time in milliseconds.
     *
     * @param name    the operation name for logging
     * @param invoker the code to run
     */
    public static void infoTimeWith(String name, Runnable invoker) {
        long start = System.currentTimeMillis();
        invoker.run();
        long end = System.currentTimeMillis();
        ReiParticlesConstants.logger.info("Executed " + name + " completed: took " + (end - start) + "ms");
    }

    /**
     * Overload with default empty name.
     */
    public static void infoTimeWith(Runnable invoker) {
        infoTimeWith("", invoker);
    }

    /**
     * Runs a {@link Supplier} and logs the elapsed time, returning the result.
     *
     * @param name    the operation name for logging
     * @param invoker the code to run
     * @param <T>     return type
     * @return the result of the supplier
     */
    public static <T> T infoTimeCallable(String name, Supplier<T> invoker) {
        long start = System.currentTimeMillis();
        T result = invoker.get();
        long end = System.currentTimeMillis();
        ReiParticlesConstants.logger.info("Executed and returned " + name + " completed: took " + (end - start) + "ms");
        return result;
    }

    /**
     * Overload with default empty name.
     */
    public static <T> T infoTimeCallable(Supplier<T> invoker) {
        return infoTimeCallable("", invoker);
    }
}
