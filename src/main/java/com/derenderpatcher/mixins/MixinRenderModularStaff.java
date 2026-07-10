package com.derenderpatcher.mixins;

import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.draconicevolution.client.render.item.RenderModularStaff;
import com.brandon3055.draconicevolution.client.render.modelfx.StaffModelEffect;
import com.derenderpatcher.compat.ShaderCompat;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RenderModularStaff.class, remap = false)
public class MixinRenderModularStaff {
    @Redirect(
            method = "renderTool",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/brandon3055/draconicevolution/client/render/modelfx/StaffModelEffect;renderEffect(Lcodechicken/lib/vec/Matrix4;Lnet/minecraft/client/renderer/MultiBufferSource;FLcom/brandon3055/brandonscore/api/TechLevel;)V"
            )
    )
    private void derenderpatcher$isolateStaffEffectTransform(StaffModelEffect effectRenderer, Matrix4 mat,
                                                              MultiBufferSource buffers, float partialTicks,
                                                              TechLevel techLevel) {
        Matrix4 effectMatrix = mat.copy();
        derenderpatcher$restoreStaffBodyTransform(mat);
        if (ShaderCompat.isShaderPackInUse()) {
            derenderpatcher$restoreStaffBodyTransform(effectMatrix);
        }

        effectRenderer.renderEffect(effectMatrix, buffers, partialTicks, techLevel);
    }

    @Unique
    private static void derenderpatcher$restoreStaffBodyTransform(Matrix4 mat) {
        mat.translate(0.5, -0.1, 0.5);
        mat.rotate(derenderpatcher$toRadians(-90), Vector3.X_NEG);
    }

    @Unique
    private static double derenderpatcher$toRadians(double degrees) {
        return degrees * 0.017453292519943295;
    }
}
