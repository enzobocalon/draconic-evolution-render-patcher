package com.derenderpatcher.compat;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;

import java.util.function.Consumer;

public class RendererCompat {
    private static IWorldRendererCompat instance;

    public static void forEachVisibleBlockEntity(Consumer<BlockEntity> consumer) {
        if (instance == null) {
            if (ModList.get().isLoaded("embeddium")) {
                instance = new EmbeddiumCompat();
            } else if (ModList.get().isLoaded("sodium")) {
                instance = new SodiumCompat();
            } else {
                throw new RuntimeException("Neither Embeddium nor Sodium found!");
            }
        }
        instance.forEachVisibleBlockEntity(consumer);
    }
}