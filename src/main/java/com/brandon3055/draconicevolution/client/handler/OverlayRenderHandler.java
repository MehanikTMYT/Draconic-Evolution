package com.brandon3055.draconicevolution.client.handler;

import com.brandon3055.draconicevolution.api.energy.ICrystalBinder;
import com.brandon3055.draconicevolution.handlers.BinderHandler;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by brandon3055 on 07/02/2023
 */
public class OverlayRenderHandler {
    public static final Logger LOGGER = LogManager.getLogger();
    private static final CrashLock LOCK = new CrashLock("Already Initialized");

    public static void init() {
        LOCK.lock();
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.AfterTranslucentParticles.class, OverlayRenderHandler::renderLevelStage);
    }

    public static void renderLevelStage(RenderLevelStageEvent.AfterTranslucentParticles event) { // port-26.1.2: AFTER_PARTICLES -> AfterTranslucentParticles (стадии стали подклассами)
        ClientEventHandler.MODELVIEW.set(new org.joml.Matrix4f(event.getModelViewMatrix()));
        // port-26.1.2-check TODO: PROJECTION больше не доступна из события (GpuBufferSlice); используем единичную матрицу, gluProject может быть неточен
        ClientEventHandler.PROJECTION.setIdentity();

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        Level world = player.level();
        ItemStack stack = player.getMainHandItem();
        ItemStack offStack = player.getOffhandItem();
        Minecraft mc = Minecraft.getInstance();
        float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false); // port-26.1.2: у события нет getPartialTick

        try {
            if (!stack.isEmpty() && stack.getItem() instanceof ICrystalBinder) {
                BinderHandler.renderWorldOverlay(player, event.getPoseStack(), world, stack, mc, partialTicks);
            } else if (!stack.isEmpty() && offStack.getItem() instanceof ICrystalBinder) {
                BinderHandler.renderWorldOverlay(player, event.getPoseStack(), world, offStack, mc, partialTicks);
            }
        } catch (Throwable e) {
            LOGGER.error("An error occurred while rendering crystal binder overlay", e);
        }
    }

}
