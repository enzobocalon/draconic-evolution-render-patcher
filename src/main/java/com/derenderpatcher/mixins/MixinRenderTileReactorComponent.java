package com.derenderpatcher.mixins;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import com.brandon3055.draconicevolution.blocks.reactor.tileentity.TileReactorComponent;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.client.render.tile.RenderTileReactorComponent;
import com.derenderpatcher.compat.DraconicImmediateRenderSession;
import com.derenderpatcher.compat.RenderStateAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
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

@Mixin(value = RenderTileReactorComponent.class, remap = false)
public abstract class MixinRenderTileReactorComponent implements BlockEntityRenderer<TileReactorComponent> {
    @Unique
    private static final ResourceLocation DERENDERPATCHER$REACTOR_STABILIZER =
            ResourceLocation.fromNamespaceAndPath(
                    DraconicEvolution.MODID,
                    "textures/block/reactor/reactor_stabilizer.png");

    @Unique
    private static final ResourceLocation DERENDERPATCHER$REACTOR_INJECTOR =
            ResourceLocation.fromNamespaceAndPath(
                    DraconicEvolution.MODID,
                    "textures/block/reactor/reactor_injector.png");

    @Mutable
    @Shadow
    @Final
    private static RenderType STAB_GLOW_TYPE;

    @Mutable
    @Shadow
    @Final
    private static RenderType INJECTOR_GLOW_TYPE;

    @Shadow
    public static void renderStabilizer(CCRenderState ccrs, Matrix4 mat, MultiBufferSource getter, float rotation, float brightness, int packedLight, int packedOverlay) {
    }

    @Shadow
    public static void renderInjector(CCRenderState ccrs, Matrix4 mat, MultiBufferSource getter, float brightness, int packedLight, int packedOverlay) {
    }

    @Unique
    private final DraconicImmediateRenderSession derenderpatcher$session =
            new DraconicImmediateRenderSession(512 * 1024, false);

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void derenderpatcher$replaceGlowRenderTypes(CallbackInfo ci) {
        STAB_GLOW_TYPE = derenderpatcher$createReactorGlowType("stab_glow", DERENDERPATCHER$REACTOR_STABILIZER);
        INJECTOR_GLOW_TYPE = derenderpatcher$createReactorGlowType("injector_glow", DERENDERPATCHER$REACTOR_INJECTOR);
    }

    @Unique
    private static RenderType derenderpatcher$createReactorGlowType(String name, ResourceLocation texture) {
        return RenderType.create(DraconicEvolution.MODID + ":derenderpatcher_" + name, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntitySolidShader))
                .setTransparencyState(RenderStateAccess.lightningTransparency())
                .createCompositeState(false)
        );
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void derenderpatcher$enterReactorComponentRender(TileReactorComponent tile, float partialTicks, PoseStack poseStack,
                                                            MultiBufferSource getter, int packedLight, int packedOverlay,
                                                            CallbackInfo ci) {
        derenderpatcher$session.begin(getter);
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/brandon3055/draconicevolution/client/render/tile/RenderTileReactorComponent;renderStabilizer(Lcodechicken/lib/render/CCRenderState;Lcodechicken/lib/vec/Matrix4;Lnet/minecraft/client/renderer/MultiBufferSource;FFII)V"
            )
    )
    private void derenderpatcher$renderStabilizerWithPrivateBuffer(CCRenderState ccrs, Matrix4 mat, MultiBufferSource getter,
                                                                  float rotation, float brightness, int packedLight,
                                                                  int packedOverlay) {
        renderStabilizer(ccrs, mat, derenderpatcher$getReactorComponentBufferOrOriginal(getter), rotation, brightness, packedLight, packedOverlay);
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/brandon3055/draconicevolution/client/render/tile/RenderTileReactorComponent;renderInjector(Lcodechicken/lib/render/CCRenderState;Lcodechicken/lib/vec/Matrix4;Lnet/minecraft/client/renderer/MultiBufferSource;FII)V"
            )
    )
    private void derenderpatcher$renderInjectorWithPrivateBuffer(CCRenderState ccrs, Matrix4 mat, MultiBufferSource getter,
                                                                float brightness, int packedLight, int packedOverlay) {
        renderInjector(ccrs, mat, derenderpatcher$getReactorComponentBufferOrOriginal(getter), brightness, packedLight, packedOverlay);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void derenderpatcher$exitReactorComponentRender(TileReactorComponent tile, float partialTicks, PoseStack poseStack,
                                                           MultiBufferSource getter, int packedLight, int packedOverlay,
                                                           CallbackInfo ci) {
        derenderpatcher$session.finish();
    }

    @Unique
    private MultiBufferSource derenderpatcher$getReactorComponentBufferOrOriginal(MultiBufferSource getter) {
        return derenderpatcher$session.select(getter);
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(TileReactorComponent tile) {
        return true;
    }
}
