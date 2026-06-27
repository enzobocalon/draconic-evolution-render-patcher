package com.derenderpatcher;

import com.derenderpatcher.compat.FancyToolModelCompat;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(DERenderPatcher.MOD_ID)
public final class DERenderPatcher {
    public static final String MOD_ID = "derenderpatcher";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DERenderPatcher(FMLJavaModLoadingContext context) {
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            String errorMsg = "DERenderPatcher is a client-side mod and cannot be run on a dedicated server!";
            LOGGER.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        IEventBus modBus = context.getModEventBus();
        modBus.addListener(EventPriority.LOWEST, FancyToolModelCompat::onModifyBakingResult);
        modBus.addListener(EventPriority.LOWEST, FancyToolModelCompat::onBakingCompleted);

        LOGGER.info("Loaded Draconic Evolution Render Patcher");
    }
}
