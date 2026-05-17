package com.derenderpatcher.mixins;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import com.brandon3055.brandonscore.client.shader.BCShaders;
import com.brandon3055.brandonscore.client.render.MultiBlockRenderers;
import com.brandon3055.brandonscore.multiblock.MultiBlockDefinition;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyCore;
import com.brandon3055.draconicevolution.client.DEShaders;
import com.brandon3055.draconicevolution.client.render.tile.RenderTileEnergyCore;
import com.derenderpatcher.compat.CompatMods;
import com.derenderpatcher.compat.RenderFormats;
import com.derenderpatcher.compat.RenderStateAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = RenderTileEnergyCore.class, remap = false)
public class MixinRenderTileEnergyCore {
    @Unique
    private static final ResourceLocation DERENDERPATCHER$ENERGY_CORE_OVERLAY = new ResourceLocation(DraconicEvolution.MODID, "textures/block/energy_core/energy_core_overlay.png");

    @Unique
    private static final ResourceLocation DERENDERPATCHER$STABILIZER_SPHERE = new ResourceLocation(DraconicEvolution.MODID, "textures/block/energy_core/stabilizer_sphere.png");

    @Unique
    private static final ResourceLocation DERENDERPATCHER$STABILIZER_BEAM = new ResourceLocation(DraconicEvolution.MODID, "textures/block/energy_core/stabilizer_beam.png");

    @Unique
    private static final Map<String, RenderType> DERENDERPATCHER$COLORED_CORE_TYPES = new ConcurrentHashMap<>();

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

    @Unique
    private RenderType derenderpatcher$currentCoreShaderType;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void derenderpatcher$replaceEnergyCoreRenderTypes(CallbackInfo ci) {
        if (!derenderpatcher$isOculusLoaded()) {
            return;
        }

        innerStabType = RenderType.create("derenderpatcher_inner_stab", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(DERENDERPATCHER$STABILIZER_SPHERE, false, false))
                .setShaderState(new RenderStateShard.ShaderStateShard(() -> BCShaders.posColourTexAlpha0))
                .setTransparencyState(RenderStateAccess.noTransparency())
                .createCompositeState(false)
        );

