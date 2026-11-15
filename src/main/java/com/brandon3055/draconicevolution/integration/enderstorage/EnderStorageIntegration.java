package com.brandon3055.draconicevolution.integration.enderstorage;

import codechicken.lib.colour.EnumColour;
import com.brandon3055.draconicevolution.integration.ModHelper;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

/**
 * Optional boundary to the real EnderStorage API (1.21.1 d801697).
 * No replacement storage, frequency or configuration classes are supplied by DE.
 * Class loading is deferred until EnderStorage is present; an incompatible installed
 * library fails explicitly instead of silently redirecting a private frequency.
 */
public final class EnderStorageIntegration {
    private EnderStorageIntegration() {}

    private static final class API {
        static final Class<?> FREQUENCY = type("api.Frequency");
        static final Class<?> CHEST = type("tile.TileEnderChest");
        static final Class<?> MANAGER = type("manager.EnderStorageManager");
        static final Class<?> STORAGE_TYPE = type("api.StorageType");
        static final Class<?> ITEM_STORAGE = type("storage.EnderItemStorage");
        @SuppressWarnings("unchecked")
        static final Codec<Object> CODEC = (Codec<Object>) field(FREQUENCY, "CODEC");
        static final Method GET_FREQUENCY = method(CHEST, "getFrequency");
        static final Method INSTANCE = method(MANAGER, "instance", boolean.class);
        static final Method GET_STORAGE = method(MANAGER, "getStorage", FREQUENCY, STORAGE_TYPE);
    }

    public static boolean isChest(BlockEntity tile) {
        return ModHelper.ENDERSTORAGE && API.CHEST.isInstance(tile);
    }

    public static CompoundTag bind(BlockEntity chest, Player player, HolderLookup.Provider registries) {
        Object frequency = call(API.GET_FREQUENCY, chest);
        @SuppressWarnings("unchecked")
        Optional<UUID> owner = (Optional<UUID>) call(method(API.FREQUENCY, "owner"), frequency);
        boolean anarchy = (boolean) field(type("config.EnderStorageConfig"), "anarchyMode");
        if (anarchy && owner.filter(player.getUUID()::equals).isEmpty()) {
            frequency = call(method(API.FREQUENCY, "withoutOwner"), frequency);
        }
        var encoded = API.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), frequency).getOrThrow();
        if (!(encoded instanceof CompoundTag tag)) throw new IllegalStateException("EnderStorage frequency is not an NBT compound");
        return tag;
    }

    public static Container storage(CompoundTag data, Player player) {
        Object frequency = frequency(data, player.registryAccess());
        Object manager = call(API.INSTANCE, null, false);
        Object storage = call(API.GET_STORAGE, manager, frequency, field(API.ITEM_STORAGE, "TYPE"));
        return Container.class.cast(storage); // The actual EnderItemStorage owns persistence, size and dirty tracking.
    }

    public static EnumColour[] colours(CompoundTag data, HolderLookup.Provider registries) {
        Object frequency = frequency(data, registries);
        return new EnumColour[] {
                (EnumColour) call(method(API.FREQUENCY, "left"), frequency),
                (EnumColour) call(method(API.FREQUENCY, "middle"), frequency),
                (EnumColour) call(method(API.FREQUENCY, "right"), frequency)
        };
    }

    public static Component tooltip(CompoundTag data, HolderLookup.Provider registries) {
        return (Component) call(method(API.FREQUENCY, "getTooltip"), frequency(data, registries));
    }

    @SuppressWarnings("unchecked")
    public static Optional<Component> ownerName(CompoundTag data, HolderLookup.Provider registries) {
        Object frequency = frequency(data, registries);
        return (boolean) call(method(API.FREQUENCY, "hasOwner"), frequency)
                ? (Optional<Component>) call(method(API.FREQUENCY, "ownerName"), frequency) : Optional.empty();
    }

    private static Object frequency(CompoundTag data, HolderLookup.Provider registries) {
        CompoundTag tag = data.contains("frequency") ? data.getCompoundOrEmpty("frequency") : data;
        var decoded = API.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).result();
        if (decoded.isPresent()) return decoded.get();
        // DE previously wrote the library's legacy numeric-colour/owner_name representation.
        try {
            return API.FREQUENCY.getConstructor(CompoundTag.class).newInstance(tag);
        } catch (ReflectiveOperationException ex) {
            throw failure("reading legacy frequency", ex);
        }
    }

    private static Class<?> type(String name) {
        try { return Class.forName("codechicken.enderstorage." + name); }
        catch (ClassNotFoundException ex) { throw failure("loading " + name, ex); }
    }
    private static Method method(Class<?> owner, String name, Class<?>... args) {
        try { return owner.getMethod(name, args); }
        catch (ReflectiveOperationException ex) { throw failure("resolving " + owner.getName() + "." + name, ex); }
    }
    private static Object field(Class<?> owner, String name) {
        try { return owner.getField(name).get(null); }
        catch (ReflectiveOperationException ex) { throw failure("reading " + owner.getName() + "." + name, ex); }
    }
    private static Object call(Method method, Object receiver, Object... args) {
        try { return method.invoke(receiver, args); }
        catch (ReflectiveOperationException ex) { throw failure("calling " + method, ex); }
    }
    private static IllegalStateException failure(String operation, ReflectiveOperationException ex) {
        Throwable cause = ex instanceof InvocationTargetException invocation ? invocation.getCause() : ex;
        return new IllegalStateException("Installed EnderStorage API is incompatible while " + operation, cause);
    }
}
