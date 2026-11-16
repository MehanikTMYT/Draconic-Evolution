package com.brandon3055.draconicevolution.client.render.entity;

import com.brandon3055.draconicevolution.DEConfig;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.client.DEShaders;
import com.brandon3055.draconicevolution.entity.GuardianCrystalEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.model.object.crystal.EndCrystalModel;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.brandon3055.brandonscore.client.render.ExtractedModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

public class GuardianCrystalRenderer extends EntityRenderer<GuardianCrystalEntity, GuardianCrystalRenderer.State> {
    private static Identifier ENDER_CRYSTAL_TEXTURES = Identifier.fromNamespaceAndPath(DraconicEvolution.MODID, "textures/entity/guardian_crystal.png");
    private static RenderType RENDER_TYPE = RenderTypes.entityCutout(ENDER_CRYSTAL_TEXTURES);
    private static final float SIN_45 = (float) Math.sin((Math.PI / 4D));
    private final EndCrystalModel model;

    public GuardianCrystalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        model = new EndCrystalModel(context.bakeLayer(ModelLayers.END_CRYSTAL));
    }

    public static class State extends EndCrystalRenderState {
        ExtractedModel geometry;
    }
    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(GuardianCrystalEntity crystal, State state, float partialTicks) {
        super.extractRenderState(crystal, state, partialTicks);
        state.ageInTicks = crystal.time + partialTicks;
        state.showsBottom = crystal.showsBottom();
        model.setupAnim(state);
        // DE's original two successive 0.875 scales yield a 0.765625 core.
        // MC26 nests the cube below innerGlass, so its local scale must be 0.875.
        model.cube.xScale = model.cube.yScale = model.cube.zScale = 0.875F;
        var capture = new ExtractedModel.Capture();
        extractGeometry(crystal, partialTicks, new PoseStack(), capture, state.lightCoords);
        state.geometry = capture.finish();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        com.brandon3055.draconicevolution.client.render.DEExtractedGeometry.submit(state.geometry, poseStack, collector, state.outlineColor);
        super.submit(state, poseStack, collector, camera);
    }

    private void extractGeometry(GuardianCrystalEntity crystal, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        float yBob = getY(crystal, partialTicks);
        poseStack.pushPose();
        poseStack.scale(2, 2, 2);
        poseStack.translate(0, -0.5, 0);
        model.renderToBuffer(poseStack, buffers.getBuffer(RENDER_TYPE), packedLight, OverlayTexture.NO_OVERLAY);
        float shieldPower = crystal.getShieldPower() / (float) Math.max(20, DEConfig.guardianCrystalShield);
        if (shieldPower > 0) {
            var shader = DraconicGuardianRenderer.GUARDIAN_SHIELD;
            shader.uniform(128).glUniform1i(0);
            shader.uniform(64).glUniform4f(1, 0, 0, 1.5F * shieldPower);
            shader.uniform(100).glUniform1f(1);
            shader.getTimeUniform().glUniform1f((com.brandon3055.brandonscore.api.TimeKeeper.getClientTick() + partialTicks) / 20F);
            model.renderToBuffer(poseStack, buffers.getBuffer(DraconicGuardianRenderer.SHIELD_TYPE), packedLight, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();

        BlockPos blockpos = crystal.getBeamTarget();
        if (blockpos != null) {
            float targetX = (float) blockpos.getX() + 0.5F;
            float targetY = (float) blockpos.getY() + 0.5F;
            float targetZ = (float) blockpos.getZ() + 0.5F;
            float xRel = (float) ((double) targetX - crystal.getX());
            float yRel = (float) ((double) targetY - crystal.getY());
            float zRel = (float) ((double) targetZ - crystal.getZ());
            poseStack.translate(xRel, yRel - 2, zRel);

            float beamPower = crystal.getBeamPower();
            if (beamPower < 1) {
                DraconicGuardianRenderer.renderBeam(-xRel, -yRel + yBob + 2, -zRel, partialTicks, crystal.time, poseStack, buffers, packedLight, beamPower);
            } else {
                DraconicGuardianRenderer.renderBeam(-xRel, -yRel + yBob + 2, -zRel, partialTicks, crystal.time, poseStack, buffers, packedLight);
            }
        }


    }

    public static float getY(GuardianCrystalEntity crystal, float partialTicks) {
        float f = (float) crystal.time + partialTicks;
        float f1 = Mth.sin(f * 0.2F) / 2.0F + 0.5F;
        f1 = (f1 * f1 + f1) * 0.4F;
        return f1 - 1.4F;
    }

    @Override
    public boolean shouldRender(GuardianCrystalEntity entity, Frustum camera, double camX, double camY, double camZ) {
        return super.shouldRender(entity, camera, camX, camY, camZ) || entity.getBeamTarget() != null;
    }
}
