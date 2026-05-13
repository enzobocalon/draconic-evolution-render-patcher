package com.derenderpatcher.mixins;

import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.render.BlockEntityRendererTransparent;
import com.derenderpatcher.compat.ShaderCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BCClientEventHandler.class, remap = false)
public class MixinBCClientEventHandler {
    @Inject(method = "renderTransparent", at = @At("HEAD"))
    private <E extends BlockEntity> void derenderpatcher$enterTransparentPass(
            Camera camera,
            BlockEntityRendererTransparent<E> renderer,
            E tile,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            CallbackInfo ci
    ) {
        ShaderCompat.enterBrandonsCoreTransparentPass();
    }

    @Inject(method = "renderTransparent", at = @At("RETURN"))
    private <E extends BlockEntity> void derenderpatcher$exitTransparentPass(
            Camera camera,
            BlockEntityRendererTransparent<E> renderer,
            E tile,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            CallbackInfo ci
    ) {
        ShaderCompat.exitBrandonsCoreTransparentPass();
    }
}
