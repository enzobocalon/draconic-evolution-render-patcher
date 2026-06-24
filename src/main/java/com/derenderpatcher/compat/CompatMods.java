package com.derenderpatcher.compat;

import net.minecraftforge.fml.ModList;

import java.util.List;
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
        return ModList.get().isLoaded("oculus");
    }

    public static boolean isImmediatelyFastLoaded() {
        return ModList.get().isLoaded("immediatelyfast");
    }

    public static boolean isUnsupportedFabricRenderStackLoaded() {
        return !getLoadedUnsupportedFabricRenderMods().isEmpty();
    }

    public static List<String> getLoadedUnsupportedFabricRenderMods() {
        ModList modList = ModList.get();
        return UNSUPPORTED_FABRIC_RENDER_STACK.stream()
                .filter(modList::isLoaded)
                .sorted()
                .collect(Collectors.toList());
    }

    private CompatMods() {
    }
}
