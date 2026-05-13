package com.derenderpatcher.compat;

import codechicken.lib.render.shader.CCShaderInstance;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

import java.util.Map;
import java.util.WeakHashMap;

public final class ShaderCompat {
    private static final Map<WorldRenderingPipeline, Runnable> PIPELINE_WRITE_TARGET_BINDERS = new WeakHashMap<>();
    private static int worldRenderDepth;
    private static int brandonsCoreTransparentPassDepth;
    private static int handRenderDepth;

    public static void registerPipelineWriteTarget(WorldRenderingPipeline pipeline, Runnable writeTargetBinder) {
        PIPELINE_WRITE_TARGET_BINDERS.put(pipeline, writeTargetBinder);
    }

    private static boolean isOculusPipelineActive() {
        return Iris.getPipelineManager().getPipelineNullable() != null;
    }

    public static void enterWorldRender() {
        worldRenderDepth++;
    }

    public static void exitWorldRender() {
        if (worldRenderDepth > 0) {
            worldRenderDepth--;
        }
        restoreMainTargetIfNoCompatPassActive();
    }

    public static void enterBrandonsCoreTransparentPass() {
        brandonsCoreTransparentPassDepth++;
    }

    public static void exitBrandonsCoreTransparentPass() {
        if (brandonsCoreTransparentPassDepth > 0) {
            brandonsCoreTransparentPassDepth--;
        }
        restoreMainTargetIfNoCompatPassActive();
    }

    public static void enterHandRenderPass() {
        handRenderDepth++;
    }

    public static void exitHandRenderPass() {
        if (handRenderDepth > 0) {
            handRenderDepth--;
        }
        restoreMainTargetIfNoCompatPassActive();
    }

    public static boolean isHandRenderPassActive() {
        return isOculusPipelineActive() && isWorldViewRendering() && handRenderDepth > 0;
    }

    private static boolean isCompatRenderPassActive() {
        if (!isWorldViewRendering()) {
            return false;
        }

        return brandonsCoreTransparentPassDepth > 0 || handRenderDepth > 0 || isCompatPipelinePhaseActive();
    }

    private static boolean isWorldViewRendering() {
        return Minecraft.getInstance().level != null && worldRenderDepth > 0;
    }

    private static boolean isCompatPipelinePhaseActive() {
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
        if (pipeline == null) {
            return false;
        }

        WorldRenderingPhase phase = pipeline.getPhase();
        return phase == WorldRenderingPhase.HAND_SOLID
                || phase == WorldRenderingPhase.HAND_TRANSLUCENT
                || phase == WorldRenderingPhase.ENTITIES
                || phase == WorldRenderingPhase.BLOCK_ENTITIES;
    }

    public static boolean shouldAllowUnknownShader(ShaderInstance shader) {
        return !ShadowRenderer.ACTIVE
                && isOculusPipelineActive()
                && isCompatRenderPassActive()
                && shader instanceof CCShaderInstance;
    }

    public static void allowUnknownShaderOutput(ShaderInstance shader) {
        if (!shouldAllowUnknownShader(shader)) {
            return;
        }

        bindPipelineWriteTarget();
    }

    public static void bindPipelineWriteTargetBeforeBatchedVboDraw() {
        if (!isOculusPipelineActive() || !isCompatRenderPassActive() || ShadowRenderer.ACTIVE) {
            return;
        }

        bindPipelineWriteTarget();
    }

    public static void bindPipelineWriteTargetAfterShaderApplyBeforeDraw(ShaderInstance shader) {
        allowUnknownShaderOutput(shader);
    }

    private static void bindPipelineWriteTarget() {
        // Switch writes to the write target registered for the current Oculus pipeline.
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
        Runnable writeTargetBinder = PIPELINE_WRITE_TARGET_BINDERS.get(pipeline);
        if (writeTargetBinder != null) {
            writeTargetBinder.run();
        }
    }

    public static void bindMainTargetIfNeeded(ShaderInstance shader) {
        if (shouldAllowUnknownShader(shader)) {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }
    }

    private static void restoreMainTargetIfNoCompatPassActive() {
        // We are no longer rendering Draconic/CodeChickenLib compat content, but the Oculus pipeline is still active.
        // Restore writes to Minecraft's main render target so later passes do not inherit the pipeline write target.
        if (!isCompatRenderPassActive() && isOculusPipelineActive()) {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }
    }

    private ShaderCompat() {
    }
}
