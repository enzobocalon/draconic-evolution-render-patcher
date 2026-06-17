package com.derenderpatcher.compat;

import com.derenderpatcher.Config;
import com.derenderpatcher.DERenderPatcher;
import net.neoforged.fml.ModList;

import java.lang.reflect.Field;

public final class ImmediatelyFastCompat {
    private static final String MOD_ID = "immediatelyfast";
    private static final String IMMEDIATELY_FAST_CLASS = "net.raphimc.immediatelyfast.ImmediatelyFast";
    private static final String[] FIELDS_TO_DISABLE = {"hud_batching", "experimental_screen_batching"};

    private ImmediatelyFastCompat() {
    }

    public static void applyRuntimeOverride() {
        if (!ModList.get().isLoaded(MOD_ID) || !isEnabled()) {
            return;
        }

        try {
            Class<?> immediatelyFast = Class.forName(IMMEDIATELY_FAST_CLASS);

            disableFields(getStaticField(immediatelyFast, "config"));
            boolean runtimeUpdated = disableFields(getStaticField(immediatelyFast, "runtimeConfig"));

            if (runtimeUpdated) {
                DERenderPatcher.LOGGER.info("ImmediatelyFast runtime hud_batching disabled for Draconic Evolution HUD rendering.");
            } else {
                DERenderPatcher.LOGGER.warn("ImmediatelyFast runtime config was not available; hud_batching could not be disabled.");
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            DERenderPatcher.LOGGER.warn("Failed to disable ImmediatelyFast runtime hud_batching.", e);
        }
    }

    private static boolean isEnabled() {
        try {
            return Config.ENABLE_IMMEDIATELYFAST_COMPAT.get();
        } catch (IllegalStateException ignored) {
            return true;
        }
    }

    private static boolean disableFields(Object target) throws ReflectiveOperationException {
        if (target == null) {
            return false;
        }

        for (String fieldName : FIELDS_TO_DISABLE) {
            setBooleanField(target, fieldName, false);
        }
        return true;
    }

    private static Object getStaticField(Class<?> owner, String name) throws ReflectiveOperationException {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(null);
    }

    private static void setBooleanField(Object target, String name, boolean value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.setBoolean(target, value);
    }
}
