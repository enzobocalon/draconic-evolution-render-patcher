package com.derenderpatcher.mixins;

import codechicken.lib.render.buffer.DelegateRenderType;
import codechicken.lib.render.buffer.VBORenderType;
import com.derenderpatcher.compat.ShaderCompat;
import net.irisshaders.batchedentityrendering.impl.BufferSegment;
import net.irisshaders.batchedentityrendering.impl.BufferSegmentRenderer;
import net.irisshaders.batchedentityrendering.impl.WrappableRenderType;
import net.irisshaders.iris.gl.blending.DepthColorStorage;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BufferSegmentRenderer.class, remap = false)
public class MixinBufferSegmentRenderer {
    @Unique
    private static final int DERENDERPATCHER$MAX_RENDER_TYPE_UNWRAP_DEPTH = 8;

    @Unique
    private static final String[] DERENDERPATCHER$DRACONIC_RENDER_TYPE_MARKERS = {
            "draconicevolution:",
            "reactor_type",
            "shield_type",
            "modeleffecttype"
    };

    @Inject(method = "drawInner", at = @At("HEAD"), cancellable = true, require = 0)
    private void derenderpatcher$drawCodeChickenVbo(BufferSegment segment, CallbackInfo ci) {
        String renderTypeChain = derenderpatcher$describeRenderTypeChain(segment.type());
        if (!derenderpatcher$isDraconicRenderTypeChain(renderTypeChain)) {
            return;
        }

        ShaderCompat.enterDraconicRender();

        VBORenderType vboRenderType = derenderpatcher$findVboRenderType(segment.type());
        if (vboRenderType == null) {
            derenderpatcher$bindDraconicCompositeRenderType(renderTypeChain);
            return;
        }

        ShaderCompat.debugOnce(
                "batched-vbo:" + vboRenderType,
                () -> "Rendering CodeChicken batched VBO through Oculus path. typeChain="
                        + renderTypeChain
                        + "; " + ShaderCompat.describeCompatState()
        );
        DepthColorStorage.unlockDepthColor();
        if (!ShaderCompat.bindPipelineWriteTargetBeforeBatchedVboDraw()) {
            ShaderCompat.debugOnce(
                    "batched-vbo-skipped-bind:" + vboRenderType,
                    () -> "Leaving CodeChicken batched VBO on the original Oculus draw path because the compat target is not active. typeChain="
                            + renderTypeChain
                            + "; " + ShaderCompat.describeCompatState()
            );
            return;
        }

        ShaderCompat.debugOnce(
                "batched-vbo-bound-private:" + vboRenderType,
                () -> "Bound Oculus pipeline target for CodeChicken batched VBO and delegated to CodeChicken VBO render. typeChain="
                        + renderTypeChain
                        + "; segmentEmpty=" + segment.renderedBuffer().isEmpty()
                        + "; " + ShaderCompat.describeCompatState()
        );

        try {
            ((VBORenderTypeAccessor) vboRenderType).derenderpatcher$render();
            ci.cancel();
        } finally {
            ShaderCompat.exitDraconicRender();
        }
    }

    @Inject(method = "drawInner", at = @At("RETURN"), require = 0)
    private void derenderpatcher$exitDraconicDraw(BufferSegment segment, CallbackInfo ci) {
        if (derenderpatcher$isDraconicRenderType(segment.type())) {
            ShaderCompat.exitDraconicRender();
        }
    }

    @Unique
    private static VBORenderType derenderpatcher$findVboRenderType(RenderType type) {
        RenderType cursor = type;

        for (int i = 0; i < DERENDERPATCHER$MAX_RENDER_TYPE_UNWRAP_DEPTH && cursor != null; i++) {
            if (cursor instanceof VBORenderType vboRenderType) {
                return vboRenderType;
            }

            if (cursor instanceof WrappableRenderType wrappableRenderType) {
                cursor = wrappableRenderType.unwrap();
                continue;
            }

            if (cursor instanceof DelegateRenderType) {
                cursor = ((DelegateRenderTypeAccessor) cursor).derenderpatcher$getParent();
                continue;
            }

            break;
        }

        return null;
    }

    @Unique
    private static void derenderpatcher$bindDraconicCompositeRenderType(String renderTypeChain) {
        ShaderCompat.debugOnce(
                "missing-vbo:" + renderTypeChain,
                () -> "Skipped batched draw because no CodeChicken VBORenderType was found. typeChain="
                        + renderTypeChain + "; " + ShaderCompat.describeCompatState()
        );

        DepthColorStorage.unlockDepthColor();
        if (ShaderCompat.bindPipelineWriteTargetBeforeBatchedVboDraw()) {
            ShaderCompat.debugOnce(
                    "composite-draconic-bind:" + renderTypeChain,
                    () -> "Bound Oculus pipeline target for Draconic composite render type. typeChain="
                            + renderTypeChain + "; " + ShaderCompat.describeCompatState()
            );
        }
    }

    @Unique
    private static String derenderpatcher$describeRenderTypeChain(RenderType type) {
        StringBuilder chain = new StringBuilder();
        RenderType cursor = type;

        for (int i = 0; i < DERENDERPATCHER$MAX_RENDER_TYPE_UNWRAP_DEPTH && cursor != null; i++) {
            if (!chain.isEmpty()) {
                chain.append(" -> ");
            }

            chain.append(cursor.getClass().getName()).append('[').append(cursor).append(']');

            if (cursor instanceof WrappableRenderType wrappableRenderType) {
                cursor = wrappableRenderType.unwrap();
                continue;
            }

            if (cursor instanceof DelegateRenderType) {
                cursor = ((DelegateRenderTypeAccessor) cursor).derenderpatcher$getParent();
                continue;
            }

            break;
        }

        return chain.toString();
    }

    @Unique
    private static boolean derenderpatcher$isDraconicRenderTypeChain(String chain) {
        String normalized = chain.toLowerCase(java.util.Locale.ROOT);
        for (String marker : DERENDERPATCHER$DRACONIC_RENDER_TYPE_MARKERS) {
            if (normalized.contains(marker)) {
                return true;
            }
        }

        return false;
    }

    @Unique
    private static boolean derenderpatcher$isDraconicRenderType(RenderType type) {
        return derenderpatcher$isDraconicRenderTypeChain(derenderpatcher$describeRenderTypeChain(type));
    }
}
