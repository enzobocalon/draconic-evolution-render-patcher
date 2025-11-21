package com.derenderpatcher.mixin;

import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.render.BlockEntityRendererTransparent;
import com.derenderpatcher.Config;
import com.derenderpatcher.compat.RendererCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BCClientEventHandler.class)
public class MixinBCClientEventHandler {
    @Inject(
            method = "renderLevelStage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void fixedRenderLevelStage(RenderLevelStageEvent event, CallbackInfo ci) {
        if (!Config.ENABLE_FIX.get()) {
            return;
        }
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        ci.cancel();

        BlockEntityRenderDispatcher tileRenderDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        MultiBufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();
        Vec3 vec3 = camera.getPosition();
        double camX = vec3.x();
        double camY = vec3.y();
        double camZ = vec3.z();

        RendererCompat.forEachVisibleBlockEntity(tile -> {
            BlockEntityRenderer<BlockEntity> renderer = tileRenderDispatcher.getRenderer(tile);
            if (renderer instanceof BlockEntityRendererTransparent rendererTransparent) {
                BlockPos pos = tile.getBlockPos();
                poseStack.pushPose();
                poseStack.translate(pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ);
                ((BCClientEventHandler) (Object) this).renderTransparent(
                        camera,
                        rendererTransparent,
                        tile,
                        event.getPartialTick().getGameTimeDeltaPartialTick(false),
                        poseStack,
                        buffers
                );
                poseStack.popPose();
            }
        });
    }
}