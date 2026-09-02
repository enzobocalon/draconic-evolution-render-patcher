package com.derenderpatcher.compat;

import codechicken.lib.render.buffer.DelegateRenderType;
import codechicken.lib.render.buffer.VBORenderType;
import com.derenderpatcher.mixins.DelegateRenderTypeAccessor;
import net.irisshaders.batchedentityrendering.impl.WrappableRenderType;
import net.minecraft.client.renderer.RenderType;

import java.util.Locale;

public final class DraconicRenderTypeResolver {
    private static final int MAX_UNWRAP_DEPTH = 8;
    private static final String[] DRACONIC_MARKERS = {
            "draconicevolution:",
            "reactor_type",
            "crystal_type",
            "shield_type",
            // upstream RenderTileReactorCore.REACTOR_BEAM_TYPE intentionally uses this typo
            "beam_typess",
            "inner_beam",
            "outer_beam",
            "modeleffecttype"
    };

    public static boolean isDraconic(RenderType type) {
        RenderType cursor = type;
        for (int depth = 0; depth < MAX_UNWRAP_DEPTH && cursor != null; depth++) {
            if (hasDraconicMarker(cursor.toString())) {
                return true;
            }
            cursor = unwrap(cursor);
        }
        return false;
    }

    public static VBORenderType findVbo(RenderType type) {
        RenderType cursor = type;
        for (int depth = 0; depth < MAX_UNWRAP_DEPTH && cursor != null; depth++) {
            if (cursor instanceof VBORenderType vboRenderType) {
                return vboRenderType;
            }
            cursor = unwrap(cursor);
        }
        return null;
    }

    public static String describeChain(RenderType type) {
        StringBuilder chain = new StringBuilder();
        RenderType cursor = type;
        for (int depth = 0; depth < MAX_UNWRAP_DEPTH && cursor != null; depth++) {
            if (!chain.isEmpty()) {
                chain.append(" -> ");
            }
            chain.append(cursor.getClass().getName()).append('[').append(cursor).append(']');
            cursor = unwrap(cursor);
        }
        return chain.toString();
    }

    public static String debugKey(Object object) {
        return object.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(object));
    }

    private static boolean hasDraconicMarker(String value) {
        if (value == null) {
            return false;
        }

        String normalized = value.toLowerCase(Locale.ROOT);
        for (String marker : DRACONIC_MARKERS) {
            if (normalized.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private static RenderType unwrap(RenderType type) {
        if (type instanceof WrappableRenderType wrappableRenderType) {
            return wrappableRenderType.unwrap();
        }
        if (type instanceof DelegateRenderType) {
            return ((DelegateRenderTypeAccessor) type).derenderpatcher$getParent();
        }
        return null;
    }

    private DraconicRenderTypeResolver() {
    }
}
