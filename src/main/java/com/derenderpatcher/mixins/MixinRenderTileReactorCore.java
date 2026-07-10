package com.derenderpatcher.mixins;

import com.brandon3055.draconicevolution.blocks.reactor.tileentity.TileReactorCore;
import com.brandon3055.draconicevolution.client.render.tile.RenderTileReactorCore;
import com.derenderpatcher.compat.ShaderCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderTileReactorCore.class, remap = false)
public class MixinRenderTileReactorCore {
    @Unique
    private boolean derenderpatcher$enteredDraconicReactorRender;

    @Inject(method = "renderTransparent", at = @At("HEAD"))
    private void derenderpatcher$enterReactorShaderRender(TileReactorCore te, float partialTicks, PoseStack poseStack,
                                                          MultiBufferSource buffers, int packedLight, int packedOverlay,
                                                          CallbackInfo ci) {
        derenderpatcher$enteredDraconicReactorRender = ShaderCompat.isShaderPackInUse();
        if (derenderpatcher$enteredDraconicReactorRender) {
            ShaderCompat.enterDraconicRender();
        }
    }

    @Inject(method = "renderTransparent", at = @At("TAIL"))
    private void derenderpatcher$exitReactorShaderRender(TileReactorCore te, float partialTicks, PoseStack poseStack,
                                                         MultiBufferSource buffers, int packedLight, int packedOverlay,
                                                         CallbackInfo ci) {
        if (derenderpatcher$enteredDraconicReactorRender) {
            ShaderCompat.exitDraconicRender();
            derenderpatcher$enteredDraconicReactorRender = false;
        }
    }
}
