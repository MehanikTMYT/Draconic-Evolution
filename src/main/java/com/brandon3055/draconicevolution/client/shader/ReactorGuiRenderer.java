package com.brandon3055.draconicevolution.client.shader;

import com.brandon3055.brandonscore.client.shader.ShaderDraw;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

/** PIP owns the target and executes GPU work in the GUI drawing phase. */
public final class ReactorGuiRenderer extends PictureInPictureRenderer<ReactorGuiState> {
    public ReactorGuiRenderer(MultiBufferSource.BufferSource buffers) { super(buffers); }
    @Override public Class<ReactorGuiState> getRenderStateClass() { return ReactorGuiState.class; }
    @Override protected String getTextureLabel() { return "Draconic reactor preview"; }
    @Override protected float getTranslateY(int height, int scale) { return height / 2F; }
    @Override protected void renderToTexture(ReactorGuiState state, PoseStack pose) {
        for (var batch : state.model().batches()) {
            var type = ShaderDraw.withPose(batch.type(), pose.last().pose());
            var buffer = bufferSource.getBuffer(type);
            for (var vertex : batch.vertices()) {
                buffer.addVertex(vertex.x(), vertex.y(), vertex.z()).setUv(vertex.u(), vertex.v());
            }
        }
    }
}
