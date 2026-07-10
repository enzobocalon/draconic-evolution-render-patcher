package com.derenderpatcher.compat;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class CompatMods {
    private static final Set<String> UNSUPPORTED_FABRIC_RENDER_STACK = Set.of(
            "connector",
            "connectormod",
            "connectorextras",
            "sinytra_connector",
            "fabric_api",
            "forgified_fabric_api",
            "fabric_model_loading_api_v1",
            "fabric_models_v0",
            "fabric_renderer_api_v1",
            "fabric_renderer_indigo",
            "fabric_rendering_v1",
            "fabric_rendering_fluids_v1",
            "fabric_rendering_data_attachment_v1"
    );

    public static boolean isOculusLoaded() {
        return isModLoaded("oculus");
    }

    public static boolean isImmediatelyFastLoaded() {
        return isModLoaded("immediatelyfast");
    }

    public static boolean isUnsupportedFabricRenderStackLoaded() {
        return !getLoadedUnsupportedFabricRenderMods().isEmpty();
    }

    public static List<String> getLoadedUnsupportedFabricRenderMods() {
        return UNSUPPORTED_FABRIC_RENDER_STACK.stream()
                .filter(CompatMods::isModLoaded)
                .sorted()
                .collect(Collectors.toList());
    }

    public static boolean isModLoaded(String modId) {
        Object modList = getForgeModList();
        if (modList == null) {
            return false;
        }

        try {
            return (Boolean) modList.getClass()
                    .getMethod("isLoaded", String.class)
                    .invoke(modList, modId);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    public static String getModVersion(String modId) {
        Object modList = getForgeModList();
        if (modList == null) {
            return "unknown";
        }

        try {
            Object optionalContainer = modList.getClass()
                    .getMethod("getModContainerById", String.class)
                    .invoke(modList, modId);
            if (!(optionalContainer instanceof Optional<?> optional) || optional.isEmpty()) {
                return "unknown";
            }

            Object container = optional.get();
            Object modInfo = container.getClass()
                    .getMethod("getModInfo")
                    .invoke(container);
            Object version = modInfo.getClass()
                    .getMethod("getVersion")
                    .invoke(modInfo);
            return version.toString();
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return "unknown";
        }
    }

    private static Object getForgeModList() {
        try {
            Class<?> modListClass = Class.forName("net.minecraftforge.fml.ModList");
            return modListClass.getMethod("get").invoke(null);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private CompatMods() {
    }
}
