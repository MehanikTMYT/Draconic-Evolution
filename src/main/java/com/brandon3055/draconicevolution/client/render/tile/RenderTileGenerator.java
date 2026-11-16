package com.brandon3055.draconicevolution.client.render.tile;

import codechicken.lib.math.MathHelper;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.lighting.LightModel;
import codechicken.lib.render.model.OBJParser;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.blocks.machines.Generator;
import com.brandon3055.draconicevolution.blocks.tileentity.TileGenerator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.Map;

/**
 * Created by brandon3055 on 1/3/20.
 */
public class RenderTileGenerator implements BlockEntityRenderer<TileGenerator, RenderTileGenerator.State> {

    private static final RenderType MODEL_TYPE = RenderTypes.entitySolid(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS);
    private static final Identifier GEN_TEXTURE = Identifier.fromNamespaceAndPath(DraconicEvolution.MODID, "block/generator/generator_2");
    private final CCModel fanModel;
    private final SpriteGetter sprites;

    public RenderTileGenerator(BlockEntityRendererProvider.Context context) {
        sprites = context.sprites();
        Map<String, CCModel> map = new OBJParser(Identifier.fromNamespaceAndPath(DraconicEvolution.MODID, "models/block/generator/generator_fan.obj")).quads().ignoreMtl().parse();
        fanModel = CCModel.combine(map.values()).backfacedCopy();
    }

    public static class State extends BlockEntityRenderState {
        float facing;
        double rotation;
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(TileGenerator tile, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.facing = tile.getBlockState().getValue(Generator.FACING).getOpposite().toYRot();
        state.rotation = tile.rotation + tile.rotationSpeed * partialTicks;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        IconTransformation icon = new IconTransformation(sprites.get(new SpriteId(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS, GEN_TEXTURE)));
        int light = state.lightCoords;
        float facing = state.facing;
        double rotation = state.rotation;
        collector.submitCustomGeometry(poseStack, MODEL_TYPE, (pose, vertices) -> {
            CCRenderState ccrs = CCRenderState.instance();
            ccrs.reset();
            ccrs.bind(vertices, MODEL_TYPE);
            ccrs.brightness = light;
            ccrs.overlay = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;
            Matrix4 mat = new Matrix4(pose);
            mat.translate(Vector3.CENTER);
            mat.apply(new Rotation(facing * -MathHelper.torad, 0, 1, 0));
            mat.apply(new Scale(0.0625));
            mat.apply(new Rotation(rotation, 1, 0, 0).at(new Vector3(0, -1.5, -4.5)));
            com.brandon3055.draconicevolution.client.render.DEModelEmission.render(fanModel, ccrs, LightModel.standardLightModel, icon, mat);
        });
    }
}
