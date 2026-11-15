package com.brandon3055.draconicevolution.integration.emi;

import com.brandon3055.draconicevolution.api.DraconicAPI;
import com.brandon3055.draconicevolution.api.crafting.IFusionRecipe;
import com.brandon3055.draconicevolution.api.crafting.StackIngredient;
import com.brandon3055.draconicevolution.init.DEContent;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Fusion recipes come from EMI's synchronized recipe map, including server datapack recipes. */
@EmiEntrypoint
public final class DEFusionEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        var category = new EmiRecipeCategory(Identifier.fromNamespaceAndPath("draconicevolution", "fusion_crafting"),
                EmiStack.of(DEContent.CRAFTING_CORE.get()));
        registry.addCategory(category);
        registry.addWorkstation(category, EmiStack.of(DEContent.CRAFTING_CORE.get()));
        registry.addWorkstation(category, EmiStack.of(DEContent.BASIC_CRAFTING_INJECTOR.get()));
        registry.addWorkstation(category, EmiStack.of(DEContent.WYVERN_CRAFTING_INJECTOR.get()));
        registry.addWorkstation(category, EmiStack.of(DEContent.AWAKENED_CRAFTING_INJECTOR.get()));
        registry.addWorkstation(category, EmiStack.of(DEContent.CHAOTIC_CRAFTING_INJECTOR.get()));
        var level = Minecraft.getInstance().level;
        if (level == null) return; // EMI also initializes its categories outside a world.
        for (var holder : registry.getRecipeMap().byType(DraconicAPI.FUSION_RECIPE_TYPE.get())) {
            registry.addRecipe(new FusionDisplay(category, holder, level.registryAccess()));
        }
    }

    private static EmiIngredient ingredient(Ingredient ingredient, boolean consumed) {
        int count = ingredient.getCustomIngredient() instanceof StackIngredient stack ? stack.getCount() : 1;
        EmiIngredient display = EmiIngredient.of(ingredient, count);
        if (consumed) {
            for (var stack : display.getEmiStacks()) {
                var remainder = stack.getItemStack().getCraftingRemainder();
                if (remainder != null) stack.setRemainder(EmiStack.of(remainder.create()));
            }
        }
        return display;
    }

    private static final class FusionDisplay implements EmiRecipe {
        private final EmiRecipeCategory category;
        private final RecipeHolder<IFusionRecipe> holder;
        private final EmiIngredient catalyst;
        private final List<EmiIngredient> injectors;
        private final List<Boolean> consumed;
        private final List<EmiIngredient> inputs;
        private final List<EmiIngredient> catalysts;
        private final List<EmiStack> outputs;

        private FusionDisplay(EmiRecipeCategory category, RecipeHolder<IFusionRecipe> holder, HolderLookup.Provider registries) {
            this.category = category;
            this.holder = holder;
            var recipe = holder.value();
            catalyst = ingredient(recipe.getCatalyst(), true); // DE's core catalyst IS consumed.
            var injectorList = new ArrayList<EmiIngredient>();
            var consumeList = new ArrayList<Boolean>();
            var inputList = new ArrayList<EmiIngredient>();
            var reusableList = new ArrayList<EmiIngredient>();
            inputList.add(catalyst);
            for (var entry : recipe.fusionIngredients()) {
                var display = ingredient(entry.get(), entry.consume());
                injectorList.add(display);
                consumeList.add(entry.consume());
                (entry.consume() ? inputList : reusableList).add(display);
            }
            injectors = List.copyOf(injectorList);
            consumed = List.copyOf(consumeList);
            inputs = List.copyOf(inputList);
            catalysts = List.copyOf(reusableList);
            outputs = List.of(EmiStack.of(recipe.getResultItem(registries)));
        }

        @Override public EmiRecipeCategory getCategory() { return category; }
        @Override public Identifier getId() { return holder.id().identifier(); }
        @Override public RecipeHolder<?> getBackingRecipe() { return holder; }
        @Override public List<EmiIngredient> getInputs() { return inputs; }
        @Override public List<EmiIngredient> getCatalysts() { return catalysts; }
        @Override public List<EmiStack> getOutputs() { return outputs; }
        @Override public int getDisplayWidth() { return 160; }
        @Override public int getDisplayHeight() { return 32 + ((injectors.size() + 7) / 8) * 18; }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            widgets.addSlot(catalyst, 0, 0).appendTooltip(Component.translatable("emi.draconicevolution.core_catalyst"));
            widgets.addText(Component.translatable("gui.draconicevolution.fusion_craft.tier."
                    + holder.value().getRecipeTier().name().toLowerCase(Locale.ROOT)), 22, 0, 0x404040, false);
            widgets.addText(Component.literal(String.format(Locale.ROOT, "%,d OP", holder.value().getEnergyCost())),
                    22, 12, 0x404040, false);
            widgets.addTexture(EmiTexture.EMPTY_ARROW, 112, 1);
            widgets.addSlot(outputs.getFirst(), 142, 0).recipeContext(this);
            for (int i = 0; i < injectors.size(); i++) {
                var slot = widgets.addSlot(injectors.get(i), (i % 8) * 18, 32 + (i / 8) * 18);
                if (!consumed.get(i)) {
                    slot.catalyst(true).appendTooltip(Component.translatable("emi.draconicevolution.not_consumed"));
                }
            }
        }
    }
}
