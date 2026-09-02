package com.derenderpatcher.mixins;

import com.brandon3055.draconicevolution.client.render.tile.RenderTileEnergyTransfuser;
import com.derenderpatcher.compat.DraconicImmediateRenderSession;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RenderTileEnergyTransfuser.class, remap = false)
public class MixinRenderTileEnergyTransfuser {
    @Unique
    private final DraconicImmediateRenderSession derenderpatcher$session =
            new DraconicImmediateRenderSession(16 * 1024);

    @Group(name = "derenderpatcher$transfuserText", min = 1, max = 1)
    @Redirect(
            method = "render",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I",
                    remap = false
            ),
            require = 0
    )
    private int derenderpatcher$drawTransfuserTextWithPrivateBufferMcp(Font font, FormattedCharSequence text, float x, float y,
                                                                       int colour, boolean dropShadow, Matrix4f matrix,
                                                                       MultiBufferSource getter, Font.DisplayMode displayMode,
                                                                       int backgroundColour, int packedLight) {
        return derenderpatcher$drawTransfuserTextWithPrivateBuffer(font, text, x, y, colour, dropShadow, matrix,
                getter, displayMode, backgroundColour, packedLight);
    }

    @Group(name = "derenderpatcher$transfuserText", min = 1, max = 1)
    @Redirect(
            method = "render",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;m_272191_(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I",
                    remap = false
            ),
            require = 0
    )
    private int derenderpatcher$drawTransfuserTextWithPrivateBufferSrg(Font font, FormattedCharSequence text, float x, float y,
                                                                       int colour, boolean dropShadow, Matrix4f matrix,
                                                                       MultiBufferSource getter, Font.DisplayMode displayMode,
                                                                       int backgroundColour, int packedLight) {
        return derenderpatcher$drawTransfuserTextWithPrivateBuffer(font, text, x, y, colour, dropShadow, matrix,
                getter, displayMode, backgroundColour, packedLight);
    }

    @Unique
    private int derenderpatcher$drawTransfuserTextWithPrivateBuffer(Font font, FormattedCharSequence text, float x, float y,
                                                                     int colour, boolean dropShadow, Matrix4f matrix,
                                                                     MultiBufferSource getter, Font.DisplayMode displayMode,
                                                                     int backgroundColour, int packedLight) {
        MultiBufferSource selected = derenderpatcher$session.begin(getter);
        try {
            return font.drawInBatch(
                    text, x, y, colour, dropShadow, matrix, selected, displayMode, backgroundColour, packedLight);
        } finally {
            derenderpatcher$session.finish();
        }
    }
}
