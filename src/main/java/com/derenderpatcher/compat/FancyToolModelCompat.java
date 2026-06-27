package com.derenderpatcher.compat;

import codechicken.lib.model.ModelRegistryHelper;
import com.brandon3055.draconicevolution.DEConfig;
import com.derenderpatcher.DERenderPatcher;
import com.derenderpatcher.mixins.ClientInitAccessor;
import com.derenderpatcher.mixins.ModelRegistryHelperAccessor;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FancyToolModelCompat {

    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get()
            .resolve("brandon3055")
            .resolve("DraconicEvolution.cfg");

    private static final Pattern FANCY_TOOL_MODELS_LINE = Pattern.compile(
            "^\\s*B:\"fancyToolModels\"\\s*=\\s*(true|false)\\s*$",
            Pattern.CASE_INSENSITIVE
    );

    private static final boolean FANCY_TOOL_MODELS_ENABLED = resolveFancyToolModelsSetting();

    private FancyToolModelCompat() {
    }

    public static boolean shouldUseFancyToolModels() {
        return FANCY_TOOL_MODELS_ENABLED;
    }

    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        if (!shouldUseFancyToolModels()) {
            return;
        }

        int reapplied = reapplyDraconicModels(event.getModels());
        DERenderPatcher.LOGGER.info(
                "Reapplied {} Draconic Evolution custom models after Forge model baking",
                reapplied
        );
    }

    public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
        if (!shouldUseFancyToolModels()) {
            return;
        }

        Map<ResourceLocation, BakedModel> bakedRegistry = getBakedRegistry(event.getModelManager());
        int reapplied = reapplyDraconicModels(bakedRegistry);

        DERenderPatcher.LOGGER.info(
                "Reapplied {} Draconic Evolution custom models after final model baking",
                reapplied
        );
    }

    private static int reapplyDraconicModels(Map<ResourceLocation, BakedModel> models) {
        List<Pair<ModelResourceLocation, BakedModel>> registeredModels = getDraconicRegisteredModels();
        for (Pair<ModelResourceLocation, BakedModel> pair : registeredModels) {
            models.put(pair.getKey(), pair.getValue());
        }
        return registeredModels.size();
    }

    private static Map<ResourceLocation, BakedModel> getBakedRegistry(ModelManager modelManager) {
        return ObfuscationReflectionHelper.getPrivateValue(ModelManager.class, modelManager, "f_119397_");
    }

    private static List<Pair<ModelResourceLocation, BakedModel>> getDraconicRegisteredModels() {
        ModelRegistryHelper modelHelper = ClientInitAccessor.derenderpatcher$getModelHelper();
        return ((ModelRegistryHelperAccessor) modelHelper).derenderpatcher$getRegisteredModels();
    }

    private static boolean resolveFancyToolModelsSetting() {
        if (!Files.isRegularFile(CONFIG_PATH)) {
            return DEConfig.fancyToolModels;
        }

        try {
            for (String line : Files.readAllLines(CONFIG_PATH)) {
                Matcher matcher = FANCY_TOOL_MODELS_LINE.matcher(line);
                if (matcher.matches()) {
                    return Boolean.parseBoolean(matcher.group(1));
                }
            }
        } catch (IOException e) {
            DERenderPatcher.LOGGER.warn("Failed to read Draconic Evolution config at {}", CONFIG_PATH, e);
        }

        return DEConfig.fancyToolModels;
    }
}
