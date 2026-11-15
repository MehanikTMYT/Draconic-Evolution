package com.brandon3055.draconicevolution.client.shader;

import com.brandon3055.brandonscore.client.shader.BCShader;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/** Original energy-crystal noise program, with extraction-owned BCParams instead of GL uniforms. */
public final class EnergyCrystalShader extends BCShader<EnergyCrystalShader> {
    private final Map<VertexFormat.Mode, RenderPipeline> pipelines = new EnumMap<>(VertexFormat.Mode.class);
    public EnergyCrystalShader() {
        super(Identifier.fromNamespaceAndPath("draconicevolution", "energy_crystal"), DefaultVertexFormat.POSITION_TEX);
    }
    public void setColour(float r, float g, float b) { uniform(64).glUniform4f(r, g, b, 1); }
    public void setMipmap(float value) { uniform(100).glUniform1f(value); }
    @Override
    public RenderPipeline pipeline(VertexFormat.Mode mode) {
        return pipelines.computeIfAbsent(mode, m -> RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath("draconicevolution", "pipeline/energy_crystal_" + m.name().toLowerCase(Locale.ROOT)))
                .withVertexShader(Identifier.fromNamespaceAndPath("draconicevolution", "core/energy_crystal"))
                .withFragmentShader(Identifier.fromNamespaceAndPath("draconicevolution", "core/energy_crystal"))
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX, m)
                .withUniform("BCParams", UniformType.UNIFORM_BUFFER)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(DepthStencilState.DEFAULT).withCull(false).build());
    }
}
