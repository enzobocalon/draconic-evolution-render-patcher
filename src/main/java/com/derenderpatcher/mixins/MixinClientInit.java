package com.derenderpatcher.mixins;

import com.brandon3055.draconicevolution.init.ClientInit;
import com.derenderpatcher.compat.FancyToolModelCompat;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ClientInit.class, remap = false)
public class MixinClientInit {
    @Redirect(
            method = "registerItemRenderers",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/brandon3055/draconicevolution/DEConfig;fancyToolModels:Z",
                    opcode = Opcodes.GETSTATIC
            ),
            require = 0
    )
    private static boolean derenderpatcher$readFancyToolModelConfig() {
        return FancyToolModelCompat.shouldUseFancyToolModels();
    }
}
