package com.brandon3055.draconicevolution.client.render.tile;

import com.brandon3055.draconicevolution.blocks.tileentity.TileEntityDetector;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

public class RenderTileEntityDetector implements BlockEntityRenderer<TileEntityDetector, RenderTileEntityDetector.State> {

    private ItemStack eye = ItemStack.EMPTY;
    private ItemStack skull = ItemStack.EMPTY;

    private final ItemModelResolver itemModels;
    public RenderTileEntityDetector(BlockEntityRendererProvider.Context context) { itemModels = context.itemModelResolver(); }

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState item = new ItemStackRenderState();
        float scale, yaw, pitch;
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(TileEntityDetector tile, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        boolean advanced = tile.isAdvanced();
        state.scale = advanced ? 0.5F : 0.35F;
        state.yaw = tile.lastLookYaw + (tile.lookYaw - tile.lastLookYaw) * partialTicks;
        state.pitch = tile.lastLookPitch + (tile.lookPitch - tile.lastLookPitch) * partialTicks;
        itemModels.updateForTopItem(state.item, getRenderStack(advanced), ItemDisplayContext.FIXED,
                tile.getLevel(), null, tile.posSeed());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.73, 0.5);
        poseStack.scale(state.scale, state.scale, state.scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.pitch));
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    private ItemStack getRenderStack(boolean advanced) {
        if (advanced) {
            if (skull.isEmpty()) {
                skull = new ItemStack(Items.WITHER_SKELETON_SKULL, 1);
            }
            return skull;
        } else {
            if (eye.isEmpty()) {
                eye = new ItemStack(Items.ENDER_EYE);
            }
            return eye;
        }
    }

    @Override
    public AABB getRenderBoundingBox(TileEntityDetector blockEntity) {
        return BlockEntityRenderer.super.getRenderBoundingBox(blockEntity).expandTowards(0, 1, 0);
    }
}
