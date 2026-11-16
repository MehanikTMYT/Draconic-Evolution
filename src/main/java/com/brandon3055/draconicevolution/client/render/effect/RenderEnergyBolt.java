package com.brandon3055.draconicevolution.client.render.effect;

import com.brandon3055.brandonscore.lib.Vec3D;
import com.brandon3055.brandonscore.utils.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import com.mojang.math.Axis;
import java.util.Random;

/** Created by brandon3055 on 22/06/2016.
 * Original DE segmented bolt and corona, with an independent triangle strip for each segment. */
public class RenderEnergyBolt {
    private static final com.mojang.blaze3d.pipeline.RenderPipeline PIPELINE = RenderPipelines.LIGHTNING.toBuilder()
            .withLocation(Identifier.fromNamespaceAndPath("draconicevolution", "pipeline/celestial_bolt"))
            .withVertexFormat(com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR, com.mojang.blaze3d.vertex.VertexFormat.Mode.TRIANGLE_STRIP).build();
    public static void register(net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent event) { event.registerPipeline(PIPELINE); }

    public static void renderBoltBetween(PoseStack pose, MultiBufferSource buffers, Vec3D point1, Vec3D point2,
                                        double scale, double maxDeflection, int maxSegments, long boltSeed, boolean corona) {
        Random random = new Random(boltSeed);


        double distance = Utils.getDistance(point1, point2);
        Vec3D dirVec = Vec3D.getDirectionVec(point1, point2);
        Vec3D invDir = new Vec3D(1D, 1D, 1D).subtract(dirVec);

        //region Draw Main Bolt

        Vec3D[] vectors = new Vec3D[maxSegments / 2 + random.nextInt(maxSegments / 2)];
        vectors[0] = point1;
        vectors[vectors.length - 1] = point2;

        for (int i = 1; i < vectors.length - 1; i++) {
            double pos = (i / (double) vectors.length) * distance;

            Vec3D point = point1.copy();
            point.add(dirVec.copy().multiply(pos, pos, pos));

            double randX = (-0.5 + random.nextDouble()) * maxDeflection * invDir.x;
            double randY = (-0.5 + random.nextDouble()) * maxDeflection * invDir.y;
            double randZ = (-0.5 + random.nextDouble()) * maxDeflection * invDir.z;

            point.add(randX, randY, randZ);

            vectors[i] = point;
        }

        double rScale = scale * (0.5 + (random.nextDouble() * 0.5));
        for (int i = 1; i < vectors.length; i++) {
            drawBoltSegment(pose, buffers, vectors[i - 1], vectors[i], (float) rScale);
        }

        //endregion

        //region Draw Corona

        if (corona) {

            Vec3D[][] coronaVecs = new Vec3D[2 + random.nextInt(4)][2 + random.nextInt(3)];

            for (int i = 0; i < coronaVecs.length; i++) {
                coronaVecs[i][0] = point1;
                double d = distance / (2 + (random.nextDouble() * 2));

                for (int v = 1; v < coronaVecs[i].length; v++) {
                    double pos = (v / (double) coronaVecs[i].length) * d;

                    Vec3D point = point1.copy();
                    point.add(dirVec.copy().multiply(pos, pos, pos));

                    double randX = (-0.5 + random.nextDouble()) * maxDeflection * invDir.x * 0.5;
                    double randY = (-0.5 + random.nextDouble()) * maxDeflection * invDir.y * 0.5;
                    double randZ = (-0.5 + random.nextDouble()) * maxDeflection * invDir.z * 0.5;

                    point.add(randX, randY, randZ);

                    coronaVecs[i][v] = point;
                }
            }

            for (int i = 0; i < coronaVecs.length; i++) {
                float f = 0.1F + (random.nextFloat() * 0.5F);
                for (int v = 1; v < coronaVecs[i].length; v++) {
                    drawBoltSegment(pose, buffers, coronaVecs[i][v - 1], coronaVecs[i][v], (float) scale * f);
                }
            }
        }

        //endregion

    }
    private static void drawBoltSegment(PoseStack pose, MultiBufferSource buffers, Vec3D p1, Vec3D p2, float scale) {
        pose.pushPose();
        pose.translate(p1.x, p1.y, p1.z);
        double dist = Utils.getDistance(p1, p2);
        float xd = (float) (p1.x - p2.x), yd = (float) (p1.y - p2.y), zd = (float) (p1.z - p2.z);
        float yaw = (float) Math.toDegrees(Math.atan2(xd, zd));
        float pitch = (float) Math.toDegrees(Math.atan2(yd, Math.sqrt(xd * xd + zd * zd)));
        pose.mulPose(Axis.XP.rotationDegrees(90));
        pose.mulPose(Axis.ZN.rotationDegrees(180 + yaw));
        pose.mulPose(Axis.XP.rotationDegrees(pitch));
        var buffer = buffers.getBuffer(RenderType.create("de_celestial_bolt_segment", RenderSetup.builder(PIPELINE).createRenderSetup()));
        for (int i = 0; i <= 9; i++) {
            float f = (i + 1F) / 9F;
            float x = (float) Math.sin((i % 3) * Math.PI * 2 / 3) * f * scale;
            float z = (float) Math.cos((i % 3) * Math.PI * 2 / 3) * f * scale;
            buffer.addVertex(pose.last(), x, (float) dist, z).setColor(0.35F, 0.65F, 0.9F, 0.3F);
            buffer.addVertex(pose.last(), x, 0, z).setColor(0.35F, 0.65F, 0.9F, 0.3F);
        }
        pose.popPose();
    }
}
