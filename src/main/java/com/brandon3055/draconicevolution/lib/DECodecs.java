package com.brandon3055.draconicevolution.lib;

import com.brandon3055.brandonscore.utils.TargetPos;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import com.brandon3055.brandonscore.inventory.InventoryDynamic;

import java.util.ArrayList;
import java.util.List;

/**
 * port-26.1.2: локальные кодеки для типов без Codec/StreamCodec в BC-26
 * (TargetPos, InventoryDynamic) — используются в DataComponentType-регистрации.
 */
public class DECodecs {

    public static final Codec<TargetPos> TARGET_POS = net.minecraft.nbt.CompoundTag.CODEC.xmap(
            tag -> {
                TargetPos pos = new TargetPos();
                pos.readFromNBT(tag);
                return pos;
            },
            pos -> pos.writeToNBT()
    );

    public static final StreamCodec<ByteBuf, TargetPos> TARGET_POS_STREAM = ByteBufCodecs.COMPOUND_TAG.map(
            tag -> {
                TargetPos pos = new TargetPos();
                pos.readFromNBT(tag);
                return pos;
            },
            pos -> pos.writeToNBT()
    );

    public static final Codec<InventoryDynamic> INVENTORY_DYNAMIC = ItemStack.CODEC.listOf().xmap(
            DECodecs::fromList,
            DECodecs::toList
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, InventoryDynamic> INVENTORY_DYNAMIC_STREAM = ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).map(
            DECodecs::fromList,
            DECodecs::toList
    );

    public static InventoryDynamic copy(InventoryDynamic inv) { // port-26.1.2: InventoryDynamic.copy удалён из BC-26
        return fromList(toList(inv));
    }

    private static InventoryDynamic fromList(List<ItemStack> stacks) {
        InventoryDynamic inv = new InventoryDynamic();
        for (ItemStack stack : stacks) {
            inv.setItem(inv.getContainerSize(), stack);
        }
        return inv;
    }

    private static List<ItemStack> toList(InventoryDynamic inv) {
        return new ArrayList<>(inv.getStacks());
    }

    /**
     * port-26.1.2: адаптер Codec -> StreamCodec&lt;ByteBuf,T&gt; через NBT-обёртку
     * (для типов, чей нативный стрим-кодек остался на MCDataInput/RegistryFriendlyByteBuf).
     */
    public static <T> StreamCodec<ByteBuf, T> nbtMapped(Codec<T> codec) {
        return ByteBufCodecs.COMPOUND_TAG.map(
                tag -> codec.parse(net.minecraft.nbt.NbtOps.INSTANCE, tag.get("v")).getOrThrow(),
                value -> {
                    net.minecraft.nbt.Tag inner = codec.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, value).getOrThrow();
                    net.minecraft.nbt.CompoundTag wrap = new net.minecraft.nbt.CompoundTag();
                    wrap.put("v", inner);
                    return wrap;
                }
        );
    }
}
