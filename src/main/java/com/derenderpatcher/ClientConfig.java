package com.derenderpatcher;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ClientConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue SHOW_BETA_WARNING;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("general");
        SHOW_BETA_WARNING = builder
                .comment("Show the beta warning with the project issue tracker when entering a world.")
                .define("showBetaWarning", true);
        builder.pop();
        SPEC = builder.build();
    }

    public static boolean showBetaWarning() {
        return SHOW_BETA_WARNING.get();
    }

    private ClientConfig() {}
}
