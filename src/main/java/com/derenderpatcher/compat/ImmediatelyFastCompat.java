package com.derenderpatcher.compat;

import com.derenderpatcher.Config;
import com.derenderpatcher.DERenderPatcher;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfastapi.ApiAccess;
import net.raphimc.immediatelyfastapi.ConfigAccess;
import net.raphimc.immediatelyfastapi.ImmediatelyFastApi;
import net.neoforged.fml.ModList;

import java.util.List;

public final class ImmediatelyFastCompat {
    private static final String MOD_ID = "immediatelyfast";
    private static final String DISPLAY_NAME = "ImmediatelyFast";
    private static final String HUD_BATCHING = "hud_batching";
    private static final String SCREEN_BATCHING = "experimental_screen_batching";

    private ImmediatelyFastCompat() {
    }

    public static void applyRuntimeOverride() {
        if (!ModList.get().isLoaded(MOD_ID) || !Config.isEnabled(Config.ENABLE_IMMEDIATELYFAST_COMPAT)) {
            return;
        }

        try {
            ImmediatelyFastAccess.disableBatching();
            DERenderPatcher.LOGGER.info("ImmediatelyFast runtime HUD and screen batching disabled for Draconic Evolution rendering.");
        } catch (RuntimeException | LinkageError e) {
            DERenderPatcher.LOGGER.warn("Failed to disable ImmediatelyFast runtime batching.", e);
        }
    }

    public static CompatStatus status() {
        if (!Config.isEnabled(Config.ENABLE_IMMEDIATELYFAST_COMPAT)) {
            return CompatStatus.disabled(DISPLAY_NAME);
        }
        if (!ModList.get().isLoaded(MOD_ID)) {
            return CompatStatus.notInstalled(DISPLAY_NAME);
        }

        try {
            return ImmediatelyFastAccess.status();
        } catch (RuntimeException | LinkageError e) {
            return CompatStatus.unavailable(DISPLAY_NAME, "failed to query ImmediatelyFast");
        }
    }

    private static final class ImmediatelyFastAccess {
        private ImmediatelyFastAccess() {
        }

        private static void disableBatching() {
            if (ImmediatelyFast.config == null || ImmediatelyFast.runtimeConfig == null) {
                throw new IllegalStateException("ImmediatelyFast configuration is not initialized");
            }

            boolean configHud = ImmediatelyFast.config.hud_batching;
            boolean configScreen = ImmediatelyFast.config.experimental_screen_batching;
            boolean runtimeHud = ImmediatelyFast.runtimeConfig.hud_batching;
            boolean runtimeScreen = ImmediatelyFast.runtimeConfig.experimental_screen_batching;

            try {
                ImmediatelyFast.config.hud_batching = false;
                ImmediatelyFast.config.experimental_screen_batching = false;
                ImmediatelyFast.runtimeConfig.hud_batching = false;
                ImmediatelyFast.runtimeConfig.experimental_screen_batching = false;
            } catch (RuntimeException | Error e) {
                ImmediatelyFast.config.hud_batching = configHud;
                ImmediatelyFast.config.experimental_screen_batching = configScreen;
                ImmediatelyFast.runtimeConfig.hud_batching = runtimeHud;
                ImmediatelyFast.runtimeConfig.experimental_screen_batching = runtimeScreen;
                throw e;
            }
        }

        private static CompatStatus status() {
            ApiAccess api = ImmediatelyFastApi.getApiImpl();
            if (api == null) {
                return CompatStatus.unavailable(DISPLAY_NAME, "ImmediatelyFast API is not initialized");
            }

            ConfigAccess configured = api.getConfig();
            ConfigAccess runtime = api.getRuntimeConfig();
            if (configured == null || runtime == null) {
                return CompatStatus.unavailable(DISPLAY_NAME, "ImmediatelyFast configuration is not initialized");
            }

            boolean configuredHud = configured.getBoolean(HUD_BATCHING, true);
            boolean effectiveHud = runtime.getBoolean(HUD_BATCHING, true);
            boolean configuredScreen = configured.getBoolean(SCREEN_BATCHING, true);
            boolean effectiveScreen = runtime.getBoolean(SCREEN_BATCHING, true);
            boolean healthy = !effectiveHud && !effectiveScreen;

            return CompatStatus.verified(
                    DISPLAY_NAME,
                    healthy,
                    List.of(
                            CompatStatus.Setting.transition(HUD_BATCHING, configuredHud, effectiveHud),
                            CompatStatus.Setting.transition(SCREEN_BATCHING, configuredScreen, effectiveScreen)
                    ),
                    healthy ? "runtime override active" : "one or more batching options remain enabled"
            );
        }
    }
}
