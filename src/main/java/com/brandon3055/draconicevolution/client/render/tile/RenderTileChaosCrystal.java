package com.brandon3055.draconicevolution.client.render.tile;

import codechicken.lib.math.MathHelper;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.model.OBJParser;
import codechicken.lib.util.ClientUtils;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Vector3;
import com.brandon3055.draconicevolution.blocks.tileentity.TileChaosCrystal;
import com.brandon3055.draconicevolution.client.DEShaders;
import com.brandon3055.draconicevolution.client.handler.ClientEventHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import com.brandon3055.brandonscore.client.render.ExtractedModel;
import com.brandon3055.draconicevolution.client.render.DEModelEmission;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.Map;

import static com.brandon3055.draconicevolution.DraconicEvolution.MODID;

/**
 * Created by brandon3055 on 24/9/2015.
 */
public class RenderTileChaosCrystal implements BlockEntityRenderer<TileChaosCrystal, RenderTileChaosCrystal.State> {

    private static final RenderType CHAOS_CRYSTAL_INNER = DEShaders.chaosBlockShader.renderType("de_chaos_crystal_inner",
            Identifier.fromNamespaceAndPath(MODID, "textures/item/equipment/chaos_shader.png"), Mode.TRIANGLES);
    private static final RenderType CHAOS_CRYSTAL = com.brandon3055.draconicevolution.client.render.tile.TileRenderPipelines.CHAOS_SURFACE;
    private static final RenderType CHAOS_CRYSTAL_SHIELD = DEShaders.shieldShader.renderType("de_chaos_crystal_shield", null, Mode.TRIANGLES);

    private final CCModel model;

    public RenderTileChaosCrystal(BlockEntityRendererProvider.Context context) {
        Map<String, CCModel> map = new OBJParser(Identifier.fromNamespaceAndPath(MODID, "models/block/chaos_crystal.obj"))
                .ignoreMtl()
                .parse();
        model = CCModel.combine(map.values())
                .backfacedCopy()
                .computeNormals();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    public static class State extends BlockEntityRenderState {
        ExtractedModel geometry;
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(TileChaosCrystal tile, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        ExtractedModel.Capture capture = new ExtractedModel.Capture();
        extractGeometry(tile, partialTicks, new PoseStack(), capture, state.lightCoords,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        state.geometry = capture.finish();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.geometry != null) state.geometry.submit(poseStack, collector);
    }

    private void extractGeometry(TileChaosCrystal te, float partialTicks, PoseStack mStack, MultiBufferSource getter, int packedLight, int packedOverlay) {
        if (te.parentPos.notNull()) return;
        Matrix4 mat = new Matrix4(mStack);
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;

        mat.translate(0.5, 0.5, 0.5);
        mat.rotate((ClientEventHandler.elapsedTicks + partialTicks) / 180F, Vector3.Y_POS);
        mat.scale(0.75F);

        Player player = Minecraft.getInstance().player;
        DEShaders.chaosBlockTime.glUniform1f((float) ClientUtils.getRenderTime());
        DEShaders.chaosBlockYaw.glUniform1f((float) ((player == null ? 0 : player.getYRot()) * MathHelper.torad));
        DEShaders.chaosBlockPitch.glUniform1f((float) -((player == null ? 0 : player.getXRot()) * MathHelper.torad));
        ccrs.bind(CHAOS_CRYSTAL_INNER, getter);
        DEModelEmission.render(model, ccrs, mat);

        ccrs.baseColour = 0xFFFFFFF0;
        ccrs.bind(CHAOS_CRYSTAL, getter);
        DEModelEmission.render(model, ccrs, mat);

        if (!te.guardianDefeated.get()) {
            DEShaders.shieldBarMode.glUniform1i(0);
            DEShaders.shieldActivation.glUniform1f(1F);
            DEShaders.shieldColour.glUniform4f(1F, 0F, 0F, 1F);
            ccrs.bind(CHAOS_CRYSTAL_SHIELD, getter);
            DEModelEmission.render(model, ccrs, mat);
        }
    }

    @Override
    public AABB getRenderBoundingBox(TileChaosCrystal blockEntity) {
        return BlockEntityRenderer.super.getRenderBoundingBox(blockEntity).inflate(3);
    }
}
