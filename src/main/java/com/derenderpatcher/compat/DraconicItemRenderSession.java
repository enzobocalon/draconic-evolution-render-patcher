package com.derenderpatcher.compat;

import codechicken.lib.render.CCRenderState;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/** Owns one immediate buffer and the Draconic shader lifecycle for an item draw. */
public final class DraconicItemRenderSession {
    private final MultiBufferSource.BufferSource buffers;
    private boolean active;

    public DraconicItemRenderSession(int initialBytes) {
        this.buffers = MultiBufferSource.immediate(new BufferBuilder(initialBytes));
    }

    public void bind(CCRenderState ccrs, RenderType type, MultiBufferSource original) {
        MultiBufferSource selected = begin(original);
        boolean bound = false;
        try {
            ccrs.bind(type, selected);
            bound = true;
        } finally {
            if (!bound) {
                finish();
            }
        }
    }

    public MultiBufferSource begin(MultiBufferSource original) {
        if (!ShaderCompat.isShaderPackInUse()) {
            return original;
        }

        if (!this.active) {
            ShaderCompat.enterDraconicRender();
            this.active = true;
        }
        return this.buffers;
    }

    public void finish() {
        if (!this.active) {
            return;
        }

        try {
            ShaderCompat.bindPipelineWriteTargetBeforeBatchedVboDraw();
            this.buffers.endBatch();
        } finally {
            this.active = false;
            ShaderCompat.exitDraconicRender();
        }
    }
}
