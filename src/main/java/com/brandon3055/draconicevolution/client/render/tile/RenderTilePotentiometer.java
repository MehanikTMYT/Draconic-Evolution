package com.brandon3055.draconicevolution.client.render.tile;

import codechicken.lib.math.MathHelper;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.lighting.LightModel;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.*;
import codechicken.lib.vec.uv.IconTransformation;
import codechicken.lib.vec.uv.UV;
import codechicken.lib.vec.uv.UVTransformation;
import com.brandon3055.draconicevolution.blocks.Potentiometer;
import com.brandon3055.draconicevolution.blocks.tileentity.TilePotentiometer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class RenderTilePotentiometer implements BlockEntityRenderer<TilePotentiometer, RenderTilePotentiometer.State> {

    private static CCModel model;
    private final net.minecraft.client.resources.model.sprite.SpriteGetter sprites;


    public RenderTilePotentiometer(BlockEntityRendererProvider.Context context) {
        sprites = context.sprites();

        double pxl = 1D / 16D;
        double ls = pxl * 2.5;      //Left Side
        double rs = pxl * 1.5;      //Right Side
        double fe = pxl * 7;        //Front Edge
        double be = pxl * 0;        //Back Edge
        double bh = pxl * 3;        //Back Height
        double fh = pxl * 0.5;      //Front Height
        int i = 24;

        model = CCModel.quadModel(40);
        model.generateBlock(0, new Cuboid6(0, 0, 0, pxl * 4, pxl, pxl * 4));

        //Top
        model.verts[i++] = new Vertex5(new Vector3(ls, bh, be), new UV(ls, be));
        model.verts[i++] = new Vertex5(new Vector3(ls, fh, fe), new UV(ls, fe));
        model.verts[i++] = new Vertex5(new Vector3(rs, fh, fe), new UV(rs, fe));
        model.verts[i++] = new Vertex5(new Vector3(rs, bh, be), new UV(rs, be));

        //Left
        model.verts[i++] = new Vertex5(new Vector3(ls, 0, fe), new UV(0, fe));
        model.verts[i++] = new Vertex5(new Vector3(ls, 0, be), new UV(0, be));
        model.verts[i++] = new Vertex5(new Vector3(ls, bh, be), new UV(bh, be));
        model.verts[i++] = new Vertex5(new Vector3(ls, fh, fe), new UV(fh, fe));

        //Right
        model.verts[i++] = new Vertex5(new Vector3(rs, 0, fe), new UV(0, fe));
        model.verts[i++] = new Vertex5(new Vector3(rs, 0, be), new UV(0, be));
        model.verts[i++] = new Vertex5(new Vector3(rs, bh, be), new UV(bh, be));
        model.verts[i++] = new Vertex5(new Vector3(rs, fh, fe), new UV(fh, fe));

        //Back
        model.verts[i++] = new Vertex5(new Vector3(ls, pxl, be), new UV(ls, 0));
        model.verts[i++] = new Vertex5(new Vector3(rs, pxl, be), new UV(rs, 0));
        model.verts[i++] = new Vertex5(new Vector3(rs, bh, be), new UV(rs, bh));
        model.verts[i++] = new Vertex5(new Vector3(ls, bh, be), new UV(ls, bh));

        model.computeNormals();
        model.computeLightCoords();
        model = model.twoFacedCopy();
    }

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.UP;
        int power;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TilePotentiometer tile, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.facing = tile.getBlockState().getValue(Potentiometer.FACING);
        state.power = tile.power.get();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        TextureAtlasSprite stoneTex = sprites.get(new net.minecraft.client.resources.model.sprite.SpriteId(TextureAtlas.LOCATION_BLOCKS,
                net.minecraft.resources.Identifier.withDefaultNamespace("block/oak_planks")));
        UVTransformation iconTransform = new IconTransformation(stoneTex);
        RenderType type = RenderTypes.entitySolid(TextureAtlas.LOCATION_BLOCKS);
        int light = state.lightCoords;
        Direction facing = state.facing;
        int power = state.power;
        collector.submitCustomGeometry(poseStack, type, (pose, vertices) -> {
            CCRenderState renderState = CCRenderState.instance();
            renderState.reset();
            renderState.bind(vertices, type);
            renderState.brightness = light;
            renderState.overlay = OverlayTexture.NO_OVERLAY;
            double pxl = 1D / 16D;
            Matrix4 mat = new Matrix4(pose);
            mat.apply(Rotation.sideOrientation(facing.getOpposite().get3DDataValue(), 0).at(Vector3.CENTER));
            mat.apply(new Translation(6 * pxl, pxl, 6 * pxl));
            mat.apply(new Rotation(power * 22.5D * -MathHelper.torad, 0, 1, 0).at(new Vector3(pxl * 2, 0, pxl * 2)));
            model.render(renderState, LightModel.standardLightModel, iconTransform, mat);
        });
    }
}
