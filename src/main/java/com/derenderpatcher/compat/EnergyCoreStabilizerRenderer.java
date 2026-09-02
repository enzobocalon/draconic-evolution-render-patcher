package com.derenderpatcher.compat;

import codechicken.lib.math.MathHelper;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Vector3;
import com.brandon3055.draconicevolution.client.handler.ClientEventHandler;
import com.brandon3055.brandonscore.lib.datamanager.ManagedPos;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyCore;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Reimplements {@code RenderTileEnergyCore.renderStabilizers} on a private
 * set of private immediate buffers so the stabilizer sphere + beam
 * geometry never touches the shared Embeddium/Oculus batched
 * {@code MultiBufferSource}.
 *
 * <p>The mixin's {@code @Inject(cancellable=true)} on
 * {@code renderStabilizers} becomes a thin delegate that calls
 * {@link #renderStabilizers} and supplies the private
 * {@code renderStabilizerBeam} shadow via the {@link BeamRenderer} callback.
 */
public final class EnergyCoreStabilizerRenderer {
    private final MultiBufferSource.BufferSource innerStabilizerBuffers = MultiBufferSource.immediate(new BufferBuilder(512 * 1024));
    private final MultiBufferSource.BufferSource outerStabilizerBuffers = MultiBufferSource.immediate(new BufferBuilder(512 * 1024));
    private final MultiBufferSource.BufferSource innerBeamBuffers = MultiBufferSource.immediate(new BufferBuilder(64 * 1024));
    private final MultiBufferSource.BufferSource outerBeamBuffers = MultiBufferSource.immediate(new BufferBuilder(64 * 1024));

    public void renderStabilizers(
            TileEnergyCore te,
            CCRenderState ccrs,
            Matrix4 matrix4,
            float partialTick,
            CCModel modelStabilizerSphere,
            RenderType innerStabType,
            RenderType outerStabType,
            RenderType beamType,
            RenderType outerBeamType,
            BeamRenderer beamRenderer) {

        if (!te.stabilizersValid.get()) {
            return;
        }

        MultiBufferSource beamGetter = createBeamGetter(beamType, outerBeamType);

        try {
            for (ManagedPos posOffset : te.stabilizerPositions) {
                Matrix4 mat = matrix4.copy();
                BlockPos pos = posOffset.get();
                mat.translate(-pos.getX() + 0.5, -pos.getY() + 0.5, -pos.getZ() + 0.5);

                Direction facing = Direction.getNearest(pos.getX(), pos.getY(), pos.getZ());
                if (facing.getAxis() == Direction.Axis.X || facing.getAxis() == Direction.Axis.Y) {
                    mat.rotate(-90F * MathHelper.torad,
                            new Vector3(-facing.getStepY(), facing.getStepX(), 0).normalize());
                } else if (facing == Direction.SOUTH) {
                    mat.rotate(180F * MathHelper.torad, new Vector3(0, 1, 0).normalize());
                }

                mat.rotate(90F * MathHelper.torad, new Vector3(1, 0, 0).normalize());

                ccrs.baseColour = 0xFFFFFFFF;
                beamRenderer.render(te, mat, beamGetter, pos, partialTick);

                if (te.tier.get() >= 5) {
                    mat.scale(-1.2F, -0.5F, -1.2F);
                } else {
                    mat.scale(-0.45, -0.45, -0.45);
                }

                Matrix4 innerMat = mat.copy();
                innerMat.scale(0.9F, 0.9F, 0.9F);
                ccrs.baseColour = 0x00FFFFFF;
                ccrs.brightness = 240;
                innerMat.rotate(
                        (ClientEventHandler.elapsedTicks + partialTick) * MathHelper.torad,
                        new Vector3(0, -1, 0));
                ccrs.bind(innerStabType, innerStabilizerBuffers);
                modelStabilizerSphere.render(ccrs, innerMat);

                mat.scale(1.1F, 1.1F, 1.1F);
                ccrs.baseColour = 0x00FFFF7F;
                ccrs.brightness = 240;
                mat.rotate(
                        (ClientEventHandler.elapsedTicks + partialTick) * 0.5F * MathHelper.torad,
                        new Vector3(0, 1, 0));
                ccrs.bind(outerStabType, outerStabilizerBuffers);
                modelStabilizerSphere.render(ccrs, mat);
            }
        } finally {
            ShaderCompat.bindPipelineWriteTargetBeforeBatchedVboDraw();
            innerBeamBuffers.endBatch();
            outerBeamBuffers.endBatch();
            innerStabilizerBuffers.endBatch();
            outerStabilizerBuffers.endBatch();
        }
    }

    private MultiBufferSource createBeamGetter(RenderType beamType, RenderType outerBeamType) {
        return renderType -> {
            if (renderType == beamType) {
                return innerBeamBuffers.getBuffer(renderType);
            }
            if (renderType == outerBeamType) {
                return outerBeamBuffers.getBuffer(renderType);
            }
            throw new IllegalArgumentException("Unexpected Energy Core beam RenderType: " + renderType);
        };
    }

    @FunctionalInterface
    public interface BeamRenderer {
        void render(TileEnergyCore te, Matrix4 matrix4, MultiBufferSource getter, BlockPos vec, float partialTick);
    }

}
