package com.derenderpatcher.compat;

import net.minecraftforge.fml.ModList;

import java.util.List;
import java.util.Set;

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

    public static boolean isImmediatelyFastLoaded() {
        return isModLoaded("immediatelyfast");
    }

    public static boolean isUnsupportedFabricRenderStackLoaded() {
        return UNSUPPORTED_FABRIC_RENDER_STACK.stream().anyMatch(CompatMods::isModLoaded);
    }

    public static List<String> getLoadedUnsupportedFabricRenderMods() {
        return UNSUPPORTED_FABRIC_RENDER_STACK.stream()
                .filter(CompatMods::isModLoaded)
                .sorted()
                .toList();
    }

    private static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static String getModVersion(String modId) {
        return ModList.get()
                .getModContainerById(modId)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("unknown");
    }

    private CompatMods() {
    }
}
