package com.brandon3055.draconicevolution.client.render.tile;

import codechicken.lib.math.MathHelper;
import com.brandon3055.draconicevolution.blocks.tileentity.TileDisenchanter;
import com.brandon3055.draconicevolution.client.handler.ClientEventHandler;
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
import org.joml.Quaternionf;

public class RenderTileDisenchanter implements BlockEntityRenderer<TileDisenchanter, RenderTileDisenchanter.State> {

    private final ItemModelResolver itemModels;
    public RenderTileDisenchanter(BlockEntityRendererProvider.Context context) { itemModels = context.itemModelResolver(); }

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState input = new ItemStackRenderState();
        final ItemStackRenderState books = new ItemStackRenderState();
        float time;
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(TileDisenchanter tile, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.time = ClientEventHandler.elapsedTicks + partialTicks;
        itemModels.updateForTopItem(state.input, tile.itemHandler.getStackInSlot(0), ItemDisplayContext.FIXED,
                tile.getLevel(), null, tile.posSeed());
        itemModels.updateForTopItem(state.books, tile.itemHandler.getStackInSlot(1), ItemDisplayContext.FIXED,
                tile.getLevel(), null, tile.posSeed());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.input.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5, 1.15 + Math.sin(state.time / 40) / 10, 0.5);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.time * 0.8F));
            state.input.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
        if (!state.books.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.76, 0.5);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(new Quaternionf().rotationXYZ(90 * (float) MathHelper.torad, 0, 180 * (float) MathHelper.torad));
            state.books.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
