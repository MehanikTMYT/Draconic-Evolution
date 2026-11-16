package com.brandon3055.draconicevolution.client.render;

import com.brandon3055.brandonscore.client.render.ExtractedModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import java.util.ArrayList;

/** Frozen surfaces keep their texture alpha masks in the entity outline target. */
public final class DEExtractedGeometry {
    private DEExtractedGeometry() {}
    public static void submit(ExtractedModel geometry, PoseStack pose, SubmitNodeCollector collector, int outlineColor) {
        geometry.submit(pose, collector);
        if (outlineColor == 0) return;
        var outlines = new ArrayList<ExtractedModel.Batch>();
        for (var batch : geometry.batches()) {
            batch.type().outline().ifPresent(type -> outlines.add(new ExtractedModel.Batch(type,
                    batch.vertices().stream().map(v -> new ExtractedModel.Vertex(v.x(), v.y(), v.z(), outlineColor,
                            v.u(), v.v(), v.overlay(), v.light(), v.nx(), v.ny(), v.nz())).toList())));
        }
        new ExtractedModel(outlines).submit(pose, collector);
    }
}
