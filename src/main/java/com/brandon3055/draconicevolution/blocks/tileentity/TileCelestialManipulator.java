package com.brandon3055.draconicevolution.blocks.tileentity;

import codechicken.lib.data.MCDataInput;

import codechicken.lib.vec.Vector3;

import com.brandon3055.brandonscore.api.power.OPStorage;

import com.brandon3055.brandonscore.blocks.TileBCore;

import com.brandon3055.brandonscore.capability.CapabilityOP;


import com.brandon3055.brandonscore.lib.IChangeListener;

import com.brandon3055.brandonscore.lib.IInteractTile;

import com.brandon3055.brandonscore.lib.Vec3D;

import com.brandon3055.brandonscore.lib.datamanager.DataFlags;

import com.brandon3055.brandonscore.lib.datamanager.ManagedBool;

import com.brandon3055.brandonscore.lib.datamanager.ManagedByte;

import com.brandon3055.draconicevolution.DEOldConfig;

import com.brandon3055.draconicevolution.DraconicEvolution;

import com.brandon3055.draconicevolution.api.modules.lib.ModularOPStorage;




import com.brandon3055.draconicevolution.handlers.DESounds;

import com.brandon3055.draconicevolution.init.DEContent;

import com.brandon3055.draconicevolution.inventory.CelestialManipulatorMenu;

import com.brandon3055.draconicevolution.utils.LogHelper;


import java.util.LinkedList;

import java.util.List;

import javax.annotation.Nullable;



import net.minecraft.core.BlockPos;

import net.minecraft.core.Direction;

import net.minecraft.network.chat.Component;

import net.minecraft.server.level.ServerLevel;

import net.minecraft.server.level.ServerPlayer;


import net.minecraft.world.InteractionHand;

import net.minecraft.world.InteractionResult;

import net.minecraft.world.MenuProvider;

import net.minecraft.world.entity.player.Inventory;

import net.minecraft.world.entity.player.Player;

import net.minecraft.world.inventory.AbstractContainerMenu;

import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.level.storage.ServerLevelData;

import net.minecraft.world.phys.BlockHitResult;



import net.neoforged.neoforge.capabilities.Capabilities;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;




/**
 * Created by brandon3055 on 28/09/2016.
 */
public class TileCelestialManipulator extends TileBCore implements IChangeListener, MenuProvider, IInteractTile {

    public final ManagedBool weatherMode = register(new ManagedBool("weather_mode", true, DataFlags.SAVE_NBT_SYNC_TILE, DataFlags.CLIENT_CONTROL));
    public final ManagedBool active = register(new ManagedBool("active", DataFlags.SAVE_NBT_SYNC_TILE));
    public final ManagedBool weatherToggleRunning = register(new ManagedBool("weather_toggle_running", DataFlags.SYNC_TILE));
    public final ManagedBool timeWarpRunning = register(new ManagedBool("time_warp_running", DataFlags.SYNC_TILE));
    public final ManagedBool timeWarpStopping = register(new ManagedBool("time_warp_stopping", DataFlags.SYNC_TILE));
    public final ManagedBool redstoneSignal = register(new ManagedBool("redstone_signal", DataFlags.SAVE_NBT_SYNC_TILE));
    public final ManagedByte rsMode = register(new ManagedByte("rs_mode", DataFlags.SAVE_NBT_SYNC_CONTAINER));
    public int timer = 0;
    public boolean storm = false;
    public boolean rain = false;
    public long targetTime;

    public OPStorage opStorage = new ModularOPStorage(this, 8000000, 4000000, 4000000);

    public TileCelestialManipulator(BlockPos pos, BlockState state) {
        super(DEContent.TILE_CELESTIAL_MANIPULATOR.get(), pos, state);
        capManager.setManaged("energy", CapabilityOP.OP, opStorage).saveBoth().syncContainer();
        weatherMode.setCCSCS();
    }

    public static void register(RegisterCapabilitiesEvent event) {
        com.brandon3055.brandonscore.capability.CapabilityOP.registerBlockEntity(event, DEContent.TILE_CELESTIAL_MANIPULATOR.get());
    }

