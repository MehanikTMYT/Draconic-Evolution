package com.brandon3055.draconicevolution.client.render.item;

import com.brandon3055.brandonscore.blocks.BlockBCore;
import com.brandon3055.draconicevolution.init.DEContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.math.Axis;

/** Created by brandon3055 on 18/04/2017. Cage, soul and tier cores are independently extracted. */
public class RenderItemStabilizedSpawner {
    public static MachineItemModel.Frozen extract(ItemStack stack, ItemModelResolver resolver, net.minecraft.client.multiplayer.ClientLevel level, int seed) {
        var mc = Minecraft.getInstance();
        var data = com.brandon3055.brandonscore.utils.ItemNBTHelper.getCompound(stack).getCompoundOrEmpty(BlockBCore.BC_TILE_DATA_TAG).getCompoundOrEmpty("bc_managed_data");
        int tier = Math.clamp(data.getCompoundOrEmpty("spawner_tier").getByteOr("value", (byte) 0), 0, 3);
        ItemStack soul = level == null ? ItemStack.EMPTY : data.read("mob_soul", ItemStack.OPTIONAL_CODEC, level.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE)).orElse(ItemStack.EMPTY);
        var entity = soul.isEmpty() ? null : DEContent.MOB_SOUL.get().getRenderEntity(soul);
        float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        var display = entity == null ? null : mc.getEntityRenderDispatcher().extractEntity(entity, partial);
        if (display != null) display.nameTag = null;
        float scale = entity == null ? 1 : 0.53125F / Math.max(1F, Math.max(entity.getBbWidth(), entity.getBbHeight()));
        float spin = (com.brandon3055.brandonscore.api.TimeKeeper.getClientTick() + partial) * 10;
        var camera = MachineItemModel.camera();
        var cage = new BlockModelRenderState();
        mc.getBlockModelResolver().update(cage, DEContent.STABILIZED_SPAWNER.get().defaultBlockState(), BlockDisplayContext.create());
        var core = new ItemStackRenderState();
        var cores = new net.minecraft.world.item.Item[]{DEContent.CORE_DRACONIUM.get(), DEContent.CORE_WYVERN.get(), DEContent.CORE_AWAKENED.get(), DEContent.CORE_CHAOTIC.get()};
        resolver.updateForTopItem(core, new ItemStack(cores[tier]), ItemDisplayContext.FIXED, level, null, seed);
        return (pose, collector, light, overlay, foil, outline) -> {
            cage.submitMultiLayer(pose, collector, light, overlay, outline);
            if (display != null) {
                pose.pushPose();
                pose.translate(0.5, 0.4, 0.5);
                pose.mulPose(Axis.YP.rotationDegrees(spin));
                pose.translate(0, -0.2, 0);
                pose.mulPose(Axis.XP.rotationDegrees(-30));
                pose.scale(scale, scale, scale);
                display.lightCoords = light; display.outlineColor = outline;
                mc.getEntityRenderDispatcher().submit(display, camera, 0, 0, 0, pose, collector);
                pose.popPose();
            }
            pose.pushPose();
            pose.translate(0.25, 1.225, 0.25);
            pose.scale(0.5F, 0.5F, 0.5F);
            pose.mulPose(Axis.XP.rotationDegrees(90));
            core.submit(pose, collector, light, overlay, outline);
            pose.translate(0, 0, 1.9);
            core.submit(pose, collector, light, overlay, outline);
            pose.popPose();
        };
    }
}
