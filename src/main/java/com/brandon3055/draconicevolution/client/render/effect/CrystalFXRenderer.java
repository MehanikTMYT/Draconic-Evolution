package com.brandon3055.draconicevolution.client.render.effect;

import com.brandon3055.brandonscore.client.shader.ShaderDraw;
import com.brandon3055.draconicevolution.client.shader.DEEffectShader;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;

import java.util.*;

/** Persistent crystal effects are independent of ParticleEngine's groups and particle limit.
 * Live tile/camera reads happen during extraction; submission only sees immutable vertices. */
public final class CrystalFXRenderer {
    private CrystalFXRenderer() {}
    public static final Identifier HIGHLIGHT = id("textures/particle/energy_beam_highlight.png");
    private static final DEEffectShader SHADER = new DEEffectShader("crystal_fx", "crystal_fx",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, true, false, false, false, true, false)
            .blend(BlendFunction.LIGHTNING);
    private static final ContextKey<List<Draw>> DRAWS = new ContextKey<>(id("crystal_effects"));
    private static final Set<CrystalFXBase<?>> ACTIVE = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Map<Identifier, RenderType> TYPES = new HashMap<>();

    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath("draconicevolution", path); }

    public static void init(IEventBus modBus) {
        modBus.addListener((RegisterRenderPipelinesEvent event) -> event.registerPipeline(SHADER.pipeline(VertexFormat.Mode.QUADS)));
        NeoForge.EVENT_BUS.addListener(CrystalFXRenderer::tick);
        NeoForge.EVENT_BUS.addListener(CrystalFXRenderer::extract);
        NeoForge.EVENT_BUS.addListener(CrystalFXRenderer::submit);
    }

    public static void add(CrystalFXBase<?> effect) { ACTIVE.add(effect); }

    private static void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        var iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            var effect = iterator.next();
            if (mc.level == null || !effect.isAlive() || !effect.belongsTo(mc.level)) {
                effect.remove();
                iterator.remove();
            } else if (!mc.isPaused()) {
                effect.tick();
            }
        }
    }

    private static void extract(ExtractLevelRenderStateEvent event) {
        List<Draw> draws = new ArrayList<>();
        float partialTick = event.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        for (var effect : ACTIVE) {
            if (!effect.isAlive() || !effect.belongsTo(event.getLevel())) continue;
            var vertices = new RecordingConsumer();
            effect.render(vertices, event.getCamera(), partialTick);
            var frozen = vertices.finish();
            if (frozen.isEmpty()) continue;
            RenderType type = TYPES.computeIfAbsent(effect.texture(), texture -> SHADER.renderType(
                    "draconicevolution/crystal_effect", texture, VertexFormat.Mode.QUADS));
            SHADER.getModelMatUniform().glUniformMatrix4f(new Matrix4f());
            draws.add(new Draw(ShaderDraw.capture(type), frozen));
        }
        event.getRenderState().setRenderData(DRAWS, List.copyOf(draws));
    }

    private static void submit(SubmitCustomGeometryEvent event) {
        List<Draw> draws = event.getLevelRenderState().getRenderData(DRAWS);
        if (draws == null) return;
        for (var draw : draws) {
            event.getSubmitNodeCollector().submitCustomGeometry(event.getPoseStack(), draw.type(), (pose, buffer) -> {
                for (var v : draw.vertices()) {
                    buffer.addVertex(pose, v.x(), v.y(), v.z()).setColor(v.color()).setUv(v.u(), v.v()).setUv2(v.lightU(), v.lightV());
                }
            });
        }
    }

    private record Draw(RenderType type, List<Vertex> vertices) {}
    private record Vertex(float x, float y, float z, int color, float u, float v, int lightU, int lightV) {}

    /** Records exactly the POSITION_COLOR_TEX_LIGHTMAP fields used by all five crystal effects. */
    private static final class RecordingConsumer implements VertexConsumer {
        private final List<Vertex> vertices = new ArrayList<>();
        private boolean pending;
        private float x, y, z, u, v;
        private int color = -1, lightU = 240, lightV = 240;
        private void flush() {
            if (pending) vertices.add(new Vertex(x, y, z, color, u, v, lightU, lightV));
        }
        List<Vertex> finish() {
            flush();
            pending = false;
            return List.copyOf(vertices);
        }
        @Override public VertexConsumer addVertex(float x, float y, float z) {
            flush(); this.x = x; this.y = y; this.z = z; pending = true; return this;
        }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) {
            return setColor((a << 24) | (r << 16) | (g << 8) | b);
        }
        @Override public VertexConsumer setColor(int color) { this.color = color; return this; }
        @Override public VertexConsumer setUv(float u, float v) { this.u = u; this.v = v; return this; }
        @Override public VertexConsumer setUv2(int u, int v) { lightU = u; lightV = v; return this; }
        @Override public VertexConsumer setUv1(int u, int v) { throw new UnsupportedOperationException("Crystal format has no overlay"); }
        @Override public VertexConsumer setNormal(float x, float y, float z) { throw new UnsupportedOperationException("Crystal format has no normals"); }
        @Override public VertexConsumer setLineWidth(float width) { throw new UnsupportedOperationException("Crystal format has no lines"); }
    }
}
