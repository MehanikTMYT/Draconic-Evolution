package com.brandon3055.draconicevolution.client.render.item;

import com.brandon3055.brandonscore.blocks.BlockBCore;
import com.brandon3055.draconicevolution.client.render.tile.DraconiumChestTileRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Created by brandon3055 on 18/04/2017.
 */
public class RenderItemDraconiumChest implements DEItemRenderer {
    private final DraconiumChestTileRenderer renderer = new DraconiumChestTileRenderer(null);
    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        int colour = 0x640096;
        CompoundTag tag = com.brandon3055.brandonscore.utils.ItemNBTHelper.getCompound(stack).getCompoundOrEmpty(BlockBCore.BC_TILE_DATA_TAG);
        colour = tag.getCompoundOrEmpty("bc_managed_data").getIntOr("colour", colour);
        renderer.renderChest(pose, buffers, 180, 0, light, overlay, 0xFF000000 | colour);
    }
    @Override public void getExtents(java.util.function.Consumer<org.joml.Vector3fc> output) {
        output.accept(new org.joml.Vector3f(0, 0, 0));
        output.accept(new org.joml.Vector3f(1, 1, 1));
    }
}
