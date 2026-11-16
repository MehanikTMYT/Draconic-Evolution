package com.brandon3055.draconicevolution.client.render.item;

import com.brandon3055.draconicevolution.init.DEContent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

/** Created by brandon3055 on 18/04/2017. MC26 state extraction. */
public class RenderItemMobSoul {
    public static MachineItemModel.Frozen extract(ItemStack stack, ItemDisplayContext context) {
        var mc = Minecraft.getInstance();
        var mob = DEContent.MOB_SOUL.get().getRenderEntity(stack);
        if (mob == null) throw new IllegalStateException("Mob soul display entity could not be resolved");
        float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        var state = mc.getEntityRenderDispatcher().extractEntity(mob, partial);
        state.nameTag = null;
        float scale = 1F / Math.max(mob.getBbWidth(), mob.getBbHeight());
        float time = com.brandon3055.brandonscore.api.TimeKeeper.getClientTick() + partial;
        var camera = MachineItemModel.camera();
        return (pose, collector, light, overlay, foil, outline) -> {
            pose.pushPose();
            pose.translate(0.5, 0, 0.5);
            pose.scale(scale, scale, scale);
            if (context != ItemDisplayContext.GROUND && context != ItemDisplayContext.FIXED) {
                float rotA = (float) Math.sin(time / 50F) * 15F * (float) Math.PI / 180F;
                pose.mulPose(new org.joml.Quaternionf().rotationXYZ(rotA, 0, -0.5F * rotA));
                pose.mulPose(Axis.YP.rotationDegrees(time * 3));
            }
            state.lightCoords = light;
            state.outlineColor = outline;
            mc.getEntityRenderDispatcher().submit(state, camera, 0, 0, 0, pose, collector);
            pose.popPose();
        };
    }
}
