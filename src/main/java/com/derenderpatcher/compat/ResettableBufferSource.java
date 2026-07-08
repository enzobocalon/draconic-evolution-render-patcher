package com.derenderpatcher.compat;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.Collections;
import java.util.Optional;

public class ResettableBufferSource extends MultiBufferSource.BufferSource {
    private final BufferBuilder bufferBuilder;

    public ResettableBufferSource(int size) {
        this(new BufferBuilder(size));
    }

    private ResettableBufferSource(BufferBuilder bufferBuilder) {
        super(bufferBuilder, Collections.emptyMap());
        this.bufferBuilder = bufferBuilder;
    }

    public void resetBufferState() {
        lastState = Optional.empty();
        startedBuffers.clear();
        bufferBuilder.discard();
    }
}
