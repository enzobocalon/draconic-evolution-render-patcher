package com.derenderpatcher.compat;

import net.minecraftforge.fml.ModList;

public final class CompatMods {
    public static boolean isOculusLoaded() {
        return ModList.get().isLoaded("oculus");
    }

    public static boolean isImmediatelyFastLoaded() {
        return ModList.get().isLoaded("immediatelyfast");
    }

    private CompatMods() {
    }
}
