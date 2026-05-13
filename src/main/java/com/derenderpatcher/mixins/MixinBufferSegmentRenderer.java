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
    @Inject(method = "drawInner", at = @At("HEAD"), cancellable = true, require = 0)
    private void derenderpatcher$drawCodeChickenVbo(BufferSegment segment, CallbackInfo ci) {
        VBORenderType vboRenderType = derenderpatcher$findVboRenderType(segment.type());
        if (vboRenderType == null) {
            return;
        }

        DepthColorStorage.unlockDepthColor();
        ShaderCompat.bindPipelineWriteTargetBeforeBatchedVboDraw();
        ((VBORenderTypeAccessor) vboRenderType).derenderpatcher$render();
        ci.cancel();
    }

    @Unique
    private static VBORenderType derenderpatcher$findVboRenderType(RenderType type) {
        RenderType cursor = type;

        for (int i = 0; i < 8 && cursor != null; i++) {
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
}
