package com.derenderpatcher.mixins;

import com.derenderpatcher.compat.ShaderCompat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.irisshaders.iris.gl.blending.DepthColorStorage;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VertexBuffer.class)
public class MixinVertexBuffer {
    @Inject(
            method = "_drawWithShader",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ShaderInstance;apply()V",
                    shift = At.Shift.AFTER
            ),
            require = 0
    )
    private void derenderpatcher$bindPipelineWriteTargetAfterShaderApply(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, ShaderInstance shader, CallbackInfo ci) {
        if (ShaderCompat.shouldAllowUnknownShader(shader)) {
            DepthColorStorage.unlockDepthColor();
            ShaderCompat.bindPipelineWriteTargetAfterShaderApplyBeforeDraw(shader);
        }
    }
}
