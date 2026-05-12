package cc.thonly.reverie_dreams.component.tooltip;

import cc.thonly.keine.api.callback.ItemStackTooltipCallback;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.DrinkProperty;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.item.base.RoleCard;
import cc.thonly.reverie_dreams.item.danmaku.AbstractDanmakuItem;
import cc.thonly.reverie_dreams.item.prop.FumoLicenseItem;
import cc.thonly.reverie_dreams.item.template.DanmakuShapeCreatorItem;
import cc.thonly.reverie_dreams.item.template.RoleCardItem;
import cc.thonly.reverie_dreams.item.template.SpellCardTemplateItem;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.content.DrinkProperties;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import net.blay09.mods.balm.platform.event.Event;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class InitTooltips {
    public static void bootstrap() {
        Event<ItemStackTooltipCallback> event = getEvent();
        event.register((stack, context, displayComponent, player, textConsumer, tooltipFlag) -> {
            if (!(stack.getItem() instanceof AbstractDanmakuItem abstractDanmakuItem)) {
                return;
            }
            DanmakuProperties properties = stack.get(RDDataComponents.DANMAKU_PROPERTIES.value());
            if (properties != null) {
                textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.damage")).append(String.valueOf(properties.damage())));
                textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.speed")).append(String.valueOf(properties.speed())));
                textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.count")).append(String.valueOf(properties.count())));
                textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.base_type")).append(Component.translatable(properties.templateId().toLanguageKey())));
            }
        });
        event.register((stack, context, displayComponent, player, textConsumer, tooltipFlag) -> {
            if (!(stack.getItem() instanceof DanmakuShapeCreatorItem danmakuShapeCreatorItem)) {
                return;
            }
            IngredientStack ingredientStack = stack.getOrDefault(RDDataComponents.DANMAKU_SHAPE.value(), IngredientStack.of(Items.AIR));
            ItemStack itemStack = ingredientStack.getLazyStack();
            textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.shape")).append(itemStack.getHoverName()));
        });
        event.register((stack, context, displayComponent, player, textConsumer, tooltipFlag) -> {
            if (!(stack.has(RDDataComponents.DRINK_ITEM_TYPE.value()))) {
                return;
            }
            List<DrinkProperty> allProperties = DrinkProperties.get(stack);
            if (!allProperties.isEmpty()) {
                textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.food_properties")));
                for (DrinkProperty property : allProperties) {
                    textConsumer.accept(Component.empty().append("§b+").append(Component.translatable(property.translateKey())));
                }
            }
        });
        event.register((stack, context, displayComponent, player, textConsumer, tooltipFlag) -> {
            if (!(stack.has(RDDataComponents.FOOD_ITEM_TYPE.value()) || stack.has(RDDataComponents.INGREDIENT_ITEM_TYPE.value()))) {
                return;
            }
            Collection<FoodProperty> foodProperties = FoodProperties.get(stack);
            if (!foodProperties.isEmpty()) {
                textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.food_properties")));
                for (FoodProperty foodProperty : foodProperties) {
                    textConsumer.accept(Component.empty().append(FoodProperty.getDisplayPrefix(stack, foodProperty)).append(foodProperty.getTooltip()));
                }
            }
        });
        event.register((stack, context, displayComponent, player, textConsumer, tooltipFlag) -> {
            if (!(stack.getItem() instanceof FumoLicenseItem)) {
                return;
            }
            textConsumer.accept(Component.translatable("item.tooltip.use.villager"));
        });
        event.register((stack, context, displayComponent, player, textConsumer, tooltipFlag) -> {
            if (!(stack.getItem() instanceof RoleCardItem roleCardItem)) {
                return;
            }
            Optional<RoleCard> roleCardComponent = roleCardItem.getRoleCardComponent(stack);
            if (roleCardComponent.isEmpty()) {
                textConsumer.accept(Component.translatable("item.disabled"));
                return;
            }
            if (roleCardComponent.get().isEmpty()) {
                textConsumer.accept(Component.translatable("item.disabled"));
                return;
            }
            textConsumer.accept(Component.translatable("item.tooltip.use"));
        });
        event.register((stack, context, displayComponent, player, textConsumer, tooltipFlag) -> {
            if (!(stack.getItem() instanceof SpellCardTemplateItem spellCardTemplateItem)) {
                return;
            }
            DanmakuProperties properties = stack.get(RDDataComponents.DANMAKU_PROPERTIES.value());
            if (properties != null) {
                textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.base_type")).append(Component.translatable(properties.templateId().toLanguageKey())));
            }
        });
        event.register((stack, tooltipContext, tooltipDisplay, player, textConsumer, tooltipFlag) -> {
            if (!stack.has(RDDataComponents.RECIPE_MEMORY.value())) {
                return;
            }
            KitchenRecipe.IdEntry recipeIdEntry = stack.get(RDDataComponents.RECIPE_MEMORY.value());
            if (recipeIdEntry == null) {
                return;
            }
            recipeIdEntry.map((key, recipe) -> {
                MutableComponent component = Component.empty();
                component.append(Component.translatable(recipe.getTypeInstance().toTranslateKey()));
                component.append(" | ");
                ItemStack lazyStack = recipe.getOutput().getLazyStack();
                for (IngredientStack ingredient : recipe.getIngredients()) {
                    component.append(ingredient.getLazyStack().getHoverName()).append(" ");
                }
                component.append("-> ").append(lazyStack.getHoverName());
                textConsumer.accept(component);
            });
        });
    }

    public static Event<ItemStackTooltipCallback> getEvent() {
        return ItemStackTooltipCallback.EVENT;
    }
}
