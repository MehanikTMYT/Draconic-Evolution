package com.brandon3055.draconicevolution.client.shader;

import com.brandon3055.brandonscore.client.shader.BCShader;
import com.brandon3055.brandonscore.client.shader.BCUniform;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.Identifier;

// port-26.1.2: портирован на BCShader (BCParams); смещения: Activation=104, BaseColor=64 (как ContribShader.transition/baseColor).
public final class ShieldShader extends DEEntityShader<ShieldShader> {

    public ShieldShader(String path, VertexFormat format) {
        this(Identifier.fromNamespaceAndPath(DraconicEvolution.MODID, path), format);
    }

    public ShieldShader(Identifier location, VertexFormat format) {
        super(location, format);
    }

    public BCUniform getActivationUniform() { return uniform(104); }
    public BCUniform getBaseColourUniform() { return uniform(64); }
}
