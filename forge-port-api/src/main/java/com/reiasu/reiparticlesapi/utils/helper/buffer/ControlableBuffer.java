package com.reiasu.reiparticlesapi.utils.helper.buffer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field on a {@link com.reiasu.reiparticlesapi.particles.Controlable} for
 * automatic buffer serialization. The {@link #name()} value is used as the buffer key.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ControlableBuffer {
    String name();
}
