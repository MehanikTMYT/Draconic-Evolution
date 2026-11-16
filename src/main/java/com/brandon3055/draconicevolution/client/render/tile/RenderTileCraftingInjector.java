package com.brandon3055.draconicevolution.client.render.tile;

import codechicken.lib.math.MathHelper;
import com.brandon3055.draconicevolution.blocks.machines.CraftingInjector;
import com.brandon3055.draconicevolution.blocks.tileentity.TileFusionCraftingInjector;
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
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

public class RenderTileCraftingInjector implements BlockEntityRenderer<TileFusionCraftingInjector, RenderTileCraftingInjector.State> {

    private final ItemModelResolver itemModels;

    public RenderTileCraftingInjector(BlockEntityRendererProvider.Context context) {
        itemModels = context.itemModelResolver();
    }

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState item = new ItemStackRenderState();
        Direction facing = Direction.UP;
        float rotation;
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(TileFusionCraftingInjector tile, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.item.clear();
        if (tile.getCore() != null && tile.getCore().isCrafting() && tile.getCore().getFusionState().ordinal() > 1) return;
        BlockState block = tile.getBlockState();
        if (block.isAir() || !block.hasProperty(CraftingInjector.FACING)) return;
        state.facing = block.getValue(CraftingInjector.FACING);
        state.rotation = (ClientEventHandler.elapsedTicks + partialTicks) * -0.8F;
        itemModels.updateForTopItem(state.item, tile.itemHandler.getStackInSlot(0), ItemDisplayContext.FIXED,
                tile.getLevel(), null, tile.posSeed());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.item.isEmpty()) return;
        Direction facing = state.facing;
        poseStack.pushPose();
        poseStack.translate(0.5 + facing.getStepX() * 0.45, 0.5 + facing.getStepY() * 0.45, 0.5 + facing.getStepZ() * 0.45);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        if (facing.getAxis() == Direction.Axis.Y) {
            if (facing == Direction.DOWN) poseStack.mulPose(Axis.XP.rotationDegrees(180));
        } else {
            poseStack.mulPose(new Quaternionf().rotationXYZ(facing.getStepZ() * 90 * (float) MathHelper.torad, 0,
                    facing.getStepX() * -90 * (float) MathHelper.torad));
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
