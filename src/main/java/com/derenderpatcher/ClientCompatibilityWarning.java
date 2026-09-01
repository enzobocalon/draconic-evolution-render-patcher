package com.derenderpatcher;

import com.derenderpatcher.compat.CompatMods;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DERenderPatcher.MOD_ID, value = Dist.CLIENT)
public final class ClientCompatibilityWarning {
    private static final String ISSUE_TRACKER_URL =
            "https://github.com/enzobocalon/draconic-evolution-render-patcher/issues";
    private static boolean shownThisSession;

    @SubscribeEvent
    public static void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        Component tag = Component.literal("[DE Render Patcher] ").withStyle(ChatFormatting.GOLD);
        if (ClientConfig.showBetaWarning()) {
            Component warningBeta = Component.literal(
                    "You're running a beta version of Draconic Evolution Render Patcher. "
                            + "If you encounter a bug or crash, please report it on "
            ).withStyle(ChatFormatting.YELLOW);
            Component issueTrackerLink = Component.literal("GitHub")
                    .withStyle(style -> style
                            .withColor(ChatFormatting.AQUA)
                            .withUnderlined(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ISSUE_TRACKER_URL))
                            .withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("Open the Draconic Evolution Render Patcher issue tracker")
                            )));
            event.getPlayer().displayClientMessage(
                    tag.copy().append(warningBeta).append(issueTrackerLink).append(Component.literal(".")),
                    false
            );
        }

        if (shownThisSession || !CompatMods.isUnsupportedFabricRenderStackLoaded()) {
            return;
        }

        shownThisSession = true;
        Component warning = Component.literal(
                "Fabric rendering mods, Forgified Fabric API, Sinytra Connector, and similar bridge mods "
                        + "are not supported. Draconic Evolution Render Patcher may not work correctly in this environment."
        ).withStyle(ChatFormatting.YELLOW);
        event.getPlayer().displayClientMessage(tag.copy().append(warning), false);
    }

    private ClientCompatibilityWarning() {
    }
}
