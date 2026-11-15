package com.brandon3055.draconicevolution.client.shader;

import com.brandon3055.brandonscore.client.shader.BCShader;
import com.brandon3055.brandonscore.client.shader.BCUniform;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.Identifier;

// port-26.1.2: CCL CCUniform/CCShaderInstance удалены. Шейдеры портированы на BC-26 BCShader (BCParams uniform-block,
// смещения как в ContribShader: UV1=80, UV2=88, BaseColor=64). GLSL у DE уже в shaders/core/ — требуется перевод
// на bc_params.glsl-блок в этапе рендер-шейдеров (см. stage2-отчёт).
public final class ToolShader extends BCShader<ToolShader> {

    public ToolShader(String path, VertexFormat format) {
        this(Identifier.fromNamespaceAndPath(DraconicEvolution.MODID, path), format);
    }

    public ToolShader(Identifier location, VertexFormat format) {
        super(location, format);
    }

    public BCUniform getUv1OverrideUniform() { return uniform(80); }
    public BCUniform getUv2OverrideUniform() { return uniform(88); }
    public BCUniform getBaseColorUniform() { return uniform(64); }
    public boolean hasUv1OverrideUniform() { return true; }
    public boolean hasUv2OverrideUniform() { return true; }
    public boolean hasBaseColorUniform() { return true; }
}
