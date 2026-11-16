package com.brandon3055.draconicevolution.client.render.entity;

import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.entity.guardian.GuardianProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class GuardianProjectileRenderer extends EntityRenderer<GuardianProjectileEntity, EntityRenderState> {
   private static final Identifier DRAGON_FIREBALL_TEXTURE = Identifier.fromNamespaceAndPath(DraconicEvolution.MODID, "textures/entity/guardian_fireball.png");
   private static final RenderType RENDER_TYPE = RenderTypes.entityCutout(DRAGON_FIREBALL_TEXTURE);

   public GuardianProjectileRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   @Override
   protected int getBlockLightLevel(GuardianProjectileEntity entityIn, BlockPos partialTicks) {
      return 15;
   }

   @Override
   public EntityRenderState createRenderState() { return new EntityRenderState(); }

   @Override
   public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
      poseStack.pushPose();
      poseStack.scale(2.0F, 2.0F, 2.0F);
      poseStack.mulPose(camera.orientation);
      poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
      int light = state.lightCoords;
      collector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, vertices) -> {
         vertex(vertices, pose.pose(), pose, light, 0.0F, 0, 0, 1);
         vertex(vertices, pose.pose(), pose, light, 1.0F, 0, 1, 1);
         vertex(vertices, pose.pose(), pose, light, 1.0F, 1, 1, 0);
         vertex(vertices, pose.pose(), pose, light, 0.0F, 1, 0, 0);
      });
      poseStack.popPose();
      super.submit(state, poseStack, collector, camera);
   }

   private static void vertex(VertexConsumer p_229045_0_, Matrix4f p_229045_1_, PoseStack.Pose p_229045_2_, int p_229045_3_, float p_229045_4_, int p_229045_5_, int p_229045_6_, int p_229045_7_) {
      p_229045_0_.addVertex(p_229045_1_, p_229045_4_ - 0.5F, (float)p_229045_5_ - 0.25F, 0.0F).setColor(255, 255, 255, 255).setUv((float)p_229045_6_, (float)p_229045_7_).setOverlay(OverlayTexture.NO_OVERLAY).setLight(p_229045_3_).setNormal(p_229045_2_, 0.0F, 1.0F, 0.0F);
   }

}
