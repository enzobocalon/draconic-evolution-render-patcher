package com.derenderpatcher.mixins;

import com.brandon3055.draconicevolution.blocks.reactor.tileentity.TileReactorComponent;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.client.render.tile.RenderTileReactorComponent;
import com.derenderpatcher.compat.CompatMods;
import com.derenderpatcher.compat.RenderFormats;
import com.derenderpatcher.compat.RenderStateAccess;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderTileReactorComponent.class, remap = false)
public abstract class MixinRenderTileReactorComponent implements BlockEntityRenderer<TileReactorComponent> {
    @Unique
    private static final ResourceLocation DERENDERPATCHER$REACTOR_STABILIZER = new ResourceLocation(DraconicEvolution.MODID, "textures/block/reactor/reactor_stabilizer.png");

    @Unique
    private static final ResourceLocation DERENDERPATCHER$REACTOR_INJECTOR = new ResourceLocation(DraconicEvolution.MODID, "textures/block/reactor/reactor_injector.png");

    @Mutable
    @Shadow
    @Final
    private static RenderType STAB_GLOW_TYPE;

    @Mutable
    @Shadow
    @Final
    private static RenderType INJECTOR_GLOW_TYPE;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void derenderpatcher$replaceGlowRenderTypes(CallbackInfo ci) {
        if (!CompatMods.isOculusLoaded()) {
            return;
        }

        STAB_GLOW_TYPE = derenderpatcher$createReactorGlowType("stab_glow", DERENDERPATCHER$REACTOR_STABILIZER);
        INJECTOR_GLOW_TYPE = derenderpatcher$createReactorGlowType("injector_glow", DERENDERPATCHER$REACTOR_INJECTOR);
    }

    @Unique
    private static RenderType derenderpatcher$createReactorGlowType(String name, ResourceLocation texture) {
        return RenderType.create(DraconicEvolution.MODID + ":derenderpatcher_" + name, RenderFormats.newEntity(), VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntitySolidShader))
                .setTransparencyState(RenderStateAccess.lightningTransparency())
                .createCompositeState(false)
        );
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
