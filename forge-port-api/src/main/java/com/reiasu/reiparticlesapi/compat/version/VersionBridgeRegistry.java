package com.reiasu.reiparticlesapi.compat.version;

import com.reiasu.reiparticlesapi.compat.version.forge120.Forge120ClientCameraBridge;
import com.reiasu.reiparticlesapi.compat.version.forge120.Forge120ModLifecycleBridge;
import com.reiasu.reiparticlesapi.compat.version.forge120.Forge120NetworkBridge;
import com.reiasu.reiparticlesapi.compat.version.forge120.Forge120ResourceLocationBridge;

public final class VersionBridgeRegistry {
    private static final ResourceLocationVersionBridge RESOURCE_LOCATION = new Forge120ResourceLocationBridge();
    private static final NetworkVersionBridge NETWORK = new Forge120NetworkBridge();
    private static final ClientCameraVersionBridge CLIENT_CAMERA = new Forge120ClientCameraBridge();
    private static final ModLifecycleVersionBridge LIFECYCLE = new Forge120ModLifecycleBridge();

    private VersionBridgeRegistry() {
    }

    public static ResourceLocationVersionBridge resourceLocation() {
        return RESOURCE_LOCATION;
    }

    public static NetworkVersionBridge network() {
        return NETWORK;
    }

    public static ClientCameraVersionBridge clientCamera() {
        return CLIENT_CAMERA;
    }

    public static ModLifecycleVersionBridge lifecycle() {
        return LIFECYCLE;
    }
}
