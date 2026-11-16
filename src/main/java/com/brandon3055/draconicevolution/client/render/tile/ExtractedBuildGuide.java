package com.brandon3055.draconicevolution.client.render.tile;

import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.brandonscore.multiblock.MultiBlockDefinition;
import com.brandon3055.brandonscore.multiblock.MultiBlockPart;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Frozen build-guide alternatives and invalid positions; no level/tile/part survives extraction. */
final class ExtractedBuildGuide {
    private static final BlockDisplayContext DISPLAY = BlockDisplayContext.create();
    private static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(net.minecraft.resources.Identifier.fromNamespaceAndPath("draconicevolution", "pipeline/build_guide_invalid"))
            .withDepthStencilState(Optional.empty()).build();
    private static final RenderType INVALID = RenderType.create("de_build_guide_invalid", RenderSetup.builder(PIPELINE).createRenderSetup());
    private record Ghost(BlockPos pos, BlockModelRenderState model) {}
    private final List<Ghost> ghosts;
    private final List<BlockPos> invalid;
    private final int overlay;
    private final float radius;
    private ExtractedBuildGuide(List<Ghost> ghosts, List<BlockPos> invalid, int overlay, float radius) {
        this.ghosts = List.copyOf(ghosts); this.invalid = List.copyOf(invalid); this.overlay = overlay; this.radius = radius;
    }
    static ExtractedBuildGuide extract(Level level, BlockPos origin, MultiBlockDefinition definition, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        int tick = TimeKeeper.getClientTick();
        int time = tick % 80;
        int anim = 5 + (time >= 70 ? (int)(Math.sin(((time - 70) / 10F) * Math.PI) * 5) : 0);
        List<Ghost> ghosts = new ArrayList<>();
        List<BlockPos> invalid = new ArrayList<>();
        Map<MultiBlockPart, BlockState> alternatives = new IdentityHashMap<>();
        definition.getBlocks().forEach((pos, part) -> {
            if (part.validBlocks().isEmpty()) return;
            BlockPos world = origin.offset(pos);
            if (level.isEmptyBlock(world)) {
                if (mc.player != null && world.distToCenterSqr(mc.player.getEyePosition()) < 16) return;
                BlockState block = alternatives.computeIfAbsent(part, p -> List.copyOf(p.validBlocks()).get((tick / 40) % p.validBlocks().size()).defaultBlockState());
                if (!block.isAir()) {
                    BlockModelRenderState model = new BlockModelRenderState();
                    mc.getBlockModelResolver().update(model, block, DISPLAY);
                    ghosts.add(new Ghost(pos.immutable(), model));
                }
            } else if (!part.isMatch(level, world)) invalid.add(pos.immutable());
        });
        return new ExtractedBuildGuide(ghosts, invalid, OverlayTexture.pack(anim, 10),
                (float)(Math.max(0.1, Math.abs(Math.sin((tick + partialTicks) / 25D * Math.PI))) / 2));
    }
    void submit(PoseStack poseStack, SubmitNodeCollector collector) {
        for (Ghost ghost : ghosts) {
            poseStack.pushPose();
            poseStack.translate(ghost.pos.getX() + 0.1, ghost.pos.getY() + 0.1, ghost.pos.getZ() + 0.1);
            poseStack.scale(0.8F, 0.8F, 0.8F);
            ghost.model.submitMultiLayer(poseStack, collector, 200, overlay, 0);
            poseStack.popPose();
        }
        float radius = this.radius;
        for (BlockPos pos : invalid) {
            poseStack.pushPose();
            poseStack.translate(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            collector.order(1).submitCustomGeometry(poseStack, INVALID, (pose, buffer) -> {
                net.minecraft.world.phys.shapes.Shapes.create(-radius, -radius, -radius, radius, radius, radius).forAllEdges((x1,y1,z1,x2,y2,z2) -> {
                    org.joml.Vector3f normal = new org.joml.Vector3f((float)(x2-x1), (float)(y2-y1), (float)(z2-z1)).normalize();
                    buffer.addVertex(pose, (float)x1, (float)y1, (float)z1).setColor(1F,0F,0F,1F).setNormal(pose, normal).setLineWidth(4F);
                    buffer.addVertex(pose, (float)x2, (float)y2, (float)z2).setColor(1F,0F,0F,1F).setNormal(pose, normal).setLineWidth(4F);
                });
            });
            poseStack.popPose();
        }
    }
    static void register(net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent event) { event.registerPipeline(PIPELINE); }
}
