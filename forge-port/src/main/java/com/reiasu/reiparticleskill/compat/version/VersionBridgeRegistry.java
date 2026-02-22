package com.reiasu.reiparticleskill.compat.version;

import com.reiasu.reiparticleskill.compat.version.forge120.Forge120CommandSourceBridge;
import com.reiasu.reiparticleskill.compat.version.forge120.Forge120EndRespawnBridge;
import com.reiasu.reiparticleskill.compat.version.forge120.Forge120ModLifecycleBridge;

public final class VersionBridgeRegistry {
    private static final CommandSourceVersionBridge COMMAND_SOURCE = new Forge120CommandSourceBridge();
    private static final ModLifecycleVersionBridge LIFECYCLE = new Forge120ModLifecycleBridge();
    private static final EndRespawnVersionBridge END_RESPAWN = new Forge120EndRespawnBridge();

    private VersionBridgeRegistry() {
    }

    public static CommandSourceVersionBridge commandSource() {
        return COMMAND_SOURCE;
    }

    public static ModLifecycleVersionBridge lifecycle() {
        return LIFECYCLE;
    }

    public static EndRespawnVersionBridge endRespawn() {
        return END_RESPAWN;
    }
}
