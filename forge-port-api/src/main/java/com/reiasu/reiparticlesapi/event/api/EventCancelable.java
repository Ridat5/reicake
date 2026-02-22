package com.reiasu.reiparticlesapi.event.api;

public interface EventCancelable {
    boolean isCancelled();

    void setCancelled(boolean cancelled);
}

