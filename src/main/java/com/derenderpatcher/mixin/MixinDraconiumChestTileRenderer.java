package com.derenderpatcher.mixin;

import com.brandon3055.draconicevolution.client.render.tile.DraconiumChestTileRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DraconiumChestTileRenderer.class)
public class MixinDraconiumChestTileRenderer {

    /**
     * Fix the colour parameter in renderChest to ensure alpha is set.
     * Without this fix, colours like 0x640096 have alpha=0x00 (fully transparent).
     * Vanilla rendering ignores vertex alpha for entityCutout, but Iris shaders
     * respect it, causing the chest to be invisible.
     */
    @ModifyVariable(
            method = "renderChest",
            at = @At("HEAD"),
            ordinal = 2,
            argsOnly = true,
            remap = false
    )
    private int fixColourAlpha(int colour) {
        // If alpha byte is 0, set it to fully opaque (0xFF)
        if ((colour & 0xFF000000) == 0) {
            return colour | 0xFF000000;
        }
        return colour;
    }
}
