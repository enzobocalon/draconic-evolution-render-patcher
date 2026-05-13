package com.derenderpatcher.mixins;

import com.derenderpatcher.compat.ShaderCompat;
import com.google.common.collect.ImmutableSet;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.targets.RenderTargets;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = IrisRenderingPipeline.class, remap = false)
public class MixinIrisRenderingPipeline {
    @Shadow @Final private RenderTargets renderTargets;
    @Shadow @Final private ImmutableSet<Integer> flippedAfterPrepare;
    @Shadow @Final private ImmutableSet<Integer> flippedAfterTranslucent;
    @Shadow public boolean isBeforeTranslucent;

    @Unique private GlFramebuffer derenderpatcher$writeTargetBeforeTranslucent;
    @Unique private GlFramebuffer derenderpatcher$writeTargetAfterTranslucent;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void derenderpatcher$createPipelineWriteTargets(ProgramSet programSet, CallbackInfo ci) {
        int colorTarget = 0;
        // In Iris' ping-pong targets, "flipped" means the current write target is alt, not main.
        // Create write targets for both pipeline states so Draconic writes into the texture Iris will compose.
        derenderpatcher$writeTargetBeforeTranslucent = flippedAfterPrepare.contains(colorTarget)
                ? renderTargets.createFramebufferWritingToAlt(new int[] { colorTarget })
                : renderTargets.createFramebufferWritingToMain(new int[] { colorTarget });
        derenderpatcher$writeTargetAfterTranslucent = flippedAfterTranslucent.contains(colorTarget)
                ? renderTargets.createFramebufferWritingToAlt(new int[] { colorTarget })
                : renderTargets.createFramebufferWritingToMain(new int[] { colorTarget });
        ShaderCompat.registerPipelineWriteTarget((WorldRenderingPipeline) (Object) this, this::derenderpatcher$bindPipelineWriteTarget);
    }

    @Unique
    private void derenderpatcher$bindPipelineWriteTarget() {
        if (isBeforeTranslucent) {
            derenderpatcher$writeTargetBeforeTranslucent.bind();
        } else {
            derenderpatcher$writeTargetAfterTranslucent.bind();
        }
    }
}
