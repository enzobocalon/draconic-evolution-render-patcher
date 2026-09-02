package com.derenderpatcher.compat;

import codechicken.lib.render.shader.CCShaderInstance;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class ShaderCompat {
    private static final boolean DEBUG_LOGGING_ENABLED = Boolean.getBoolean("derenderpatcher.debug");
    private static final Set<String> DEBUG_MESSAGES = ConcurrentHashMap.newKeySet();
    private static final ThreadLocal<Runnable> PENDING_SHADER_UNIFORM_APPLIER = new ThreadLocal<>();

    public static boolean isShaderPackInUse() {
        try {
            IrisApi irisApi = IrisApi.getInstance();
            return irisApi.isShaderPackInUse() && irisApi.getConfig().areShadersEnabled();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public static void enterWorldRender() {
        RenderPass.WORLD.enter();
    }

    public static void exitWorldRender() {
        RenderPass.WORLD.exit();
        restoreMainTargetIfNoCompatPassActive();
    }

    public static void enterBrandonsCoreTransparentPass() {
        RenderPass.BRANDONS_CORE_TRANSPARENT.enter();
    }

    public static void exitBrandonsCoreTransparentPass() {
        RenderPass.BRANDONS_CORE_TRANSPARENT.exit();
        restoreMainTargetIfNoCompatPassActive();
    }

    public static void enterHandRenderPass() {
        RenderPass.HAND.enter();
    }

    public static void exitHandRenderPass() {
        RenderPass.HAND.exit();
        restoreMainTargetIfNoCompatPassActive();
    }

    public static void enterDraconicRender() {
        RenderPass.DRACONIC.enter();
    }

    public static void exitDraconicRender() {
        RenderPass.DRACONIC.exit();
    }

    private static boolean isCompatRenderPassActive() {
        if (!isClientLevelActive()) {
            return false;
        }

        return RenderPass.BRANDONS_CORE_TRANSPARENT.isActive()
                || RenderPass.HAND.isActive()
                || (RenderPass.DRACONIC.isActive() && isWorldViewRendering())
                || isCompatPipelinePhaseActive();
    }

    private static boolean isWorldViewRendering() {
        return Minecraft.getInstance().level != null && RenderPass.WORLD.isActive();
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
                && RenderPass.DRACONIC.isActive()
                && shader instanceof CCShaderInstance;
    }

    public static boolean isDraconicRenderActive() {
        return RenderPass.DRACONIC.isActive();
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
                + ", worldDepth=" + RenderPass.WORLD.depth()
                + ", brandonsTransparentDepth=" + RenderPass.BRANDONS_CORE_TRANSPARENT.depth()
                + ", handDepth=" + RenderPass.HAND.depth()
                + ", draconicDepth=" + RenderPass.DRACONIC.depth()
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
        if (!isShaderPackInUse() || !isCompatRenderPassActive()
                || isShadowRendererActive() || !RenderPass.DRACONIC.isActive()) {
            return false;
        }

        return bindPipelineWriteTarget();
    }

    private static boolean bindPipelineWriteTarget() {
        // Switch writes to the target exposed by the current Oculus pipeline mixin.
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
        if (pipeline instanceof PipelineWriteTargetAccess writeTargetAccess) {
            writeTargetAccess.derenderpatcher$bindPipelineWriteTarget();
            debugOnce(
                    "pipeline-write-target-bound:" + System.identityHashCode(pipeline),
                    () -> "Bound Oculus pipeline write target. pipeline="
                            + pipeline.getClass().getName()
                            + '@' + Integer.toHexString(System.identityHashCode(pipeline))
                            + "; " + describeCompatState()
            );
            return true;
        }

        debugOnce(
                "pipeline-write-target-missing:" + (pipeline == null ? "null" : System.identityHashCode(pipeline)),
                () -> "The current Oculus pipeline does not expose a compatible write target. pipeline="
                        + (pipeline == null ? "null" : pipeline.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(pipeline)))
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

    private enum RenderPass {
        WORLD,
        BRANDONS_CORE_TRANSPARENT,
        HAND,
        DRACONIC;

        private int depth;

        void enter() {
            depth++;
        }

        void exit() {
            if (depth > 0) {
                depth--;
                return;
            }
            ShaderCompat.debugOnce(
                    "unbalanced-render-pass:" + name(),
                    () -> "Ignored unbalanced exit for render pass " + name());
        }

        boolean isActive() {
            return depth > 0;
        }

        int depth() {
            return depth;
        }
    }

    private ShaderCompat() {
    }
}
