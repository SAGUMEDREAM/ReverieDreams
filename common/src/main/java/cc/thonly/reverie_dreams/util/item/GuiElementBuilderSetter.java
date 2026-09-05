package cc.thonly.reverie_dreams.util.item;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import cc.thonly.keine.item.ItemStackTemplate;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"rawtypes", "unchecked"})
public class GuiElementBuilderSetter {
    public static void setter(GuiElementBuilder builder, Item item) {
        builder.setItem(item);
    }

    public static void setter(GuiElementBuilder builder, ItemStack itemStack) {
        builder.setItem(itemStack.getItem());
        builder.setCount(itemStack.getCount());
        for (TypedDataComponent<?> component : itemStack.components) {
            DataComponentType type = component.type();
            Object value = component.value();
            builder.setComponent(type, value);
        }
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void setter(GuiElementBuilder builder, ItemStackTemplate template) {
        builder.setItem(template.item().value());
        builder.setCount(template.count());
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : template.components().entrySet()) {
            DataComponentType type = entry.getKey();
            Optional optional = entry.getValue();
            if (optional.isEmpty()) {
                continue;
            }
            builder.setComponent(type, optional.get());
        }
    }
}
