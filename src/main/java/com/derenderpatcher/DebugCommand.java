package com.derenderpatcher;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@EventBusSubscriber(modid = DERenderPatcher.MOD_ID)
public class DebugCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("derender")
                        .requires(source -> source.hasPermission(0))
                        .then(Commands.literal("status")
                                .executes(DebugCommand::checkConfig))
        );
    }

    private static int checkConfig(CommandContext<CommandSourceStack> context) {
        StringBuilder message = new StringBuilder()
                .append("§6=-=-= Draconic Render Patcher =-=-=§r\n")
                .append("§7Status Report §8|§7 Version §f").append(getModVersion()).append("§r\n\n");

        appendPatcherStatus(message);
        appendImmediatelyFastStatus(message);
        appendIrisStatus(message);
        appendConfigHint(message);

        String finalMessage = message.toString();
        context.getSource().sendSuccess(() -> Component.literal(finalMessage), false);

        return 1;
    }

    private static void appendPatcherStatus(StringBuilder message) {
        message.append("§eDraconic Render Patcher§r\n");
        appendLine(message, "Main patch", enabledText(enabled(Config.ENABLE_FIX)));
        message.append("\n");
    }

    private static void appendImmediatelyFastStatus(StringBuilder message) {
        message.append("§bImmediatelyFast§r\n");

        boolean compatEnabled = enabled(Config.ENABLE_IMMEDIATELYFAST_COMPAT);
        if (!compatEnabled) {
            appendLine(message, "Compat", enabledText(false));
            message.append("\n");
            return;
        }

        try {
            Class<?> ifClass = Class.forName("net.raphimc.immediatelyfast.ImmediatelyFast");
            Object runtimeConfig = getStaticField(ifClass, "runtimeConfig");

            Boolean fileHudBatching = readImmediatelyFastFileValue("hud_batching");
            Boolean runtimeHudBatching = readBooleanField(runtimeConfig, "hud_batching");
            boolean effective = Boolean.FALSE.equals(runtimeHudBatching);

            appendLine(message, "Compat", enabledText(true));
            appendLine(message, "hud_batching", transition(fileHudBatching, runtimeHudBatching));
            appendLine(message, "Status", result(effective, "override active"));
            message.append("\n");

        } catch (ClassNotFoundException e) {
            appendLine(message, "Compat", enabledText(true));
            appendLine(message, "Mod", "§7NOT FOUND§r");
            appendLine(message, "Result", "§7SKIPPED§r");
            message.append("\n");
        } catch (ReflectiveOperationException | RuntimeException e) {
            appendLine(message, "Compat", enabledText(true));
            appendLine(message, "Mod", "§aLOADED§r");
            appendLine(message, "Result", "§cFAILED TO VERIFY§r");
            message.append("\n");
        }
    }

    private static void appendIrisStatus(StringBuilder message) {
        message.append("§dIris§r\n");

        boolean compatEnabled = enabled(Config.ENABLE_IRIS_COMPAT);
        if (!compatEnabled) {
            appendLine(message, "Compat", enabledText(false));
            return;
        }

        try {
            Class<?> irisClass = Class.forName("net.irisshaders.iris.Iris");
            Object config = getIrisConfig(irisClass);
            Boolean fileAllowUnknownShaders = readIrisFileValue("allowUnknownShaders");

            Method shouldAllowUnknownShaders = config.getClass().getDeclaredMethod("shouldAllowUnknownShaders");
            shouldAllowUnknownShaders.setAccessible(true);
            boolean allowValue = (boolean) shouldAllowUnknownShaders.invoke(config);

            appendLine(message, "Compat", enabledText(true));
            appendLine(message, "allowUnknownShaders", transition(fileAllowUnknownShaders, allowValue));
            appendLine(message, "Status", result(allowValue, "override active"));

        } catch (ClassNotFoundException e) {
            appendLine(message, "Compat", enabledText(true));
            appendLine(message, "Mod", "§7NOT FOUND§r");
            appendLine(message, "Result", "§7SKIPPED§r");
        } catch (ReflectiveOperationException | RuntimeException e) {
            appendLine(message, "Compat", enabledText(true));
            appendLine(message, "Mod", "§aLOADED§r");
            appendLine(message, "Result", "§cFAILED TO VERIFY§r");
        }
    }

    private static Object getIrisConfig(Class<?> irisClass) throws ReflectiveOperationException {
        try {
            Method getIrisConfig = irisClass.getDeclaredMethod("getIrisConfig");
            getIrisConfig.setAccessible(true);
            return getIrisConfig.invoke(null);
        } catch (NoSuchMethodException e) {
            Field configField = irisClass.getDeclaredField("irisConfig");
            configField.setAccessible(true);
            return configField.get(null);
        }
    }

    private static String getModVersion() {
        try {
            return ModList.get()
                    .getModContainerById(DERenderPatcher.MOD_ID)
                    .map(container -> container.getModInfo().getVersion().toString())
                    .orElse("unknown");
        } catch (RuntimeException ignored) {
            return "unknown";
        }
    }

    private static Object getStaticField(Class<?> owner, String name) throws ReflectiveOperationException {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(null);
    }

    private static Boolean readBooleanField(Object target, String fieldName) throws ReflectiveOperationException {
        if (target == null) {
            return null;
        }

        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getBoolean(target);
    }

    private static Boolean readIrisFileValue(String key) {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve("iris.properties");
        if (!Files.exists(configPath)) {
            return null;
        }

        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(configPath)) {
            properties.load(reader);
            return parseBoolean(properties.getProperty(key));
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
    }

    private static Boolean readImmediatelyFastFileValue(String key) {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve("immediatelyfast.json");
        if (!Files.exists(configPath)) {
            return null;
        }

        try {
            JsonObject config = JsonParser.parseString(Files.readString(configPath)).getAsJsonObject();
            if (!config.has(key)) {
                return null;
            }
            return config.get(key).getAsBoolean();
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
    }

    private static Boolean parseBoolean(String value) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        return null;
    }

    private static boolean enabled(ModConfigSpec.BooleanValue value) {
        try {
            return value.get();
        } catch (IllegalStateException ignored) {
            return true;
        }
    }

    private static String enabledText(boolean value) {
        return value ? "§aENABLED§r" : "§cDISABLED§r";
    }

    private static String disabledWhenFalse(Boolean value) {
        if (value == null) {
            return "§7UNAVAILABLE§r";
        }

        return value ? "§cTRUE §7(ENABLED)§r" : "§aFALSE §7(DISABLED)§r";
    }

    private static String valueText(Boolean value) {
        if (value == null) {
            return "§7UNAVAILABLE§r";
        }

        return value ? "§eTRUE§r" : "§eFALSE§r";
    }

    private static String transition(Boolean fileValue, Boolean effectiveValue) {
        return valueText(fileValue) + " §7(file) ->§r " + valueText(effectiveValue) + " §7(effective)§r";
    }

    private static String result(boolean ok, String detail) {
        return (ok ? "§aOK§r" : "§cNEEDS ATTENTION§r") + " §7(" + detail + ")§r";
    }

    private static void appendConfigHint(StringBuilder message) {
        message.append("\n§7Compat options can be changed in §fderenderpatcher-client.toml§7.§r");
    }

    private static void appendLine(StringBuilder message, String label, String value) {
        message.append("  §7").append(label).append(":§r ").append(value).append("\n");
    }
}