    @Override
    public void tick() {
        super.tick();

        if (weatherToggleRunning.get()) {
            timer++;
            if (level.isClientSide()) {
                com.brandon3055.draconicevolution.client.DEClientHooks.celestialWeatherTick(this);
            } else {
                if (timer >= 230) {
                    timer = 0;
                    weatherToggleRunning.set(false);
                    int time = (10 * 60 * 20) + level.getRandom().nextInt(20 * 60 * 20);
                    var weather = ((ServerLevel) level).getWeatherData();
                    weather.setRaining(rain);
                    weather.setThundering(storm);
                    weather.setRainTime(rain ? time : 0);
                    weather.setClearWeatherTime(rain ? 0 : time);
                }
            }
        } else if (timeWarpRunning.get()) {
            timer++;
            if (level.isClientSide()) {
                com.brandon3055.draconicevolution.client.DEClientHooks.celestialSunTick(this);
            }
            if (timer > 100) {
                if (opStorage.getEnergyStored() > 320) {
                    int extracted = opStorage.extractEnergy(16000, true);
                    int ticks = extracted / 320;
                    if (level instanceof ServerLevel serverLevel) {
                        // MC26 clock changes sync authoritatively; preserve clock rate and pause state.
                        var clock = serverLevel.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.WORLD_CLOCK)
                                .getOrThrow(net.minecraft.world.clock.WorldClocks.OVERWORLD);
                        serverLevel.clockManager().addTicks(clock, ticks);
                        opStorage.extractEnergy(ticks * 320, false);
                    }
                } else {
                    stopTimeWarp();
                }
                if (!level.isClientSide() && level.getOverworldClockTime() >= targetTime) {
                    stopTimeWarp();
                }
            }
        } else if (timeWarpStopping.get()) {
            if (timer > 100) {
                timer = 100;
            }

            timer--;
            if (timer <= 0 && !level.isClientSide()) {
                timeWarpStopping.set(false);
            }

            if (level.isClientSide()) {
                com.brandon3055.draconicevolution.client.DEClientHooks.celestialStopTick(this);
            }
        } else if (active.get()) {
            active.set(false);
        } else if (level.isClientSide()) {
            com.brandon3055.draconicevolution.client.DEClientHooks.celestialStandby(this);
        }
    }

    public void toggleWeather(boolean rain, boolean storm) {
        if (level.isClientSide() || active.get()) {
            return;
        }

        this.storm = storm;
        this.rain = rain;
        timer = 0;
        weatherToggleRunning.set(true);
        //        sendPacketToClients(new PacketTileMessage(this, (byte) 0, 0, false), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 128));
        active.set(true);
    }

    public void startTimeWarp(long targetTime) {
        active.set(true);
        this.targetTime = targetTime;
        timer = 0;
        timeWarpRunning.set(true);
        //        sendPacketToClients(new PacketTileMessage(this, (byte) 1, 0, false), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 128));
    }

    public void stopTimeWarp() {
        //        sendPacketToClients(new PacketTileMessage(this, (byte) 2, 0, false), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 128));
        timeWarpRunning.set(false);
        timeWarpStopping.set(true);
    }

    //region Interact


    @Override
    public void receivePacketFromClient(MCDataInput data, ServerPlayer client, int id) {
        if (id == 0) {
            handleInteract(data.readString(), client);
        } else if (id == 1) {
            int intValue = data.readInt();
            if (intValue >= 0 && intValue <= 9) {
                rsMode.set((byte) intValue);
            }
        }
    }

    public void handleInteract(String action, Player player) {
        if (action.endsWith("STOP") && active.get() && timeWarpRunning.get()) {
            stopTimeWarp();
            return;
        }

        if (active.get()) {
            sendMessage(Component.translatable("msg." + DraconicEvolution.MODID + ".celestial_manipulator.alreadyRunning"), player);
            return;
        }

        switch (action) {
            case "STOP_RAIN":
                if (!level.isRaining()) {
                    sendMessage(Component.translatable("msg." + DraconicEvolution.MODID + ".celestial_manipulator.notRaining"), player);
                    return;
                }
                if (opStorage.getEnergyStored() < 256000) {
                    sendMessage(Component.translatable("msg." + DraconicEvolution.MODID + ".celestial_manipulator.insufficientPower").append(" (256000RF)"), player);
                    return;
                }
                opStorage.modifyEnergyStored(-256000);
                toggleWeather(false, false);
                LogHelper.info("Stopped rain! Cause: " + worldPosition);
                return;
            case "START_RAIN":
                if (level.isRaining()) {
                    sendMessage(Component.translatable("msg." + DraconicEvolution.MODID + ".celestial_manipulator.alreadyRaining"), player);
                    return;
                }
                if (opStorage.getEnergyStored() < 256000) {
                    sendMessage(Component.translatable("msg." + DraconicEvolution.MODID + ".celestial_manipulator.insufficientPower").append(" (256000RF)"), player);
                    return;
                }
                opStorage.modifyEnergyStored(-256000);
                toggleWeather(true, false);
                LogHelper.info("Started rain! Cause: " + worldPosition);
                return;
            case "START_STORM":
                if (level.isRaining() && level.isThundering()) {
                    sendMessage(Component.translatable("msg." + DraconicEvolution.MODID + ".celestial_manipulator.alreadyStorming"), player);
                    return;
                }
                if (opStorage.getEnergyStored() < 384000) {
                    sendMessage(Component.translatable("msg." + DraconicEvolution.MODID + ".celestial_manipulator.insufficientPower").append(" (384000RF)"), player);
                    return;
                }
                opStorage.modifyEnergyStored(-384000);
                toggleWeather(true, true);
                LogHelper.info("Started storm! Cause: " + worldPosition);
                return;
            case "SUN_RISE":
                startTimeWarp(level.getOverworldClockTime() + calculateTimeTill(0));
                LogHelper.info("Set time to sunrise! Cause: " + worldPosition);
                break;
            case "MID_DAY":
                startTimeWarp(level.getOverworldClockTime() + calculateTimeTill(5900));
                LogHelper.info("Set time to midday! Cause: " + worldPosition);
                break;
            case "SUN_SET":
                startTimeWarp(level.getOverworldClockTime() + calculateTimeTill(12000));
                LogHelper.info("Set time to sunset! Cause: " + worldPosition);
                break;
            case "MOON_RISE":
                startTimeWarp(level.getOverworldClockTime() + calculateTimeTill(13000));
                LogHelper.info("Set time to moonrise! Cause: " + worldPosition);
                break;
            case "MIDNIGHT":
                startTimeWarp(level.getOverworldClockTime() + calculateTimeTill(17900));
                LogHelper.info("Set time to midnight! Cause: " + worldPosition);
                break;
            case "MOON_SET":
                startTimeWarp(level.getOverworldClockTime() + calculateTimeTill(22500));
                LogHelper.info("Set time to moonset! Cause: " + worldPosition);
                break;
            case "SKIP_24":
                startTimeWarp(level.getOverworldClockTime() + 24000);
                LogHelper.info("Skipped one day! Cause: " + worldPosition);
                break;
        }
    }

    private void sendMessage(Component message, Player player) {
        if (player != null) {
            player.sendSystemMessage(message);
        }
    }

    private int calculateTimeTill(int time) {
        int currentTime = (int) (level.getOverworldClockTime() % 24000);
        return currentTime > time ? (24000 - (currentTime - time)) : time - currentTime;
    }

    //endregion

    //region Weather/Sun effects (client) — port-26.1.2: логика перенесена в client.DEClientHooks (dist-clean)

    //endregion

    private String[] ACTIONS = new String[]{"STOP_RAIN", "START_RAIN", "START_STORM", "SUN_RISE", "MID_DAY", "SUN_SET", "MOON_RISE", "MIDNIGHT", "MOON_SET", "SKIP_24"};

    @Override
    public void onNeighborChange(BlockPos blockChanged) {
        if (level.hasNeighborSignal(worldPosition)) {
            if (!redstoneSignal.get()) {
                redstoneSignal.set(true);
                handleInteract(ACTIONS[rsMode.get()], null);
            }
        } else {
            if (redstoneSignal.get()) {
                redstoneSignal.set(false);
                if (timeWarpRunning.get() && rsMode.get() == 9) {
                    stopTimeWarp();
                }
            }
        }
    }

    public float getSoundVolume() {
        return DEOldConfig.disableLoudCelestialManipulator ? 1 : 10;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int currentWindowIndex, Inventory playerInventory, Player player) {
        return new CelestialManipulatorMenu(currentWindowIndex, player.getInventory(), this);
    }

    @Override
    public InteractionResult onBlockUse(BlockState state, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (player instanceof ServerPlayer) {
            player.openMenu(this, worldPosition);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }
}
