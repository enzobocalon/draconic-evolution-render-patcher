package com.derenderpatcher.compat;

import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.client.DEShaders;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class EnergyCoreRenderTypes {
    private static final ResourceLocation ENERGY_CORE_BASE = texture("energy_core_base.png");
    private static final ResourceLocation ENERGY_CORE_OVERLAY = texture("energy_core_overlay.png");
    private static final ResourceLocation STABILIZER_SPHERE = texture("stabilizer_sphere.png");
    private static final ResourceLocation STABILIZER_BEAM = texture("stabilizer_beam.png");

    public static Types create() {
        RenderType innerCore = createEntitySolidType("inner_core", ENERGY_CORE_BASE);
        RenderType outerCore = RenderType.create(
                name("outer_core"),
                DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
                RenderType.CompositeState.builder()
                        .setTextureState(textureState(ENERGY_CORE_OVERLAY))
                        .setShaderState(brandonShader())
                        .setTransparencyState(RenderStateAccess.translucentTransparency())
                        .createCompositeState(false));

        RenderType innerStabilizer = RenderType.create(
                name("inner_stab"),
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true,
                RenderType.CompositeState.builder()
                        .setTextureState(textureState(STABILIZER_SPHERE))
                        .setShaderState(brandonShader())
                        .setTransparencyState(RenderStateAccess.noTransparency())
                        .createCompositeState(false));

        RenderType outerStabilizer = RenderType.create(
                name("outer_stab"),
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true,
                RenderType.CompositeState.builder()
                        .setTextureState(textureState(STABILIZER_SPHERE))
                        .setShaderState(brandonShader())
                        .setTransparencyState(RenderStateAccess.translucentTransparency())
                        .createCompositeState(false));

        RenderType innerBeam = RenderType.create(
                "derenderpatcher_inner_beam",
                DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, true,
                RenderType.CompositeState.builder()
                        .setTextureState(textureState(STABILIZER_BEAM))
                        .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionTexShader))
                        .setTransparencyState(RenderStateAccess.translucentTransparency())
                        .createCompositeState(false));

        RenderType outerBeam = RenderType.create(
                "derenderpatcher_outer_beam",
                DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.TRIANGLE_STRIP, 256, false, false,
                RenderType.CompositeState.builder()
                        .setTextureState(textureState(STABILIZER_BEAM))
                        .setShaderState(brandonShader())
                        .setTransparencyState(RenderStateAccess.translucentTransparency())
                        .setWriteMaskState(RenderStateAccess.colorWrite())
                        .createCompositeState(false));

        RenderType coreShader = RenderType.create(
                name("energy_core_shader"),
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true,
                RenderType.CompositeState.builder()
                        .setTextureState(textureState(ENERGY_CORE_OVERLAY))
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> DEShaders.energyCoreShader))
                        .setTransparencyState(RenderStateAccess.translucentTransparency())
                        .setCullState(RenderStateAccess.noCull())
                        .createCompositeState(false));

        return new Types(
                innerCore, outerCore, innerStabilizer, outerStabilizer, innerBeam, outerBeam, coreShader);
    }

    private static RenderType createEntitySolidType(String typeName, ResourceLocation texture) {
        return RenderType.create(
                name(typeName),
                DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false,
                RenderType.CompositeState.builder()
                        .setTextureState(textureState(texture))
                        .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntitySolidShader))
                        .setLightmapState(RenderStateAccess.lightmap())
                        .setOverlayState(RenderStateAccess.overlay())
                        .createCompositeState(true));
    }

    private static RenderStateShard.TextureStateShard textureState(ResourceLocation texture) {
        return new RenderStateShard.TextureStateShard(texture, false, false);
    }

    private static RenderStateShard.ShaderStateShard brandonShader() {
        return new RenderStateShard.ShaderStateShard(
                () -> com.brandon3055.brandonscore.client.shader.BCShaders.posColourTexAlpha0);
    }

    private static ResourceLocation texture(String fileName) {
        return ResourceLocation.fromNamespaceAndPath(
                DraconicEvolution.MODID,
                "textures/block/energy_core/" + fileName);
    }

    private static String name(String typeName) {
        return DraconicEvolution.MODID + ":derenderpatcher_" + typeName;
    }

    public record Types(
            RenderType innerCore,
            RenderType outerCore,
            RenderType innerStabilizer,
            RenderType outerStabilizer,
            RenderType innerBeam,
            RenderType outerBeam,
            RenderType coreShader) {
    }

    private EnergyCoreRenderTypes() {
    }
}
