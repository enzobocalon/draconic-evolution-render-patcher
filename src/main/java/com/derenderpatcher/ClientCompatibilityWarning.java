package com.derenderpatcher;

import com.derenderpatcher.compat.CompatMods;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DERenderPatcher.MOD_ID, value = Dist.CLIENT)
public final class ClientCompatibilityWarning {
    private static boolean shownThisSession;

    @SubscribeEvent
    public static void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        Component tag = Component.literal("[DE Render Patcher] ").withStyle(ChatFormatting.GOLD);
        Component warning_beta = Component.literal(
                "You're running a beta version of Draconic Evolution Render Patcher "
                        + "You can experience crashes and render bugs when using it."
        ).withStyle(ChatFormatting.YELLOW);
        event.getPlayer().displayClientMessage(tag.copy().append(warning_beta), false);
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
