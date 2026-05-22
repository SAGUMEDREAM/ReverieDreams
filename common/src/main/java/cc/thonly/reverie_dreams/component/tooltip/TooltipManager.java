package cc.thonly.reverie_dreams.component.tooltip;

import cc.thonly.reverie_dreams.api.item.ItemStackTooltipCallback;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.DrinkProperty;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.item.base.RoleCard;
import cc.thonly.reverie_dreams.item.danmaku.AbstractDanmakuItem;
import cc.thonly.reverie_dreams.item.prop.FumoLicenseItem;
import cc.thonly.reverie_dreams.item.template.DanmakuShapeCreatorItem;
import cc.thonly.reverie_dreams.item.template.RoleCardItem;
import cc.thonly.reverie_dreams.item.template.SpellCardTemplateItem;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.registry.content.DrinkProperties;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import net.blay09.mods.balm.platform.event.Event;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

public class TooltipManager {
    public static void bootstrap() {
        Event<ItemStackTooltipCallback> event = getEvent();
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
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
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
            if (!(stack.getItem() instanceof DanmakuShapeCreatorItem danmakuShapeCreatorItem)) {
                return;
            }
            ItemStackWrapper itemStackWrapper = stack.getOrDefault(RDDataComponents.DANMAKU_SHAPE.value(), ItemStackWrapper.of(Items.AIR));
            ItemStack itemStack = itemStackWrapper.getItemStack();
            textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.shape")).append(itemStack.getHoverName()));
        });
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
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
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
//            if (!(stack.has(RDDataComponents.FOOD_ITEM_TYPE.value()) || stack.has(RDDataComponents.INGREDIENT_ITEM_TYPE.value()))) {
//                return;
//            }
            Collection<FoodProperty> foodProperties = FoodProperties.get(stack);
            if (!foodProperties.isEmpty()) {
                textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.food_properties")));
                for (FoodProperty foodProperty : foodProperties) {
                    textConsumer.accept(Component.empty().append(FoodProperty.getDisplayPrefix(stack, foodProperty)).append(foodProperty.getTooltip()));
                }
            }
        });
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
            if (!(stack.getItem() instanceof FumoLicenseItem)) {
                return;
            }
            textConsumer.accept(Component.translatable("item.tooltip.use.villager"));
        });
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
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
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
            if (!(stack.getItem() instanceof SpellCardTemplateItem spellCardTemplateItem)) {
                return;
            }
            DanmakuProperties properties = stack.get(RDDataComponents.DANMAKU_PROPERTIES.value());
            if (properties != null) {
                textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.base_type")).append(Component.translatable(properties.templateId().toLanguageKey())));
            }
        });
    }

    public static Event<ItemStackTooltipCallback> getEvent() {
        return ItemStackTooltipCallback.EVENT;
    }
}
