package com.brandon3055.draconicevolution.lib;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;
import java.util.UUID;

/**
 * port-26.1.2: у CompoundTag в 26 нет getUUID/putUUID/hasUUID, NbtUtils.writeBlockPos/readBlockPos удалены
 * (javap/sources 26.1.2). DE-локальные хелперы: UUID как int-array (как в 1.21), BlockPos как int-array [x,y,z].
 * ВНИМАНИЕ: getIntArray возвращает Optional (ловушка №16).
 */
public class TagUtils26 {

    public static void putUUID(CompoundTag tag, String key, UUID uuid) {
        tag.putIntArray(key, UUIDUtil.uuidToIntArray(uuid));
    }

    public static UUID getUUID(CompoundTag tag, String key) {
        return UUIDUtil.uuidFromIntArray(tag.getIntArray(key).orElseThrow());
    }

    public static Optional<UUID> getUUIDOpt(CompoundTag tag, String key) {
        return tag.getIntArray(key).map(UUIDUtil::uuidFromIntArray);
    }

    public static boolean hasUUID(CompoundTag tag, String key) {
        return tag.getIntArray(key).isPresent();
    }

    public static void writePos(CompoundTag tag, String key, BlockPos pos) {
        tag.putIntArray(key, new int[]{pos.getX(), pos.getY(), pos.getZ()});
    }

    public static Optional<BlockPos> readPos(CompoundTag tag, String key) {
        return tag.getIntArray(key).filter(a -> a.length == 3).map(a -> new BlockPos(a[0], a[1], a[2]));
    }
}
