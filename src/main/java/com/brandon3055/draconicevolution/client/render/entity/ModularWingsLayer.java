package com.brandon3055.draconicevolution.client.render.entity;

import com.brandon3055.draconicevolution.init.DEClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** Modular armor and equipment-manager elytra visibility is extracted on the entity thread. */
public final class ModularWingsLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final Identifier DEFAULT_TEXTURE = Identifier.withDefaultNamespace("textures/entity/equipment/wings/elytra.png");
    private final ElytraModel model;
    private final ElytraModel babyModel;

    public ModularWingsLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent, EntityModelSet models) {
        super(parent);
        model = new ElytraModel(models.bakeLayer(ModelLayers.ELYTRA));
        babyModel = new ElytraModel(models.bakeLayer(ModelLayers.ELYTRA_BABY));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float yaw, float pitch) {
        if (!Boolean.TRUE.equals(state.getRenderData(DEClient.MODULAR_WINGS))) return;
        Identifier texture = DEFAULT_TEXTURE;
        if (state.skin.elytra() != null) texture = state.skin.elytra().texturePath();
        else if (state.skin.cape() != null && state.showCape) texture = state.skin.cape().texturePath();
        poseStack.pushPose();
        poseStack.translate(0, 0, 0.125F);
        ElytraModel wings = state.isBaby ? babyModel : model;
        collector.order(0).submitModel(wings, state, poseStack,
                net.minecraft.client.renderer.rendertype.RenderTypes.armorCutoutNoCull(texture), light,
                OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
        if (state.chestEquipment.hasFoil()) {
            collector.order(1).submitModel(wings, state, poseStack,
                    net.minecraft.client.renderer.rendertype.RenderTypes.armorEntityGlint(), light,
                    OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
        }
        poseStack.popPose();
    }
}
