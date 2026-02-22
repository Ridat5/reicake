package com.reiasu.reiparticlesapi.test;

import com.reiasu.reiparticlesapi.test.api.TestOption;

public final class SimpleTestOption implements TestOption {
    private final String id;
    private int ticks;
    private boolean started;
    private boolean valid = true;

    public SimpleTestOption(String id) {
        this.id = id;
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void stop() {
        valid = false;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void onFailed() {
        valid = false;
    }

    @Override
    public void onSuccess() {
        valid = false;
    }

    @Override
    public String optionID() {
        return id;
    }

    @Override
    public void doTick() {
        if (!started || !valid) {
            return;
        }
        ticks++;
        if (ticks >= 20) {
            onSuccess();
        }
    }
}
