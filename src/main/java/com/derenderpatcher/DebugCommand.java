package com.derenderpatcher;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DERenderPatcher.MOD_ID, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class DebugCommand {
    private static final String ENABLED = "§a✓ ENABLED§r";

    @SubscribeEvent
    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("derender")
                        .requires(source -> source.hasPermission(0))
                        .then(Commands.literal("status")
                                .executes(DebugCommand::status))
        );
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        StringBuilder message = new StringBuilder("§6[Draconic Render Patcher Status]§r\n\n");

        message.append("§e[Draconic Render Patcher]§r\n");
        message.append("  loaded: ").append(ENABLED).append("\n");
        message.append("  mode: §7Oculus pipeline render target patch§r\n\n");

        message.append("§b[Optional Compat]§r\n");
        appendImmediatelyFastStatus(message);

        context.getSource().sendSuccess(() -> Component.literal(message.toString()), false);
        return 1;
    }

    private static void appendImmediatelyFastStatus(StringBuilder message) {
        if (ModList.get().isLoaded("immediatelyfast")) {
            message.append("  ImmediatelyFast: ").append(ENABLED).append("\n");
            message.append("  §e⚠ If you find HUD/item rendering issues, try running without ImmediatelyFast or adjusting its config.§r\n");
        } else {
            message.append("  ImmediatelyFast: §7NOT FOUND§r\n");
        }
    }

    private DebugCommand() {
    }
}
