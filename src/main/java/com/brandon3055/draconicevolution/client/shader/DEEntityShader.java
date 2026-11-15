package com.brandon3055.draconicevolution.client.shader;

import com.brandon3055.brandonscore.client.shader.BCShader;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/** DE's original tool vertex/fragment programs, not BC's contributor shader. */
public class DEEntityShader<T extends DEEntityShader<T>> extends BCShader<T> {
    private final Identifier program;
    private final VertexFormat vertexFormat;
    private final Map<VertexFormat.Mode, RenderPipeline> pipelines = new EnumMap<>(VertexFormat.Mode.class);
    protected DEEntityShader(Identifier program, VertexFormat format) {
        super(program, format);
        this.program = program; this.vertexFormat = format;
    }
    @Override
    public RenderPipeline pipeline(VertexFormat.Mode mode) {
        return pipelines.computeIfAbsent(mode, m -> RenderPipeline.builder(RenderPipelines.MATRICES_FOG_LIGHT_DIR_SNIPPET)
                .withLocation(program.withSuffix("_" + m.name().toLowerCase(Locale.ROOT)))
                .withVertexShader(Identifier.fromNamespaceAndPath(program.getNamespace(), "core/tools/tool_base"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(program.getNamespace(), "core/" + program.getPath()))
                .withVertexFormat(vertexFormat, m).withCull(false)
                .withSampler("Sampler0").withSampler("Sampler1").withSampler("Sampler2")
                .withUniform("BCParams", UniformType.UNIFORM_BUFFER)
                .withColorTargetState(program.getPath().equals("tools/bow_string") ? new ColorTargetState(BlendFunction.LIGHTNING)
                        : program.getPath().equals("tools/chestpiece_shield") ? new ColorTargetState(BlendFunction.TRANSLUCENT) : ColorTargetState.DEFAULT)
                .withDepthStencilState(program.getPath().equals("tools/bow_string") ? new DepthStencilState(com.mojang.blaze3d.platform.CompareOp.LESS_THAN_OR_EQUAL, false) : DepthStencilState.DEFAULT).build());
    }
}
