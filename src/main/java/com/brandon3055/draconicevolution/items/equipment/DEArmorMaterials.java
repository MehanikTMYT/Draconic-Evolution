package com.brandon3055.draconicevolution.items.equipment;

import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.init.EquipCfg;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.Item;
import net.minecraft.tags.ItemTags;

import java.util.EnumMap;
import java.util.Map;

/**
 * port-26.1.2: ArmorItem/ArmorMaterials-DIAMOND больше не применяются напрямую —
 * броня 26 = Item.Properties.humanoidArmor(ArmorMaterial, ArmorType) (эталон Items26).
 * DE-броня модульная: базовые статы DIAMOND-уровня, усиление через модули (DE-баланс сохранён).
 */
public class DEArmorMaterials {

    public static final ArmorMaterial MODULAR_CHESTPIECE = new ArmorMaterial(
        33, // Original ArmorMaterials.DIAMOND durability multiplier; TechProperties suppresses durability.
        defenseByType(ArmorType.CHESTPLATE, 8),
        10,
        SoundEvents.ARMOR_EQUIP_DIAMOND,
        2.0F,
        0.0F,
        ItemTags.REPAIRS_DIAMOND_ARMOR,
        ResourceKey.create(net.minecraft.world.item.equipment.EquipmentAssets.ROOT_ID, net.minecraft.resources.Identifier.fromNamespaceAndPath(DraconicEvolution.MODID, "modular_chestpiece")) // port-26.1.2: Registries.EQUIPMENT_ASSET отсутствует -> EquipmentAssets.ROOT_ID
    );

    private static Map<ArmorType, Integer> defenseByType(ArmorType type, int defense) {
        Map<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
        map.put(type, defense);
        return map;
    }

    /** Применяет humanoidArmor-свойства к переданным props (правило port-26.1.2: props-цепочка, как pickaxe/sword). */
    public static Item.Properties apply(ArmorMaterial material, Item.Properties props, ArmorType type) {
        return props.humanoidArmor(material, type);
    }
}
