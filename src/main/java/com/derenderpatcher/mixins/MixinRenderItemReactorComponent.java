package com.derenderpatcher.mixins;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import com.brandon3055.draconicevolution.client.render.item.RenderItemReactorComponent;
import com.brandon3055.draconicevolution.client.render.tile.RenderTileReactorComponent;
import com.brandon3055.draconicevolution.client.render.tile.RenderTileReactorCore;
import com.derenderpatcher.compat.DraconicItemRenderSession;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(value = RenderItemReactorComponent.class, remap = false)
public class MixinRenderItemReactorComponent {
    @Unique
    private final DraconicItemRenderSession derenderpatcher$session = new DraconicItemRenderSession(1024 * 1024);

    @Redirect(
            method = "renderItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/brandon3055/draconicevolution/client/render/tile/RenderTileReactorCore;renderCore(Lcodechicken/lib/vec/Matrix4;Lcodechicken/lib/render/CCRenderState;FDFFFLnet/minecraft/client/renderer/MultiBufferSource;)V"
            )
    )
    private void derenderpatcher$renderCorePrivately(Matrix4 matrix, CCRenderState ccrs, float rotation,
                                                     double animation, float shieldCharge, float coreDiameter,
                                                     float brightness, MultiBufferSource getter) {
        derenderpatcher$withPrivateBuffer(getter, selected ->
                RenderTileReactorCore.renderCore(matrix, ccrs, rotation, animation, shieldCharge, coreDiameter, brightness, selected));
    }

    @Redirect(
            method = "renderItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/brandon3055/draconicevolution/client/render/tile/RenderTileReactorComponent;renderStabilizer(Lcodechicken/lib/render/CCRenderState;Lcodechicken/lib/vec/Matrix4;Lnet/minecraft/client/renderer/MultiBufferSource;FFII)V"
            )
    )
    private void derenderpatcher$renderStabilizerPrivately(CCRenderState ccrs, Matrix4 matrix,
                                                           MultiBufferSource getter, float rotation, float brightness,
                                                           int packedLight, int packedOverlay) {
        derenderpatcher$withPrivateBuffer(getter, selected ->
                RenderTileReactorComponent.renderStabilizer(ccrs, matrix, selected, rotation, brightness, packedLight, packedOverlay));
    }

    @Redirect(
            method = "renderItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/brandon3055/draconicevolution/client/render/tile/RenderTileReactorComponent;renderInjector(Lcodechicken/lib/render/CCRenderState;Lcodechicken/lib/vec/Matrix4;Lnet/minecraft/client/renderer/MultiBufferSource;FII)V"
            )
    )
    private void derenderpatcher$renderInjectorPrivately(CCRenderState ccrs, Matrix4 matrix,
                                                         MultiBufferSource getter, float brightness,
                                                         int packedLight, int packedOverlay) {
        derenderpatcher$withPrivateBuffer(getter, selected ->
                RenderTileReactorComponent.renderInjector(ccrs, matrix, selected, brightness, packedLight, packedOverlay));
    }

    @Redirect(
            method = "renderItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/brandon3055/draconicevolution/client/render/tile/RenderTileReactorComponent;renderComponent(Lnet/minecraft/world/item/Item;Lcodechicken/lib/render/CCRenderState;Lcodechicken/lib/vec/Matrix4;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"
            )
    )
    private void derenderpatcher$renderComponentPrivately(Item item, CCRenderState ccrs, Matrix4 matrix,
                                                          MultiBufferSource getter, int packedLight, int packedOverlay) {
        derenderpatcher$withPrivateBuffer(getter, selected ->
                RenderTileReactorComponent.renderComponent(item, ccrs, matrix, selected, packedLight, packedOverlay));
    }

    @Unique
    private void derenderpatcher$withPrivateBuffer(MultiBufferSource original, Consumer<MultiBufferSource> renderer) {
        MultiBufferSource selected = derenderpatcher$session.begin(original);
        try {
            renderer.accept(selected);
        } finally {
            derenderpatcher$session.finish();
        }
    }
}
