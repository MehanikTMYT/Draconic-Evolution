package com.brandon3055.draconicevolution.client.shader;

import com.brandon3055.brandonscore.client.render.ExtractedModel;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.joml.Matrix3x2f;

/** GUI geometry and parameters are frozen while the tile is available. */
public record ReactorGuiState(int x0, int y0, int x1, int y1, ExtractedModel model,
        Matrix3x2f pose, ScreenRectangle scissorArea) implements PictureInPictureRenderState {
    @Override public float scale() { return 1; }
    @Override public ScreenRectangle bounds() {
        ScreenRectangle bounds = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
        return scissorArea == null ? bounds : scissorArea.intersection(bounds);
    }
}
