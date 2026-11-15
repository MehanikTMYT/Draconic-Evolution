package com.brandon3055.draconicevolution.client.render.item;

import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.brandonscore.client.render.ExtractedModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import java.util.List;
import java.util.function.Consumer;

/** Extract each context while stack/module state is available; submission only consumes frozen batches. */
public record ModularToolItemModel(ToolRenderBase renderer, ModelRenderProperties properties,
                                   Matrix4fc transformation) implements ItemModel {
    public record Unbaked(Identifier base, String family, int tier) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf("base").forGetter(Unbaked::base),
                Codec.STRING.validate(s -> List.of("axe", "hoe", "pickaxe", "shovel", "sword", "chestpiece", "bow", "staff").contains(s)
                        ? com.mojang.serialization.DataResult.success(s)
                        : com.mojang.serialization.DataResult.error(() -> "Unknown modular family: " + s)).fieldOf("family").forGetter(Unbaked::family),
                Codec.intRange(1, 3).fieldOf("tier").forGetter(Unbaked::tier)
        ).apply(i, Unbaked::new));
        @Override public MapCodec<Unbaked> type() { return MAP_CODEC; }
        @Override public void resolveDependencies(ResolvableModel.Resolver resolver) { resolver.markDependency(base); }
        @Override public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            TechLevel level = TechLevel.values()[tier];
            ToolRenderBase renderer = switch (family) {
                case "axe" -> new RenderModularAxe(level);
                case "hoe" -> new RenderModularHoe(level);
                case "pickaxe" -> new RenderModularPickaxe(level);
                case "shovel" -> new RenderModularShovel(level);
                case "sword" -> new RenderModularSword(level);
                case "bow" -> new RenderModularBow(level);
                case "staff" -> new RenderModularStaff(level);
                case "chestpiece" -> new RenderModularChestpiece(level);
                default -> throw new IllegalArgumentException(family);
            };
            var model = context.blockModelBaker().getModel(base);
            return new ModularToolItemModel(renderer, ModelRenderProperties.fromResolvedModel(
                    context.blockModelBaker(), model, model.getTopTextureSlots()), transformation);
        }
    }
    @Override public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver,
                                 ItemDisplayContext context, ClientLevel level, ItemOwner owner, int seed) {
        var capture = new ExtractedModel.Capture();
        renderer.prepareExtraction(owner, level);
        try {
            renderer.renderItem(stack, context, new PoseStack(), capture, -1, -1);
        } finally {
            renderer.finishExtraction();
        }
        var geometry = capture.finish();
        var bounds = new java.util.ArrayList<Vector3fc>();
        renderer.getExtents(bounds::add);
        Vector3fc[] extents = bounds.toArray(Vector3fc[]::new);
        var layer = output.newLayer();
        layer.setLocalTransform(transformation);
        layer.setExtents(() -> extents);
        properties.applyToLayer(layer, context);
        layer.setupSpecialModel(new FrozenRenderer(geometry, renderer.surfaceGeometry(), List.copyOf(bounds)), geometry);
        if (stack.hasFoil()) layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
        output.appendModelIdentityElement(this);
        output.appendModelIdentityElement(geometry);
        output.setAnimated();
    }
    private record FrozenRenderer(ExtractedModel geometry, ExtractedModel surfaces, List<Vector3fc> bounds) implements SpecialModelRenderer<ExtractedModel> {
        @Override public ExtractedModel extractArgument(ItemStack stack) {
            return geometry;
        }
        @Override public void getExtents(Consumer<Vector3fc> output) {
            bounds.forEach(output);
        }
        @Override public void submit(ExtractedModel model, PoseStack pose, SubmitNodeCollector collector,
                                     int light, int overlay, boolean foil, int outline) {
            if (model == null) return;
            new ExtractedModel(model.batches().stream().map(batch -> new ExtractedModel.Batch(batch.type(),
                    batch.vertices().stream().map(v -> new ExtractedModel.Vertex(v.x(), v.y(), v.z(), v.color(),
                            v.u(), v.v(), v.overlay() == -1 ? overlay : v.overlay(), v.light() == -1 ? light : v.light(), v.nx(), v.ny(), v.nz())).toList())).toList()).submit(pose, collector);
            if (foil) {
                new ExtractedModel(surfaces.batches().stream().map(batch ->
                        new ExtractedModel.Batch(ToolSurfaceRenderTypes.GLINT, batch.vertices())).toList()).submit(pose, collector);
            }
            if (outline != 0) {
                new ExtractedModel(surfaces.batches().stream().map(batch -> new ExtractedModel.Batch(batch.type(),
                        batch.vertices().stream().map(v -> new ExtractedModel.Vertex(v.x(), v.y(), v.z(), outline,
                                v.u(), v.v(), v.overlay() == -1 ? overlay : v.overlay(), v.light() == -1 ? light : v.light(), v.nx(), v.ny(), v.nz())).toList())).toList()).submit(pose, collector);
            }
        }
    }
}
