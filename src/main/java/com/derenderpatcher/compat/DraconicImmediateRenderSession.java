package com.derenderpatcher.compat;

import codechicken.lib.render.CCRenderState;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public final class DraconicImmediateRenderSession {
    private final MultiBufferSource.BufferSource buffers;
    private final boolean bindWriteTargetBeforeFlush;
    private boolean active;

    public DraconicImmediateRenderSession(int initialBytes) {
        this(initialBytes, true);
    }

    public DraconicImmediateRenderSession(int initialBytes, boolean bindWriteTargetBeforeFlush) {
        this.buffers = MultiBufferSource.immediate(new BufferBuilder(initialBytes));
        this.bindWriteTargetBeforeFlush = bindWriteTargetBeforeFlush;
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

    public MultiBufferSource select(MultiBufferSource original) {
        return this.active ? this.buffers : original;
    }

    public void finish() {
        if (!this.active) {
            return;
        }

        try {
            if (this.bindWriteTargetBeforeFlush) {
                ShaderCompat.bindPipelineWriteTargetBeforeBatchedVboDraw();
            }
            this.buffers.endBatch();
        } finally {
            this.active = false;
            ShaderCompat.exitDraconicRender();
        }
    }
}
