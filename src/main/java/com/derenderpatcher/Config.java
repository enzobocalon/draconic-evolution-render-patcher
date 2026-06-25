package com.derenderpatcher;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_FIX = BUILDER
            .comment("Enable Draconic Render Patch.")
            .define("enableFix", true);

    public static final ModConfigSpec.BooleanValue ENABLE_IRIS_COMPAT = BUILDER
            .comment("Force enableUnknownShaders to true in Iris to ensure Draconic Evolution entity render. This will try to override Iris settings.")
            .define("enableIrisCompat", true);

    public static final ModConfigSpec.BooleanValue ENABLE_IMMEDIATELYFAST_COMPAT = BUILDER
            .comment("Force ImmediatelyFast runtime HUD and screen batching off to ensure Draconic Evolution's items render correctly.")
            .define("enableImmediatelyFastCompat", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }

    public static boolean isEnabled(ModConfigSpec.BooleanValue value) {
        try {
            return value.get();
        } catch (IllegalStateException ignored) {
            return true;
        }
    }
}
