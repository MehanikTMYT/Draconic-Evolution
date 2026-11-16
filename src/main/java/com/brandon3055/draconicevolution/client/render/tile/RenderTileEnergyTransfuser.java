package com.brandon3055.draconicevolution.client.render.tile;

import codechicken.lib.math.MathHelper;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyTransfuser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

/**
 * Created by brandon3055 on 12/12/2020.
 */
public class RenderTileEnergyTransfuser implements BlockEntityRenderer<TileEnergyTransfuser, RenderTileEnergyTransfuser.State> {

    private static final Style GALACTIC_STYLE = Style.EMPTY.withFont(new net.minecraft.network.chat.FontDescription.Resource(Minecraft.ALT_FONT));
    public static final Component[] TEXT = {
            Component.literal("N").withStyle(GALACTIC_STYLE),
            Component.literal("E").withStyle(GALACTIC_STYLE),
            Component.literal("S").withStyle(GALACTIC_STYLE),
            Component.literal("W").withStyle(GALACTIC_STYLE)
    };
    private static final Direction[] DIR_MAP = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    private final ItemModelResolver itemModels;
    public RenderTileEnergyTransfuser(BlockEntityRendererProvider.Context context) {
        itemModels = context.itemModelResolver();
    }

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState[] items = {new ItemStackRenderState(), new ItemStackRenderState(), new ItemStackRenderState(), new ItemStackRenderState()};
        final int[] colours = new int[4];
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(TileEnergyTransfuser tile, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        for (int i = 0; i < 4; i++) {
            state.colours[i] = tile.ioModes[i].get().getColour();
            itemModels.updateForTopItem(state.items[i], tile.itemsCombined.getStackInSlot(i), ItemDisplayContext.FIXED,
                    tile.getLevel(), null, tile.posSeed());
        }
    }

    @Override
    public void submit(State state, PoseStack mStack, SubmitNodeCollector collector, CameraRenderState camera) {
        Minecraft mc = Minecraft.getInstance();

        mStack.pushPose();
        mStack.translate(0.5, 0.5, 0.5);
        for (int i = 0; i < 4; i++) {
            Direction dir = DIR_MAP[i];
            mStack.pushPose();
            mStack.translate(0.5 * dir.getStepX(), 0, 0.5 * dir.getStepZ());
            mStack.mulPose(dir.getRotation());

            mStack.pushPose();
            mStack.mulPose(Axis.XP.rotationDegrees(90));
            double xOffset = dir == Direction.NORTH ? 2 : dir == Direction.SOUTH ? 1 : 2.5;
            mStack.translate(0.0625 * -xOffset, 0.0625 * -3.5, 0.0625 * 1.375);
            mStack.scale(0.0625F, 0.0625F, 0.0625F);
            collector.submitText(mStack, 0, 0, TEXT[i].getVisualOrderText(), false, Font.DisplayMode.NORMAL, 240, state.colours[i], 240, 0);
            mStack.popPose();

            ItemStackRenderState stack = state.items[i];
            if (!stack.isEmpty()) {
                mStack.mulPose(new Quaternionf().rotationYXZ(90 * (float) MathHelper.torad, 90 * (float) MathHelper.torad, 180 * (float) MathHelper.torad));
                mStack.translate(0, 0, 0.0625 * (1.5 / 2));
                mStack.scale(0.5F, 0.5F, 0.5F);
                stack.submit(mStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            }

            mStack.popPose();
        }
        mStack.popPose();
    }
}
