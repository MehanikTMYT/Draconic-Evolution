package com.brandon3055.draconicevolution.client.render.tile;

import codechicken.lib.math.MathHelper;
import com.brandon3055.draconicevolution.blocks.PlacedItem;
import com.brandon3055.draconicevolution.blocks.tileentity.TilePlacedItem;
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
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;

import java.util.List;

/**
 * Created by brandon3055 on 25/07/2016.
 */
public class RenderTilePlacedItem implements BlockEntityRenderer<TilePlacedItem, RenderTilePlacedItem.State> {

    private final ItemModelResolver itemModels;
    public RenderTilePlacedItem(BlockEntityRendererProvider.Context context) { itemModels = context.itemModelResolver(); }

    public static class State extends BlockEntityRenderState {
        final java.util.List<ItemStackRenderState> items = new java.util.ArrayList<>();
        final java.util.List<Boolean> blocks = new java.util.ArrayList<>();
        final java.util.List<Float> rotations = new java.util.ArrayList<>();
        Direction direction = Direction.UP;
        float scale;
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(TilePlacedItem tile, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        List<ItemStack> stacks = tile.getStacksInOrder();
        state.scale = stacks.size() == 1 && tile.toolMode.get() ? 14 / 16F : 7 / 16F;
        state.direction = tile.getBlockState().getValue(PlacedItem.FACING);
        state.items.clear();
        state.blocks.clear();
        state.rotations.clear();
        int seed = (int) tile.getBlockPos().asLong();
        for (int i = 0; i < stacks.size(); i++) {
            ItemStackRenderState item = new ItemStackRenderState();
            itemModels.updateForTopItem(item, stacks.get(i), ItemDisplayContext.FIXED, tile.getLevel(), null, seed + i);
            state.items.add(item);
            state.blocks.add(stacks.get(i).getItem() instanceof BlockItem);
            state.rotations.add(tile.rotation[i].get() * -22.5F);
        }
    }

    @Override
    public void submit(State state, PoseStack mStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float scale = state.scale;
        Direction direction = state.direction;
        int size = state.items.size();
        mStack.pushPose();
        mStack.translate(0.5, 0.5, 0.5);
        rotateToSide(direction, mStack);
        for (int i = 0; i < size; i++) {
            mStack.pushPose();
            if (state.blocks.get(i)) {
                mStack.translate(PlacedItem.getXOffset(i, size), -0.5 + 3 / 16D, PlacedItem.getZOffset(i, size));
                mStack.mulPose(Axis.YP.rotationDegrees(state.rotations.get(i)));
                if (direction.getAxis() != Direction.Axis.Y) mStack.mulPose(Axis.XP.rotationDegrees(90));
                mStack.scale(6 / 8F, 6 / 8F, 6 / 8F);
            } else {
                mStack.translate(PlacedItem.getXOffset(i, size), -0.5 + (0.55 / 16D) * scale, PlacedItem.getZOffset(i, size));
                mStack.mulPose(Axis.YP.rotationDegrees(state.rotations.get(i)));
                mStack.mulPose(Axis.XP.rotationDegrees(90));
                mStack.scale(scale, scale, scale);
            }
            state.items.get(i).submit(mStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            mStack.popPose();
            mStack.translate(0, 0.00005F, 0);
        }
        mStack.popPose();
    }

    //TODO Test these
    private void rotateToSide(Direction direction, PoseStack mStack) {
        switch (direction) {
            case DOWN:
                mStack.mulPose(Axis.XP.rotationDegrees(180));
                break;
            case NORTH:
                mStack.mulPose(Axis.XN.rotationDegrees(90));
                break;
            case SOUTH:
                mStack.mulPose(new Quaternionf().rotationXYZ(-90 * (float) MathHelper.torad, 0, 180 * (float) MathHelper.torad));
                break;
            case WEST:
                mStack.mulPose(new Quaternionf().rotationXYZ(-90 * (float) MathHelper.torad, 0, 90 * (float) MathHelper.torad));
                break;
            case EAST:
                mStack.mulPose(new Quaternionf().rotationXYZ(-90 * (float) MathHelper.torad, 0, -90 * (float) MathHelper.torad));
                break;
        }
    }

    @Override
    public AABB getRenderBoundingBox(TilePlacedItem blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(1);
    }
}
