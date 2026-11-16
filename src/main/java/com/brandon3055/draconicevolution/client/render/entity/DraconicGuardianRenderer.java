package com.brandon3055.draconicevolution.client.render.entity;

import com.brandon3055.draconicevolution.DEConfig;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.client.DEShaders;
import com.brandon3055.draconicevolution.entity.guardian.DraconicGuardianEntity;
import com.brandon3055.draconicevolution.entity.guardian.control.ChargeUpPhase;
import com.brandon3055.draconicevolution.entity.guardian.control.IPhase;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.entity.state.EnderDragonRenderState;
import net.minecraft.client.model.monster.dragon.EnderDragonModel;
import net.minecraft.world.entity.boss.enderdragon.DragonFlightHistory;
import com.brandon3055.brandonscore.client.render.ExtractedModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.Random;

public class DraconicGuardianRenderer extends EntityRenderer<DraconicGuardianEntity, DraconicGuardianRenderer.State> {
    public static final Identifier ENDERCRYSTAL_BEAM_TEXTURES = Identifier.fromNamespaceAndPath(DraconicEvolution.MODID, "textures/entity/guardian_crystal_beam.png");
    private static final Identifier DRAGON_EXPLODING_TEXTURES = Identifier.withDefaultNamespace("textures/entity/enderdragon/dragon_exploding.png");
    private static final Identifier DRAGON_TEXTURE = Identifier.withDefaultNamespace("textures/entity/enderdragon/dragon.png");
    private static final Identifier GUARDIAN_TEXTURE = Identifier.fromNamespaceAndPath(DraconicEvolution.MODID, "textures/entity/chaos_guardian.png");
    private static final Identifier EYES_TEXTURE = Identifier.withDefaultNamespace("textures/entity/enderdragon/dragon_eyes.png");
    private static final RenderType dragonCutoutType = RenderTypes.entityCutout(GUARDIAN_TEXTURE);
    private static final RenderType dragonDeathType = RenderTypes.entityCutoutDissolve(GUARDIAN_TEXTURE, DRAGON_EXPLODING_TEXTURES);
    private static final RenderType eyesType = RenderTypes.eyes(EYES_TEXTURE);
    private static final RenderType beamType = RenderTypes.endCrystalBeam(ENDERCRYSTAL_BEAM_TEXTURES);
    private static final com.mojang.blaze3d.pipeline.RenderPipeline BEAM_PIPELINE = RenderPipelines.END_CRYSTAL_BEAM.toBuilder()
            .withLocation(Identifier.fromNamespaceAndPath(DraconicEvolution.MODID, "pipeline/guardian_beam"))
            .withColorTargetState(new com.mojang.blaze3d.pipeline.ColorTargetState(com.mojang.blaze3d.pipeline.BlendFunction.TRANSLUCENT))
            .withCull(false).build();
    private static final RenderType BEAM_TYPE2 = RenderType.create("de_guardian_beam", RenderSetup.builder(BEAM_PIPELINE)
            .withTexture("Sampler0", ENDERCRYSTAL_BEAM_TEXTURES).useLightmap().useOverlay().createRenderSetup());
    public static final com.brandon3055.draconicevolution.client.shader.DEEffectShader GUARDIAN_SHIELD =
            new com.brandon3055.draconicevolution.client.shader.DEEffectShader("shield", "shield", DefaultVertexFormat.POSITION_TEX,
                    true, false, true, false, false, false, com.mojang.blaze3d.platform.CompareOp.EQUAL);
    public static final RenderType SHIELD_TYPE = GUARDIAN_SHIELD.renderType("de_guardian_shield", null, VertexFormat.Mode.QUADS);
    public static void registerPipelines(net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent event) {
        event.registerPipeline(BEAM_PIPELINE);
        event.registerPipeline(GUARDIAN_SHIELD.pipeline(VertexFormat.Mode.QUADS));
    }

    private static final float sqrt3div2 = (float) (Math.sqrt(3.0D) / 2.0D);
    private final EnderDragonModel model;


