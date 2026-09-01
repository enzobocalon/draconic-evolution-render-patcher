package com.derenderpatcher.mixins;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import com.brandon3055.brandonscore.client.render.MultiBlockRenderers;
import com.brandon3055.brandonscore.multiblock.MultiBlockDefinition;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyCore;
import com.brandon3055.draconicevolution.client.DEShaders;
import com.brandon3055.draconicevolution.client.render.tile.RenderTileEnergyCore;
import com.derenderpatcher.compat.CompatMods;
import com.derenderpatcher.compat.DraconicBlockEntityRenderSession;
import com.derenderpatcher.compat.EnergyCoreShaderTypes;
import com.derenderpatcher.compat.EnergyCoreStabilizerRenderer;
import com.derenderpatcher.compat.RenderStateAccess;
import com.derenderpatcher.compat.ShaderCompat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderTileEnergyCore.class, remap = false)
public class MixinRenderTileEnergyCore {
    @Unique
    private static final ResourceLocation DERENDERPATCHER$ENERGY_CORE_BASE =
            ResourceLocation.fromNamespaceAndPath(
                    DraconicEvolution.MODID,
                    "textures/block/energy_core/energy_core_base.png");

    @Unique
    private static final ResourceLocation DERENDERPATCHER$ENERGY_CORE_OVERLAY =
            ResourceLocation.fromNamespaceAndPath(
                    DraconicEvolution.MODID,
                    "textures/block/energy_core/energy_core_overlay.png");

    @Unique
    private static final ResourceLocation DERENDERPATCHER$STABILIZER_SPHERE =
            ResourceLocation.fromNamespaceAndPath(
                    DraconicEvolution.MODID,
                    "textures/block/energy_core/stabilizer_sphere.png");

    @Unique
    private static final ResourceLocation DERENDERPATCHER$STABILIZER_BEAM =
            ResourceLocation.fromNamespaceAndPath(
                    DraconicEvolution.MODID,
                    "textures/block/energy_core/stabilizer_beam.png");

    @Mutable
    @Shadow
    @Final
    private static RenderType innerCoreType;

    @Mutable
    @Shadow
    @Final
    private static RenderType outerCoreType;

    @Mutable
    @Shadow
    @Final
    private static RenderType innerStabType;

    @Mutable
    @Shadow
    @Final
    private static RenderType outerStabType;

    @Mutable
    @Shadow
    @Final
    private static RenderType beamType;

    @Mutable
    @Shadow
    @Final
    private static RenderType outerBeamType;

    @Shadow
    private static RenderType coreShaderType;

    @Shadow
    @Final
    private CCModel modelStabilizerSphere;

    @Shadow
    private void renderStabilizerBeam(TileEnergyCore te, Matrix4 matrix4, MultiBufferSource getter, BlockPos vec, float partialTick) {
    }

    @Unique
    private final DraconicBlockEntityRenderSession derenderpatcher$session = new DraconicBlockEntityRenderSession(512 * 1024);

