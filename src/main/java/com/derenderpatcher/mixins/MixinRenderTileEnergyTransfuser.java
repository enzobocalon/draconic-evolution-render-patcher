package com.derenderpatcher.mixins;

import com.brandon3055.draconicevolution.client.render.tile.RenderTileEnergyTransfuser;
import com.derenderpatcher.compat.ResettableBufferSource;
import com.derenderpatcher.compat.ShaderCompat;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RenderTileEnergyTransfuser.class, remap = false)
public class MixinRenderTileEnergyTransfuser {
    @Unique
    private final ResettableBufferSource derenderpatcher$textBuffers = new ResettableBufferSource(16 * 1024);

    @Redirect(
            method = "render",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I",
                    remap = true
            )
    )
    private int derenderpatcher$drawTransfuserTextWithPrivateBuffer(Font font, FormattedCharSequence text, float x, float y,
                                                                    int colour, boolean dropShadow, Matrix4f matrix,
                                                                    MultiBufferSource getter, Font.DisplayMode displayMode,
                                                                    int backgroundColour, int packedLight) {
        if (!ShaderCompat.isShaderPackInUse()) {
            return font.drawInBatch(text, x, y, colour, dropShadow, matrix, getter, displayMode, backgroundColour, packedLight);
        }

        ShaderCompat.enterDraconicRender();
        derenderpatcher$textBuffers.resetBufferState();

        try {
            int result = font.drawInBatch(text, x, y, colour, dropShadow, matrix, derenderpatcher$textBuffers, displayMode, backgroundColour, packedLight);
            ShaderCompat.bindPipelineWriteTargetBeforeBatchedVboDraw();
            derenderpatcher$textBuffers.endBatch();
            return result;
        } finally {
            derenderpatcher$textBuffers.resetBufferState();
            ShaderCompat.exitDraconicRender();
        }
    }
}
