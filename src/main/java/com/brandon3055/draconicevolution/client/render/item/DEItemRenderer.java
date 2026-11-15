package com.brandon3055.draconicevolution.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import java.util.function.Consumer;

/** DE geometry emitter. The special-model adapter captures separate immutable material batches. */
public interface DEItemRenderer {
    void renderItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay);
    void getExtents(Consumer<Vector3fc> output);
}
