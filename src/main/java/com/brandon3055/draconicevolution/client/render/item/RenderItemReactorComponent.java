package com.brandon3055.draconicevolution.client.render.item;

import codechicken.lib.math.MathHelper;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.draconicevolution.client.handler.ClientEventHandler;
import com.brandon3055.draconicevolution.client.render.tile.RenderTileReactorComponent;
import com.brandon3055.draconicevolution.client.render.tile.RenderTileReactorCore;
import com.brandon3055.draconicevolution.init.DEContent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Created by brandon3055 on 21/11/2016.
 */
public class RenderItemReactorComponent implements DEItemRenderer {
    public RenderItemReactorComponent() { RenderTileReactorComponent.reloadModels(); RenderTileReactorCore.reloadModel(); }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack mStack, MultiBufferSource getter, int packedLight, int packedOverlay) {
        Matrix4 mat = new Matrix4(mStack);
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;
        Item item = stack.getItem();

        DeltaTracker delta = Minecraft.getInstance().getDeltaTracker();
        if (item == DEContent.REACTOR_CORE.get().asItem()) {
            mat.translate(0.5, 0.5, 0.5);
            mat.scale(1.5);
            RenderTileReactorCore.renderCore(mat, ccrs, (TimeKeeper.getClientTick() + delta.getGameTimeDeltaPartialTick(false)) / 100F, 0F, 0.F, 0.5F, 0, getter);
        } else if (item == DEContent.REACTOR_STABILIZER.get().asItem()) {
            float coreRotation = (TimeKeeper.getClientTick() + delta.getGameTimeDeltaPartialTick(false)) * 5F;
            mat.translate(0.5, 0, 0.5);
            RenderTileReactorComponent.renderStabilizer(ccrs, mat, getter, coreRotation, 1F, packedLight, packedOverlay);
        } else if (item == DEContent.REACTOR_INJECTOR.get().asItem()) {
            mat.translate(0.5, 0.5, 0);
            mat.rotate(90 * MathHelper.torad, Vector3.X_POS);
            RenderTileReactorComponent.renderInjector(ccrs, mat, getter, 1F, packedLight, packedOverlay);
        } else {
            RenderTileReactorComponent.renderComponent(item, ccrs, mat, getter, packedLight, packedOverlay);
        }
    }

    @Override public void getExtents(java.util.function.Consumer<org.joml.Vector3fc> output) {
        output.accept(new org.joml.Vector3f(-0.5F, -0.5F, -0.5F));
        output.accept(new org.joml.Vector3f(1.5F, 1.5F, 1.5F));
    }
}
