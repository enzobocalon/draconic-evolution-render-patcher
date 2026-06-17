package com.derenderpatcher.mixin;

import com.derenderpatcher.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.irisshaders.iris.config.IrisConfig", remap = false)
public class MixinIrisConfig {
    @Inject(method = "shouldAllowUnknownShaders", at = @At("HEAD"), cancellable = true)
    private void derenderpatcher$allowUnknownShaders(CallbackInfoReturnable<Boolean> cir) {
        if (isIrisCompatEnabled()) {
            cir.setReturnValue(true);
        }
    }

    private static boolean isIrisCompatEnabled() {
        try {
            return Config.ENABLE_IRIS_COMPAT.get();
        } catch (IllegalStateException e) {
            return true;
        }
    }
}
