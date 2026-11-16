package com.brandon3055.draconicevolution.client.render.particle;

import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.CompareOp;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

/** MC26 extraction layers retain the old blend modes and disabled depth writes. */
public final class DEParticleLayers {
    private DEParticleLayers() {}

    private static final RenderPipeline TRANSLUCENT_PIPELINE = pipeline("particle_translucent_no_write", BlendFunction.TRANSLUCENT);
    private static final RenderPipeline ADDITIVE_PIPELINE = pipeline("particle_additive", BlendFunction.LIGHTNING);
    public static final SingleQuadParticle.Layer TRANSLUCENT_NO_WRITE = new SingleQuadParticle.Layer(true, TextureAtlas.LOCATION_PARTICLES, TRANSLUCENT_PIPELINE);
    public static final SingleQuadParticle.Layer ADDITIVE = new SingleQuadParticle.Layer(true, TextureAtlas.LOCATION_PARTICLES, ADDITIVE_PIPELINE);

    private static RenderPipeline pipeline(String name, BlendFunction blend) {
        return RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath("draconicevolution", "pipeline/" + name))
                .withColorTargetState(new ColorTargetState(blend))
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                .build();
    }

    public static void register(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(TRANSLUCENT_PIPELINE);
        event.registerPipeline(ADDITIVE_PIPELINE);
    }
}
