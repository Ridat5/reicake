package com.reiasu.reiparticlesapi.test.api;

public interface TestOption {
    void start();

    void stop();

    boolean isValid();

    void onFailed();

    void onSuccess();

    String optionID();

    void doTick();
}
