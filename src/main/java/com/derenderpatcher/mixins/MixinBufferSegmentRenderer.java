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
            "crystal_type",
            "shield_type",
            "beam_typess",
            "inner_beam",
            "outer_beam",
            "modeleffecttype"
    };

    @Inject(method = "drawInner", at = @At("HEAD"), cancellable = true, require = 0)
    private void derenderpatcher$drawCodeChickenVbo(BufferSegment segment, CallbackInfo ci) {
        RenderType renderType = segment.type();
        if (!derenderpatcher$isDraconicRenderType(renderType)) {
            return;
        }

        ShaderCompat.enterDraconicRender();

        VBORenderType vboRenderType = derenderpatcher$findVboRenderType(renderType);
        if (vboRenderType == null) {
            derenderpatcher$bindDraconicCompositeRenderType(renderType);
            return;
        }

        if (ShaderCompat.isDebugLoggingEnabled()) {
            ShaderCompat.debugOnce(
                    "batched-vbo:" + derenderpatcher$debugKey(vboRenderType),
                    () -> "Rendering CodeChicken batched VBO through Oculus path. typeChain="
                            + derenderpatcher$describeRenderTypeChain(renderType)
                            + "; " + ShaderCompat.describeCompatState()
            );
        }

        DepthColorStorage.unlockDepthColor();
        boolean shaderPackInUse = ShaderCompat.isShaderPackInUse();
        if (shaderPackInUse && !ShaderCompat.bindPipelineWriteTargetBeforeBatchedVboDraw()) {
            if (ShaderCompat.isDebugLoggingEnabled()) {
                ShaderCompat.debugOnce(
                        "batched-vbo-skipped-bind:" + derenderpatcher$debugKey(vboRenderType),
                        () -> "Leaving CodeChicken batched VBO on the original Oculus draw path because the compat target is not active. typeChain="
                                + derenderpatcher$describeRenderTypeChain(renderType)
                                + "; " + ShaderCompat.describeCompatState()
                );
            }
            return;
        }

        if (ShaderCompat.isDebugLoggingEnabled()) {
            ShaderCompat.debugOnce(
                    "batched-vbo-private:" + derenderpatcher$debugKey(vboRenderType) + ":shaderpack=" + shaderPackInUse,
                    () -> "Delegated CodeChicken batched VBO render through private VBO path. typeChain="
                            + derenderpatcher$describeRenderTypeChain(renderType)
                            + "; segmentEmpty=" + segment.renderedBuffer().isEmpty()
                            + "; " + ShaderCompat.describeCompatState()
            );
        }

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
    private static void derenderpatcher$bindDraconicCompositeRenderType(RenderType renderType) {
        if (ShaderCompat.isDebugLoggingEnabled()) {
            ShaderCompat.debugOnce(
                    "missing-vbo:" + derenderpatcher$debugKey(renderType),
                    () -> "Skipped batched draw because no CodeChicken VBORenderType was found. typeChain="
                            + derenderpatcher$describeRenderTypeChain(renderType) + "; " + ShaderCompat.describeCompatState()
            );
        }

        DepthColorStorage.unlockDepthColor();
        if (ShaderCompat.bindPipelineWriteTargetBeforeBatchedVboDraw()) {
            if (ShaderCompat.isDebugLoggingEnabled()) {
                ShaderCompat.debugOnce(
                        "composite-draconic-bind:" + derenderpatcher$debugKey(renderType),
                        () -> "Bound Oculus pipeline target for Draconic composite render type. typeChain="
                                + derenderpatcher$describeRenderTypeChain(renderType) + "; " + ShaderCompat.describeCompatState()
                );
            }
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
    private static boolean derenderpatcher$hasDraconicRenderTypeMarker(String value) {
        if (value == null) {
            return false;
        }

        String normalized = value.toLowerCase(java.util.Locale.ROOT);
        for (String marker : DERENDERPATCHER$DRACONIC_RENDER_TYPE_MARKERS) {
            if (normalized.contains(marker)) {
                return true;
            }
        }

        return false;
    }

    @Unique
    private static String derenderpatcher$debugKey(Object object) {
        return object.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(object));
    }

    @Unique
    private static boolean derenderpatcher$isDraconicRenderType(RenderType type) {
        RenderType cursor = type;

        for (int i = 0; i < DERENDERPATCHER$MAX_RENDER_TYPE_UNWRAP_DEPTH && cursor != null; i++) {
            if (derenderpatcher$hasDraconicRenderTypeMarker(cursor.toString())) {
                return true;
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

        return false;
    }
}
