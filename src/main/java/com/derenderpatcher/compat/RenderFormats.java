package com.derenderpatcher.compat;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public final class RenderFormats {
    private static final VertexFormat NEW_ENTITY = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
            .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
            .put("Color", DefaultVertexFormat.ELEMENT_COLOR)
            .put("UV0", DefaultVertexFormat.ELEMENT_UV0)
            .put("UV1", DefaultVertexFormat.ELEMENT_UV1)
            .put("UV2", DefaultVertexFormat.ELEMENT_UV2)
            .put("Normal", DefaultVertexFormat.ELEMENT_NORMAL)
            .put("Padding", DefaultVertexFormat.ELEMENT_PADDING)
            .build());

    private static final VertexFormat POSITION_COLOR_TEX_LIGHTMAP = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
            .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
            .put("Color", DefaultVertexFormat.ELEMENT_COLOR)
            .put("UV0", DefaultVertexFormat.ELEMENT_UV0)
            .put("UV2", DefaultVertexFormat.ELEMENT_UV2)
            .build());

    public static VertexFormat positionColorTexLightmap() {
        return POSITION_COLOR_TEX_LIGHTMAP;
    }

    public static VertexFormat newEntity() {
        return NEW_ENTITY;
    }

    private RenderFormats() {
    }
}
