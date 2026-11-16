package com.brandon3055.draconicevolution.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WitherBossRenderer;

/** The vanilla state renderer retains the wither's head animations, armor layer,
 * full-bright block lighting, invulnerability texture flashing and spawn scale. */
public class GuardianWitherRenderer extends WitherBossRenderer {
    public GuardianWitherRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
