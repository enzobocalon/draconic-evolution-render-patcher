package com.derenderpatcher.mixins;

import codechicken.lib.render.CCRenderState;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyCoreStabilizer;
import com.brandon3055.draconicevolution.client.render.tile.RenderEnergyCoreStabilizer;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.derenderpatcher.compat.CompatMods;
import com.derenderpatcher.compat.DraconicBlockEntityRenderSession;
import com.derenderpatcher.compat.RenderFormats;
import com.derenderpatcher.compat.RenderStateAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderEnergyCoreStabilizer.class, remap = false)
public abstract class MixinRenderEnergyCoreStabilizer implements BlockEntityRenderer<TileEnergyCoreStabilizer> {
    @Unique
    private static final ResourceLocation DERENDERPATCHER$STABILIZER_LARGE =
            new ResourceLocation(DraconicEvolution.MODID, "textures/block/energy_core/stabilizer_large.png");

    @Shadow
    @Final
    private static RenderType MODEL_TYPE;

    @Mutable
    @Shadow
    @Final
    private static RenderType MODEL_TYPE_ACTIVE;

    @Unique
    private final DraconicBlockEntityRenderSession derenderpatcher$session = new DraconicBlockEntityRenderSession(256 * 1024);

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void derenderpatcher$replaceActiveStabilizerRenderType(CallbackInfo ci) {
        if (!CompatMods.isOculusLoaded()) {
            return;
        }

        MODEL_TYPE_ACTIVE = RenderType.create(
                DraconicEvolution.MODID + ":derenderpatcher_energy_core_stabilizer_active",
                RenderFormats.positionColorTexLightmap(), VertexFormat.Mode.QUADS, 256, false, true,
                RenderType.CompositeState.builder()
                        .setTextureState(new RenderStateShard.TextureStateShard(DERENDERPATCHER$STABILIZER_LARGE, false, false))
                        .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorTexLightmapShader))
                        .setLightmapState(RenderStateAccess.lightmap())
                        .createCompositeState(false));
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void derenderpatcher$enterStabilizerRender(TileEnergyCoreStabilizer tile, float partialTicks,
                                                       PoseStack poseStack, MultiBufferSource getter,
                                                       int packedLight, int packedOverlay, CallbackInfo ci) {
        derenderpatcher$session.enter();
        derenderpatcher$session.prepareForBind();
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcodechicken/lib/render/CCRenderState;bind(Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;)V"
            )
    )
    private void derenderpatcher$bindStabilizerPrivately(CCRenderState ccrs, RenderType renderType, MultiBufferSource getter, PoseStack poseStack,
                                                         TileEnergyCoreStabilizer tile, float partialTicks,
                                                         PoseStack methodPoseStack, MultiBufferSource methodGetter,
                                                         int packedLight, int packedOverlay) {
        derenderpatcher$session.bind(ccrs, renderType, getter, poseStack);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void derenderpatcher$flushStabilizerRenderType(TileEnergyCoreStabilizer tile, float partialTicks,
                                                           PoseStack poseStack, MultiBufferSource getter,
                                                           int packedLight, int packedOverlay, CallbackInfo ci) {
        derenderpatcher$session.exit();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(TileEnergyCoreStabilizer tile) {
        return true;
    }
}