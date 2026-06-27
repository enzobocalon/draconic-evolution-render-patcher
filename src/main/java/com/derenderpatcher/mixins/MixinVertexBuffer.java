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
                    target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;draw()V"
            ),
            require = 0
    )
    private void derenderpatcher$bindPipelineWriteTargetBeforeDraw(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, ShaderInstance shader, CallbackInfo ci) {
        derenderpatcher$bindPipelineWriteTargetBeforeVertexBufferDraw(shader);
    }

    @Inject(
            method = "m_166876_(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/ShaderInstance;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;m_166882_()V",
                    remap = false
            ),
            require = 0,
            remap = false
    )
    private void derenderpatcher$bindPipelineWriteTargetBeforeDrawSrg(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, ShaderInstance shader, CallbackInfo ci) {
        derenderpatcher$bindPipelineWriteTargetBeforeVertexBufferDraw(shader);
    }

    private void derenderpatcher$bindPipelineWriteTargetBeforeVertexBufferDraw(ShaderInstance shader) {
        if (ShaderCompat.isDraconicRenderActive()) {
            ShaderCompat.debugOnce(
                    "vertex-buffer-draconic-shader:" + shader.getName(),
                    () -> "Draconic shader reached VertexBuffer draw after shader apply. shader=" + shader.getName()
                            + ", allowed=" + ShaderCompat.shouldAllowUnknownShader(shader)
                            + "; " + ShaderCompat.describeCompatState()
            );
        }

        if (ShaderCompat.shouldAllowUnknownShader(shader)) {
            DepthColorStorage.unlockDepthColor();
            ShaderCompat.bindPipelineWriteTargetAfterShaderApplyBeforeDraw(shader);
        }
    }
}