    public DraconicGuardianRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.model = new EnderDragonModel(context.bakeLayer(ModelLayers.ENDER_DRAGON));
    }

    /** Copies guardian history/phase before baking all model and beam vertices. */
    public static class State extends EnderDragonRenderState {
        final DragonFlightHistory.Sample[] history = new DragonFlightHistory.Sample[24];
        ExtractedModel geometry;
        @Override public DragonFlightHistory.Sample getHistoricalPos(int delay) { return history[delay]; }
    }

    @Override protected boolean affectedByCulling(DraconicGuardianEntity entity) { return false; }

    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(DraconicGuardianEntity guardian, State state, float partialTicks) {
        super.extractRenderState(guardian, state, partialTicks);
        state.flapTime = Mth.lerp(partialTicks, guardian.oFlapTime, guardian.flapTime);
        state.isSitting = guardian.getPhaseManager().getCurrentPhase().getIsStationary();
        state.isLandingOrTakingOff = false;
        for (int i = 0; i < state.history.length; i++) {
            double[] point = guardian.getLatencyPos(i, partialTicks);
            state.history[i] = new DragonFlightHistory.Sample(point[1], (float) point[0]);
        }
        model.setupAnim(state);
        ExtractedModel.Capture capture = new ExtractedModel.Capture();
        extractGeometry(guardian, partialTicks, new PoseStack(), capture, state.lightCoords);
        state.geometry = capture.finish();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        com.brandon3055.draconicevolution.client.render.DEExtractedGeometry.submit(state.geometry, poseStack, collector, state.outlineColor);
        super.submit(state, poseStack, collector, camera);
    }

    private void extractGeometry(DraconicGuardianEntity guardian, float partialTicks, PoseStack poseStack, MultiBufferSource getter, int packedLight) {
        poseStack.pushPose();
        float f = (float) guardian.getLatencyPos(7, partialTicks)[0];
        float f1 = (float) (guardian.getLatencyPos(5, partialTicks)[1] - guardian.getLatencyPos(10, partialTicks)[1]);
        poseStack.mulPose(Axis.YP.rotationDegrees(-f));
        poseStack.mulPose(Axis.XP.rotationDegrees(f1 * 10.0F));
        poseStack.translate(0.0D, 0.0D, 1.0D);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0D, -1.501F, 0.0D);
        boolean flag = guardian.hurtTime > 0;


        if (guardian.deathTicks > 0) {
            float progress = (float)guardian.deathTicks / 200.0F;
            int fadeColour = ARGB.white(1.0F - progress);
            VertexConsumer builder = getter.getBuffer(dragonDeathType);
            this.model.renderToBuffer(poseStack, builder, packedLight, OverlayTexture.NO_OVERLAY, fadeColour);
        } else {
            VertexConsumer builder = getter.getBuffer(dragonCutoutType);
            this.model.renderToBuffer(poseStack, builder, packedLight, OverlayTexture.pack(0.0F, flag));
        }
        VertexConsumer builder;

        GUARDIAN_SHIELD.getTimeUniform().glUniform1f((com.brandon3055.brandonscore.api.TimeKeeper.getClientTick() + partialTicks) / 20F);
        boolean isImmune = guardian.getPhaseManager().getCurrentPhase().isInvulnerable();
        float shieldState = guardian.getEntityData().get(DraconicGuardianEntity.SHIELD_POWER) / (float) DEConfig.guardianShield;
        if (shieldState > 0 || isImmune) {
            if (isImmune) {
                GUARDIAN_SHIELD.uniform(64).glUniform4f(0F, 1F, 1F, 2);
            } else {
                GUARDIAN_SHIELD.uniform(64).glUniform4f(1F, 0F, 0F, 1.5F * shieldState);
            }
            GUARDIAN_SHIELD.uniform(128).glUniform1i(0);
            GUARDIAN_SHIELD.uniform(100).glUniform1f(1F);

            builder = getter.getBuffer(SHIELD_TYPE);
            this.model.renderToBuffer(poseStack, builder, packedLight, OverlayTexture.pack(0.0F, flag));
        }

        builder = getter.getBuffer(eyesType);
        this.model.renderToBuffer(poseStack, builder, packedLight, OverlayTexture.NO_OVERLAY);
        if (guardian.deathTicks > 0) {
            float f5 = ((float) guardian.deathTicks + partialTicks) / 200.0F;
            float f7 = Math.min(f5 > 0.8F ? (f5 - 0.8F) / 0.2F : 0.0F, 1.0F);
            Random random = new Random(432L);
            builder = getter.getBuffer(RenderTypes.lightning());
            poseStack.pushPose();
            poseStack.translate(0.0D, -1.0D, -2.0D);

            for (int i = 0; (float) i < (f5 + f5 * f5) / 2.0F * 60.0F; ++i) {
                poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F + f5 * 90.0F));
                float f3 = random.nextFloat() * 20.0F + 5.0F + f7 * 10.0F;
                float f4 = random.nextFloat() * 2.0F + 1.0F + f7 * 2.0F;
                Matrix4f matrix4f = poseStack.last().pose();
                int j = (int) (255.0F * (1.0F - f7));
                deathAnimA(builder, matrix4f, j);
                deathAnimB(builder, matrix4f, f3, f4);
                deathAnimC(builder, matrix4f, f3, f4);
                deathAnimA(builder, matrix4f, j);
                deathAnimC(builder, matrix4f, f3, f4);
                deathAnimD(builder, matrix4f, f3, f4);
                deathAnimA(builder, matrix4f, j);
                deathAnimD(builder, matrix4f, f3, f4);
                deathAnimB(builder, matrix4f, f3, f4);
            }

            poseStack.popPose();
        }

        poseStack.popPose();
        if (guardian.closestGuardianCrystal != null) {
            poseStack.pushPose();
            float relX = (float) (guardian.closestGuardianCrystal.getX() - Mth.lerp(partialTicks, guardian.xo, guardian.getX()));
            float relY = (float) (guardian.closestGuardianCrystal.getY() - Mth.lerp(partialTicks, guardian.yo, guardian.getY()));
            float relZ = (float) (guardian.closestGuardianCrystal.getZ() - Mth.lerp(partialTicks, guardian.zo, guardian.getZ()));
            renderBeam(relX, relY + GuardianCrystalRenderer.getY(guardian.closestGuardianCrystal, partialTicks), relZ, partialTicks, guardian.tickCount, poseStack, getter, packedLight);
            poseStack.popPose();
        }

        IPhase iPhase = guardian.getPhaseManager().getCurrentPhase();
        if (iPhase instanceof ChargeUpPhase && guardian.getArenaOrigin() != null) {
            ChargeUpPhase phase = (ChargeUpPhase) iPhase;
            if (phase.animState() != 0) {
                BlockPos origin = guardian.getArenaOrigin();
//                float beamSin = MathHelper.sin((Math.min(1, phase.animState() + 0.3F)) * (float) Math.PI);
                float beamSin = Mth.sin(phase.animState() * (float) Math.PI);
                poseStack.pushPose();
                float relX = (float) ((origin.getX() + 0.5) - Mth.lerp(partialTicks, guardian.xo, guardian.getX()));
                float relY = (float) ((origin.getY() + 0.5) - Mth.lerp(partialTicks, guardian.yo, guardian.getY()));
                float relZ = (float) ((origin.getZ() + 0.5) - Mth.lerp(partialTicks, guardian.zo, guardian.getZ()));
                renderChargingBeam(relX, relY, relZ, partialTicks, guardian.tickCount, poseStack, getter, packedLight, beamSin);
                poseStack.popPose();
            }
        }


    }

    private static void deathAnimA(VertexConsumer builder, Matrix4f mat, int alpha) {
        builder.addVertex(mat, 0.0F, 0.0F, 0.0F).setColor(255, 0, 0, alpha);
        builder.addVertex(mat, 0.0F, 0.0F, 0.0F).setColor(255, 0, 0, alpha);
    }

    private static void deathAnimB(VertexConsumer builder, Matrix4f mat, float p_229060_2_, float p_229060_3_) {
        builder.addVertex(mat, -sqrt3div2 * p_229060_3_, p_229060_2_, -0.5F * p_229060_3_).setColor(255, 0, 0, 0);
    }

    private static void deathAnimC(VertexConsumer builder, Matrix4f mat, float p_229062_2_, float p_229062_3_) {
        builder.addVertex(mat, sqrt3div2 * p_229062_3_, p_229062_2_, -0.5F * p_229062_3_).setColor(255, 0, 255, 0);
    }

    private static void deathAnimD(VertexConsumer builder, Matrix4f mat, float p_229063_2_, float p_229063_3_) {
        builder.addVertex(mat, 0.0F, p_229063_2_, 1.0F * p_229063_3_).setColor(255, 0, 0, 0);
    }

    public static void renderBeam(float crystalRelX, float crystalRelY, float crystalRelZ, float partialTicks, int animTicks, PoseStack mStack, MultiBufferSource getter, int packedLight) {
        float xzDistance = Mth.sqrt(crystalRelX * crystalRelX + crystalRelZ * crystalRelZ);
        float distance = Mth.sqrt(crystalRelX * crystalRelX + crystalRelY * crystalRelY + crystalRelZ * crystalRelZ);
        mStack.pushPose();
        mStack.translate(0.0D, 2.0D, 0.0D);
        mStack.mulPose(Axis.YP.rotation((float) (-Math.atan2(crystalRelZ, crystalRelX)) - ((float) Math.PI / 2F)));
        mStack.mulPose(Axis.XP.rotation((float) (-Math.atan2(xzDistance, crystalRelY)) - ((float) Math.PI / 2F)));
        VertexConsumer builder = getter.getBuffer(beamType);
        float f2 = 0.0F - ((float) animTicks + partialTicks) * 0.01F;
        float f3 = Mth.sqrt(crystalRelX * crystalRelX + crystalRelY * crystalRelY + crystalRelZ * crystalRelZ) / 32.0F - ((float) animTicks + partialTicks) * 0.01F;
        float f4 = 0.0F;
        float f5 = 0.75F;
        float f6 = 0.0F;
        PoseStack.Pose stackLast = mStack.last();
        Matrix4f lastMatrix = stackLast.pose();

        for (int j = 1; j <= 8; ++j) {
            float rSin = Mth.sin((float) j * ((float) Math.PI * 2F) / 8.0F) * 0.75F;
            float rCos = Mth.cos((float) j * ((float) Math.PI * 2F) / 8.0F) * 0.75F;
            float indexDecimal = (float) j / 8.0F;
            builder.addVertex(lastMatrix, f4 * 0.2F, f5 * 0.2F, 0.0F).setColor(0, 0, 0, 255).setUv(f6, f2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(stackLast, 0.0F, -1.0F, 0.0F);
            builder.addVertex(lastMatrix, f4, f5, distance).setColor(255, 255, 255, 255).setUv(f6, f3).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(stackLast, 0.0F, -1.0F, 0.0F);
            builder.addVertex(lastMatrix, rSin, rCos, distance).setColor(255, 255, 255, 255).setUv(indexDecimal, f3).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(stackLast, 0.0F, -1.0F, 0.0F);
            builder.addVertex(lastMatrix, rSin * 0.2F, rCos * 0.2F, 0.0F).setColor(0, 0, 0, 255).setUv(indexDecimal, f2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(stackLast, 0.0F, -1.0F, 0.0F);
            f4 = rSin;
            f5 = rCos;
            f6 = indexDecimal;
        }

        mStack.popPose();
    }

    public static void renderBeam(float crystalRelX, float crystalRelY, float crystalRelZ, float partialTicks, int animTicks, PoseStack mStack, MultiBufferSource getter, int packedLight, float alpha) {
        float xzDistance = Mth.sqrt(crystalRelX * crystalRelX + crystalRelZ * crystalRelZ);
        float distance = Mth.sqrt(crystalRelX * crystalRelX + crystalRelY * crystalRelY + crystalRelZ * crystalRelZ);
        mStack.pushPose();
        mStack.translate(0.0D, 2.0D, 0.0D);
        mStack.mulPose(Axis.YP.rotation((float) (-Math.atan2(crystalRelZ, crystalRelX)) - ((float) Math.PI / 2F)));
        mStack.mulPose(Axis.XP.rotation((float) (-Math.atan2(xzDistance, crystalRelY)) - ((float) Math.PI / 2F)));
        VertexConsumer builder = getter.getBuffer(BEAM_TYPE2);
        float f2 = 0.0F - ((float) animTicks + partialTicks) * 0.01F;
        float f3 = Mth.sqrt(crystalRelX * crystalRelX + crystalRelY * crystalRelY + crystalRelZ * crystalRelZ) / 32.0F - ((float) animTicks + partialTicks) * 0.01F;
        float f4 = 0.0F;
        float f5 = 0.75F;
        float f6 = 0.0F;
        PoseStack.Pose stackLast = mStack.last();
        Matrix4f lastMatrix = stackLast.pose();

        for (int j = 1; j <= 8; ++j) {
            float rSin = Mth.sin((float) j * ((float) Math.PI * 2F) / 8.0F) * 0.75F;
            float rCos = Mth.cos((float) j * ((float) Math.PI * 2F) / 8.0F) * 0.75F;
            float indexDecimal = (float) j / 8.0F;
            builder.addVertex(lastMatrix, f4 * 0.2F, f5 * 0.2F, 0.0F).setColor(1F, 1F, 1F, alpha).setUv(f6, f2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(stackLast, 0.0F, -1.0F, 0.0F);
            builder.addVertex(lastMatrix, f4, f5, distance).setColor(1F, 1F, 1F, alpha).setUv(f6, f3).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(stackLast, 0.0F, -1.0F, 0.0F);
            builder.addVertex(lastMatrix, rSin, rCos, distance).setColor(1F, 1F, 1F, alpha).setUv(indexDecimal, f3).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(stackLast, 0.0F, -1.0F, 0.0F);
            builder.addVertex(lastMatrix, rSin * 0.2F, rCos * 0.2F, 0.0F).setColor(1F, 1F, 1F, alpha).setUv(indexDecimal, f2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(stackLast, 0.0F, -1.0F, 0.0F);
            f4 = rSin;
            f5 = rCos;
            f6 = indexDecimal;
        }

        mStack.popPose();
    }

    public static void renderChargingBeam(float crystalRelX, float crystalRelY, float crystalRelZ, float partialTicks, int animTicks, PoseStack mStack, MultiBufferSource getter, int packedLight, float alpha) {
        float xzDistance = Mth.sqrt(crystalRelX * crystalRelX + crystalRelZ * crystalRelZ);
        float distance = Mth.sqrt(crystalRelX * crystalRelX + crystalRelY * crystalRelY + crystalRelZ * crystalRelZ);
        mStack.pushPose();
        mStack.translate(0.0D, 2.0D, 0.0D);
        mStack.mulPose(Axis.YP.rotation((float) (-Math.atan2(crystalRelZ, crystalRelX)) - ((float) Math.PI / 2F)));
        mStack.mulPose(Axis.XP.rotation((float) (-Math.atan2(xzDistance, crystalRelY)) - ((float) Math.PI / 2F)));
        VertexConsumer builder = getter.getBuffer(BEAM_TYPE2);
        float vMin = ((float) animTicks + partialTicks) * 0.01F;
        float vMax = (Mth.sqrt(crystalRelX * crystalRelX + crystalRelY * crystalRelY + crystalRelZ * crystalRelZ) / 32.0F) + (((float) animTicks + partialTicks) * 0.01F);
        float f4 = 0.0F;
        float f5 = 0.1F;
        float texU = 0.0F;
        PoseStack.Pose stackLast = mStack.last();
        Matrix4f lastMatrix = stackLast.pose();

        float taperOffset = 10F;//0.2F;

        for (int j = 1; j <= 8; ++j) {
//            int shell = j / 8;
            float rSin = Mth.sin((float) j * ((float) Math.PI * 2F) / 8.0F) * 0.1F;
            float rCos = Mth.cos((float) j * ((float) Math.PI * 2F) / 8.0F) * 0.1F;
            float indexDecimal = (float) j / 8.0F;
            builder.addVertex(lastMatrix, f4 * taperOffset, f5 * taperOffset, 0.0F).setColor(1F, 1F, 1F, alpha).setUv(texU, vMin).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(stackLast, 0.0F, -1.0F, 0.0F);
            builder.addVertex(lastMatrix, f4, f5, distance).setColor(1F, 1F, 1F, alpha).setUv(texU, vMax).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(stackLast, 0.0F, -1.0F, 0.0F);
            builder.addVertex(lastMatrix, rSin, rCos, distance).setColor(1F, 1F, 1F, alpha).setUv(indexDecimal, vMax).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(stackLast, 0.0F, -1.0F, 0.0F);
            builder.addVertex(lastMatrix, rSin * taperOffset, rCos * taperOffset, 0.0F).setColor(1F, 1F, 1F, alpha).setUv(indexDecimal, vMin).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(stackLast, 0.0F, -1.0F, 0.0F);
            f4 = rSin;
            f5 = rCos;
            texU = indexDecimal;
        }

        mStack.popPose();
    }

    //Original
    public static void renderShaderBeam(float crystalRelX, float crystalRelY, float crystalRelZ, float partialTicks, int animTicks, PoseStack mStack, MultiBufferSource getter, int packedLight) {
        float xzDistance = Mth.sqrt(crystalRelX * crystalRelX + crystalRelZ * crystalRelZ);
        float distance = Mth.sqrt(crystalRelX * crystalRelX + crystalRelY * crystalRelY + crystalRelZ * crystalRelZ);
        mStack.pushPose();
        mStack.translate(0.0D, 2.0D, 0.0D);
        mStack.mulPose(Axis.YP.rotation((float) (-Math.atan2(crystalRelZ, crystalRelX)) - ((float) Math.PI / 2F)));
        mStack.mulPose(Axis.XP.rotation((float) (-Math.atan2(xzDistance, crystalRelY)) - ((float) Math.PI / 2F)));
        VertexConsumer builder = getter.getBuffer(beamType);
        float f2 = 0.0F - ((float) animTicks + partialTicks) * 0.01F;
        float f3 = Mth.sqrt(crystalRelX * crystalRelX + crystalRelY * crystalRelY + crystalRelZ * crystalRelZ) / 32.0F - ((float) animTicks + partialTicks) * 0.01F;
        float f4 = 0.0F;
        float f5 = 0.75F;
        float f6 = 0.0F;
        PoseStack.Pose stackLast = mStack.last();
        Matrix4f lastMatrix = stackLast.pose();

        for (int j = 1; j <= 8; ++j) {
            float rSin = Mth.sin((float) j * ((float) Math.PI * 2F) / 8.0F) * 0.75F;
            float rCos = Mth.cos((float) j * ((float) Math.PI * 2F) / 8.0F) * 0.75F;
            float indexDecimal = (float) j / 8.0F;
            builder.addVertex(lastMatrix, f4 * 0.2F, f5 * 0.2F, 0.0F).setColor(0, 0, 0, 255).setUv(f6, f2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(stackLast, 0.0F, -1.0F, 0.0F);
            builder.addVertex(lastMatrix, f4, f5, distance).setColor(255, 255, 255, 255).setUv(f6, f3).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(stackLast, 0.0F, -1.0F, 0.0F);
            builder.addVertex(lastMatrix, rSin, rCos, distance).setColor(255, 255, 255, 255).setUv(indexDecimal, f3).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(stackLast, 0.0F, -1.0F, 0.0F);
            builder.addVertex(lastMatrix, rSin * 0.2F, rCos * 0.2F, 0.0F).setColor(0, 0, 0, 255).setUv(indexDecimal, f2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(stackLast, 0.0F, -1.0F, 0.0F);
            f4 = rSin;
            f5 = rCos;
            f6 = indexDecimal;
        }

        mStack.popPose();
    }

}
