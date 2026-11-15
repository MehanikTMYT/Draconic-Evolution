package com.brandon3055.draconicevolution.client.render.item;

import codechicken.lib.math.MathHelper;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.brandonscore.client.shader.BCShader;
import com.brandon3055.brandonscore.client.shader.BCShaders;
import com.brandon3055.brandonscore.client.shader.ChaosEntityShader;
import com.brandon3055.draconicevolution.client.DEShaders;
import com.brandon3055.draconicevolution.client.shader.ToolShader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static com.brandon3055.draconicevolution.DraconicEvolution.MODID;

/**
 * Created by brandon3055 on 22/5/20.
 */
public abstract class ToolRenderBase implements DEItemRenderer {

    protected final TechLevel techLevel;
    protected final String tool;
    private final java.util.Map<RenderType, Identifier> materialTextures = new java.util.IdentityHashMap<>();
    private com.brandon3055.brandonscore.client.render.ExtractedModel.Capture surfaces;
    public com.brandon3055.brandonscore.client.render.ExtractedModel surfaceGeometry() { return surfaces.finish(); }
    protected net.minecraft.world.entity.LivingEntity entity;
    protected net.minecraft.client.multiplayer.ClientLevel world;
    protected float partialTicks;
    private int itemLight, itemOverlay;

    public void prepareExtraction(net.minecraft.world.entity.ItemOwner owner, net.minecraft.client.multiplayer.ClientLevel level) {
        entity = owner instanceof net.minecraft.world.entity.LivingEntity living ? living : null;
        world = level != null ? level : entity != null && entity.level() instanceof net.minecraft.client.multiplayer.ClientLevel client ? client : null;
        partialTicks = net.minecraft.client.Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }
    public void finishExtraction() { entity = null; world = null; }


    public ToolRenderBase(TechLevel techLevel, String tool) {
        this.techLevel = techLevel;
        this.tool = tool;
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack mStack, MultiBufferSource getter, int packedLight, int packedOverlay) {
        itemLight = packedLight; itemOverlay = packedOverlay;
        extents.clear();
        surfaces = new com.brandon3055.brandonscore.client.render.ExtractedModel.Capture();
        Matrix4 mat = new Matrix4(mStack);
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;

        DEShaders.TOOL_BASE_SHADER.getUv1OverrideUniform().glUniform2i(packedOverlay & 0xFFFF, (packedOverlay >> 16) & 0xFFFF);
        DEShaders.TOOL_BASE_SHADER.getUv2OverrideUniform().glUniform2i(packedLight & 0xFFFF, (packedLight >> 16) & 0xFFFF);

        renderTool(ccrs, stack, transformType, mat, getter, transformType == ItemDisplayContext.GUI);
    }

    public abstract void renderTool(CCRenderState ccrs, ItemStack stack, ItemDisplayContext transform, Matrix4 mat, MultiBufferSource buffers, boolean gui);

    public void transform(Matrix4 mat, double x, double y, double z, double scale) {
        mat.translate(x, y, z);
        mat.rotate(MathHelper.torad * 90, Vector3.Y_NEG);
        mat.rotate(MathHelper.torad * 45, Vector3.X_POS);
        mat.scale(scale);
    }

    // Display transforms now live in the native item base models.
    public boolean usesBlockLight() { return false; }

    protected final java.util.List<org.joml.Vector3fc> extents = new java.util.ArrayList<>();
    @Override
    public void getExtents(java.util.function.Consumer<org.joml.Vector3fc> output) {
        extents.forEach(output);
    }

    protected static float[][] baseColours = {
            { 0.0F, 0.5F, 0.8F, 1F },
            { 0.55F, 0.0F, 0.65F, 1F },
            { 0.8F, 0.5F, 0.1F, 1F },
            { 0.75F, 0.05F, 0.05F, 0.2F }
    };

    public static void glUniformBaseColor(BCShader<?> shader, TechLevel techLevel, float pulse) {
        if (!(shader instanceof ToolShader toolShader) || !toolShader.hasBaseColorUniform()) return;
        float[] baseColour = baseColours[techLevel.index];
        float r = baseColour[0];
        float g = baseColour[1];
        float b = baseColour[2];
        float a = baseColour[3];
        switch (techLevel) {
            case DRACONIUM, WYVERN, DRACONIC -> a *= 1F + pulse;
            case CHAOTIC -> {
                r += pulse * 0.2F;
                g += pulse * 0.2F;
                b += pulse * 0.2F;
            }
        }
        toolShader.getBaseColorUniform().glUniform4f(r, g, b, a);
    }

    protected ToolPart basePart(CCModel model) {
        return new BaseToolPart(model, type(DEShaders.TOOL_BASE_SHADER, toolTexture()), DEShaders.TOOL_BASE_SHADER);
    }

