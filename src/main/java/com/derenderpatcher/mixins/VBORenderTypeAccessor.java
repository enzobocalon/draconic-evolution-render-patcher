package com.derenderpatcher.mixins;

import codechicken.lib.render.buffer.VBORenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = VBORenderType.class, remap = false)
public interface VBORenderTypeAccessor {
    @Invoker("render")
    void derenderpatcher$render();
}
