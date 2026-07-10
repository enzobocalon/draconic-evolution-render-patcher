package com.derenderpatcher.mixins;

import codechicken.lib.render.CCRenderState;
import com.brandon3055.brandonscore.client.render.RenderUtils;
import com.brandon3055.draconicevolution.blocks.energynet.tileentity.TileCrystalBase;
import com.brandon3055.draconicevolution.client.render.tile.RenderTileEnergyCrystal;
import com.derenderpatcher.compat.DraconicBlockEntityRenderSession;
import com.derenderpatcher.compat.EnergyCrystalRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderTileEnergyCrystal.class, remap = false)
public class MixinRenderTileEnergyCrystal {
    @Shadow
    public static RenderType crystalType;

    @Unique
    private final DraconicBlockEntityRenderSession derenderpatcher$session = new DraconicBlockEntityRenderSession(512 * 1024);

    @Inject(method = "render", at = @At("HEAD"))
    private void derenderpatcher$enterCrystalRender(TileCrystalBase tile, float partialTicks, PoseStack poseStack,
                                                    MultiBufferSource getter, int packedLight, int packedOverlay,
                                                    CallbackInfo ci) {
        derenderpatcher$session.enter();
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcodechicken/lib/render/CCRenderState;bind(Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;)V"
            )
    )
    private void derenderpatcher$bindCrystal(CCRenderState ccrs, RenderType renderType, MultiBufferSource getter,
                                             TileCrystalBase tile, float partialTicks, PoseStack poseStack,
                                             MultiBufferSource methodGetter, int packedLight, int packedOverlay) {
        if (!derenderpatcher$session.isActive()) {
            ccrs.bind(renderType, getter);
            return;
        }

        RenderType selectedRenderType = renderType;
        if (renderType == crystalType) {
            selectedRenderType = EnergyCrystalRenderTypes.getCrystalShaderType(
                    tile.getTier(), derenderpatcher$getCrystalMipmap(tile));
        }
        derenderpatcher$session.bind(ccrs, selectedRenderType, getter);
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/brandon3055/brandonscore/client/render/RenderUtils;endBatch(Lnet/minecraft/client/renderer/MultiBufferSource;)V"
            )
    )
    private void derenderpatcher$flushCrystalBeforeUniformsCanChange(MultiBufferSource getter) {
        derenderpatcher$session.flush();
        RenderUtils.endBatch(getter);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void derenderpatcher$exitCrystalRender(TileCrystalBase tile, float partialTicks, PoseStack poseStack,
                                                   MultiBufferSource getter, int packedLight, int packedOverlay,
                                                   CallbackInfo ci) {
        derenderpatcher$session.exit();
    }

    @Unique
    private static float derenderpatcher$getCrystalMipmap(TileCrystalBase tile) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return 0F;
        }

        BlockPos pos = tile.getBlockPos();
        double dx = player.getX() - (pos.getX() + 0.5D);
        double dy = player.getY() - (pos.getY() + 0.5D);
        double dz = player.getZ() - (pos.getZ() + 0.5D);
        double mipmap = ((dx * dx) + (dy * dy) + (dz * dz) - 5D) / 4096D;
        return (float) Mth.clamp(mipmap, 0D, 1D);
    }
}
