package com.derenderpatcher.mixins;

import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyCoreStabilizer;
import com.brandon3055.draconicevolution.client.render.tile.RenderEnergyCoreStabilizer;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.derenderpatcher.compat.RenderStateAccess;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderEnergyCoreStabilizer.class, remap = false)
public abstract class MixinRenderEnergyCoreStabilizer implements BlockEntityRenderer<TileEnergyCoreStabilizer> {
    @Unique
    private static final ResourceLocation DERENDERPATCHER$STABILIZER_LARGE = new ResourceLocation(DraconicEvolution.MODID, "textures/block/energy_core/stabilizer_large.png");

    @Unique
    private static final VertexFormat DERENDERPATCHER$POSITION_COLOR_TEX_LIGHTMAP = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
            .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
            .put("Color", DefaultVertexFormat.ELEMENT_COLOR)
            .put("UV0", DefaultVertexFormat.ELEMENT_UV0)
            .put("UV2", DefaultVertexFormat.ELEMENT_UV2)
            .build());

    @Mutable
    @Shadow
    @Final
    private static RenderType MODEL_TYPE_ACTIVE;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void derenderpatcher$replaceActiveStabilizerRenderType(CallbackInfo ci) {
        if (!ModList.get().isLoaded("oculus")) {
            return;
        }

        MODEL_TYPE_ACTIVE = RenderType.create("derenderpatcher_energy_core_stabilizer_active", DERENDERPATCHER$POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(DERENDERPATCHER$STABILIZER_LARGE, false, false))
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorTexLightmapShader))
                .setLightmapState(RenderStateAccess.lightmap())
                .createCompositeState(false)
        );
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
