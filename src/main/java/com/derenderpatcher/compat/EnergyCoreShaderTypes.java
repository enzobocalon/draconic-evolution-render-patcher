package com.derenderpatcher.compat;

import codechicken.lib.render.shader.CCUniform;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyCore;
import com.brandon3055.draconicevolution.client.DEShaders;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds and caches the per-colour {@code RenderType} used by Draconic
 * Evolution's Energy Core fancy outer layer.
 *
 * <p>Each Energy Core tier / custom-colour triple gets its own
 * {@link RenderType} so the per-frame {@code TexturingStateShard} callback can
 * seed the energy-core shader uniforms ({@code FrameColour},
 * {@code RotTriColour}, {@code EffectColour}, {@code Activation}) with the
 * correct values before the draw.
 *
 * <p>Mirrors the shape of {@link EnergyCrystalRenderTypes}.
 */
public final class EnergyCoreShaderTypes {
    private static final int MAX_CORE_SHADER_TYPES = 256;

    private static final ResourceLocation ENERGY_CORE_OVERLAY =
            ResourceLocation.fromNamespaceAndPath(
                    DraconicEvolution.MODID,
                    "textures/block/energy_core/energy_core_overlay.png");

    private static final Map<String, RenderType> CORE_SHADER_TYPES = new LinkedHashMap<>(32, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, RenderType> eldest) {
            return size() > MAX_CORE_SHADER_TYPES;
        }
    };

    public static RenderType getCoreShaderType(TileEnergyCore te) {
        int frameColour;
        int triangleColour;
        int effectColour;

        if (te.customColour.get()) {
            frameColour = te.frameColour.get();
            triangleColour = te.innerColour.get();
            effectColour = te.effectColour.get();
        } else {
            boolean tierEight = te.tier.get() == 8;
            frameColour = tierEight ? TileEnergyCore.DEFAULT_FRAME_COLOUR_T8 : TileEnergyCore.DEFAULT_FRAME_COLOUR;
            triangleColour = tierEight ? TileEnergyCore.DEFAULT_TRIANGLE_COLOUR_T8 : TileEnergyCore.DEFAULT_TRIANGLE_COLOUR;
            effectColour = tierEight ? TileEnergyCore.DEFAULT_EFFECT_COLOUR_T8 : TileEnergyCore.DEFAULT_EFFECT_COLOUR;
        }

        String key = colourKey(frameColour, triangleColour, effectColour);

        synchronized (CORE_SHADER_TYPES) {
            return CORE_SHADER_TYPES.computeIfAbsent(
                    key, ignored -> createCoreShaderType(key, frameColour, triangleColour, effectColour));
        }
    }

    public static int getLegacyCoreOverlayColour(TileEnergyCore te) {
        if (te.customColour.get()) {
            return packRgb(te.effectColour.get());
        }
        return te.tier.get() == 8 ? packRgb(0xF27300) : packRgb(0x33FFFF);
    }

    private static String colourKey(int frameColour, int triangleColour, int effectColour) {
        return Integer.toHexString(frameColour & 0xFFFFFF)
                + '_' + Integer.toHexString(triangleColour & 0xFFFFFF)
                + '_' + Integer.toHexString(effectColour & 0xFFFFFF);
    }

    private static int packRgb(int colour) {
        return ((colour & 0xFFFFFF) << 8) | 0xFF;
    }

    private static RenderType createCoreShaderType(String key, int frameColour, int triangleColour, int effectColour) {
        return RenderType.create(
                DraconicEvolution.MODID + ":derenderpatcher_energy_core_shader_" + key,
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS, 256, false, true,
                RenderType.CompositeState.builder()
                        .setTextureState(new RenderStateShard.TextureStateShard(ENERGY_CORE_OVERLAY, false, false))
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> DEShaders.energyCoreShader))
                        .setTransparencyState(RenderStateAccess.translucentTransparency())
                        .setCullState(RenderStateAccess.noCull())
                        .setTexturingState(new RenderStateShard.TexturingStateShard(
                                "derenderpatcher_energy_core_colours_" + key,
                                () -> setupCoreShaderColours(frameColour, triangleColour, effectColour),
                                ShaderCompat::clearPendingShaderUniformApplier))
                        .createCompositeState(false));
    }

    private static void setupCoreShaderColours(int frameColour, int triangleColour, int effectColour) {
        Runnable uniformApplier = () -> applyCoreShaderColours(frameColour, triangleColour, effectColour);
        ShaderCompat.setPendingShaderUniformApplier(uniformApplier);
        uniformApplier.run();
    }

    private static void applyCoreShaderColours(int frameColour, int triangleColour, int effectColour) {
        setUniform1(DEShaders.energyCoreActivation, 1F);
        setUniform3(DEShaders.energyCoreFrameColour, frameColour);
        setUniform3(DEShaders.energyCoreRotTriColour, triangleColour);
        setUniform3(DEShaders.energyCoreEffectColour, effectColour);
    }

    private static void setUniform1(CCUniform uniform, float value) {
        if (uniform == null) {
            return;
        }
        uniform.glUniform1f(value);
    }

    private static void setUniform3(CCUniform uniform, int colour) {
        if (uniform == null) {
            return;
        }
        uniform.glUniform3f(
                ((colour >> 16) & 0xFF) / 255F,
                ((colour >> 8) & 0xFF) / 255F,
                (colour & 0xFF) / 255F);
    }

    private EnergyCoreShaderTypes() {
    }
}
