package com.derenderpatcher.mixins;

import com.derenderpatcher.compat.ShaderCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pathways.HandRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HandRenderer.class, remap = false)
public class MixinOculusHandRenderer {
    @Inject(method = "renderSolid", at = @At("HEAD"), require = 0)
    private void derenderpatcher$enterSolidHandPass(PoseStack poseStack, float tickDelta, Camera camera, GameRenderer gameRenderer, WorldRenderingPipeline pipeline, CallbackInfo ci) {
        ShaderCompat.enterHandRenderPass();
    }

    @Inject(method = "renderSolid", at = @At("RETURN"), require = 0)
    private void derenderpatcher$exitSolidHandPass(PoseStack poseStack, float tickDelta, Camera camera, GameRenderer gameRenderer, WorldRenderingPipeline pipeline, CallbackInfo ci) {
        ShaderCompat.exitHandRenderPass();
    }

    @Inject(method = "renderTranslucent", at = @At("HEAD"), require = 0)
    private void derenderpatcher$enterTranslucentHandPass(PoseStack poseStack, float tickDelta, Camera camera, GameRenderer gameRenderer, WorldRenderingPipeline pipeline, CallbackInfo ci) {
        ShaderCompat.enterHandRenderPass();
    }

    @Inject(method = "renderTranslucent", at = @At("RETURN"), require = 0)
    private void derenderpatcher$exitTranslucentHandPass(PoseStack poseStack, float tickDelta, Camera camera, GameRenderer gameRenderer, WorldRenderingPipeline pipeline, CallbackInfo ci) {
        ShaderCompat.exitHandRenderPass();
    }
}
