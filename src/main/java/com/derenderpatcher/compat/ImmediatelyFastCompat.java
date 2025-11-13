package com.derenderpatcher.compat;

import com.derenderpatcher.Config;
import com.derenderpatcher.DERenderPatcher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.reflect.Field;

public class ImmediatelyFastCompat {
    private static final String MODIFIED_BY_KEY = "_modified_by_DraconicEvolutionRenderPatcher";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void modifyImmediatelyFastConfig() {
        try {
            if (!Config.ENABLE_IMMEDIATELYFAST_COMPAT.get()) return;
            Class<?> immediatelyFastClass = Class.forName("net.raphimc.immediatelyfast.ImmediatelyFast");

            Field configField = immediatelyFastClass.getDeclaredField("config");
            configField.setAccessible(true);
            Object configInstance = configField.get(null);

            Class<?> configClass = configInstance.getClass();
            Field hudBatchingField = configClass.getDeclaredField("hud_batching");
            hudBatchingField.setAccessible(true);
            hudBatchingField.set(configInstance, false);

            DERenderPatcher.LOGGER.info("Successfully disabled hud_batching in ImmediatelyFast");

        } catch (ClassNotFoundException e) {
            DERenderPatcher.LOGGER.warn("ImmediatelyFast not found, skipping config modification");
        } catch (Exception e) {
            DERenderPatcher.LOGGER.error("Failed to modify ImmediatelyFast config", e);
        }
    }

    public static void generateImmediatelyFastConfigComment() {
        try {
            Path configPath = Paths.get("config", "immediatelyfast.json");

            if (!Files.exists(configPath)) {
                DERenderPatcher.LOGGER.warn("immediatelyfast.json not found");
                return;
            }

            String content = Files.readString(configPath);
            JsonObject config = GSON.fromJson(content, JsonObject.class);

            if (config.has(MODIFIED_BY_KEY)) {
                return;
            }

            config.addProperty(MODIFIED_BY_KEY, "hud_batching can be set to false by Draconic Evolution Render Patcher, overriding the value in this file. Disable this behaviour in Draconic Evolution Render Patcher if undesired.");


            Files.writeString(configPath, GSON.toJson(config));

            DERenderPatcher.LOGGER.info("Modified immediatelyfast.json");

        } catch (IOException e) {
            DERenderPatcher.LOGGER.error("Failed to modify ImmediatelyFast config", e);
        }
    }
}
