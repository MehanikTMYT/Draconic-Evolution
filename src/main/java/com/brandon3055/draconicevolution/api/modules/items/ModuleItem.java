package com.brandon3055.draconicevolution.api.modules.items;

import com.brandon3055.draconicevolution.api.capability.DECapabilities;
import com.brandon3055.draconicevolution.api.capability.ModuleProvider;
import com.brandon3055.draconicevolution.api.modules.Module;
import com.brandon3055.draconicevolution.api.modules.data.ModuleData;
import com.brandon3055.draconicevolution.api.modules.lib.LimitedModuleContext;
import com.brandon3055.draconicevolution.api.modules.lib.ModuleEntity;
import com.brandon3055.draconicevolution.api.modules.lib.StackModuleContext;
import net.covers1624.quack.util.SneakyUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by covers1624 on 4/16/20.
 */
public class ModuleItem<P extends ModuleData<P>> extends Item implements ModuleProvider<P> {

    private Module<P> moduleCache = null;
    private Supplier<Module<P>> moduleSupplier;

    public ModuleItem(Properties properties, Supplier<Module<?>> moduleSupplier) {
        super(properties);
        this.moduleSupplier = SneakyUtils.unsafeCast(moduleSupplier);
    }

    public ModuleItem(Supplier<Module<?>> moduleSupplier) {
        super(new Properties());
        this.moduleSupplier = SneakyUtils.unsafeCast(moduleSupplier);
    }

    public static Module<?> getModule(ItemStack stack) {
        ModuleProvider<?> cap = stack.getCapability(DECapabilities.Module.ITEM);
        if (!stack.isEmpty() && cap != null) {
            return cap.getModule();
        }
        return null;
    }

    @Override
    public Module<P> getModule() {
        if (moduleCache == null) {
            moduleCache = moduleSupplier.get();
        }
        return moduleCache;
    }

    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag flagIn) { // port-26.1.2: @Override removed, signature now from Item appendHoverText(ItemStack,TooltipContext,TooltipDisplay,Consumer<T>,TooltipFlag)
        List<Component> tooltipList = new ArrayList<>();
        getModule().addInformation(tooltipList, new LimitedModuleContext(stack, null, context.level(), null));
        tooltipList.forEach(tooltip);
        ModuleEntity<?> entity = getModule().createEntity();
        if (context.level() != null) {
            entity.loadEntityFromStack(stack, new StackModuleContext(stack, null, null));
            List<Component> entityTooltip = new ArrayList<>();
            entity.addToolTip(entityTooltip);
            entityTooltip.forEach(tooltip);
        }
    }

//    @Override
//    public Rarity getRarity(ItemStack stack) {
//        return getModule().getProperties().getTechLevel().getRarity();
//    }
}
