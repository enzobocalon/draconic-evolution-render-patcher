package com.derenderpatcher.compat;

import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Consumer;

public class SodiumCompat implements IWorldRendererCompat {
    @Override
    public void forEachVisibleBlockEntity(Consumer<BlockEntity> consumer) {
        SodiumWorldRenderer.instance().iterateVisibleBlockEntities(consumer);
    }
}
