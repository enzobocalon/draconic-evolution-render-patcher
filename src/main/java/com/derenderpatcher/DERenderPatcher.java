package com.derenderpatcher;

import com.derenderpatcher.compat.ImmediatelyFastCompat;
import com.derenderpatcher.compat.IrisCompat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = DERenderPatcher.MOD_ID, dist = Dist.CLIENT)
public class DERenderPatcher {
    public static final String MOD_ID = "derenderpatcher";
    public static final Logger LOGGER = LoggerFactory.getLogger("derenderpatcher");

    public DERenderPatcher(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);

        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            if (ModList.get().isLoaded("iris")) {
                LOGGER.info("Iris found.");
                IrisCompat.modifyIrisConfig();
                IrisCompat.generateIrisConfigComment();
            }

            if (ModList.get().isLoaded("immediatelyfast")) {
                LOGGER.info("ImmediatelyFast found.");
                ImmediatelyFastCompat.modifyImmediatelyFastConfig();
                ImmediatelyFastCompat.generateImmediatelyFastConfigComment();
            }
        });
    }
}
