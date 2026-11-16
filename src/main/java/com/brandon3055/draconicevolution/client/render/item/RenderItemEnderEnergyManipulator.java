package com.brandon3055.draconicevolution.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.resources.Identifier;
import com.brandon3055.draconicevolution.client.DEShaders;
import com.mojang.math.Axis;

/**
 * Created by brandon3055 on 18/04/2017.
 */
public class RenderItemEnderEnergyManipulator implements DEItemRenderer {
    private final SkullModel skull = new SkullModel(SkullModel.createMobHeadLayer().bakeRoot());
    private static final net.minecraft.client.renderer.rendertype.RenderType TEXTURE = RenderTypes.entityCutout(
            Identifier.withDefaultNamespace("textures/entity/skeleton/wither_skeleton.png"));
    private static final net.minecraft.client.renderer.rendertype.RenderType SHIELD = DEShaders.reactorShieldShader.renderType(
            "de_ender_manipulator_shield", null, com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS);
    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        if (context == ItemDisplayContext.FIXED) pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.scale(-1, -1, 1);
        pose.translate(0, 0.25, 0);
        var state = new SkullModelBase.State();
        state.yRot = 180;
        skull.setupAnim(state);
        skull.renderToBuffer(pose, buffers.getBuffer(TEXTURE), light, overlay);
        float time = com.brandon3055.brandonscore.api.TimeKeeper.getClientTick()
                + net.minecraft.client.Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        DEShaders.reactorShieldTime.glUniform1f(time / -100F);
        DEShaders.reactorShieldIntensity.glUniform1f(0.09F);
        skull.renderToBuffer(pose, buffers.getBuffer(SHIELD), light, overlay);
        DEShaders.reactorShieldTime.glUniform1f(time / 100F);
        DEShaders.reactorShieldIntensity.glUniform1f(0.02F);
        skull.renderToBuffer(pose, buffers.getBuffer(SHIELD), light, overlay);
        pose.popPose();
    }
    @Override public void getExtents(java.util.function.Consumer<org.joml.Vector3fc> output) {
        output.accept(new org.joml.Vector3f(0.25F, 0.25F, 0.25F));
        output.accept(new org.joml.Vector3f(0.75F, 0.75F, 0.75F));
    }
}
