package com.reiasu.reiparticlesapi.compat.version.forge120;

import com.reiasu.reiparticlesapi.compat.version.ResourceLocationVersionBridge;
import net.minecraft.resources.ResourceLocation;

public final class Forge120ResourceLocationBridge implements ResourceLocationVersionBridge {
    @Override
    public ResourceLocation modLocation(String modId, String path) {
        return new ResourceLocation(modId, path);
    }
}
