package com.draconicembeddiumfix;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.lang.reflect.Field;

@EventBusSubscriber(modid = DraconicEmbeddiumFix.MOD_ID)
public class DebugCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("draconicembeddiumfix")
                        .requires(source -> source.hasPermission(0))
                        .then(Commands.literal("status")
                                .executes(DebugCommand::checkConfig))
        );
    }

    private static int checkConfig(CommandContext<CommandSourceStack> context) {
        StringBuilder message = new StringBuilder("§6[Draconic Embeddium Fix Config Status]§r\n\n");

        boolean isFixEnabled = Config.ENABLE_FIX.get();

        message.append("§e[Draconic Embeddium Fix]§r\n");
        message.append("  enableFix: ").append(isFixEnabled)
                .append(" ").append(isFixEnabled ? "§a✓ ENABLED" : "§c✗ DISABLED").append("\n\n");

        try {
            Class<?> ifClass = Class.forName("net.raphimc.immediatelyfast.ImmediatelyFast");
            Field configField = ifClass.getDeclaredField("config");
            configField.setAccessible(true);
            Object config = configField.get(null);

            Field hudBatching = config.getClass().getDeclaredField("hud_batching");
            hudBatching.setAccessible(true);
            boolean hudValue = hudBatching.getBoolean(config);

            String status = hudValue ? "§c✗ ENABLED" : "§a✓ DISABLED";
            message.append("§b[ImmediatelyFast]§r\n");
            message.append("  hud_batching: ").append(hudValue).append(" ").append(status).append("\n\n");

        } catch (ClassNotFoundException e) {
            message.append("§b[ImmediatelyFast]§r §7(NOT FOUND)§r\n\n");
        } catch (Exception e) {
            message.append("§b[ImmediatelyFast]§r §cFAILED TO VERIFY STATUS§r\n\n");
        }

        try {
            Class<?> irisClass = Class.forName("net.irisshaders.iris.Iris");
            Field configField = irisClass.getDeclaredField("irisConfig");
            configField.setAccessible(true);
            Object config = configField.get(null);

            Field allowUnknownField = config.getClass().getDeclaredField("allowUnknownShaders");
            allowUnknownField.setAccessible(true);
            boolean allowValue = allowUnknownField.getBoolean(config);

            String status = allowValue ? "§a✓ ENABLED" : "§c✗ DISABLED";
            message.append("§d[Iris]§r\n");
            message.append("  allowUnknownShaders: ").append(allowValue).append(" ").append(status).append("\n");

        } catch (ClassNotFoundException e) {
            message.append("§d[Iris]§r §7(NOT FOUND)§r\n");
        } catch (Exception e) {
            message.append("§d[Iris]§r §cFAILED TO VERIFY STATUS§r\n");
        }

        String finalMessage = message.toString();
        context.getSource().sendSuccess(() -> Component.literal(finalMessage), false);

        return 1;
    }
}
