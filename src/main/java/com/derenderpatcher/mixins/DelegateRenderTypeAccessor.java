package com.derenderpatcher.mixins;

import codechicken.lib.render.buffer.DelegateRenderType;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = DelegateRenderType.class, remap = false)
public interface DelegateRenderTypeAccessor {
    @Accessor("parent")
    RenderType derenderpatcher$getParent();
}
