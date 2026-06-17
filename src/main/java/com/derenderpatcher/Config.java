package com.derenderpatcher;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_FIX = BUILDER
            .comment("Enable Draconic Render Patch.")
            .define("enableFix", true);

    public static final ModConfigSpec.BooleanValue ENABLE_IRIS_COMPAT = BUILDER
            .comment("Force enableUnknownShaders to true in Iris to ensure Draconic Evolution entity render. This will try to override Iris settings.")
            .define("enableIrisCompat", true);

    public static final ModConfigSpec.BooleanValue ENABLE_IMMEDIATELYFAST_COMPAT = BUILDER
            .comment("Force ImmediatelyFast runtime hud_batching to false to ensure Draconic Evolution's items can render in hud.")
            .define("enableImmediatelyFastCompat", true);

    static final ModConfigSpec SPEC = BUILDER.build();

}
