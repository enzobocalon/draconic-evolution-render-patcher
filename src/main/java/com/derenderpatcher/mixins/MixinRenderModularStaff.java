package com.derenderpatcher.mixins;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Vector3;
import com.brandon3055.draconicevolution.client.render.item.RenderModularStaff;
import com.derenderpatcher.compat.ShaderCompat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderModularStaff.class, remap = false)
public class MixinRenderModularStaff {
    @Inject(
            method = "renderTool",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/brandon3055/draconicevolution/client/render/modelfx/StaffModelEffect;renderEffect(Lcodechicken/lib/vec/Matrix4;Lnet/minecraft/client/renderer/MultiBufferSource;FLcom/brandon3055/brandonscore/api/TechLevel;)V"
            )
    )
    private void derenderpatcher$fixStaffEffectTransform(CCRenderState ccrs, ItemStack stack, ItemDisplayContext transform, Matrix4 mat, MultiBufferSource buffers, boolean gui, CallbackInfo ci) {
        if (!ShaderCompat.isShaderPackInUse()) {
            return;
        }

        mat.translate(0.5, -0.1, 0.5);
        mat.rotate(derenderpatcher$toRadians(-90), Vector3.X_NEG);
    }

    @Unique
    private static double derenderpatcher$toRadians(double degrees) {
        return degrees * 0.017453292519943295;
    }
}
