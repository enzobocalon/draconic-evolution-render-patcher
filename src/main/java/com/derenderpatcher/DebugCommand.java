package com.derenderpatcher;

import com.derenderpatcher.compat.CompatStatus;
import com.derenderpatcher.compat.ImmediatelyFastCompat;
import com.derenderpatcher.compat.IrisCompat;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(modid = DERenderPatcher.MOD_ID)
public final class DebugCommand {
    private DebugCommand() {
    }

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
        appendCompatStatus(message, "§b", ImmediatelyFastCompat.status());
        appendCompatStatus(message, "§d", IrisCompat.status());
        message.append("§7Compat options can be changed in §fderenderpatcher-client.toml§7.§r");

        String finalMessage = message.toString();
        context.getSource().sendSuccess(() -> Component.literal(finalMessage), false);

        return 1;
    }

    private static void appendPatcherStatus(StringBuilder message) {
        message.append("§eDraconic Render Patcher§r\n");
        appendLine(message, "Main patch", enabledText(Config.isEnabled(Config.ENABLE_FIX)));
        message.append("\n");
    }

    private static void appendCompatStatus(StringBuilder message, String color, CompatStatus status) {
        message.append(color).append(status.name()).append("§r\n");
        appendLine(message, "Compat", enabledText(status.state() != CompatStatus.State.DISABLED));

        for (CompatStatus.Setting setting : status.settings()) {
            appendLine(message, setting.name(), settingText(setting));
        }

        appendLine(message, "Status", stateText(status));
        message.append("\n");
    }

    private static String settingText(CompatStatus.Setting setting) {
        if (setting.configured() == CompatStatus.Value.UNAVAILABLE) {
            return valueText(setting.effective()) + " §7(effective)§r";
        }

        return valueText(setting.configured())
                + " §7(configured) ->§r "
                + valueText(setting.effective())
                + " §7(effective)§r";
    }

    private static String stateText(CompatStatus status) {
        String state = switch (status.state()) {
            case DISABLED -> "§7DISABLED§r";
            case NOT_INSTALLED -> "§7SKIPPED§r";
            case ACTIVE -> "§aOK§r";
            case NEEDS_ATTENTION -> "§cNEEDS ATTENTION§r";
            case UNAVAILABLE -> "§cFAILED TO VERIFY§r";
        };
        return state + " §7(" + status.detail() + ")§r";
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

    private static String enabledText(boolean value) {
        return value ? "§aENABLED§r" : "§cDISABLED§r";
    }

    private static String valueText(CompatStatus.Value value) {
        return switch (value) {
            case TRUE -> "§eTRUE§r";
            case FALSE -> "§eFALSE§r";
            case UNAVAILABLE -> "§7UNAVAILABLE§r";
        };
    }

    private static void appendLine(StringBuilder message, String label, String value) {
        message.append("  §7").append(label).append(":§r ").append(value).append("\n");
    }
}
