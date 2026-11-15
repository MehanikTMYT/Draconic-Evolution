package com.brandon3055.draconicevolution.lib;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * port-26.1.2: neoforge INBTSerializable удалён (universal-jar 26.1.2 подтверждён).
 * DE-локальная копия старого интерфейса — семантика 1:1, чтобы не трогать ~2 класса-наследника.
 */
public interface INBTSerializable<T extends Tag> {
    T serializeNBT(net.minecraft.core.HolderLookup.Provider provider);

    void deserializeNBT(net.minecraft.core.HolderLookup.Provider provider, T nbt);

    interface Compound extends INBTSerializable<CompoundTag> {
    }
}
