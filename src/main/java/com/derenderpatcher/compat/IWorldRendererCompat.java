package com.derenderpatcher.compat;

import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Consumer;

public interface IWorldRendererCompat {
    void forEachVisibleBlockEntity(Consumer<BlockEntity> consumer);
}