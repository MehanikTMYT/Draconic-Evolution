package com.brandon3055.draconicevolution.client.render.effect;

import codechicken.lib.render.CCRenderState;
import com.brandon3055.brandonscore.lib.Vec3D;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.world.level.Level;

import java.util.Random;

/**
 * Created by brandon3055 on 23/06/2016.
 */
public class EffectTrackerCelestialManipulator {
    public static double interpPosX = 0;
    public static double interpPosY = 0;
    public static double interpPosZ = 0;
    private Random rand = new Random();
    public Vec3D effectFocus;
    public Vec3D linkPos = null;
    private int renderBolt = 0;
    private float rotation;
    private float rotationSpeed = 2;
    private float aRandomFloat = 0;
    public boolean positionLocked = false;
    public Vec3D startPos;
    public Vec3D pos;
    public Vec3D prevPos = new Vec3D();
    public Vec3D circlePosition = new Vec3D();
    private Level world;
    private long boltSeed = 0;
    public boolean renderBolts = true;

    public float alpha = 0F;
    public float scale = 1F;
    public float red = 0F;
    public float green = 1F;
    public float blue = 1F;

    public EffectTrackerCelestialManipulator(Level world, Vec3D pos, Vec3D effectFocus) {
        this.world = world;
        this.effectFocus = effectFocus;
        this.rotation = rand.nextInt(1000);
        this.aRandomFloat = rand.nextFloat();
        this.pos = pos.copy();
        this.startPos = pos.copy();
        this.prevPos.set(pos);
        red = 0.1F;
        green = 0.1F;
        alpha = 1F;
    }

    public void onUpdate() {
        prevPos.set(pos);

        if (renderBolt > 0) {
            renderBolt--;
        }

        renderBolt = 1;
        boltSeed = rand.nextLong();
        //TODO Particles
//        BCEffectHandler.spawnFXDirect(DEParticles.DE_SHEET, new SubParticle(world, effectFocus), 128, true);

        rotationSpeed = -1F;
        rotation += rotationSpeed;
    }

    /** Called during block entity extraction; output contains no live tracker references. */
    public void extractEffect(com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.MultiBufferSource buffers,
                              float partialTicks, net.minecraft.core.BlockPos origin, boolean weather) {
        double x = prevPos.x + (pos.x - prevPos.x) * partialTicks;
        double y = prevPos.y + (pos.y - prevPos.y) * partialTicks;
        double z = prevPos.z + (pos.z - prevPos.z) * partialTicks;
        pose.pushPose();
        pose.translate(x - origin.getX(), y - origin.getY(), z - origin.getZ());
        pose.pushPose();
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation + partialTicks * rotationSpeed));
        pose.scale(0.15F * scale, 0.15F * scale, 0.15F * scale);
        var consumer = buffers.getBuffer(weather ? com.brandon3055.draconicevolution.client.render.tile.TileRenderPipelines.CELESTIAL_WEATHER
                : com.brandon3055.draconicevolution.client.render.tile.TileRenderPipelines.CELESTIAL_TIME);
        for (var vertex : codechicken.lib.render.CCModelLibrary.icosahedron7.verts) {
            consumer.addVertex(pose.last(), (float) vertex.vec.x, (float) vertex.vec.y, (float) vertex.vec.z)
                    .setUv((float) vertex.uv.u, (float) vertex.uv.v).setColor(red, green, blue, alpha);
        }
        pose.popPose();
        int segments = Math.max(4, (int) (8 * scale));
        if (renderBolt > 0 && scale > 0 && renderBolts) {
            RenderEnergyBolt.renderBoltBetween(pose, buffers, new Vec3D(), effectFocus.copy().subtract(x, y, z),
                    0.05 * scale, scale * 0.5, segments, boltSeed, false);
        }
        if (linkPos != null && scale > 0) {
            RenderEnergyBolt.renderBoltBetween(pose, buffers, new Vec3D(), linkPos.copy().subtract(x, y, z),
                    0.05 * scale, scale * 0.5, segments, boltSeed, false);
        }
        pose.popPose();
    }
}
