package com.derenderpatcher.compat;

import com.derenderpatcher.Config;
import net.irisshaders.iris.Iris;
import net.neoforged.fml.ModList;

import java.util.List;

public final class IrisCompat {
    private static final String MOD_ID = "iris";
    private static final String DISPLAY_NAME = "Iris";

    private IrisCompat() {
    }

    public static CompatStatus status() {
        if (!Config.isEnabled(Config.ENABLE_IRIS_COMPAT)) {
            return CompatStatus.disabled(DISPLAY_NAME);
        }
        if (!ModList.get().isLoaded(MOD_ID)) {
            return CompatStatus.notInstalled(DISPLAY_NAME);
        }

        try {
            return IrisAccess.status();
        } catch (RuntimeException | LinkageError e) {
            return CompatStatus.unavailable(DISPLAY_NAME, "failed to query Iris");
        }
    }

    private static final class IrisAccess {
        private IrisAccess() {
        }

        private static CompatStatus status() {
            boolean effective = Iris.getIrisConfig().shouldAllowUnknownShaders();
            return CompatStatus.verified(
                    DISPLAY_NAME,
                    effective,
                    List.of(CompatStatus.Setting.effectiveOnly("allowUnknownShaders", effective)),
                    effective ? "override active" : "override inactive"
            );
        }
    }
}
