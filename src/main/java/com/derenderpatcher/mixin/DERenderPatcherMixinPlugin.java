package com.derenderpatcher.mixin;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DERenderPatcherMixinPlugin implements IMixinConfigPlugin {
    private static final Map<String, List<String>> MIXIN_REQUIRED_MODS = Map.of(
            "com.derenderpatcher.mixin.MixinIrisConfig", List.of("iris")
    );

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        List<String> requiredMods = MIXIN_REQUIRED_MODS.get(mixinClassName);

        if (requiredMods != null) {
            return requiredMods.stream().allMatch(DERenderPatcherMixinPlugin::isModLoaded);
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    private static boolean isModLoaded(String modId) {
        try {
            ModList modList = ModList.get();
            if (modList != null) {
                return modList.isLoaded(modId);
            }
        } catch (IllegalStateException ignored) {
        }

        return LoadingModList.get().getModFileById(modId) != null;
    }
}
