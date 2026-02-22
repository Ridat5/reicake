package com.reiasu.reiparticlesapi;

import com.reiasu.reiparticlesapi.event.ReiEventBus;
import com.reiasu.reiparticlesapi.event.api.ReiEvent;
import com.reiasu.reiparticlesapi.test.SimpleTestGroupBuilder;
import com.reiasu.reiparticlesapi.test.TestManager;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ReiParticlesAPI {
    public static final ReiParticlesAPI INSTANCE = new ReiParticlesAPI();
    public static final Scheduler scheduler = new Scheduler();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
    private static final AtomicBoolean SCANNERS_LOADED = new AtomicBoolean(false);
    private static final AtomicBoolean TEST_HOOKS_REGISTERED = new AtomicBoolean(false);
    private static final AtomicBoolean STYLE_HOOKS_REGISTERED = new AtomicBoolean(false);
    private static final AtomicBoolean KEY_HOOKS_REGISTERED = new AtomicBoolean(false);

    private ReiParticlesAPI() {
    }

    public static void init() {
        if (INITIALIZED.compareAndSet(false, true)) {
            LOGGER.info("ReiParticlesAPI init completed");
        }
    }

    public static boolean isInitialized() {
        return INITIALIZED.get();
    }

    public void loadScannerPackages() {
        if (SCANNERS_LOADED.compareAndSet(false, true)) {
            LOGGER.info("ReiParticlesAPI scanner packages loaded");
            ReiEventBus.INSTANCE.scanListeners();
            ReiEventBus.INSTANCE.initListeners();
        }
    }

    public boolean scannersLoaded() {
        return SCANNERS_LOADED.get();
    }

    public void registerTest() {
        if (TEST_HOOKS_REGISTERED.compareAndSet(false, true)) {
            TestManager.INSTANCE.register("api-test-group-builder", user -> buildSmokeTestGroup(user));
            LOGGER.info("ReiParticlesAPI test hooks registered");
        }
    }

    public boolean testHooksRegistered() {
        return TEST_HOOKS_REGISTERED.get();
    }

    public void registerTests() {
        registerTest();
    }

    public void registerTestHooks() {
        registerTest();
    }

    public void registerParticleStyles() {
        if (STYLE_HOOKS_REGISTERED.compareAndSet(false, true)) {
            LOGGER.info("ReiParticlesAPI particle styles registered");
        }
    }

    public boolean styleHooksRegistered() {
        return STYLE_HOOKS_REGISTERED.get();
    }

    public void registerStyles() {
        registerParticleStyles();
    }

    public void registerStyleHooks() {
        registerParticleStyles();
    }

    public void registerKeyBindings() {
        if (KEY_HOOKS_REGISTERED.compareAndSet(false, true)) {
            LOGGER.info("ReiParticlesAPI key hooks registered");
        }
    }

    public boolean keyHooksRegistered() {
        return KEY_HOOKS_REGISTERED.get();
    }

    public void registerKeybinds() {
        registerKeyBindings();
    }

    public void registerClientKeyBindings() {
        registerKeyBindings();
    }

    public void appendEventListenerTarget(String modId, String target) {
        ReiEventBus.INSTANCE.appendListenerTarget(modId, target);
    }

    public void initEventListeners() {
        ReiEventBus.INSTANCE.initListeners();
    }

    public void registerEventListener(String modId, Object listener) {
        ReiEventBus.INSTANCE.registerListenerInstance(modId, listener);
    }

    public <T extends ReiEvent> T callEvent(T event) {
        return ReiEventBus.call(event);
    }

    public static final class Scheduler {
        private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

        public void runTask(int ticks, Runnable task) {
            long delayMs = Math.max(1L, ticks) * 50L;
            exec.schedule(task, delayMs, TimeUnit.MILLISECONDS);
        }
    }

    private static SimpleTestGroupBuilder buildSmokeTestGroup(ServerPlayer user) {
        return new SimpleTestGroupBuilder("api-test-group-builder", user);
    }
}
