package com.draconicembeddiumfix;

import com.draconicembeddiumfix.compat.ImmediatelyFastCompat;
import com.draconicembeddiumfix.compat.IrisCompat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = DraconicEmbeddiumFix.MOD_ID, dist = Dist.CLIENT)
public class DraconicEmbeddiumFix {
    public static final String MOD_ID = "draconicembeddiumfix";
    public static final Logger LOGGER = LoggerFactory.getLogger("DraconicEmbeddiumFix");

    public DraconicEmbeddiumFix(IEventBus modEventBus, ModContainer modContainer) {
        // Registrar config
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);

        // Registrar listener do cliente
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
