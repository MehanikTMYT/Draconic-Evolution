package com.brandon3055.draconicevolution.client;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.render.buffer.TransformingVertexConsumer;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.vec.Cuboid6;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.brandon3055.brandonscore.client.gui.modular.elements.GuiElement;
import com.brandon3055.brandonscore.client.gui.modular.lib.GuiRender;
import com.brandon3055.brandonscore.client.gui.modular.sprite.Material;
import com.brandon3055.brandonscore.lib.Vec3D;
import com.brandon3055.draconicevolution.api.modules.lib.ModuleContext;
import com.brandon3055.draconicevolution.api.modules.lib.ModuleEntity;
import com.brandon3055.draconicevolution.api.render.DERenderTypes;
import com.brandon3055.draconicevolution.client.gui.DislocatorGui;
import com.brandon3055.draconicevolution.client.model.ModularChestpieceModel;
import com.brandon3055.draconicevolution.client.render.effect.CrystalFXBase;
import com.brandon3055.draconicevolution.client.render.effect.CrystalFXIO;
import com.brandon3055.draconicevolution.client.render.effect.CrystalFXRing;
import com.brandon3055.draconicevolution.client.render.effect.EffectTrackerCelestialManipulator;
import com.brandon3055.draconicevolution.client.render.effect.StaffBeamEffect;
import com.brandon3055.draconicevolution.client.render.particle.GuardianChargeParticle;
import com.brandon3055.draconicevolution.client.render.particle.ParticlePortal;
import com.brandon3055.draconicevolution.client.sound.CelestialModifierSound;
import com.brandon3055.draconicevolution.client.sound.SimpleSoundImpl;
import com.brandon3055.draconicevolution.blocks.energynet.tileentity.TileCrystalBase;
import com.brandon3055.draconicevolution.blocks.tileentity.TileCelestialManipulator;
import com.brandon3055.draconicevolution.handlers.DESounds;
import com.brandon3055.draconicevolution.init.DEParticles;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * port-26.1.2: общий хук клиентского кода. Common-классы не должны содержать
 * ни одного прямого использования client-only классов MC (net.minecraft.client.*,
 * com.mojang.blaze3d.*) в телах методов — верификация резолвит ссылки и падает на
 * dedicated server (NeoForgeDevDistCleaner). Вся такая логика собрана здесь;
 * вызовы из common-кода исполняются только на клиенте (за dist-guard'ом).
 */
public class DEClientHooks {

    //region Common helpers

    public static boolean hasShiftDown() {
        return Minecraft.getInstance().hasShiftDown();
    }

    public static Player clientPlayer() {
        return Minecraft.getInstance().player;
    }

    public static Level clientLevel() {
        return Minecraft.getInstance().level;
    }

    public static RegistryAccess clientRegistryAccess() {
        return Minecraft.getInstance().level.registryAccess();
    }

    //endregion

    //region ModuleEntity rendering (ModuleEntity.renderModule / renderModuleOverlay / drawChargeProgress)

    public static void renderModuleBase(ModuleEntity<?> entity, GuiElement<?> parent, GuiRender render, int x, int y, int width, int height, double mouseX, double mouseY, boolean stackRender, float partialTicks) {
        int colour = entity.getModuleColour(entity.getModule());
        render.rect(x, y, width, height, colour);
        render.borderRect(x, y, width, height, 1, colour, GuiRender.mixColours(colour, 0x20202000, true));

        if (entity.getModule().getProperties().getTechLevel() == com.brandon3055.brandonscore.api.TechLevel.CHAOTIC) {
            var builder = new TransformingVertexConsumer(render.buffers().getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.glint()), render.pose());
            builder.addVertex(x, y + height, 0).setUv(0, ((float) height / width) / 64F);
            builder.addVertex(x + width, y + height, 0).setUv(((float) width / height) / 64F, ((float) height / width) / 64F);
            builder.addVertex(x + width, y, 0).setUv(((float) width / height) / 64F, 0);
            builder.addVertex(x, y, 0).setUv(0, 0);
            com.brandon3055.brandonscore.client.render.RenderUtils.endBatch(render.buffers());
        }

        Material texture = entity.getModule().getTexture();
        drawModuleTexture(render, texture, x, y, width, height, 1F);

        if (!stackRender && GuiRender.isInRect(x, y, width, height, mouseX, mouseY)) {
            render.rect(x, y, width, height, 0x50FFFFFF);
        }
    }

    public static void drawModuleTexture(GuiRender render, Material texture, int x, int y, int width, int height, float alpha) {
        TextureAtlasSprite sprite = texture.sprite();
        float ar = (float) sprite.contents().width() / (float) sprite.contents().height();
        float iar = (float) sprite.contents().height() / (float) sprite.contents().width();

        if (iar * width <= height) { //Fit Width
            double h = width * iar;
            render.texRect(texture, x, y + (height / 2D) - (h / 2D), width, h, 1F, 1F, 1F, alpha);
        } else { //Fit height
            double w = height * ar;
            render.texRect(texture, x + (width / 2D) - (w / 2D), y, w, height, 1F, 1F, 1F, alpha);
        }
    }

    /** Тултип модуля при наведении (клиентская часть ModuleEntity.renderModuleOverlay). */
    public static boolean moduleOverlayTooltip(ModuleEntity<?> entity, ModuleContext context, GuiRender render, double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        Item item = entity.getModule().getItem();
        ItemStack stack = new ItemStack(item);
        entity.saveEntityToStack(stack, context);
        List<Component> list = stack.getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, mc.options.advancedItemTooltips ? net.minecraft.world.item.TooltipFlag.Default.ADVANCED : net.minecraft.world.item.TooltipFlag.Default.NORMAL);
        render.componentTooltip(list, (int) mouseX, (int) mouseY);
        return true;
    }

    public static void drawChargeProgress(GuiRender render, int x, int y, int width, int height, double progress, @Nullable Component text1, @Nullable Component text2) {
        double diameter = Math.min(width, height) * 0.425;

        render.rect(x, y, width, height, 0x60FF0000);
        var builder = new TransformingVertexConsumer(render.buffers().getBuffer(com.brandon3055.brandonscore.client.render.RenderUtils.FAN_TYPE), render.pose());
        builder.addVertex(x + (width / 2F), y + (height / 2F), 0).setColor(0, 255, 255, 128);
        for (double d = 0; d <= 1; d += 1D / 30D) {
            double angle = (d * progress) + 0.5 - progress;
            double vertX = x + (width / 2D) + Math.sin(angle * (Math.PI * 2)) * diameter;
            double vertY = y + (height / 2D) + Math.cos(angle * (Math.PI * 2)) * diameter;
            builder.addVertex((float) vertX, (float) vertY, 0).setColor(255, 255, 255, 128);
        }
        com.brandon3055.brandonscore.client.render.RenderUtils.endBatch(render.buffers());

        if (text1 != null) {
            ModuleEntity.drawBackgroundString(render, text1, x + width / 2F, y + height / 2F - (text2 == null ? 4 : 8), 0, 0x8000FF00, 1, false, true);
        }
        if (text2 != null) {
            ModuleEntity.drawBackgroundString(render, text2, x + width / 2F, y + height / 2F + 1, 0, 0x8000FF00, 1, false, true);
        }
    }

    //endregion

    //region Modular equipment (штаты/броня)

    /** Клиентская часть DefaultStaffDmgMod.handleEffect. */
    public static void handleStaffHitEffect(LivingEntity source, MCDataInput data) {
        codechicken.lib.vec.Vector3 pos = data.readVector();
        ClientLevel world = (ClientLevel) source.level();
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().play(new SimpleSoundInstance(DESounds.STAFF_HIT_DEFAULT.get(), SoundSource.PLAYERS, 10, 1, world.getRandom(), pos.pos()));
        mc.particleEngine.add(new StaffBeamEffect(world, source, pos));
    }

    /** Клиентская часть ModularChestpiece.getExtendedModel. */
    public static com.brandon3055.brandonscore.client.render.EquippedItemModel chestExtendedModel(com.brandon3055.draconicevolution.items.equipment.ModularChestpiece item, LivingEntity entity, ItemStack stack, @Nullable EquipmentSlot slot, net.minecraft.client.model.HumanoidModel<?> parentModel, boolean slim) {
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        boolean onArmor = slot == null && !chest.isEmpty() && chest.has(net.minecraft.core.component.DataComponents.EQUIPPABLE);
        ModularChestpieceModel<?> model = CHEST_MODELS.computeIfAbsent(item.getTechLevel(), k -> Map.of(false, new ModularChestpieceModel<>(k, false), true, new ModularChestpieceModel<>(k, true))).get(onArmor);
        net.neoforged.neoforge.client.ClientHooks.copyModelProperties(parentModel, model);
        return model;
    }

    /** Кэш моделей нагрудника (переехал из common-класса ModularChestpiece). */
    private static final Map<com.brandon3055.brandonscore.api.TechLevel, Map<Boolean, ModularChestpieceModel<?>>> CHEST_MODELS = new ConcurrentHashMap<>();

    //endregion

    //region GUI / items

    public static void openDislocatorGui(ItemStack stack, Player player) {
        Minecraft.getInstance().setScreen(new DislocatorGui.Screen(stack.getHoverName(), player));
    }

    /** IModularMiningTool: клиентский STOP_DESTROY_BLOCK. */
    public static void sendStopDestroy(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit) {
            mc.getConnection().send(new net.minecraft.network.protocol.game.ServerboundPlayerActionPacket(net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pos, hit.getDirection()));
        }
    }

    /** InfoTablet.openPIGui — клиентская ветка без Project Intelligence. */
    public static void sendPIRequiredMessage() {
        Player player = Minecraft.getInstance().player;
        net.minecraft.network.chat.MutableComponent message = Component.literal("Project Intelligence is required to view DE documentation. ").withStyle(net.minecraft.ChatFormatting.RED);
        net.minecraft.network.chat.MutableComponent link = Component.literal("[Click here to view curse page]").withStyle(net.minecraft.ChatFormatting.BLUE);
        link.setStyle(link.getStyle().withClickEvent(new net.minecraft.network.chat.ClickEvent.OpenUrl(java.net.URI.create("https://www.curseforge.com/minecraft/mc-mods/project-intelligence"))));
        link.setStyle(link.getStyle().withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(Component.literal("https://www.curseforge.com/minecraft/mc-mods/project-intelligence"))));
        message.append(link);
        player.sendSystemMessage(message);
    }

    //endregion

    //region TilePortal

    public static void portalParticles(com.brandon3055.draconicevolution.blocks.tileentity.TilePortal tile) {
        Minecraft mc = Minecraft.getInstance();
        Level level = tile.getLevel();
        Player player = mc.player;
        BlockPos worldPosition = tile.getBlockPos();
        var state = level.getBlockState(worldPosition);
        if (!state.is(com.brandon3055.draconicevolution.init.DEContent.PORTAL.get())) {
            return;
        }
        Direction.Axis axis = state.getValue(com.brandon3055.draconicevolution.blocks.Portal.AXIS);

        double rD1 = level.getRandom().nextDouble();
        double rD2 = level.getRandom().nextDouble();
        double rO1 = -0.1 + level.getRandom().nextDouble() * 0.2;
        double rO2 = -0.1 + level.getRandom().nextDouble() * 0.2;

        codechicken.lib.vec.Vector3 base = codechicken.lib.vec.Vector3.fromTileCenter(tile);
        if (axis == Direction.Axis.Z && player.getZ() < worldPosition.getZ() + 0.5) {
            mc.particleEngine.add(new ParticlePortal((ClientLevel) level, base.add(rD1, rD2, 0), base.add(rD1 + rO1, rD2 + rO2, 0.75)));
        } else if (axis == Direction.Axis.Z && player.getZ() > worldPosition.getZ() + 0.5) {
            mc.particleEngine.add(new ParticlePortal((ClientLevel) level, base.add(rD1, rD2, 1), base.add(rD1 + rO1, rD2 + rO2, 0.25)));
        } else if (axis == Direction.Axis.X && player.getX() < worldPosition.getX() + 0.5) {
            mc.particleEngine.add(new ParticlePortal((ClientLevel) level, base.add(0, rD1, rD2), base.add(0.75, rD1 + rO1, rD2 + rO2)));
        } else if (axis == Direction.Axis.X && player.getX() > worldPosition.getX() + 0.5) {
            mc.particleEngine.add(new ParticlePortal((ClientLevel) level, base.add(1, rD1, rD2), base.add(0.25, rD1 + rO1, rD2 + rO2)));
        } else if (axis == Direction.Axis.Y && player.getY() + player.getEyeHeight() > worldPosition.getY() + 0.5) {
            mc.particleEngine.add(new ParticlePortal((ClientLevel) level, base.add(rD1, 1, rD2), base.add(rD1 + rO1, 0.25, rD2 + rO2)));
        } else if (axis == Direction.Axis.Y && player.getY() + player.getEyeHeight() < worldPosition.getY() + 0.5) {
            mc.particleEngine.add(new ParticlePortal((ClientLevel) level, base.add(rD1, 0, rD2), base.add(rD1 + rO1, 0.75, rD2 + rO2)));
        }
    }

    /** Мостик для TilePortal: общий блок портала. */
    // (DEContent.PORTAL используется напрямую)

    //endregion

    //region Crystal FX (TileCrystal* createStaticFX)

    public static CrystalFXBase crystalFXRing(TileCrystalBase tile) {
        return new CrystalFXRing((ClientLevel) tile.getLevel(), tile);
    }

    public static CrystalFXBase crystalFXIO(TileCrystalBase tile) {
        return new CrystalFXIO((ClientLevel) tile.getLevel(), tile);
    }

    //endregion

    //region TileReactorCore GUI sync (переехало из common)

    public static void reactorComponentSelected(BlockEntity tile, BlockPos pos) {
        Screen screen = Minecraft.getInstance().screen;
        if (tile instanceof com.brandon3055.draconicevolution.blocks.reactor.tileentity.TileReactorComponent && screen instanceof com.brandon3055.draconicevolution.client.gui.ReactorGui.Screen gui) {
            ((com.brandon3055.draconicevolution.client.gui.ReactorGui) gui.getModularGui().getProvider()).component = (com.brandon3055.draconicevolution.blocks.reactor.tileentity.TileReactorComponent) tile;
        }
    }

    //endregion

    //region TreeHarvestEntity pose (переехало из common)

    public static void treeHarvestFirstPersonPose(net.neoforged.neoforge.client.event.RenderHandEvent event, boolean leftHand) {
        com.mojang.blaze3d.vertex.PoseStack poseStack = event.getPoseStack();
        Player player = Minecraft.getInstance().player;
        int handOffset = !leftHand ? 1 : -1;

        poseStack.translate((float) handOffset * -0.2785682F, 0.18344387F, 0.15731531F);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-13.935F));
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees((float) handOffset * 35.3F));
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees((float) handOffset * -9.785F));
        float drawTime = (float) 72000 - ((float) player.getUseItemRemainingTicks() - event.getPartialTick() + 1.0F);
        float charge = drawTime / 20.0F;
        charge = (charge * charge + charge * 2.0F) / 3.0F;
        if (charge > 1.0F) {
            charge = 1.0F;
        }

        if (charge > 0.1F) {
            float f15 = net.minecraft.util.Mth.sin((drawTime - 0.1F) * 1.3F);
            float f18 = charge - 0.1F;
            float animOffset = f15 * f18;
            poseStack.translate(animOffset * 0.0F, animOffset * 0.004F, animOffset * 0.0F);
        }

        poseStack.translate(charge * 0.0F, charge * 0.0F, charge * 0.04F);
        poseStack.scale(1.0F, 1.0F, 1.0F + charge * 0.2F);
        poseStack.mulPose(com.mojang.math.Axis.YN.rotationDegrees((float) handOffset * 45.0F));
    }

    public static void treeHarvestModelPose(Player player, net.minecraft.client.model.player.PlayerModel model, boolean leftHand) {
        if (!leftHand) {
            model.rightArm.yRot = -0.1F + model.head.yRot;
            model.leftArm.yRot = 0.1F + model.head.yRot + 0.4F;
            model.rightArm.xRot = (-(float) Math.PI / 2F) + model.head.xRot;
            model.leftArm.xRot = (-(float) Math.PI / 2F) + model.head.xRot;
        } else {
            model.rightArm.yRot = -0.1F + model.head.yRot - 0.4F;
            model.leftArm.yRot = 0.1F + model.head.yRot;
            model.rightArm.xRot = (-(float) Math.PI / 2F) + model.head.xRot;
            model.leftArm.xRot = (-(float) Math.PI / 2F) + model.head.xRot;
        }

        model.leftPants.loadPose(model.leftLeg.storePose());
        model.rightPants.loadPose(model.rightLeg.storePose());
        model.leftSleeve.loadPose(model.leftArm.storePose());
        model.rightSleeve.loadPose(model.rightArm.storePose());
        model.jacket.loadPose(model.body.storePose());
    }

    //endregion

    //region Guardian phases

    public static void spawnChargeParticles(Entity guardian, BlockPos origin, int effectTime) {
        for (int i = 0; i < 32; i++) {
            Minecraft.getInstance().particleEngine.add(new GuardianChargeParticle((ClientLevel) guardian.level(), codechicken.lib.vec.Vector3.fromBlockPosCenter(origin), codechicken.lib.vec.Vector3.fromEntity(guardian), i / 32D, effectTime, null));
        }
    }

    public static void playBeamSound(SoundEvent sound, SoundSource source, net.minecraft.util.RandomSource random, java.util.function.Supplier<Float> pitchSupplier, java.util.function.Supplier<Boolean> stoppedSupplier, Entity pos, float volume) {
        SimpleSoundImpl.create(sound, source, random)
                .setPitchSupplier(pitchSupplier)
                .setStoppedSupplier(stoppedSupplier)
                .setPos(pos)
                .setVolume(volume)
                .loop()
                .play(Minecraft.getInstance());
    }

    //endregion

    //region Binder overlay (BinderHandler.renderWorldOverlay)

    public static void renderBinderOverlay(LocalPlayer player, com.mojang.blaze3d.vertex.PoseStack pStack, Level level, ItemStack stack, GlobalPos gPos, float partialTicks) {
        BlockPos pos = gPos.pos();
        boolean valid = level.getBlockEntity(pos) instanceof com.brandon3055.draconicevolution.api.energy.ICrystalLink;

        var state = level.getBlockState(pos);
        var shape = state.getShape(level, pos);
        if (shape.isEmpty()) {
            shape = net.minecraft.world.phys.shapes.Shapes.block();
        }
        Cuboid6 cuboid6 = new Cuboid6(shape.bounds());

        MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.position();

        pStack.pushPose();
        pStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        pStack.translate(pos.getX(), pos.getY(), pos.getZ());

        RenderUtils.bufferCuboidSolid(
                new TransformingVertexConsumer(source.getBuffer(DERenderTypes.BOX_NO_DEPTH), pStack),
                cuboid6,
                valid ? 0 : 1, valid ? 1 : 0, 0, 0.5F
        );

        source.endBatch();

        RenderUtils.bufferCuboidOutline(
                new TransformingVertexConsumer(source.getBuffer(DERenderTypes.OUTLINE_TYPE), pStack),
                cuboid6,
                0, 0, 0, 1
        );

        source.endBatch();
        pStack.popPose();
    }

    //endregion

    //region Celestial Manipulator FX (переехало из TileCelestialManipulator)

    private static final Map<BlockPos, CelestialState> CELESTIAL_STATES = new ConcurrentHashMap<>();

    private static CelestialState celestialState(TileCelestialManipulator tile) {
        return CELESTIAL_STATES.computeIfAbsent(tile.getBlockPos(), k -> new CelestialState());
    }

    public static void celestialWeatherTick(TileCelestialManipulator tile) {
        CelestialState s = celestialState(tile);
        if (s.effects == null || s.effects.size() < 8 || s.sound == null) {
            celestialStartWeather(tile, s);
            return;
        }

        double riseEnd = 20;
        double expandStart = 60;
        double expandEnd = 100;
        double ascendStart = 140;
        double ascendStop = 200;
        double ascendHeight = 500;

        int timer = tile.timer;
        double height = (Math.min(riseEnd, timer) / riseEnd) * 3D;
        double expansion = timer > expandStart ? ((Math.min(expandEnd, timer) - expandStart) / (expandEnd - expandStart)) : 0D;
        double secondaryExpand = 0;

        double ascPos = 0;

        if (timer > ascendStart) {
            ascPos = (timer - ascendStart) / (ascendStop - ascendStart);
            height += ascPos * (ascendHeight * ascPos);
            secondaryExpand = ascPos * ascPos * 100D;
        }

        Vec3D effectFocus = Vec3D.getCenter(tile.getBlockPos()).add(0, height, 0);

        Level level = tile.getLevel();
        double scaleTime = (timer - expandStart) / (ascendStart - expandStart);
        if (timer > expandStart && timer < ascendStart) {
            for (int i = 0; i < 10; i++) {
                Direction dir = Direction.values()[2 + level.getRandom().nextInt(4)];
                var data = new com.brandon3055.brandonscore.client.particle.IntParticleType.IntParticleData(DEParticles.SPARK.get(),
                        0, 127, 255,
                        (int) (1F * (level.getRandom().nextFloat() + 0.1) * 100),
                        (int) (5D * scaleTime * 100),
                        10, 0,
                        (int) (0),
                        (int) (1F * 1000),
                        1
                );
                double speed = 0.3 / 10;
                codechicken.lib.vec.Vector3 pos = codechicken.lib.vec.Vector3.fromBlockPosCenter(tile.getBlockPos()).add(dir.getStepX() * -0.3, 0, dir.getStepZ() * -0.3);
                level.addParticle(data, pos.x, pos.y, pos.z, speed * dir.getStepX(), 0, speed * dir.getStepZ());
            }
        }

        if (timer == ascendStart) {
            for (int i = 0; i < 100; i++) {
                var data = new com.brandon3055.brandonscore.client.particle.IntParticleType.IntParticleData(DEParticles.SPARK.get(),
                        0, 127, 255,
                        (int) (5F * (level.getRandom().nextFloat() + 0.1) * 100),
                        (int) (100 * 100),
                        80, 0,
                        (int) (-0.5 * 1000),
                        (int) (1F * 1000)
                );
                codechicken.lib.vec.Vector3 pos = codechicken.lib.vec.Vector3.fromBlockPosCenter(tile.getBlockPos());
                level.addParticle(data, pos.x, pos.y, pos.z, 0.5 * level.getRandom().nextGaussian(), 0, 0.5 * level.getRandom().nextGaussian());
            }
        }

        if (timer == ascendStart) {
            level.playLocalSound(effectFocus.x, effectFocus.y, effectFocus.z, DESounds.FUSION_COMPLETE.get(), SoundSource.BLOCKS, 1F, 2F, false);
        }

        s.sound.updateSound(effectFocus.copy().add(0, -(height * 0.8), 0), (float) expansion, (float) expansion + 0.5F);

        for (EffectTrackerCelestialManipulator effect : s.effects) {
            effect.onUpdate();
        }

        double rotation = (double) timer * (expansion * 0.05D);
        for (EffectTrackerCelestialManipulator effect : s.effects) {
            effect.effectFocus = effectFocus;
            effect.scale = (float) expansion * (1F + (float) (ascPos * 5));

            double indexPos = (double) s.effects.indexOf(effect) / (double) s.effects.size();
            double offset = indexPos * (Math.PI * 2);
            double offsetX = Math.sin(rotation + offset) * expansion * (3 + secondaryExpand);
            double offsetZ = Math.cos(rotation + offset) * expansion * (3 + secondaryExpand);

            effect.pos = effectFocus.copy().add(offsetX, 0, offsetZ);

            offset = (indexPos + (1D / (double) s.effects.size())) * (Math.PI * 2);
            offsetX = Math.sin(rotation + offset) * expansion * (3 + secondaryExpand);
            offsetZ = Math.cos(rotation + offset) * expansion * (3 + secondaryExpand);

            effect.linkPos = effectFocus.copy().add(offsetX, 0, offsetZ);
        }

        if (timer >= 220) {
            level.playLocalSound(effectFocus.x, effectFocus.y, effectFocus.z, DESounds.BOOM.get(), SoundSource.BLOCKS, com.brandon3055.draconicevolution.DEOldConfig.disableLoudCelestialManipulator ? 1 : 100, 1F, false);
            tile.timer = 0;
            tile.weatherToggleRunning.set(false);
            s.effects.clear();
            s.sound.kill();
            s.sound = null;
        }
    }

    private static void celestialStartWeather(TileCelestialManipulator tile, CelestialState s) {
        tile.timer = 0;
        s.effects = new LinkedList<>();
        for (int i = 0; i < 8; i++) {
            s.effects.add(new EffectTrackerCelestialManipulator(tile.getLevel(), Vec3D.getCenter(tile.getBlockPos()).add(0, 1.5, 0), Vec3D.getCenter(tile.getBlockPos()).add(0, 1.5, 0)));
        }

        Vec3D vec = Vec3D.getCenter(tile.getBlockPos().offset(0, 1, 0));
        s.sound = new CelestialModifierSound(DESounds.ELECTRIC_BUZZ.get(), tile.getBlockPos(), tile.getLevel().getRandom());
        s.sound.updateSound(vec, 0.01F, 0.5F);
        Minecraft.getInstance().getSoundManager().play(s.sound);
        tile.getLevel().playLocalSound(vec.x, vec.y, vec.z, DESounds.FUSION_COMPLETE.get(), SoundSource.BLOCKS, tile.getSoundVolume(), 0.5F, false);
    }

    public static void celestialSunTick(TileCelestialManipulator tile) {
        CelestialState s = celestialState(tile);
        if (s.effects == null || s.effects.size() < 2 || s.sound == null) {
            celestialStartSun(tile, s);
        }

        Level level = tile.getLevel();
        int timer = tile.timer;
        double depProg = Math.min(timer / 100F, 1D);

        EffectTrackerCelestialManipulator effect;
        Vec3D focus = Vec3D.getCenter(tile.getBlockPos()).add(0, 5.5 * depProg, 0);
        s.sound.updateSound(focus, (float) depProg, 0.5F + (float) depProg);

        var data = new com.brandon3055.brandonscore.client.particle.IntParticleType.IntParticleData(DEParticles.SPARK.get(),
                255, 127, 0,
                (int) (1.2F * (level.getRandom().nextFloat() + 0.1) * 100),
                (int) (0.5F * 100),
                30, 10,
                (int) (0)
        );
        codechicken.lib.vec.Vector3 pos = codechicken.lib.vec.Vector3.fromBlockPosCenter(tile.getBlockPos());
        level.addParticle(data, pos.x, pos.y, pos.z, 0.002D, 0.04, 0.002D);

        Direction dir = Direction.values()[2 + level.getRandom().nextInt(4)];
        data = new com.brandon3055.brandonscore.client.particle.IntParticleType.IntParticleData(DEParticles.SPARK.get(),
                255, 127, 255,
                (int) (1F * (level.getRandom().nextFloat() + 0.1) * 100),
                (int) (0.5F * 100),
                10, 0,
                (int) (0),
                (int) (1F * 1000),
                1
        );
        double speed = 0.3 / 10;
        pos = codechicken.lib.vec.Vector3.fromBlockPosCenter(tile.getBlockPos()).add(dir.getStepX() * -0.3, 0, dir.getStepZ() * -0.3);
        level.addParticle(data, pos.x, pos.y, pos.z, speed * dir.getStepX(), 0, speed * dir.getStepZ());

        double rotation = (level.getOverworldClockTime() % 24000) / 24000D;
        double offset;
        double offsetX;
        double offsetY;

        for (int i = 0; i < 2; i++) {
            effect = s.effects.get(i);
            effect.scale = (float) depProg;
            effect.onUpdate();
            offset = rotation * (Math.PI * 2);
            offsetX = Math.cos(offset) * 2.5 * depProg;
            offsetY = Math.sin(offset) * 2.5 * depProg;
            effect.pos = focus.copy().add(offsetX, offsetY, 0);
            rotation += 0.5F;
            effect.effectFocus = focus;
        }

        rotation = timer / 50D;

        double inc = 1D / (s.effects.size() - 2);
        for (int i = 2; i < s.effects.size(); i++) {
            effect = s.effects.get(i);
            offset = rotation * (Math.PI * 2);
            double x = Math.cos(offset) * 1 * depProg;
            double z = Math.sin(offset) * 1 * depProg;
            effect.scale = (float) depProg;
            effect.onUpdate();

            effect.pos = focus.copy().add(x, (i % 2 == 0 ? x : -x) * 0.8, z);
            if (i % 2 == 0) {
                effect.red = 0.9F;
                effect.green = 0.5F;
                effect.blue = 0.1F;
            } else {
                effect.red = 0.3F;
                effect.green = 0.2F;
                effect.blue = 1;
            }

            effect.effectFocus = focus;
            effect.renderBolts = false;
            rotation += inc;
        }
    }

    private static void celestialStartSun(TileCelestialManipulator tile, CelestialState s) {
        tile.timeWarpRunning.set(true);
        tile.timeWarpStopping.set(false);
        tile.timer = 0;

        s.effects = new LinkedList<>();
        for (int i = 0; i < 12; i++) {
            s.effects.add(new EffectTrackerCelestialManipulator(tile.getLevel(), Vec3D.getCenter(tile.getBlockPos()).add(0, 1.5, 0), Vec3D.getCenter(tile.getBlockPos()).add(0, 1.5, 0)));
        }

        s.effects.get(0).red = 1F;
        s.effects.get(0).green = 0.821F;
        s.effects.get(0).blue = 0.174F;
        s.effects.get(0).renderBolts = false;

        s.effects.get(1).red = 1F;
        s.effects.get(1).green = 1F;
        s.effects.get(1).blue = 1F;
        s.effects.get(1).renderBolts = false;

        s.sound = new CelestialModifierSound(DESounds.SUN_DIAL_EFFECT.get(), tile.getBlockPos(), tile.getLevel().getRandom());
        s.sound.updateSound(Vec3D.getCenter(tile.getBlockPos()), tile.getSoundVolume(), 0.5F);
        Minecraft.getInstance().getSoundManager().play(s.sound);
    }

    public static void celestialStopTick(TileCelestialManipulator tile) {
        CelestialState s = celestialState(tile);
        if (tile.timer >= 0) {
            celestialSunTick(tile);
        } else {
            if (s.effects != null) {
                s.effects.clear();
            }
            if (s.sound != null) {
                s.sound.kill();
                s.sound = null;
            }
        }
    }

    public static void celestialStandby(TileCelestialManipulator tile) {
        Level level = tile.getLevel();
        var data = new com.brandon3055.brandonscore.client.particle.IntParticleType.IntParticleData(DEParticles.SPARK.get(),
                76, 0, 255,
                (int) (0.4F * (level.getRandom().nextFloat() + 0.1) * 100),
                (int) (0.15F * 100),
                30, 10,
                (int) (0)
        );
        codechicken.lib.vec.Vector3 pos = codechicken.lib.vec.Vector3.fromBlockPosCenter(tile.getBlockPos());
        level.addParticle(data, pos.x, pos.y, pos.z, 0.002D, 0.01, 0.002D);

        celestialState(tile).effects = null;
    }

    public static void celestialExtractEffects(TileCelestialManipulator tile, com.mojang.blaze3d.vertex.PoseStack pose, MultiBufferSource buffers, float partialTicks) {
        List<EffectTrackerCelestialManipulator> effects = celestialState(tile).effects;
        if (effects == null) return;
        for (EffectTrackerCelestialManipulator effect : effects) {
            effect.extractEffect(pose, buffers, partialTicks, tile.getBlockPos(), tile.weatherToggleRunning.get());
        }
    }

    private static class CelestialState {
        private CelestialModifierSound sound;
        private List<EffectTrackerCelestialManipulator> effects;
    }

    //endregion
}
