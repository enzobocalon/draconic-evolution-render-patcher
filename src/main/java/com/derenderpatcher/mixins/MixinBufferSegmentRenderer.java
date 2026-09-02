package com.derenderpatcher.mixins;

import codechicken.lib.render.buffer.VBORenderType;
import com.derenderpatcher.compat.DraconicRenderTypeResolver;
import com.derenderpatcher.compat.ShaderCompat;
import net.irisshaders.batchedentityrendering.impl.BufferSegment;
import net.irisshaders.batchedentityrendering.impl.BufferSegmentRenderer;
import net.irisshaders.iris.gl.blending.DepthColorStorage;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BufferSegmentRenderer.class, remap = false)
public class MixinBufferSegmentRenderer {
    @Inject(method = "drawInner", at = @At("HEAD"), cancellable = true)
    private void derenderpatcher$drawCodeChickenVbo(BufferSegment segment, CallbackInfo ci) {
        RenderType renderType = segment.type();
        if (!DraconicRenderTypeResolver.isDraconic(renderType)) {
            return;
        }

        ShaderCompat.enterDraconicRender();
        try {
            VBORenderType vboRenderType = DraconicRenderTypeResolver.findVbo(renderType);
            if (vboRenderType == null) {
                derenderpatcher$bindDraconicCompositeRenderType(renderType);
                return;
            }

            if (ShaderCompat.isDebugLoggingEnabled()) {
                ShaderCompat.debugOnce(
                        "batched-vbo:" + DraconicRenderTypeResolver.debugKey(vboRenderType),
                        () -> "Rendering CodeChicken batched VBO through Oculus path. typeChain="
                                + DraconicRenderTypeResolver.describeChain(renderType)
                                + "; " + ShaderCompat.describeCompatState()
                );
            }

            DepthColorStorage.unlockDepthColor();
            boolean shaderPackInUse = ShaderCompat.isShaderPackInUse();
            if (shaderPackInUse && !ShaderCompat.bindPipelineWriteTargetBeforeBatchedVboDraw()) {
                if (ShaderCompat.isDebugLoggingEnabled()) {
                    ShaderCompat.debugOnce(
                            "batched-vbo-skipped-bind:" + DraconicRenderTypeResolver.debugKey(vboRenderType),
                            () -> "Leaving CodeChicken batched VBO on the original Oculus draw path because the compat target is not active. typeChain="
                                    + DraconicRenderTypeResolver.describeChain(renderType)
                                    + "; " + ShaderCompat.describeCompatState()
                    );
                }
                return;
            }

            if (ShaderCompat.isDebugLoggingEnabled()) {
                ShaderCompat.debugOnce(
                        "batched-vbo-private:" + DraconicRenderTypeResolver.debugKey(vboRenderType) + ":shaderpack=" + shaderPackInUse,
                        () -> "Delegated CodeChicken batched VBO render through private VBO path. typeChain="
                                + DraconicRenderTypeResolver.describeChain(renderType)
                                + "; segmentEmpty=" + segment.renderedBuffer().isEmpty()
                                + "; " + ShaderCompat.describeCompatState()
                );
            }

            try {
                ((VBORenderTypeAccessor) vboRenderType).derenderpatcher$render();
                ci.cancel();
            } finally {
                segment.renderedBuffer().release();
            }
        } finally {
            ShaderCompat.exitDraconicRender();
        }
    }

    @Unique
    private static void derenderpatcher$bindDraconicCompositeRenderType(RenderType renderType) {
        if (ShaderCompat.isDebugLoggingEnabled()) {
            ShaderCompat.debugOnce(
                    "missing-vbo:" + DraconicRenderTypeResolver.debugKey(renderType),
                    () -> "Skipped batched draw because no CodeChicken VBORenderType was found. typeChain="
                            + DraconicRenderTypeResolver.describeChain(renderType) + "; " + ShaderCompat.describeCompatState()
            );
        }

        DepthColorStorage.unlockDepthColor();
        if (ShaderCompat.bindPipelineWriteTargetBeforeBatchedVboDraw()) {
            if (ShaderCompat.isDebugLoggingEnabled()) {
                ShaderCompat.debugOnce(
                        "composite-draconic-bind:" + DraconicRenderTypeResolver.debugKey(renderType),
                        () -> "Bound Oculus pipeline target for Draconic composite render type. typeChain="
                                + DraconicRenderTypeResolver.describeChain(renderType) + "; " + ShaderCompat.describeCompatState()
                );
            }
        }
    }

}
