package com.derenderpatcher.mixins;

import codechicken.lib.model.ModelRegistryHelper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = ModelRegistryHelper.class, remap = false)
public interface ModelRegistryHelperAccessor {
    @Accessor("registerModels")
    List<Pair<ModelResourceLocation, BakedModel>> derenderpatcher$getRegisteredModels();
}
