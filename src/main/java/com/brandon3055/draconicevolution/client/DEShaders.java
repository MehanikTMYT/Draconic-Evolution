package com.brandon3055.draconicevolution.client;

import codechicken.lib.util.ClientUtils;
import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.draconicevolution.client.shader.ShieldShader;
import com.brandon3055.draconicevolution.client.shader.ToolShader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

import static com.brandon3055.draconicevolution.DraconicEvolution.MODID;

/**
 * Created by brandon3055 on 15/05/2022
 * port-26.1.2: RegisterShadersEvent/CCL-шейдеры удалены в 26.
 * - Tool/Shield шейдеры портированы на BCShader/BCParams (pipeline-API), регистрация через RegisterRenderPipelinesEvent (эталон BCShaders-26).
 * - ЭТАП 3 (решение владельца): 8 core-шейдеров CCShaderInstance (reactor/reactor_shield/chaos_block/shield/energy_crystal/
 *   energy_core/reactor_beam/explosion*) требуют перевода GLSL на bc_params.glsl uniform-block и замены рендер-вызовов
 *   на BCShader.renderType()/ShaderDraw — см. stage2-отчёт, визуальный QA обязателен.
 */
public class DEShaders {

    private static final CrashLock LOCK = new CrashLock("Already Initialized");

    public static final com.brandon3055.draconicevolution.client.shader.ToolChaosShader TOOL_CHAOS_SHADER =
            new com.brandon3055.draconicevolution.client.shader.ToolChaosShader();
    public static final ToolShader TOOL_BASE_SHADER = new ToolShader("tools/tool_base", DefaultVertexFormat.ENTITY);
    public static final ToolShader TOOL_GEM_SHADER = new ToolShader("tools/tool_gem", DefaultVertexFormat.ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));
    public static final ToolShader TOOL_TRACE_SHADER = new ToolShader("tools/tool_trace", DefaultVertexFormat.ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));
    public static final ToolShader TOOL_BLADE_SHADER = new ToolShader("tools/tool_blade", DefaultVertexFormat.ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));
    public static final ToolShader BOW_STRING_SHADER = new ToolShader("tools/bow_string", DefaultVertexFormat.ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));
    public static final ToolShader CHESTPIECE_GEM_SHADER = new ToolShader("tools/chestpiece_gem", DefaultVertexFormat.ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));
    public static final ShieldShader CHESTPIECE_SHIELD_SHADER = new ShieldShader("tools/chestpiece_shield", DefaultVertexFormat.ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));

    public static final com.brandon3055.draconicevolution.client.shader.EnergyCrystalShader ENERGY_CRYSTAL =
            new com.brandon3055.draconicevolution.client.shader.EnergyCrystalShader()
                    .onShaderApplied(shader -> shader.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));

    public static void init(IEventBus modBus) {
        LOCK.lock();
        modBus.addListener((RegisterRenderPipelinesEvent event) -> {
            for (var shader : new com.brandon3055.brandonscore.client.shader.BCShader<?>[]{TOOL_BASE_SHADER, TOOL_GEM_SHADER, TOOL_TRACE_SHADER, TOOL_BLADE_SHADER,
                    BOW_STRING_SHADER, CHESTPIECE_GEM_SHADER, CHESTPIECE_SHIELD_SHADER, ENERGY_CRYSTAL, TOOL_CHAOS_SHADER}) {
                event.registerPipeline(shader.pipeline(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS));
                event.registerPipeline(shader.pipeline(com.mojang.blaze3d.vertex.VertexFormat.Mode.TRIANGLES));
            }
        });
    }

    // port-26.1.2:Identifier/time-хелперы ниже не используются до этапа 3
    static Identifier coreId(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    static float shieldTime() {
        return (TimeKeeper.getClientTick() + com.brandon3055.brandonscore.client.render.RenderUtils.partialTick()) / 20F;
    }
}
