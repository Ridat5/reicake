package com.reiasu.reiparticlesapi.compat.version;

import net.minecraft.resources.ResourceLocation;

public interface ResourceLocationVersionBridge {
    ResourceLocation modLocation(String modId, String path);
}
