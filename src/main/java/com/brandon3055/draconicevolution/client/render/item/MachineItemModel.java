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

/** Context-aware machine items. Stack/world reads end in update; submit only consumes frozen arguments. */
public record MachineItemModel(String family, DEItemRenderer renderer, ModelRenderProperties properties,
                               Matrix4fc transformation) implements ItemModel {
    @FunctionalInterface public interface Frozen {
        void submit(PoseStack pose, SubmitNodeCollector collector, int light, int overlay, boolean foil, int outline);
    }
    public static net.minecraft.client.renderer.state.level.CameraRenderState camera() {
        var camera = new net.minecraft.client.renderer.state.level.CameraRenderState();
        camera.orientation = new org.joml.Quaternionf(net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        return camera;
    }
    public record Unbaked(Identifier base, String family) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf("base").forGetter(Unbaked::base),
                Codec.STRING.validate(s -> List.of("chest", "reactor", "mob_soul", "spawner", "ender_manipulator").contains(s)
                        ? com.mojang.serialization.DataResult.success(s)
                        : com.mojang.serialization.DataResult.error(() -> "Unknown machine item family: " + s)).fieldOf("family").forGetter(Unbaked::family)
        ).apply(i, Unbaked::new));
        @Override public MapCodec<Unbaked> type() { return MAP_CODEC; }
        @Override public void resolveDependencies(ResolvableModel.Resolver resolver) { resolver.markDependency(base); }
        @Override public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            DEItemRenderer renderer = switch (family) {
                case "chest" -> new RenderItemDraconiumChest();
                case "reactor" -> new RenderItemReactorComponent();
                case "ender_manipulator" -> new RenderItemEnderEnergyManipulator();
                default -> null;
            };
            var model = context.blockModelBaker().getModel(base);
            return new MachineItemModel(family, renderer, ModelRenderProperties.fromResolvedModel(
                    context.blockModelBaker(), model, model.getTopTextureSlots()), transformation);
        }
    }
    @Override public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver,
                                 ItemDisplayContext context, ClientLevel level, ItemOwner owner, int seed) {
        Frozen frozen;
        if (family.equals("mob_soul")) frozen = RenderItemMobSoul.extract(stack, context);
        else if (family.equals("spawner")) frozen = RenderItemStabilizedSpawner.extract(stack, resolver, level, seed);
        else {
            var capture = new ExtractedModel.Capture();
            renderer.renderItem(stack, context, new PoseStack(), capture, -1, -1);
            var geometry = capture.finish();
            frozen = (pose, collector, light, overlay, foil, outline) -> submitGeometry(geometry, pose, collector, light, overlay, foil, outline);
        }
        Vector3fc[] extents = {new org.joml.Vector3f(-0.5F, -0.5F, -0.5F), new org.joml.Vector3f(1.5F, 1.5F, 1.5F)};
        var layer = output.newLayer();
        layer.setLocalTransform(transformation);
        layer.setExtents(() -> extents);
        properties.applyToLayer(layer, context);
        layer.setupSpecialModel(new FrozenRenderer(frozen, extents), frozen);
        if (stack.hasFoil()) layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
        output.appendModelIdentityElement(this);
        output.appendModelIdentityElement(frozen);
        output.setAnimated();
    }
    private static void submitGeometry(ExtractedModel model, PoseStack pose, SubmitNodeCollector collector,
                                       int light, int overlay, boolean foil, int outline) {
        var batches = model.batches().stream().map(batch -> new ExtractedModel.Batch(batch.type(),
                batch.vertices().stream().map(v -> new ExtractedModel.Vertex(v.x(), v.y(), v.z(), v.color(), v.u(), v.v(),
                        v.overlay() == -1 ? overlay : v.overlay(), v.light() == -1 ? light : v.light(), v.nx(), v.ny(), v.nz())).toList())).toList();
        new ExtractedModel(batches).submit(pose, collector);
        // These emitters use QUADS. Glint uses the same local surface geometry.
        if (foil) new ExtractedModel(batches.stream().filter(b -> b.type().pipeline().getVertexFormatMode() == com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS)
                .map(b -> new ExtractedModel.Batch(net.minecraft.client.renderer.rendertype.RenderTypes.glint(), b.vertices())).toList()).submit(pose, collector);
        if (outline != 0) {
            var outlines = new java.util.ArrayList<ExtractedModel.Batch>();
            for (var batch : batches) batch.type().outline().ifPresent(type -> outlines.add(new ExtractedModel.Batch(type,
                    batch.vertices().stream().map(v -> new ExtractedModel.Vertex(v.x(), v.y(), v.z(), outline, v.u(), v.v(),
                            v.overlay(), v.light(), v.nx(), v.ny(), v.nz())).toList())));
            new ExtractedModel(outlines).submit(pose, collector);
        }
    }
    private record FrozenRenderer(Frozen frozen, Vector3fc[] bounds) implements SpecialModelRenderer<Frozen> {
        @Override public Frozen extractArgument(ItemStack stack) { return frozen; }
        @Override public void getExtents(Consumer<Vector3fc> output) { for (var bound : bounds) output.accept(bound); }
        @Override public void submit(Frozen state, PoseStack pose, SubmitNodeCollector collector,
                                     int light, int overlay, boolean foil, int outline) {
            state.submit(pose, collector, light, overlay, foil, outline);
        }
    }
}
