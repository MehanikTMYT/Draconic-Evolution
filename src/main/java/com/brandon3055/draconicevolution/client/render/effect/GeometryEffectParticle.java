package com.brandon3055.draconicevolution.client.render.effect;

import com.brandon3055.brandonscore.client.render.ExtractedModel;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;

/** MC26 particle group extracts each effect's material batches before deferred submission. */
public abstract class GeometryEffectParticle extends Particle {
    private static final ParticleRenderType GROUP = new ParticleRenderType("draconicevolution:geometry_effects");
    protected GeometryEffectParticle(ClientLevel level, double x, double y, double z) { super(level, x, y, z); }
    @Override public final ParticleRenderType getGroup() { return GROUP; }
    protected abstract void extractGeometry(MultiBufferSource buffers, Camera camera, float partialTicks);
    public static void register(net.neoforged.neoforge.client.event.RegisterParticleGroupsEvent event) {
        event.register(GROUP, Group::new);
    }
    private static final class Group extends ParticleGroup<GeometryEffectParticle> {
        Group(ParticleEngine engine) { super(engine); }
        @Override public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTicks) {
            var capture = new ExtractedModel.Capture();
            for (var effect : particles) effect.extractGeometry(capture, camera, partialTicks);
            var geometry = capture.finish();
            return (collector, cameraState) -> geometry.submit(new com.mojang.blaze3d.vertex.PoseStack(), collector);
        }
    }
}
