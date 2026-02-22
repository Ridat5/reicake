package com.reiasu.reiparticlesapi.event.api;

public interface EventInterruptible {
    boolean isInterrupted();

    void setInterrupted(boolean interrupted);
}

