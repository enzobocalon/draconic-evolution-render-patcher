package com.derenderpatcher.compat;

import com.derenderpatcher.Config;
import com.derenderpatcher.DERenderPatcher;
import net.irisshaders.iris.Iris;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class IrisCompat {
    public static final String COMMENT_MARKER = "# Modified by Draconic Evolution Render Patcher";

    public static void modifyIrisConfig() {
        try {
            if (!Config.ENABLE_IRIS_COMPAT.get()) return;
            Class<?> irisClass = Class.forName("net.irisshaders.iris.Iris");

            Field configField = irisClass.getDeclaredField("irisConfig");
            configField.setAccessible(true);
            Object irisConfig = configField.get(null);

            if (irisConfig == null) {
                return;
            }

            Class<?> configClass = irisConfig.getClass();
            Field allowUnknownField = configClass.getDeclaredField("allowUnknownShaders");
            allowUnknownField.setAccessible(true);


            allowUnknownField.set(irisConfig, true);

        } catch (NoSuchFieldException e) {
            DERenderPatcher.LOGGER.warn("Iris not found, skipping config modification");
        } catch (Exception e) {
            DERenderPatcher.LOGGER.error("Failed to modify Iris config", e);
        }
    }

    public static void generateIrisConfigComment() {
        try {
            Path configPath = Paths.get("config", "iris.properties");

            if (!Files.exists(configPath)) {
                DERenderPatcher.LOGGER.warn("iris.properties not found");
                return;
            }

            List<String> lines = Files.readAllLines(configPath);

            boolean commentExists = lines.stream()
                    .anyMatch(line -> line.contains(COMMENT_MARKER));

            if (commentExists) {
                return;
            }

            lines.add(0, COMMENT_MARKER);
            lines.add(1, "# allowUnknownShaders can be set to true by Draconic Evolution Render Patcher to ensure proper rendering of Draconic Evolution entities.");
            lines.add(2, "# Draconic Evolution Render Patcher will override allowUnknownShaders value in this file to enable this feature.");
            lines.add(3, "# Disable this behavior in Draconic Evolution Render Patcher's config if undesired.");
            lines.add(4, "");

            Files.write(configPath, lines, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            DERenderPatcher.LOGGER.error("Failed to add comment to iris.properties", e);
        }
    }
}
