package com.derenderpatcher.mixin;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyCore;
import com.brandon3055.draconicevolution.client.DEShaders;
import com.brandon3055.draconicevolution.client.render.tile.RenderTileEnergyCore;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(RenderTileEnergyCore.class)
public class MixinRenderTileEnergyCore {

    @Unique
    private static final ResourceLocation DERENDERPATCHER$ENERGY_CORE_OVERLAY = ResourceLocation.fromNamespaceAndPath(DraconicEvolution.MODID, "textures/block/energy_core/energy_core_overlay.png");
    @Unique
    private static final Map<String, RenderType> DERENDERPATCHER$COLORED_CORE_TYPES = new ConcurrentHashMap<>();

    @Shadow(remap = false)
    private static RenderType coreShaderType;

    @Unique
    private RenderType derenderpatcher$currentCoreShaderType;

    @Inject(method = "renderFancyOuterCore", at = @At("HEAD"), remap = false)
    private void derenderpatcher$selectCoreShaderType(TileEnergyCore te, CCRenderState ccrs, Matrix4 mat,
                                                      MultiBufferSource getter, float partialTicks,
                                                      float rotation, double scale, CallbackInfo ci) {
        derenderpatcher$endCoreShaderBatch(getter, coreShaderType);
        derenderpatcher$currentCoreShaderType = derenderpatcher$getCoreShaderType(te);
        derenderpatcher$endCoreShaderBatch(getter, derenderpatcher$currentCoreShaderType);
    }

    @Redirect(
            method = "renderFancyOuterCore",
            at = @At(
                    value = "INVOKE",
                    target = "Lcodechicken/lib/render/CCRenderState;bind(Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;)V"
            ),
            remap = false
    )
    private void derenderpatcher$bindColorIsolatedCoreShader(CCRenderState ccrs, RenderType renderType, MultiBufferSource getter) {
        ccrs.bind(derenderpatcher$currentCoreShaderType == null ? renderType : derenderpatcher$currentCoreShaderType, getter);
    }

    @Inject(method = "renderFancyOuterCore", at = @At("TAIL"), remap = false)
    private void derenderpatcher$flushCoreShaderBatchAfterVertices(TileEnergyCore te, CCRenderState ccrs, Matrix4 mat,
                                                                   MultiBufferSource getter, float partialTicks,
                                                                   float rotation, double scale, CallbackInfo ci) {
        derenderpatcher$endCoreShaderBatch(getter, derenderpatcher$currentCoreShaderType);
        derenderpatcher$currentCoreShaderType = null;
    }

    @Unique
    private static RenderType derenderpatcher$getCoreShaderType(TileEnergyCore te) {
        boolean t8 = te.tier.get() == 8;
        int frame;
        int triangle;
        int effect;

        if (te.customColour.get()) {
            frame = te.frameColour.get();
            triangle = te.innerColour.get();
            effect = te.effectColour.get();
        } else {
            frame = t8 ? TileEnergyCore.DEFAULT_FRAME_COLOUR_T8 : TileEnergyCore.DEFAULT_FRAME_COLOUR;
            triangle = t8 ? TileEnergyCore.DEFAULT_TRIANGLE_COLOUR_T8 : TileEnergyCore.DEFAULT_TRIANGLE_COLOUR;
            effect = t8 ? TileEnergyCore.DEFAULT_EFFECT_COLOUR_T8 : TileEnergyCore.DEFAULT_EFFECT_COLOUR;
        }

        String key = Integer.toHexString(frame) + "_" + Integer.toHexString(triangle) + "_" + Integer.toHexString(effect);
        return DERENDERPATCHER$COLORED_CORE_TYPES.computeIfAbsent(key, ignored -> derenderpatcher$createCoreShaderType(key, frame, triangle, effect));
    }

    @Unique
    private static RenderType derenderpatcher$createCoreShaderType(String key, int frame, int triangle, int effect) {
        return RenderType.create("derenderpatcher_energy_core_" + key, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(DERENDERPATCHER$ENERGY_CORE_OVERLAY, false, false))
                .setShaderState(new RenderStateShard.ShaderStateShard(() -> {
                    derenderpatcher$applyCoreUniforms(frame, triangle, effect);
                    return DEShaders.energyCoreShader;
                }))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(RenderStateShard.NO_CULL)
                .createCompositeState(false)
        );
    }

    @Unique
    private static void derenderpatcher$applyCoreUniforms(int frame, int triangle, int effect) {
        float[] frameRgb = derenderpatcher$unpack(frame);
        float[] triangleRgb = derenderpatcher$unpack(triangle);
        float[] effectRgb = derenderpatcher$unpack(effect);

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
