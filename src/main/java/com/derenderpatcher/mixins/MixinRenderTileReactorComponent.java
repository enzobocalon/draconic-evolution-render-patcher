package com.derenderpatcher.mixins;

import com.brandon3055.draconicevolution.blocks.reactor.tileentity.TileReactorComponent;
import com.brandon3055.draconicevolution.client.render.tile.RenderTileReactorComponent;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = RenderTileReactorComponent.class, remap = false)
public abstract class MixinRenderTileReactorComponent implements BlockEntityRenderer<TileReactorComponent> {
    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(TileReactorComponent tile) {
        return true;
    }
}
