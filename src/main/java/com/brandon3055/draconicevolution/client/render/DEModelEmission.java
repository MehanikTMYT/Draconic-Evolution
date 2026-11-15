package com.brandon3055.draconicevolution.client.render;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.buffer.ISpriteAwareVertexConsumer;
import codechicken.lib.render.pipeline.IVertexOperation;
import com.mojang.blaze3d.vertex.VertexFormatElement;

/** Emits the actual CCL pipeline outputs using MC26's semantic vertex elements. */
public final class DEModelEmission {
    private DEModelEmission() {}

    public static void render(CCModel model, CCRenderState state, IVertexOperation... operations) {
        var consumer = java.util.Objects.requireNonNull(state.r, "Unbound vertex consumer");
        var format = java.util.Objects.requireNonNull(state.fmt, "Unbound vertex format");
        // Fail before emitting a partial primitive if a new format requires additional data.
        for (var element : format.getElements()) {
            if (!element.equals(VertexFormatElement.POSITION) && !element.equals(VertexFormatElement.COLOR)
                    && !element.equals(VertexFormatElement.UV0) && !element.equals(VertexFormatElement.UV1)
                    && !element.equals(VertexFormatElement.UV2) && !element.equals(VertexFormatElement.NORMAL)) {
                throw new IllegalArgumentException("Unsupported DE model element: " + element);
            }
        }
        state.setPipeline(model, 0, model.getVertexCount(), operations);
        for (state.vertexIndex = state.firstVertexIndex; state.vertexIndex < state.lastVertexIndex; state.vertexIndex++) {
            model.prepareVertex(state);
            state.vert.set(model.verts[state.vertexIndex]);
            state.runPipeline();
            if (state.sprite != null && consumer instanceof ISpriteAwareVertexConsumer aware) aware.sprite(state.sprite);
            consumer.addVertex((float) state.vert.vec.x, (float) state.vert.vec.y, (float) state.vert.vec.z);
            // CCL colours are RRGGBBAA; VertexConsumer's packed overload uses ARGB.
            if (format.contains(VertexFormatElement.COLOR)) {
                int rgba = state.colour;
                consumer.setColor(rgba >>> 24, (rgba >>> 16) & 255, (rgba >>> 8) & 255, rgba & 255);
            }
            if (format.contains(VertexFormatElement.UV0)) consumer.setUv((float) state.vert.uv.u, (float) state.vert.uv.v);
            if (format.contains(VertexFormatElement.UV1)) consumer.setOverlay(state.overlay);
            if (format.contains(VertexFormatElement.UV2)) consumer.setLight(state.brightness);
            if (format.contains(VertexFormatElement.NORMAL)) consumer.setNormal((float) state.normal.x, (float) state.normal.y, (float) state.normal.z);
        }
    }
}
