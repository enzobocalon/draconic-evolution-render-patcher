package com.derenderpatcher.mixins;

import com.derenderpatcher.compat.ShaderCompat;
import net.irisshaders.iris.gl.blending.DepthColorStorage;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ShaderInstance.class, priority = 1)
public class MixinShaderInstance {
    // This call is injected by Oculus, so it is intentionally optional across Mixin application order.
    @Inject(
            method = {"apply", "m_173363_"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/gl/blending/DepthColorStorage;disableDepthColor()V",
                    shift = At.Shift.AFTER,
                    remap = false
            ),
            require = 0,
            remap = false
    )
    private void derenderpatcher$allowCodeChickenShaderOutput(CallbackInfo ci) {
        ShaderInstance shader = (ShaderInstance) (Object) this;
        if (ShaderCompat.shouldAllowUnknownShader(shader)) {
            DepthColorStorage.unlockDepthColor();
            ShaderCompat.allowUnknownShaderOutput(shader);
            ShaderCompat.applyPendingShaderUniforms(shader);
        }
    }

    @Inject(method = {"apply", "m_173363_"}, at = @At("RETURN"), remap = false)
    private void derenderpatcher$bindPipelineWriteTargetAfterApply(CallbackInfo ci) {
        ShaderInstance shader = (ShaderInstance) (Object) this;
        if (ShaderCompat.shouldAllowUnknownShader(shader)) {
            DepthColorStorage.unlockDepthColor();
            ShaderCompat.allowUnknownShaderOutput(shader);
            ShaderCompat.applyPendingShaderUniforms(shader);
        }
    }

    @Inject(method = {"clear", "m_173362_"}, at = @At("RETURN"), remap = false)
    private void derenderpatcher$restoreMainTargetAfterClear(CallbackInfo ci) {
        ShaderCompat.bindMainTargetIfNeeded((ShaderInstance) (Object) this);
    }
}
