package com.derenderpatcher.mixins;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.pipeline.IVertexOperation;
import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.draconicevolution.client.render.item.RenderItemEnergyCrystal;
import com.brandon3055.draconicevolution.client.render.tile.RenderTileEnergyCrystal;
import com.derenderpatcher.compat.EnergyCrystalRenderTypes;
import com.derenderpatcher.compat.DraconicItemRenderSession;
import com.derenderpatcher.compat.ShaderCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RenderItemEnergyCrystal.class, remap = false)
public class MixinRenderItemEnergyCrystal {
    @Shadow
    @Final
    private TechLevel techLevel;

    @Unique
    private final DraconicItemRenderSession derenderpatcher$session = new DraconicItemRenderSession(512 * 1024);

    @Redirect(
            method = "renderItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lcodechicken/lib/render/CCRenderState;bind(Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;)V"
            )
    )
    private void derenderpatcher$bindItemCrystalWithStoredUniforms(CCRenderState ccrs, RenderType renderType, MultiBufferSource getter,
                                                                  ItemStack stack, ItemDisplayContext transform,
                                                                  PoseStack poseStack, MultiBufferSource methodGetter,
                                                                  int packedLight, int packedOverlay) {
        RenderType selectedRenderType = renderType;
        if (ShaderCompat.isShaderPackInUse() && renderType == RenderTileEnergyCrystal.crystalType) {
            selectedRenderType = EnergyCrystalRenderTypes.getCrystalShaderType(techLevel.index, 0F);
        }

        derenderpatcher$session.bind(ccrs, selectedRenderType, getter);
    }

    @Redirect(
            method = "renderItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lcodechicken/lib/render/CCModel;render(Lcodechicken/lib/render/CCRenderState;[Lcodechicken/lib/render/pipeline/IVertexOperation;)V"
            )
    )
    private void derenderpatcher$renderAndFlushItemCrystal(CCModel model, CCRenderState ccrs, IVertexOperation[] operations) {
        try {
            model.render(ccrs, operations);
        } finally {
            derenderpatcher$session.finish();
        }
    }
}
