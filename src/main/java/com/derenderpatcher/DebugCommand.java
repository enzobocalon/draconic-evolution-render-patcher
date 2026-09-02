package com.derenderpatcher;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.derenderpatcher.compat.CompatMods;
import com.derenderpatcher.compat.FancyToolModelCompat;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = DERenderPatcher.MOD_ID, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class DebugCommand {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("derender")
                        .requires(source -> source.hasPermission(0))
                        .then(Commands.literal("status")
                                .executes(DebugCommand::checkStatus))
        );
    }

    private static int checkStatus(CommandContext<CommandSourceStack> context) {
        StringBuilder message = new StringBuilder()
                .append("§6=-=-= Draconic Render Patcher =-=-=§r\n")
                .append("§7Status Report §8|§7 Version §f")
                .append(CompatMods.getModVersion(DERenderPatcher.MOD_ID))
                .append("§r\n\n");

        appendPatcherStatus(message);
        appendOculusStatus(message);
        appendImmediatelyFastStatus(message);
        appendFabricBridgeStatus(message);
        appendConfigHint(message);

        String finalMessage = message.toString();
        context.getSource().sendSuccess(() -> Component.literal(finalMessage), false);
        return 1;
    }

    private static void appendPatcherStatus(StringBuilder message) {
        message.append("§eDraconic Render Patcher§r\n");
        appendLine(message, "Main patch", enabledText(true));

        boolean fancyModels = FancyToolModelCompat.shouldUseFancyToolModels();
        appendLine(message, "Fancy 3D models", enabledText(fancyModels));
        appendLine(message, "Model bake", result(fancyModels, fancyModels ? "forced registration active" : "disabled by Draconic Evolution config"));
        message.append("\n");
    }

    private static void appendOculusStatus(StringBuilder message) {
        message.append("§dOculus§r\n");

        appendLine(message, "Mod", "§aLOADED§r");
        appendLine(message, "Pipeline patch", enabledText(true));
        appendLine(message, "Status", result(true, "render target patch active"));
        message.append("\n");
    }

    private static void appendImmediatelyFastStatus(StringBuilder message) {
        message.append("§bImmediatelyFast§r\n");

        boolean loaded = CompatMods.isImmediatelyFastLoaded();
        appendLine(message, "Mod", loaded ? "§aLOADED§r" : "§7NOT FOUND§r");
        appendLine(message, "Compat", loaded ? "§ePASSIVE§r" : "§7SKIPPED§r");
        appendLine(message, "Status", loaded
                ? result(true, "no runtime override required")
                : result(true, "not installed"));
        message.append("\n");
    }

    private static void appendFabricBridgeStatus(StringBuilder message) {
        message.append("§cFabric / Connector§r\n");

        List<String> loadedMods = CompatMods.getLoadedUnsupportedFabricRenderMods();
        if (loadedMods.isEmpty()) {
            appendLine(message, "Stack", "§7NOT FOUND§r");
            appendLine(message, "Status", result(true, "supported environment"));
            return;
        }

        appendLine(message, "Stack", "§cDETECTED§r");
        appendLine(message, "Mods", "§f" + String.join(", ", loadedMods) + "§r");
        appendLine(message, "Status", result(false, "unsupported rendering environment"));
    }

    private static String enabledText(boolean value) {
        return value ? "§aENABLED§r" : "§cDISABLED§r";
    }

    private static String result(boolean ok, String detail) {
        return (ok ? "§aOK§r" : "§cNEEDS ATTENTION§r") + " §7(" + detail + ")§r";
    }

    private static void appendConfigHint(StringBuilder message) {
        message.append("\n§7Fancy model rendering follows §fconfig/brandon3055/DraconicEvolution.cfg§7.§r");
    }

    private static void appendLine(StringBuilder message, String label, String value) {
        message.append("  §7").append(label).append(":§r ").append(value).append("\n");
    }

    private DebugCommand() {
    }
}
