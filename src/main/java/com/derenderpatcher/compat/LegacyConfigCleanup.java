package com.derenderpatcher.compat;

import com.derenderpatcher.DERenderPatcher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LegacyConfigCleanup {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String IMMEDIATELY_FAST_LEGACY_KEY = "_modified_by_DraconicEvolutionRenderPatcher";
    private static final List<String> IRIS_LEGACY_COMMENT_BLOCK = List.of(
            "# Modified by Draconic Evolution Render Patcher",
            "# allowUnknownShaders can be set to true by Draconic Evolution Render Patcher to ensure proper rendering of Draconic Evolution entities.",
            "# Draconic Evolution Render Patcher will override allowUnknownShaders value in this file to enable this feature.",
            "# Disable this behavior in Draconic Evolution Render Patcher's config if undesired."
    );

    private LegacyConfigCleanup() {
    }

    public static void cleanup() {
        Path configDir = FMLPaths.CONFIGDIR.get();
        runCleanup(configDir.resolve("iris.properties"),
                LegacyConfigCleanup::cleanupIrisConfig,
                "Removed legacy Draconic Render Patcher comments from iris.properties.",
                "Failed to cleanup legacy Iris config comments.");
        runCleanup(configDir.resolve("immediatelyfast.json"),
                LegacyConfigCleanup::cleanupImmediatelyFastConfig,
                "Removed legacy Draconic Render Patcher marker from immediatelyfast.json.",
                "Failed to cleanup legacy ImmediatelyFast config marker.");
    }

    private static void runCleanup(Path configPath, ConfigCleanupAction action, String successMessage, String failureMessage) {
        if (!Files.exists(configPath)) {
            return;
        }

        try {
            if (action.run(configPath)) {
                DERenderPatcher.LOGGER.info(successMessage);
            }
        } catch (IOException | RuntimeException e) {
            DERenderPatcher.LOGGER.warn(failureMessage, e);
        }
    }

    private static boolean cleanupIrisConfig(Path configPath) throws IOException {
        List<String> lines = new ArrayList<>(Files.readAllLines(configPath));
        int markerIndex = Collections.indexOfSubList(lines, IRIS_LEGACY_COMMENT_BLOCK);
        if (markerIndex == -1) {
            return false;
        }

        int removeTo = markerIndex + IRIS_LEGACY_COMMENT_BLOCK.size();
        if (removeTo < lines.size() && lines.get(removeTo).isBlank()) {
            removeTo++;
        }

        lines.subList(markerIndex, removeTo).clear();
        Files.write(configPath, lines, StandardOpenOption.TRUNCATE_EXISTING);
        return true;
    }

    private static boolean cleanupImmediatelyFastConfig(Path configPath) throws IOException {
        JsonObject config = JsonParser.parseString(Files.readString(configPath)).getAsJsonObject();
        if (config.remove(IMMEDIATELY_FAST_LEGACY_KEY) == null) {
            return false;
        }

        Files.writeString(configPath, GSON.toJson(config), StandardOpenOption.TRUNCATE_EXISTING);
        return true;
    }

    @FunctionalInterface
    private interface ConfigCleanupAction {
        boolean run(Path path) throws IOException;
    }
}
