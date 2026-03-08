// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public final class APIConfigSpec {
    public static final ForgeConfigSpec SPEC;
    private static final APIConfigSpec INSTANCE;

    static {
        Pair<APIConfigSpec, ForgeConfigSpec> pair =
                new ForgeConfigSpec.Builder().configure(APIConfigSpec::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
        INSTANCE.applyValues();
    }

    private final ForgeConfigSpec.IntValue particleCountLimit;
    private final ForgeConfigSpec.IntValue packetsPerTickLimit;
    private final ForgeConfigSpec.IntValue maxEmitterVisibleRange;

    private APIConfigSpec(ForgeConfigSpec.Builder builder) {
        builder.push("particles");

        particleCountLimit = builder.comment("Maximum number of active emitters tracked by the runtime (legacy key name retained for compatibility)")
                .defineInRange("particleCountLimit", 131072, 1, 1_000_000);
        packetsPerTickLimit = builder.comment("Maximum shared visibility-sync packets sent per server tick across emitters, styles, displays, render entities, and particle groups")
                .defineInRange("packetsPerTickLimit", 512, 16, 4096);
        maxEmitterVisibleRange = builder.comment("Maximum visible range (blocks) for emitter sync packets")
                .defineInRange("maxEmitterVisibleRange", 256, 32, 1024);

        builder.pop();
    }

    public static boolean owns(ModConfig config) {
        return config != null && config.getSpec() == SPEC;
    }

    public static void apply() {
        INSTANCE.applyValues();
    }

    private void applyValues() {
        APIConfig.INSTANCE.setParticleCountLimit(particleCountLimit.get());
        APIConfig.INSTANCE.setPacketsPerTickLimit(packetsPerTickLimit.get());
        APIConfig.INSTANCE.setMaxEmitterVisibleRange(maxEmitterVisibleRange.get());
    }
}
