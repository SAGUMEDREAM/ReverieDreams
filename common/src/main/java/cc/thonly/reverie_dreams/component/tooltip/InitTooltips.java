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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class InitTooltips {
    public static void bootstrap() {
        Event<ItemStackTooltipCallback> event = getEvent();
        registerTooltip(InitTooltips::appendDanmakuTooltip);
        registerTooltip(InitTooltips::appendDanmakuShapeTooltip);
        registerTooltip(InitTooltips::appendDrinkItemTooltip);
        registerTooltip(InitTooltips::appendTagFoodTooltip);
        registerTooltip(InitTooltips::appendFumoLicenseTooltip);
        registerTooltip(InitTooltips::appendRoleCardItemTooltip);
        registerTooltip(InitTooltips::appendDanmakuPropertiesTooltip);
        registerTooltip(InitTooltips::appendFastRecipeBookTooltip);
    }

    public static void appendDanmakuTooltip(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        if (!(itemStack.getItem() instanceof AbstractDanmakuItem abstractDanmakuItem)) {
            return;
        }
        DanmakuProperties properties = itemStack.get(RDDataComponents.DANMAKU_PROPERTIES.value());
        if (properties != null) {
            consumer.accept(Component.empty().append(Component.translatable("item.tooltip.damage")).append(String.valueOf(properties.damage())));
            consumer.accept(Component.empty().append(Component.translatable("item.tooltip.speed")).append(String.valueOf(properties.speed())));
            consumer.accept(Component.empty().append(Component.translatable("item.tooltip.count")).append(String.valueOf(properties.count())));
            consumer.accept(Component.empty().append(Component.translatable("item.tooltip.base_type")).append(Component.translatable(properties.templateId().toLanguageKey())));
        }
    }

    public static void appendDanmakuShapeTooltip(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        if (!(itemStack.getItem() instanceof DanmakuShapeCreatorItem danmakuShapeCreatorItem)) {
            return;
        }
        IngredientStack ingredientStack = itemStack.getOrDefault(RDDataComponents.DANMAKU_SHAPE.value(), IngredientStack.of(Items.AIR));
        ItemStack lazyStack = ingredientStack.getLazyStack();
        consumer.accept(Component.empty().append(Component.translatable("item.tooltip.shape")).append(lazyStack.getHoverName()));

    }

    public static void appendDrinkItemTooltip(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        if (!(itemStack.has(RDDataComponents.DRINK_ITEM_TYPE.value()))) {
            return;
        }
        List<DrinkProperty> allProperties = DrinkProperties.get(itemStack);
        if (!allProperties.isEmpty()) {
            consumer.accept(Component.empty().append(Component.translatable("item.tooltip.food_properties")));
            for (DrinkProperty property : allProperties) {
                consumer.accept(Component.empty().append("§b+").append(Component.translatable(property.translateKey())));
            }
        }
    }

    public static void appendTagFoodTooltip(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
//            if (!(itemStack.has(RDDataComponents.FOOD_ITEM_TYPE.value()) || itemStack.has(RDDataComponents.INGREDIENT_ITEM_TYPE.value()))) {
//                return;
//            }
        Collection<FoodProperty> foodProperties = FoodProperties.get(itemStack);
        if (!foodProperties.isEmpty()) {
            consumer.accept(Component.empty().append(Component.translatable("item.tooltip.food_properties")));
            for (FoodProperty foodProperty : foodProperties) {
                consumer.accept(Component.empty().append(FoodProperty.getDisplayPrefix(itemStack, foodProperty)).append(foodProperty.getTooltip()));
            }
        }
    }

    public static void appendFumoLicenseTooltip(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        if (!(itemStack.getItem() instanceof FumoLicenseItem)) {
            return;
        }
        consumer.accept(Component.translatable("item.tooltip.use.villager"));
    }

    public static void appendRoleCardItemTooltip(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        if (!(itemStack.getItem() instanceof RoleCardItem roleCardItem)) {
            return;
        }
        Optional<RoleCard> roleCardComponent = roleCardItem.getRoleCardComponent(itemStack);
        if (roleCardComponent.isEmpty()) {
            consumer.accept(Component.translatable("item.disabled"));
            return;
        }
        if (roleCardComponent.get().isEmpty()) {
            consumer.accept(Component.translatable("item.disabled"));
            return;
        }
        consumer.accept(Component.translatable("item.tooltip.use"));
    }

    public static void appendDanmakuPropertiesTooltip(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        if (!(itemStack.getItem() instanceof SpellCardTemplateItem spellCardTemplateItem)) {
            return;
        }
        DanmakuProperties properties = itemStack.get(RDDataComponents.DANMAKU_PROPERTIES.value());
        if (properties != null) {
            consumer.accept(Component.empty().append(Component.translatable("item.tooltip.base_type")).append(Component.translatable(properties.templateId().toLanguageKey())));
        }
    }

    public static void appendFastRecipeBookTooltip(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        if (!itemStack.has(RDDataComponents.RECIPE_MEMORY.value())) {
            return;
        }
        KitchenRecipe.IdEntry recipeIdEntry = itemStack.get(RDDataComponents.RECIPE_MEMORY.value());
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
            component.append(" | %ss".formatted(recipe.getCostTime()));
            consumer.accept(component);
        });
    }

    public static void registerTooltip(ItemStackTooltipCallback callback) {
        Event<ItemStackTooltipCallback> event = getEvent();
        event.register((itemStack, tooltipContext, tooltipDisplay, player, consumer, tooltipFlag) -> {
            invokeBypassShowOnly(callback, itemStack, tooltipContext, tooltipDisplay, player, consumer, tooltipFlag);
        });
    }

    public static boolean isShowOnly(ItemStack itemStack) {
        return itemStack.has(RDDataComponents.SHOW_ONLY.value());
    }

    private static void invokeBypassShowOnly(ItemStackTooltipCallback callback, ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        if (isShowOnly(itemStack)) {
            return;
        }
        callback.appendTooltip(itemStack, context, display, player, consumer, flag);
    }

    public static Event<ItemStackTooltipCallback> getEvent() {
        return ItemStackTooltipCallback.EVENT;
    }
}
