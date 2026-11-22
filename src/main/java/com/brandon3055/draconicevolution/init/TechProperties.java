package com.brandon3055.draconicevolution.init;

import com.brandon3055.brandonscore.api.TechLevel;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 05/05/2022
 */
public class TechProperties extends Item.Properties {

    @Deprecated
    private final TechLevel techLevel;
    // port-26.1.2: Item.Properties.components builder больше не публичный — ведём собственную карту патчей для copy()
    private final Map<DataComponentType<?>, Object> techComponents = new HashMap<>();

    public TechProperties(TechLevel techLevel) {
        this.techLevel = techLevel;
//        this.component(ItemData.TECH_LEVEL, techLevel);
    }

    @Override
    public <T> TechProperties component(DataComponentType<T> type, T value) {
        techComponents.put(type, value);
        super.component(type, value);
        return this;
    }

    @Override
    public Item.Properties durability(int p_41504_) {
        this.component(DataComponents.MAX_STACK_SIZE, 1);
        return this;
    }

    public TechProperties copy() {
        TechProperties copy = new TechProperties(techLevel);
        techComponents.forEach((type, value) -> copy.component((DataComponentType) type, value)); // port-26.1.2: components.addAll недоступен — переигрываем патчи
        return copy;
    }

    @Deprecated
    public TechLevel getTechLevel() {
        return techLevel;
    }
}
