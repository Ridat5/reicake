package com.reiasu.reiparticlesapi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReiParticlesAPITest {
    @Test
    void shouldAllowRepeatedLifecycleCalls() {
        assertDoesNotThrow(ReiParticlesAPI::init);
        assertDoesNotThrow(ReiParticlesAPI::init);
        assertTrue(ReiParticlesAPI.isInitialized());

        assertDoesNotThrow(() -> ReiParticlesAPI.INSTANCE.loadScannerPackages());
        assertDoesNotThrow(() -> ReiParticlesAPI.INSTANCE.loadScannerPackages());
        assertTrue(ReiParticlesAPI.INSTANCE.scannersLoaded());

        assertDoesNotThrow(() -> ReiParticlesAPI.INSTANCE.registerParticleStyles());
        assertDoesNotThrow(() -> ReiParticlesAPI.INSTANCE.registerStyles());
        assertDoesNotThrow(() -> ReiParticlesAPI.INSTANCE.registerStyleHooks());
        assertTrue(ReiParticlesAPI.INSTANCE.styleHooksRegistered());

        assertDoesNotThrow(() -> ReiParticlesAPI.INSTANCE.registerTestHooks());
        assertDoesNotThrow(() -> ReiParticlesAPI.INSTANCE.registerTest());
        assertDoesNotThrow(() -> ReiParticlesAPI.INSTANCE.registerTests());
        assertTrue(ReiParticlesAPI.INSTANCE.testHooksRegistered());

        assertDoesNotThrow(() -> ReiParticlesAPI.INSTANCE.registerKeyBindings());
        assertDoesNotThrow(() -> ReiParticlesAPI.INSTANCE.registerKeybinds());
        assertDoesNotThrow(() -> ReiParticlesAPI.INSTANCE.registerClientKeyBindings());
        assertTrue(ReiParticlesAPI.INSTANCE.keyHooksRegistered());
    }
}
