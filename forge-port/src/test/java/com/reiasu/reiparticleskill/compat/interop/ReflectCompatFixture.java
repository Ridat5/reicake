package com.reiasu.reiparticleskill.compat.interop;

public final class ReflectCompatFixture {
    public static final ReflectCompatFixture INSTANCE = new ReflectCompatFixture();
    public static boolean STATIC_CALLED = false;
    public boolean instanceCalled = false;

    private ReflectCompatFixture() {
    }

    public static void staticPing() {
        STATIC_CALLED = true;
    }

    public void instancePing() {
        instanceCalled = true;
    }

    public void acceptObject(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("value");
        }
    }
}