    @Unique
    private final EnergyCoreStabilizerRenderer derenderpatcher$stabilizerRenderer = new EnergyCoreStabilizerRenderer();

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void derenderpatcher$replaceEnergyCoreRenderTypes(CallbackInfo ci) {
        if (!CompatMods.isOculusLoaded()) {
            return;
        }

        innerCoreType = derenderpatcher$createEntitySolidType("inner_core", DERENDERPATCHER$ENERGY_CORE_BASE);

        outerCoreType = RenderType.create(
                DraconicEvolution.MODID + ":derenderpatcher_outer_core",
                DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
                RenderType.CompositeState.builder()
                        .setTextureState(new RenderStateShard.TextureStateShard(DERENDERPATCHER$ENERGY_CORE_OVERLAY, false, false))
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> com.brandon3055.brandonscore.client.shader.BCShaders.posColourTexAlpha0))
                        .setTransparencyState(RenderStateAccess.translucentTransparency())
                        .createCompositeState(false));

        innerStabType = RenderType.create(
                DraconicEvolution.MODID + ":derenderpatcher_inner_stab",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true,
                RenderType.CompositeState.builder()
                        .setTextureState(new RenderStateShard.TextureStateShard(DERENDERPATCHER$STABILIZER_SPHERE, false, false))
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> com.brandon3055.brandonscore.client.shader.BCShaders.posColourTexAlpha0))
                        .setTransparencyState(RenderStateAccess.noTransparency())
                        .createCompositeState(false));

        outerStabType = RenderType.create(
                DraconicEvolution.MODID + ":derenderpatcher_outer_stab",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true,
                RenderType.CompositeState.builder()
                        .setTextureState(new RenderStateShard.TextureStateShard(DERENDERPATCHER$STABILIZER_SPHERE, false, false))
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> com.brandon3055.brandonscore.client.shader.BCShaders.posColourTexAlpha0))
                        .setTransparencyState(RenderStateAccess.translucentTransparency())
                        .createCompositeState(false));

        beamType = RenderType.create(
                "derenderpatcher_inner_beam",
                DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, true,
                RenderType.CompositeState.builder()
                        .setTextureState(new RenderStateShard.TextureStateShard(DERENDERPATCHER$STABILIZER_BEAM, false, false))
                        .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionTexShader))
                        .setTransparencyState(RenderStateAccess.translucentTransparency())
                        .createCompositeState(false));

        outerBeamType = RenderType.create(
                "derenderpatcher_outer_beam",
                DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.TRIANGLE_STRIP, 256, false, false,
                RenderType.CompositeState.builder()
                        .setTextureState(new RenderStateShard.TextureStateShard(DERENDERPATCHER$STABILIZER_BEAM, false, false))
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> com.brandon3055.brandonscore.client.shader.BCShaders.posColourTexAlpha0))
                        .setTransparencyState(RenderStateAccess.translucentTransparency())
                        .setWriteMaskState(RenderStateAccess.colorWrite())
                        .createCompositeState(false));

        coreShaderType = RenderType.create(
                DraconicEvolution.MODID + ":derenderpatcher_energy_core_shader",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true,
                RenderType.CompositeState.builder()
                        .setTextureState(new RenderStateShard.TextureStateShard(DERENDERPATCHER$ENERGY_CORE_OVERLAY, false, false))
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> DEShaders.energyCoreShader))
                        .setTransparencyState(RenderStateAccess.translucentTransparency())
                        .setCullState(RenderStateAccess.noCull())
                        .createCompositeState(false));
    }

    @Unique
    private static RenderType derenderpatcher$createEntitySolidType(String name, ResourceLocation texture) {
        return RenderType.create(
                DraconicEvolution.MODID + ":derenderpatcher_" + name,
                DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false,
                RenderType.CompositeState.builder()
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntitySolidShader))
                        .setLightmapState(RenderStateAccess.lightmap())
                        .setOverlayState(RenderStateAccess.overlay())
                        .createCompositeState(true));
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/brandon3055/brandonscore/client/render/MultiBlockRenderers;renderBuildGuide(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/brandon3055/brandonscore/multiblock/MultiBlockDefinition;IF)V"
            )
    )
    private void derenderpatcher$renderBuildGuideWithVanillaBuffer(
            Level level, BlockPos inWorldOrigin, PoseStack poseStack, MultiBufferSource getter,
            MultiBlockDefinition structure, int packedLight, float partialTicks) {
        if (!ShaderCompat.isShaderPackInUse()) {
            MultiBlockRenderers.renderBuildGuide(level, inWorldOrigin, poseStack, getter, structure, packedLight, partialTicks);
            return;
        }

        MultiBufferSource.BufferSource safeGetter = Minecraft.getInstance().renderBuffers().bufferSource();
        MultiBlockRenderers.renderBuildGuide(level, inWorldOrigin, poseStack, safeGetter, structure, packedLight, partialTicks);
        safeGetter.endBatch();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void derenderpatcher$enterEnergyCoreRender(TileEnergyCore te, float partialTicks, PoseStack poseStack,
                                                       MultiBufferSource getter, int packedLight, int packedOverlay,
                                                       CallbackInfo ci) {
        derenderpatcher$session.enter();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void derenderpatcher$exitEnergyCoreRender(TileEnergyCore te, float partialTicks, PoseStack poseStack,
                                                      MultiBufferSource getter, int packedLight, int packedOverlay,
                                                      CallbackInfo ci) {
        derenderpatcher$session.exit();
    }

    @Redirect(
            method = "renderInnerCore",
            at = @At(
                    value = "INVOKE",
                    target = "Lcodechicken/lib/render/CCRenderState;bind(Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;)V"
            )
    )
    private void derenderpatcher$bindInnerCore(CCRenderState ccrs, RenderType renderType, MultiBufferSource getter,
                                               TileEnergyCore te, CCRenderState methodCcrs, Matrix4 mat,
                                               MultiBufferSource methodGetter, float partialTicks,
                                               float rotation, double scale) {
        derenderpatcher$session.bind(ccrs, renderType, getter);
    }

    @Redirect(
            method = "renderLegacyOuterCore",
            at = @At(
                    value = "INVOKE",
                    target = "Lcodechicken/lib/render/CCRenderState;bind(Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;)V"
            )
    )
    private void derenderpatcher$bindLegacyOuterCore(CCRenderState ccrs, RenderType renderType, MultiBufferSource getter,
                                                     TileEnergyCore te, CCRenderState methodCcrs, Matrix4 mat,
                                                     MultiBufferSource methodGetter, float partialTicks,
                                                     float rotation, double scale) {
        derenderpatcher$session.bind(ccrs, renderType, getter);
    }

    @Redirect(
            method = "renderFancyOuterCore",
            at = @At(
                    value = "INVOKE",
                    target = "Lcodechicken/lib/render/CCRenderState;bind(Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;)V"
            )
    )
    private void derenderpatcher$bindFancyOuterCore(CCRenderState ccrs, RenderType renderType, MultiBufferSource getter,
                                                    TileEnergyCore te, CCRenderState methodCcrs, Matrix4 mat,
                                                    MultiBufferSource methodGetter, float partialTicks,
                                                    float rotation, double scale) {
        if (!derenderpatcher$session.isActive()) {
            ccrs.bind(renderType, getter);
            return;
        }

        RenderType selectedRenderType = EnergyCoreShaderTypes.getCoreShaderType(te);
        derenderpatcher$session.bind(ccrs, selectedRenderType, getter);
    }

    @Inject(
            method = "renderLegacyOuterCore",
            at = @At(
                    value = "INVOKE",
                    target = "Lcodechicken/lib/render/CCRenderState;bind(Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;)V"
            )
    )
    private void derenderpatcher$setLegacyOuterCoreColour(TileEnergyCore te, CCRenderState ccrs, Matrix4 mat,
                                                           MultiBufferSource getter, float partialTicks,
                                                           float rotation, double scale, CallbackInfo ci) {
        if (!ShaderCompat.isShaderPackInUse()) {
            ccrs.baseColour = EnergyCoreShaderTypes.getLegacyCoreOverlayColour(te);
        }
    }

    @Inject(method = {"renderInnerCore", "renderLegacyOuterCore"}, at = @At("RETURN"))
    private void derenderpatcher$flushCoreBuffer(TileEnergyCore te, CCRenderState ccrs, Matrix4 mat,
                                                 MultiBufferSource getter, float partialTicks, float rotation,
                                                 double scale, CallbackInfo ci) {
        derenderpatcher$session.flush();
    }

    @Inject(method = "renderFancyOuterCore", at = @At("RETURN"))
    private void derenderpatcher$flushFancyCoreBuffer(TileEnergyCore te, CCRenderState ccrs, Matrix4 mat,
                                                       MultiBufferSource getter, float partialTicks, float rotation,
                                                       double scale, CallbackInfo ci) {
        derenderpatcher$session.flush();
    }

    @Inject(method = "renderStabilizers", at = @At("HEAD"), cancellable = true)
    private void derenderpatcher$renderStabilizersWithPrivateBuffers(TileEnergyCore te, CCRenderState ccrs, Matrix4 matrix4,
                                                                     MultiBufferSource getter, float partialTick,
                                                                     CallbackInfo ci) {
        if (!ShaderCompat.isShaderPackInUse()) {
            return;
        }

        ci.cancel();
        derenderpatcher$stabilizerRenderer.renderStabilizers(
                te, ccrs, matrix4, partialTick,
                modelStabilizerSphere,
                innerStabType, outerStabType, beamType, outerBeamType,
                this::renderStabilizerBeam);
    }
}
