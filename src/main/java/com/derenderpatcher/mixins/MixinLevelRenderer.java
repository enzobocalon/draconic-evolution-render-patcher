package com.derenderpatcher.mixins;

import com.derenderpatcher.compat.ShaderCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void derenderpatcher$enterWorldRender(PoseStack poseStack, float partialTick, long finishTimeNano,
                                                  boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
                                                  LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (!ShaderCompat.isShaderPackInUse()) {
            return;
        }

        ShaderCompat.enterWorldRender();
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void derenderpatcher$exitWorldRender(PoseStack poseStack, float partialTick, long finishTimeNano,
                                                 boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
                                                 LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        ShaderCompat.exitWorldRender();
    }
}
