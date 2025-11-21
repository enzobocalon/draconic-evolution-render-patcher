package com.derenderpatcher.compat;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.embeddedt.embeddium.impl.render.EmbeddiumWorldRenderer;

import java.util.function.Consumer;

public class EmbeddiumCompat implements IWorldRendererCompat {
    @Override
    public void forEachVisibleBlockEntity(Consumer<BlockEntity> consumer) {
        EmbeddiumWorldRenderer.instance().forEachVisibleBlockEntity(consumer);
    }
}