    protected ToolPart materialPart(CCModel model) {
        if (techLevel != TechLevel.CHAOTIC) return basePart(model);
        return new ChaoticToolPart(model, type(DEShaders.TOOL_CHAOS_SHADER,
                Identifier.fromNamespaceAndPath(MODID, "textures/item/equipment/chaos_shader.png")), DEShaders.TOOL_CHAOS_SHADER);
    }

    protected Identifier toolTexture() {
        return Identifier.fromNamespaceAndPath(MODID, "textures/item/equipment/" + techLevel.name().toLowerCase(Locale.ROOT) + "_" + tool + ".png");
    }

    protected Identifier fallbackTexture() {
        return Identifier.fromNamespaceAndPath(MODID, "textures/item/equipment/shader_fallback_" + techLevel.name().toLowerCase(Locale.ROOT) + ".png");
    }

    protected RenderType type(BCShader<?> shader, Identifier texture) {
        RenderType type = shader.renderType(MODID + ":" + tool, texture, VertexFormat.Mode.TRIANGLES);
        materialTextures.put(type, texture);
        return type;
    }

    protected ToolPart gemPart(CCModel model) {
        return new SimpleToolPart(model, type(DEShaders.TOOL_GEM_SHADER, fallbackTexture()), DEShaders.TOOL_GEM_SHADER);
    }
    protected ToolPart tracePart(CCModel model) {
        return new SimpleToolPart(model, type(DEShaders.TOOL_TRACE_SHADER, fallbackTexture()), DEShaders.TOOL_TRACE_SHADER);
    }
    protected ToolPart bladePart(CCModel model) {
        return new SimpleToolPart(model, type(DEShaders.TOOL_BLADE_SHADER, fallbackTexture()), DEShaders.TOOL_BLADE_SHADER);
    }

    protected abstract class ToolPart {
        protected final BCShader<?> shader;
        protected final CCModel model;
        protected final RenderType renderType;
        protected ToolPart(CCModel model, RenderType renderType, BCShader<?> shader) {
            this.model = model; this.renderType = renderType; this.shader = shader;
        }
        public final void render(ItemDisplayContext context, MultiBufferSource buffers, Matrix4 mat) {
            render(context, buffers, mat, 1F);
        }
        public abstract void render(ItemDisplayContext context, MultiBufferSource buffers, Matrix4 mat, float pulse);

        protected final void emit(MultiBufferSource buffers, Matrix4 mat, boolean chaos) {
            shader.getModelMatUniform().glUniformMatrix4f(mat);
            // Light/overlay arrive at submission; use the vertex values, not stale global overrides.
            shader.uniform(80).glUniform2i(-1, -1);
            shader.uniform(88).glUniform2i(-1, -1);
            shader.uniform(124).glUniform1i(0);
            shader.uniform(128).glUniform1i(chaos ? 1 : 0);
            shader.uniform(132).glUniform1i(chaos ? 1 : 0);
            CCRenderState state = CCRenderState.instance();
            int light = itemLight, overlay = itemOverlay;
            state.reset(); state.brightness = light; state.overlay = overlay;
            // Capture freezes parameters at bind time, before another part mutates the shader.
            state.bind(renderType, buffers);
            com.brandon3055.draconicevolution.client.render.DEModelEmission.render(model, state);
            // A CPU-transformed copy supports vanilla glint and outline without flattening the visible shader batches.
            state.reset();
            state.bind(ToolSurfaceRenderTypes.outline(materialTextures.get(renderType)), surfaces);
            com.brandon3055.draconicevolution.client.render.DEModelEmission.render(model, state, mat);
            state.brightness = itemLight; state.overlay = itemOverlay;
            for (var vertex : model.verts) {
                var v = vertex.vec.copy(); mat.apply(v);
                extents.add(new org.joml.Vector3f((float)v.x, (float)v.y, (float)v.z));
            }
        }
    }

    protected class BaseToolPart extends ToolPart {
        public BaseToolPart(CCModel model, RenderType type, BCShader<?> shader) { super(model, type, shader); }
        @Override public void render(ItemDisplayContext context, MultiBufferSource buffers, Matrix4 mat, float pulse) {
            shader.uniform(64).glUniform4f(1, 1, 1, 1);
            emit(buffers, mat, false);
        }
    }
    protected class SimpleToolPart extends ToolPart {
        public SimpleToolPart(CCModel model, RenderType type, BCShader<?> shader) { super(model, type, shader); }
        @Override public void render(ItemDisplayContext context, MultiBufferSource buffers, Matrix4 mat, float pulse) {
            glUniformBaseColor(shader, techLevel, pulse);
            emit(buffers, mat, false);
        }
    }
    protected class ChaoticToolPart extends SimpleToolPart {
        private final BCShader<?> chaosShader;
        public ChaoticToolPart(CCModel model, RenderType type, BCShader<?> shader) {
            super(model, type, shader); chaosShader = shader;
        }
        @Override public void render(ItemDisplayContext context, MultiBufferSource buffers, Matrix4 mat, float pulse) {
            chaosShader.uniform(120).glUniform1f(0.7F);
            emit(buffers, mat, true);
        }
    }
}
