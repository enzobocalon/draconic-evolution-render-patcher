package com.derenderpatcher.compat;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public final class RenderStateAccess extends RenderType {
    private RenderStateAccess() {
        super("derenderpatcher:state_access", DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 0, false, false, () -> {}, () -> {});
    }

    public static RenderStateShard.TransparencyStateShard translucentTransparency() {
        return TRANSLUCENT_TRANSPARENCY;
    }

    public static RenderStateShard.TransparencyStateShard noTransparency() {
        return NO_TRANSPARENCY;
    }

    public static RenderStateShard.WriteMaskStateShard colorWrite() {
        return COLOR_WRITE;
    }

    public static RenderStateShard.CullStateShard noCull() {
        return NO_CULL;
    }

    public static RenderStateShard.LightmapStateShard lightmap() {
        return LIGHTMAP;
    }

    public static RenderStateShard.TransparencyStateShard lightningTransparency() {
        return LIGHTNING_TRANSPARENCY;
    }
}
