package com.derenderpatcher.mixins;

import com.derenderpatcher.compat.ShaderCompat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.irisshaders.iris.gl.blending.DepthColorStorage;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VertexBuffer.class)
public class MixinVertexBuffer {
    @Group(name = "derenderpatcher$beforeVertexBufferDraw", min = 1, max = 1)
    @Inject(
            method = "_drawWithShader",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;draw()V",
                    remap = false
            ),
            require = 0,
            remap = false
    )
    private void derenderpatcher$bindPipelineWriteTargetBeforeDrawMcp(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, ShaderInstance shader, CallbackInfo ci) {
        derenderpatcher$bindPipelineWriteTargetBeforeVertexBufferDraw(shader);
    }

    @Group(name = "derenderpatcher$beforeVertexBufferDraw", min = 1, max = 1)
    @Inject(
            method = "m_166876_",
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
