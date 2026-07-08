package com.derenderpatcher.compat;

import codechicken.lib.render.CCRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.layer.BlockEntityRenderStateShard;
import net.irisshaders.iris.layer.OuterWrappedRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns the per-tile render session lifecycle for the Draconic block entities we patch.
 *
 * <p>A session wraps one private {@link ResettableBufferSource} and the iris
 * {@code BlockEntityRenderStateShard} phase-bracketing needed so our draws
 * land on the right Oculus pipeline write target with the right shader
 * uniforms, without ever touching the shared Embeddium/Oculus batched
 * {@code MultiBufferSource}.
 *
 * <p>Contract:
 * <ul>
 *   <li>{@link #enter()} at the {@code render()} HEAD of the tile renderer.</li>
 *   <li>{@link #prepareForBind()} at the HEAD of any inner sub-method that does
 *       its own {@code ccrs.bind} + flush cycle.</li>
 *   <li>{@link #bind} at every {@code ccrs.bind(type, getter)} site we redirect.</li>
 *   <li>{@link #flush()} at any intermediate flush point; idempotent.</li>
 *   <li>{@link #exit()} at the {@code render()} RETURN; calls {@link #flush()}
 *       as a safety net.</li>
 * </ul>
 *
 * <p>When Oculus is not installed the session is inert: every method degrades
 * to the original passthrough, so vanilla behaviour is unchanged.
 */
public final class DraconicBlockEntityRenderSession {
    private final ResettableBufferSource buffers;
    private final List<RenderType> pending = new ArrayList<>();
    private boolean active;

    public DraconicBlockEntityRenderSession(int initialBytes) {
        this.buffers = new ResettableBufferSource(initialBytes);
    }

    public boolean isActive() {
        return active;
    }

    public void enter() {
        this.active = CompatMods.isOculusLoaded();
        if (!this.active) {
            return;
        }

        ShaderCompat.enterDraconicRender();
    }

    public void exit() {
        flush();
        if (this.active) {
            ShaderCompat.exitDraconicRender();
        }
        this.active = false;
    }

    public void prepareForBind() {
        if (!this.active) {
            return;
        }

        this.buffers.resetBufferState();
        this.pending.clear();
    }

    public void bind(CCRenderState ccrs, RenderType type, MultiBufferSource original) {
        if (!this.active) {
            ccrs.bind(type, original);
            return;
        }

        RenderType wrapped = wrap(type);
        if (!this.pending.contains(wrapped)) {
            this.pending.add(wrapped);
        }
        ccrs.bind(wrapped, this.buffers);
    }

    public void bind(CCRenderState ccrs, RenderType type, MultiBufferSource original, PoseStack poseStack) {
        if (!this.active) {
            ccrs.bind(type, original, poseStack);
            return;
        }

        RenderType wrapped = wrap(type);
        if (!this.pending.contains(wrapped)) {
            this.pending.add(wrapped);
        }
        ccrs.bind(wrapped, this.buffers, poseStack);
    }

    public void flush() {
        if (!this.active || this.pending.isEmpty()) {
            return;
        }

        ShaderCompat.bindPipelineWriteTargetBeforeBatchedVboDraw();
        for (RenderType type : this.pending) {
            this.buffers.endBatch(type);
        }
        this.buffers.resetBufferState();
        this.pending.clear();
    }

    private static RenderType wrap(RenderType type) {
        return OuterWrappedRenderType.wrapExactlyOnce(
                "iris:is_block_entity", type, BlockEntityRenderStateShard.INSTANCE);
    }
}