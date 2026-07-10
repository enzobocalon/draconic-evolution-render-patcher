package com.derenderpatcher.compat;

import codechicken.lib.render.shader.CCShaderInstance;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class ShaderCompat {
    private static final boolean DEBUG_LOGGING_ENABLED = Boolean.getBoolean("derenderpatcher.debug");
    private static final Map<WorldRenderingPipeline, Runnable> PIPELINE_WRITE_TARGET_BINDERS = new WeakHashMap<>();
    private static final Set<String> DEBUG_MESSAGES = ConcurrentHashMap.newKeySet();
    private static final ThreadLocal<Runnable> PENDING_SHADER_UNIFORM_APPLIER = new ThreadLocal<>();

    private static int worldRenderDepth;
    private static int brandonsCoreTransparentPassDepth;
    private static int handRenderDepth;
    private static int draconicRenderDepth;

    public static void registerPipelineWriteTarget(WorldRenderingPipeline pipeline, Runnable writeTargetBinder) {
        PIPELINE_WRITE_TARGET_BINDERS.put(pipeline, writeTargetBinder);
        debugOnce(
                "pipeline-write-target-registered:" + System.identityHashCode(pipeline),
                () -> "Registered Oculus pipeline write target binder. pipeline="
                        + pipeline.getClass().getName()
                        + '@' + Integer.toHexString(System.identityHashCode(pipeline))
        );
    }

    public static boolean isShaderPackInUse() {
        if (!CompatMods.isOculusLoaded()) {
            return false;
        }

        try {
            IrisApi irisApi = IrisApi.getInstance();
            return irisApi.isShaderPackInUse() && irisApi.getConfig().areShadersEnabled();
        } catch (RuntimeException | LinkageError ignored) {
            return false;
        }
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
        return isShaderPackInUse() && isWorldViewRendering() && handRenderDepth > 0;
    }

    public static void enterDraconicRender() {
        draconicRenderDepth++;
    }

    public static void exitDraconicRender() {
        if (draconicRenderDepth > 0) {
            draconicRenderDepth--;
        }
    }

    private static boolean isCompatRenderPassActive() {
        if (!isClientLevelActive()) {
            return false;
        }

        return brandonsCoreTransparentPassDepth > 0
                || handRenderDepth > 0
                || (draconicRenderDepth > 0 && isWorldViewRendering())
                || isCompatPipelinePhaseActive();
    }

    private static boolean isWorldViewRendering() {
        return Minecraft.getInstance().level != null && worldRenderDepth > 0;
    }

    private static boolean isClientLevelActive() {
        return Minecraft.getInstance().level != null;
    }

    private static boolean isCompatPipelinePhaseActive() {
        if (!isShaderPackInUse()) {
            return false;
        }

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
        return isShaderPackInUse()
                && !isShadowRendererActive()
                && isCompatRenderPassActive()
                && draconicRenderDepth > 0
                && shader instanceof CCShaderInstance;
    }

    public static boolean isDraconicRenderActive() {
        return draconicRenderDepth > 0;
    }

    public static boolean isDebugLoggingEnabled() {
        return DEBUG_LOGGING_ENABLED;
    }

    public static String describeCompatState() {
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
        WorldRenderingPhase phase = pipeline == null ? null : pipeline.getPhase();
        return "shaderPack=" + isShaderPackInUse()
                + ", oculusPipeline=" + (pipeline != null)
                + ", shadow=" + isShadowRendererActive()
                + ", worldDepth=" + worldRenderDepth
                + ", brandonsTransparentDepth=" + brandonsCoreTransparentPassDepth
                + ", handDepth=" + handRenderDepth
                + ", draconicDepth=" + draconicRenderDepth
                + ", phase=" + phase
                + ", compatPass=" + isCompatRenderPassActive();
    }

    public static void debugOnce(String key, Supplier<String> messageSupplier) {
        if (DEBUG_LOGGING_ENABLED && DEBUG_MESSAGES.add(key)) {
            com.derenderpatcher.DERenderPatcher.LOGGER.info("[DE Render Patcher debug] {}", messageSupplier.get());
        }
    }

    public static void allowUnknownShaderOutput(ShaderInstance shader) {
        if (!shouldAllowUnknownShader(shader)) {
            return;
        }

        bindPipelineWriteTarget();
    }

    public static void setPendingShaderUniformApplier(Runnable uniformApplier) {
        if (uniformApplier == null) {
            clearPendingShaderUniformApplier();
            return;
        }

        PENDING_SHADER_UNIFORM_APPLIER.set(uniformApplier);
    }

    public static void clearPendingShaderUniformApplier() {
        PENDING_SHADER_UNIFORM_APPLIER.remove();
    }

    public static void applyPendingShaderUniforms(ShaderInstance shader) {
        if (!shouldAllowUnknownShader(shader)) {
            return;
        }

        Runnable uniformApplier = PENDING_SHADER_UNIFORM_APPLIER.get();
        if (uniformApplier != null) {
            uniformApplier.run();
        }
    }

    public static boolean bindPipelineWriteTargetBeforeBatchedVboDraw() {
        if (!isShaderPackInUse() || !isCompatRenderPassActive() || isShadowRendererActive() || draconicRenderDepth <= 0) {
            return false;
        }

        return bindPipelineWriteTarget();
    }

    public static void bindPipelineWriteTargetAfterShaderApplyBeforeDraw(ShaderInstance shader) {
        allowUnknownShaderOutput(shader);
    }

    private static boolean bindPipelineWriteTarget() {
        // Switch writes to the write target registered for the current Oculus pipeline.
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
        Runnable writeTargetBinder = PIPELINE_WRITE_TARGET_BINDERS.get(pipeline);
        if (writeTargetBinder != null) {
            writeTargetBinder.run();
            debugOnce(
                    "pipeline-write-target-bound:" + System.identityHashCode(pipeline),
                    () -> "Bound registered Oculus pipeline write target. pipeline="
                            + pipeline.getClass().getName()
                            + '@' + Integer.toHexString(System.identityHashCode(pipeline))
                            + "; " + describeCompatState()
            );
            return true;
        }

        debugOnce(
                "pipeline-write-target-missing:" + (pipeline == null ? "null" : System.identityHashCode(pipeline)),
                () -> "No registered Oculus pipeline write target binder was found. pipeline="
                        + (pipeline == null ? "null" : pipeline.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(pipeline)))
                        + ", registeredBinders=" + PIPELINE_WRITE_TARGET_BINDERS.size()
                        + "; " + describeCompatState()
        );
        return false;
    }

    public static void bindMainTargetIfNeeded(ShaderInstance shader) {
        if (shouldAllowUnknownShader(shader)) {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }
    }

    private static void restoreMainTargetIfNoCompatPassActive() {
        // We are no longer rendering Draconic/CodeChickenLib compat content, but shaders are still active.
        // Restore writes to Minecraft's main render target so later passes do not inherit the pipeline write target.
        if (!isCompatRenderPassActive() && isShaderPackInUse()) {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }
    }

    private static boolean isShadowRendererActive() {
        try {
            return ShadowRenderer.ACTIVE;
        } catch (LinkageError ignored) {
            return false;
        }
    }

    private ShaderCompat() {
    }
}
