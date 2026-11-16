package com.brandon3055.draconicevolution.client.render.tile;

import codechicken.lib.math.MathHelper;

import com.brandon3055.brandonscore.client.render.RenderUtils;

import com.brandon3055.brandonscore.utils.Utils;

import com.brandon3055.draconicevolution.DEOldConfig;

import com.brandon3055.draconicevolution.blocks.tileentity.TileDislocatorPedestal;

import com.brandon3055.draconicevolution.init.DEContent;

import com.brandon3055.draconicevolution.items.tools.DislocatorAdvanced;

import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.blaze3d.vertex.VertexConsumer;

import com.mojang.math.Axis;

import java.util.List;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;

import net.minecraft.client.gui.Font;

import net.minecraft.client.player.LocalPlayer;

import net.minecraft.client.renderer.MultiBufferSource;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import net.minecraft.client.renderer.rendertype.RenderType;

import net.minecraft.client.resources.model.geometry.BakedQuad;

import net.minecraft.core.component.DataComponents;

import net.minecraft.network.chat.Component;

import net.minecraft.world.item.ItemDisplayContext;

import net.minecraft.world.item.ItemStack;

import net.minecraft.world.phys.BlockHitResult;

import net.minecraft.world.phys.HitResult;

import org.joml.Quaternionf;




/**
 * Created by brandon3055 on 27/09/2016.
 */
public class RenderTileDislocatorPedestal implements BlockEntityRenderer<TileDislocatorPedestal, RenderTileDislocatorPedestal.State> {

    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();
    private final BlockModelResolver blockModels;
    private final ItemModelResolver itemModels;

    public RenderTileDislocatorPedestal(BlockEntityRendererProvider.Context context) {
        blockModels = context.blockModelResolver();
        itemModels = context.itemModelResolver();
    }

    public static class State extends BlockEntityRenderState {
        final BlockModelRenderState pedestal = new BlockModelRenderState();
        final ItemStackRenderState item = new ItemStackRenderState();
        float rotation;
        String name = "";
        Vec3 playerOffset = Vec3.ZERO;
        Vec3 playerBaseOffset = Vec3.ZERO;
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(TileDislocatorPedestal tile, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        // Resolve from the current model manager each extraction; never cache quads across resource reloads.
        blockModels.update(state.pedestal, DEContent.DISLOCATOR_PEDESTAL.get().defaultBlockState(), DISPLAY_CONTEXT);
        state.rotation = -tile.rotation.get() * 22.5F;
        ItemStack stack = tile.itemHandler.getStackInSlot(0);
        itemModels.updateForTopItem(state.item, stack, ItemDisplayContext.FIXED, tile.getLevel(), null, tile.posSeed());
        state.name = "";
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || stack.isEmpty()) return;
        HitResult hit = player.pick(10, partialTicks, true);
        boolean cursorOver = hit instanceof BlockHitResult block && block.getBlockPos().equals(tile.getBlockPos());
        if (!cursorOver && player.isShiftKeyDown() != DEOldConfig.invertDPDSB) return;
        String name = stack.getOrDefault(DataComponents.CUSTOM_NAME, Component.empty()).getString();
        if (stack.getItem() instanceof DislocatorAdvanced advanced) {
            DislocatorAdvanced.DislocatorTarget target = advanced.getSelected(stack);
            if (target != null) name = target.getName();
        }
        state.name = name;
        state.playerOffset = player.getEyePosition().subtract(Vec3.atCenterOf(tile.getBlockPos()));
        state.playerBaseOffset = player.position().subtract(Vec3.atCenterOf(tile.getBlockPos()));
    }

    @Override
    public void submit(State state, PoseStack mStack, SubmitNodeCollector collector, CameraRenderState camera) {
        mStack.pushPose();
        mStack.translate(0.5, 0.5, 0.5);
        mStack.mulPose(Axis.YP.rotationDegrees(state.rotation));
        mStack.translate(-0.5, -0.5, -0.5);
        state.pedestal.submitMultiLayer(mStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        if (!state.item.isEmpty()) {
            mStack.pushPose();
            mStack.translate(0.5, 0.79, 0.52);
            mStack.scale(0.5F, 0.5F, 0.5F);
            mStack.mulPose(Axis.XP.rotationDegrees(-67.5F));
            state.item.submit(mStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            mStack.popPose();
        }
        mStack.popPose();
        if (!state.name.isEmpty()) drawName(state, mStack, collector);
    }

    private void drawName(State state, PoseStack mStack, SubmitNodeCollector collector) {
        Minecraft mc = Minecraft.getInstance();
        String name = state.name;
        mStack.pushPose();
        mStack.translate(0.5, 1.125, 0.5);
        mStack.scale(0.02F, 0.02F, 0.02F);
        mStack.mulPose(new Quaternionf().rotationXYZ(0, -90 * (float) MathHelper.torad, 180 * (float) MathHelper.torad));

        double xDiff = state.playerOffset.x;
        double yDiff = state.playerOffset.y - 0.625;
        double zDiff = state.playerOffset.z;
        double yawAngle = Math.toDegrees(Math.atan2(zDiff, xDiff));
        double pitchAngle = Math.toDegrees(Math.atan2(yDiff, state.playerBaseOffset.length()));

        mStack.mulPose(Axis.YP.rotationDegrees((float) yawAngle));
        mStack.mulPose(Axis.XP.rotationDegrees((float) -pitchAngle));
        mStack.scale(1, 1, -1);

        int textWidth = mc.font.width(name);
        mStack.translate(0, 0, -0.0125);
        collector.submitText(mStack, -(textWidth / 2F), 0, Component.literal(name).getVisualOrderText(), true, Font.DisplayMode.NORMAL, 15728880, 0xffffff, 0, 0);
        mStack.popPose();
    }
}
