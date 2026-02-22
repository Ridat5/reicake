package com.reiasu.reiparticleskill.compat.interop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReflectCompatTest {
    @Test
    void shouldInvokeStaticAndInstanceNoArgMethods() {
        ReflectCompatFixture.STATIC_CALLED = false;
        ReflectCompatFixture.INSTANCE.instanceCalled = false;

        String fixtureClass = ReflectCompatFixture.class.getName();

        assertTrue(ReflectCompat.invokeStaticNoArg(fixtureClass, "staticPing"));
        assertTrue(ReflectCompatFixture.STATIC_CALLED);

        assertTrue(ReflectCompat.invokeOnInstanceNoArg(fixtureClass, "INSTANCE", "instancePing"));
        assertTrue(ReflectCompatFixture.INSTANCE.instanceCalled);
    }

    @Test
    void shouldResolveCompatibleMethodByAssignableSignature() throws Exception {
        var method = ReflectCompat.findMethod(
                ReflectCompatFixture.class,
                "acceptObject",
                String.class
        );
        assertNotNull(method);
        method.invoke(ReflectCompatFixture.INSTANCE, "ok");
    }

    @Test
    void shouldTryCandidateMethodsUntilOneMatches() {
        String fixtureClass = ReflectCompatFixture.class.getName();
        assertTrue(ReflectCompat.invokeAnyStaticNoArg(fixtureClass, "missing", "staticPing"));
        assertTrue(ReflectCompat.invokeAnyOnInstanceNoArg(fixtureClass, "INSTANCE", "missing", "instancePing"));
    }
}
