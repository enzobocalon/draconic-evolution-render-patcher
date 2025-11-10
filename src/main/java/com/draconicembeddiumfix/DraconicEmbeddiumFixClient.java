package com.draconicembeddiumfix;

import com.draconicembeddiumfix.compat.ImmediatelyFastCompat;
import com.draconicembeddiumfix.compat.IrisCompat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Mod(value = DraconicEmbeddiumFix.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = DraconicEmbeddiumFix.MOD_ID, value = Dist.CLIENT)

public class DraconicEmbeddiumFixClient {
    public static final Logger LOGGER = LoggerFactory.getLogger("DraconicEmbeddiumFix");

    public DraconicEmbeddiumFixClient(ModContainer container) {
    }


    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
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
    public static class ClientEventHandler { }
}
