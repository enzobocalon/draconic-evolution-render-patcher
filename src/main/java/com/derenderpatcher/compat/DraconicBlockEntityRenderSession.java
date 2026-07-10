package com.derenderpatcher.compat;

import codechicken.lib.render.CCRenderState;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.layer.BlockEntityRenderStateShard;
import net.irisshaders.iris.layer.OuterWrappedRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * Owns the per-tile render session lifecycle for the Draconic block entities we patch.
 *
 * <p>A session owns one private immediate buffer and the Iris
 * {@code BlockEntityRenderStateShard} phase-bracketing needed so our draws
 * land on the right Oculus pipeline write target with the right shader
 * uniforms, without ever touching the shared Embeddium/Oculus batched
 * {@code MultiBufferSource}.
 *
 * <p>Contract:
 * <ul>
 *   <li>{@link #enter()} at the {@code render()} HEAD of the tile renderer.</li>
 *   <li>{@link #bind} at every {@code ccrs.bind(type, getter)} site we redirect.</li>
 *   <li>Each bind first finalizes a pending batch before starting the next one.</li>
 *   <li>{@link #flush()} at any intermediate flush point; it is a no-op without pending geometry.</li>
 *   <li>{@link #exit()} at the {@code render()} RETURN; calls {@link #flush()}
 *       as a safety net.</li>
 * </ul>
 *
 * <p>When Oculus is not installed the session is inert: every method degrades
 * to the original passthrough, so vanilla behaviour is unchanged.
 */
public final class DraconicBlockEntityRenderSession {
    private final MultiBufferSource.BufferSource buffers;
    private boolean active;
    private boolean hasPendingBatch;

    public DraconicBlockEntityRenderSession(int initialBytes) {
        this.buffers = MultiBufferSource.immediate(new BufferBuilder(initialBytes));
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
        if (!this.active) {
            return;
        }

        try {
            flush();
        } finally {
            this.hasPendingBatch = false;
            this.active = false;
            ShaderCompat.exitDraconicRender();
        }
    }

    public void bind(CCRenderState ccrs, RenderType type, MultiBufferSource original) {
        if (!this.active) {
            ccrs.bind(type, original);
            return;
        }

        flush();
        RenderType wrapped = wrap(type);
        ccrs.bind(wrapped, this.buffers);
        this.hasPendingBatch = true;
    }

    public void bind(CCRenderState ccrs, RenderType type, MultiBufferSource original, PoseStack poseStack) {
        if (!this.active) {
            ccrs.bind(type, original, poseStack);
            return;
        }

        flush();
        RenderType wrapped = wrap(type);
        ccrs.bind(wrapped, this.buffers, poseStack);
        this.hasPendingBatch = true;
    }

    public void flush() {
        if (!this.active || !this.hasPendingBatch) {
            return;
        }

        try {
            ShaderCompat.bindPipelineWriteTargetBeforeBatchedVboDraw();
            this.buffers.endBatch();
        } finally {
            this.hasPendingBatch = false;
        }
    }

    private static RenderType wrap(RenderType type) {
        return OuterWrappedRenderType.wrapExactlyOnce(
                "iris:is_block_entity", type, BlockEntityRenderStateShard.INSTANCE);
    }
}
