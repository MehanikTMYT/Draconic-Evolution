package com.brandon3055.draconicevolution.client.shader;

import com.brandon3055.brandonscore.client.shader.BCShader;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import java.util.*;

/** Original DE effect programs. de_effect_params.glsl explicitly defines this shader-specific 176 byte layout. */
public final class DEEffectShader extends BCShader<DEEffectShader> {
    private final Identifier vertex, fragment;
    private final VertexFormat format;
    private final boolean translucent, cull, depthWrite, fog, texture, lightmap;
    private final com.mojang.blaze3d.platform.CompareOp depthTest;
    private BlendFunction blend = BlendFunction.TRANSLUCENT;
    private final Map<VertexFormat.Mode, RenderPipeline> pipelines = new EnumMap<>(VertexFormat.Mode.class);
    public DEEffectShader(String vertex, String fragment, VertexFormat format, boolean translucent, boolean cull, boolean depthWrite, boolean fog, boolean texture, boolean lightmap) {
        this(vertex, fragment, format, translucent, cull, depthWrite, fog, texture, lightmap, com.mojang.blaze3d.platform.CompareOp.LESS_THAN_OR_EQUAL);
    }
    public DEEffectShader(String vertex, String fragment, VertexFormat format, boolean translucent, boolean cull, boolean depthWrite, boolean fog, boolean texture, boolean lightmap, com.mojang.blaze3d.platform.CompareOp depthTest) {
        super(id(fragment), format);
        this.depthTest = depthTest;
        this.vertex = id(vertex); this.fragment = id(fragment); this.format = format;
        this.translucent = translucent; this.cull = cull; this.depthWrite = depthWrite;
        this.fog = fog; this.texture = texture; this.lightmap = lightmap;
        // BC's constructor initializes BC's layout. Explicitly initialize every effect member.
        for (int offset = 64; offset < 176; offset += 4) uniform(offset).glUniform1f(0);
        uniform(64).glUniform4f(1, 1, 1, 1);
        uniform(120).glUniform1f(1);
    }
    /** Configure before pipeline registration; parameters still use the shared 176-byte effect schema. */
    public DEEffectShader blend(BlendFunction blend) {
        if (!pipelines.isEmpty()) throw new IllegalStateException("Pipeline already registered");
        this.blend = blend;
        return this;
    }
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath("draconicevolution", "core/" + path); }
    @Override
    public RenderPipeline pipeline(VertexFormat.Mode mode) {
        return pipelines.computeIfAbsent(mode, m -> {
            var builder = RenderPipeline.builder(fog ? RenderPipelines.MATRICES_FOG_SNIPPET : RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withLocation(fragment.withSuffix((depthWrite ? "_depth_" : "_color_") + depthTest.name().toLowerCase(Locale.ROOT) + "_" + m.name().toLowerCase(Locale.ROOT)))
                .withVertexShader(vertex).withFragmentShader(fragment).withVertexFormat(format, m)
                .withUniform("BCParams", UniformType.UNIFORM_BUFFER)
                .withCull(cull).withColorTargetState(translucent ? new ColorTargetState(blend) : ColorTargetState.DEFAULT)
                .withDepthStencilState(new DepthStencilState(depthTest, depthWrite));
            if (texture) builder.withSampler("Sampler0");
            if (lightmap) builder.withSampler("Sampler2");
            return builder.build();
        });
    }
}
