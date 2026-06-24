package com.derenderpatcher.mixins;

import codechicken.lib.model.ModelRegistryHelper;
import com.brandon3055.draconicevolution.init.ClientInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ClientInit.class, remap = false)
public interface ClientInitAccessor {
    @Accessor("MODEL_HELPER")
    static ModelRegistryHelper derenderpatcher$getModelHelper() {
        throw new AssertionError();
    }
}
