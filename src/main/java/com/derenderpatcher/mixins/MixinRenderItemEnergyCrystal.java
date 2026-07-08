package com.derenderpatcher.mixins;

import codechicken.lib.render.CCRenderState;
import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.draconicevolution.client.render.item.RenderItemEnergyCrystal;
import com.brandon3055.draconicevolution.client.render.tile.RenderTileEnergyCrystal;
import com.derenderpatcher.compat.EnergyCrystalRenderTypes;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderItemEnergyCrystal.class, remap = false)
public class MixinRenderItemEnergyCrystal {
    @Shadow
    @Final
    private TechLevel techLevel;

    @Unique
    private boolean derenderpatcher$enteredItemCrystalRender;

    @Inject(method = "renderItem", at = @At("HEAD"))
    private void derenderpatcher$enterItemCrystalRender(ItemStack stack, ItemDisplayContext transform, PoseStack poseStack,
                                                       MultiBufferSource getter, int packedLight, int packedOverlay,
                                                       CallbackInfo ci) {
        derenderpatcher$enteredItemCrystalRender = ShaderCompat.isShaderPackInUse();
        if (derenderpatcher$enteredItemCrystalRender) {
            ShaderCompat.enterDraconicRender();
        }
    }

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

        ccrs.bind(selectedRenderType, getter);
    }

    @Inject(method = "renderItem", at = @At("RETURN"))
    private void derenderpatcher$exitItemCrystalRender(ItemStack stack, ItemDisplayContext transform, PoseStack poseStack,
                                                      MultiBufferSource getter, int packedLight, int packedOverlay,
                                                      CallbackInfo ci) {
        if (derenderpatcher$enteredItemCrystalRender) {
            ShaderCompat.exitDraconicRender();
        }

        derenderpatcher$enteredItemCrystalRender = false;
    }
}