        outerStabType = RenderType.create("derenderpatcher_outer_stab", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(DERENDERPATCHER$STABILIZER_SPHERE, false, false))
                .setShaderState(new RenderStateShard.ShaderStateShard(() -> BCShaders.posColourTexAlpha0))
                .setTransparencyState(RenderStateAccess.translucentTransparency())
                .createCompositeState(false)
        );

        beamType = RenderType.create("derenderpatcher_inner_beam", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(DERENDERPATCHER$STABILIZER_BEAM, false, false))
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionTexShader))
                .setTransparencyState(RenderStateAccess.translucentTransparency())
                .createCompositeState(false)
        );

        outerBeamType = RenderType.create("derenderpatcher_outer_beam", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.TRIANGLE_STRIP, 256, false, false, RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(DERENDERPATCHER$STABILIZER_BEAM, false, false))
                .setShaderState(new RenderStateShard.ShaderStateShard(() -> BCShaders.posColourTexAlpha0))
                .setTransparencyState(RenderStateAccess.translucentTransparency())
                .setWriteMaskState(RenderStateAccess.colorWrite())
                .createCompositeState(false)
        );
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/brandon3055/brandonscore/client/render/MultiBlockRenderers;renderBuildGuide(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/brandon3055/brandonscore/multiblock/MultiBlockDefinition;IF)V"
            )
    )
    private void derenderpatcher$renderBuildGuideWithVanillaBuffer(Level level, BlockPos inWorldOrigin, PoseStack poseStack, MultiBufferSource getter, MultiBlockDefinition structure, int packedLight, float partialTicks) {
        if (!derenderpatcher$isOculusLoaded()) {
            MultiBlockRenderers.renderBuildGuide(level, inWorldOrigin, poseStack, getter, structure, packedLight, partialTicks);
            return;
        }

        MultiBufferSource.BufferSource safeGetter = Minecraft.getInstance().renderBuffers().bufferSource();
        MultiBlockRenderers.renderBuildGuide(level, inWorldOrigin, poseStack, safeGetter, structure, packedLight, partialTicks);
        safeGetter.endBatch();
    }

    @Unique
    private static boolean derenderpatcher$isOculusLoaded() {
        return CompatMods.isOculusLoaded();
    }

    @Inject(method = "renderFancyOuterCore", at = @At("HEAD"))
    private void derenderpatcher$selectCoreShaderType(TileEnergyCore te, CCRenderState ccrs, Matrix4 mat,
                                                      MultiBufferSource getter, float partialTicks,
                                                      float rotation, double scale, CallbackInfo ci) {
        derenderpatcher$currentCoreShaderType = derenderpatcher$getCoreShaderType(te);
    }

    @Redirect(
            method = "renderFancyOuterCore",
            at = @At(
                    value = "INVOKE",
                    target = "Lcodechicken/lib/render/CCRenderState;bind(Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;)V"
            )
    )
    private void derenderpatcher$bindColorIsolatedCoreShader(CCRenderState ccrs, RenderType renderType, MultiBufferSource getter) {
        ccrs.bind(derenderpatcher$currentCoreShaderType == null ? renderType : derenderpatcher$currentCoreShaderType, getter);
    }

    @Inject(method = "renderFancyOuterCore", at = @At("TAIL"))
    private void derenderpatcher$flushCoreShaderBatchAfterVertices(TileEnergyCore te, CCRenderState ccrs, Matrix4 mat,
                                                                   MultiBufferSource getter, float partialTicks,
                                                                   float rotation, double scale, CallbackInfo ci) {
        derenderpatcher$endCoreShaderBatch(getter, derenderpatcher$currentCoreShaderType);
        derenderpatcher$currentCoreShaderType = null;
    }

    @Unique
    private static RenderType derenderpatcher$getCoreShaderType(TileEnergyCore te) {
        boolean tierEight = te.tier.get() == 8;
        int frame;
        int triangle;
        int effect;

        if (te.customColour.get()) {
            frame = te.frameColour.get();
            triangle = te.innerColour.get();
            effect = te.effectColour.get();
        } else {
            frame = tierEight ? TileEnergyCore.DEFAULT_FRAME_COLOUR_T8 : TileEnergyCore.DEFAULT_FRAME_COLOUR;
            triangle = tierEight ? TileEnergyCore.DEFAULT_TRIANGLE_COLOUR_T8 : TileEnergyCore.DEFAULT_TRIANGLE_COLOUR;
            effect = tierEight ? TileEnergyCore.DEFAULT_EFFECT_COLOUR_T8 : TileEnergyCore.DEFAULT_EFFECT_COLOUR;
        }

        String key = Integer.toHexString(frame) + "_" + Integer.toHexString(triangle) + "_" + Integer.toHexString(effect);
        return DERENDERPATCHER$COLORED_CORE_TYPES.computeIfAbsent(key, ignored -> derenderpatcher$createCoreShaderType(key, frame, triangle, effect));
    }

    @Unique
    private static RenderType derenderpatcher$createCoreShaderType(String key, int frame, int triangle, int effect) {
        float[] frameRgb = derenderpatcher$unpack(frame);
        float[] triangleRgb = derenderpatcher$unpack(triangle);
        float[] effectRgb = derenderpatcher$unpack(effect);

        return RenderType.create("derenderpatcher_energy_core_" + key, RenderFormats.positionColorTexLightmap(), VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(DERENDERPATCHER$ENERGY_CORE_OVERLAY, false, false))
                .setShaderState(new RenderStateShard.ShaderStateShard(() -> {
                    derenderpatcher$applyCoreUniforms(frameRgb, triangleRgb, effectRgb);
                    return DEShaders.energyCoreShader;
                }))
                .setTransparencyState(RenderStateAccess.translucentTransparency())
                .setCullState(RenderStateAccess.noCull())
                .createCompositeState(false)
        );
    }

    @Unique
    private static void derenderpatcher$applyCoreUniforms(float[] frameRgb, float[] triangleRgb, float[] effectRgb) {
        DEShaders.energyCoreActivation.glUniform1f(1);
        DEShaders.energyCoreFrameColour.glUniform3f(frameRgb[0], frameRgb[1], frameRgb[2]);
        DEShaders.energyCoreRotTriColour.glUniform3f(triangleRgb[0], triangleRgb[1], triangleRgb[2]);
        DEShaders.energyCoreEffectColour.glUniform3f(effectRgb[0], effectRgb[1], effectRgb[2]);
    }

    @Unique
    private static float[] derenderpatcher$unpack(int colour) {
        return new float[]{((colour >> 16) & 0xFF) / 255F, ((colour >> 8) & 0xFF) / 255F, (colour & 0xFF) / 255F};
    }

    @Unique
    private static void derenderpatcher$endCoreShaderBatch(MultiBufferSource getter, RenderType renderType) {
        if (renderType == null) {
            return;
        }

        if (getter instanceof MultiBufferSource.BufferSource bufferSource) {
            bufferSource.endBatch(renderType);
        } else {
            Minecraft.getInstance().renderBuffers().bufferSource().endBatch(renderType);
        }
    }
}
