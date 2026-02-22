package com.reiasu.reiparticlesapi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class for automatic registration by the ReiParticles API scanner.
 * Classes annotated with this will be discovered and registered at mod init time.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ReiAutoRegister {
}
