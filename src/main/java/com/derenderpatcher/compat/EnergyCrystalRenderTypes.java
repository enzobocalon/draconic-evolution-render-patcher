package com.derenderpatcher.compat;

import codechicken.lib.render.shader.CCUniform;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.client.DEShaders;
import com.brandon3055.draconicevolution.client.render.tile.RenderTileEnergyCrystal;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EnergyCrystalRenderTypes {
    private static final int MIPMAP_BUCKETS = 64;
    private static final int MAX_CRYSTAL_SHADER_TYPES = 256;

    private static final Map<String, RenderType> CRYSTAL_SHADER_TYPES = new LinkedHashMap<>(32, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, RenderType> eldest) {
            return size() > MAX_CRYSTAL_SHADER_TYPES;
        }
    };

    public static RenderType getCrystalShaderType(int tier, float mipmap) {
        float[][] colours = RenderTileEnergyCrystal.COLOURS;
        int clampedTier = Mth.clamp(tier, 0, colours.length - 1);
        int mipmapBucket = Mth.clamp(Math.round(mipmap * MIPMAP_BUCKETS), 0, MIPMAP_BUCKETS);
        float bucketedMipmap = mipmapBucket / (float) MIPMAP_BUCKETS;
        float[] colour = colours[clampedTier];
        String key = clampedTier + "_" + mipmapBucket;

        synchronized (CRYSTAL_SHADER_TYPES) {
            return CRYSTAL_SHADER_TYPES.computeIfAbsent(key, ignored -> derenderpatcher$createCrystalShaderType(key, bucketedMipmap, colour[0], colour[1], colour[2]));
        }
    }

    private static RenderType derenderpatcher$createCrystalShaderType(String key, float mipmap, float red, float green, float blue) {
        return RenderType.create(DraconicEvolution.MODID + ":derenderpatcher_crystal_type_" + key, DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(() -> DEShaders.energyCrystalShader))
                .setTransparencyState(RenderStateAccess.translucentTransparency())
                .setTexturingState(new RenderStateShard.TexturingStateShard("derenderpatcher_energy_crystal_colours_" + key, () -> derenderpatcher$setupCrystalShaderUniforms(mipmap, red, green, blue), ShaderCompat::clearPendingShaderUniformApplier))
                .createCompositeState(false)
        );
    }

    private static void derenderpatcher$setupCrystalShaderUniforms(float mipmap, float red, float green, float blue) {
        Runnable uniformApplier = () -> derenderpatcher$applyCrystalShaderUniforms(mipmap, red, green, blue);
        ShaderCompat.setPendingShaderUniformApplier(uniformApplier);
        uniformApplier.run();
    }

    private static void derenderpatcher$applyCrystalShaderUniforms(float mipmap, float red, float green, float blue) {
        derenderpatcher$setUniform1(DEShaders.energyCrystalMipmap, mipmap);
        derenderpatcher$setUniform3(DEShaders.energyCrystalColour, red, green, blue);
    }

    private static void derenderpatcher$setUniform1(CCUniform uniform, float value) {
        if (uniform != null) {
            uniform.glUniform1f(value);
        }
    }

    private static void derenderpatcher$setUniform3(CCUniform uniform, float red, float green, float blue) {
        if (uniform != null) {
            uniform.glUniform3f(red, green, blue);
        }
    }

    private EnergyCrystalRenderTypes() {
    }
}
