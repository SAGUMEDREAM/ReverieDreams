package cc.thonly.reverie_dreams.util.item;

import cc.thonly.reverie_dreams.inf.IItemStackTemplateModifier;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
public class ItemStackTemplateHelper {
    public static ItemStackTemplate create(Item item) {
        return new ItemStackTemplate(item);
    }

    public static ItemStackTemplate create(Item item, int count) {
        return new ItemStackTemplate(item, count);
    }

    public static ItemStackTemplate create(Item item, int count, DataComponentPatch components) {
        return new ItemStackTemplate(item.builtInRegistryHolder(), count, components);
    }

    public static ItemStackTemplate create(Item item, BiConsumer<ItemStackTemplate, Modifier> consumer) {
        ItemStackTemplate template = new ItemStackTemplate(item);
        consumer.accept(template, new ModifierImpl(template));
        return template;
    }

    public static ItemStackTemplate create(Item item, int count, BiConsumer<ItemStackTemplate, Modifier> consumer) {
        ItemStackTemplate template = new ItemStackTemplate(item, count);
        consumer.accept(template, new ModifierImpl(template));
        return template;
    }

    public static ItemStackTemplate create(Item item, int count, DataComponentPatch components, BiConsumer<ItemStackTemplate, Modifier> consumer) {
        ItemStackTemplate template = new ItemStackTemplate(item.builtInRegistryHolder(), count, components);
        consumer.accept(template, new ModifierImpl(template));
        return template;
    }

    public static void modify(ItemStackTemplate template, BiConsumer<ItemStackTemplate, Modifier> consumer) {
        consumer.accept(template, new ModifierImpl(template));
    }

    public static Component getHoverName(ItemStackTemplate template) {
        Component customName = getCustomName(template);
        return customName != null ? customName : getItemName(template);
    }

    public static Component getItemName(ItemStackTemplate template) {
        String translationKey = template.item()
                .unwrapKey()
                .map(key -> template.item().value().getDescriptionId())
                .orElse("item.unknown");
        Component result = translationKey.equals("item.unknown") ? CommonComponents.EMPTY : Component.translatable(translationKey);
        return getOrDefault(template, DataComponents.ITEM_NAME, result);
    }

    public static Component getCustomName(ItemStackTemplate template) {
        Component customName = get(template, DataComponents.CUSTOM_NAME);
        if (customName != null) {
            return customName;
        } else {
            WrittenBookContent content = get(template, DataComponents.WRITTEN_BOOK_CONTENT);
            if (content != null) {
                String title = content.title().raw();
                if (!StringUtil.isBlank(title)) {
                    return Component.literal(title);
                }
            }

            return null;
        }
    }

    public static <T> T get(ItemStackTemplate template, DataComponentType<T> type) {
        DataComponentPatch components = template.components();
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : components.entrySet()) {
            DataComponentType key = entry.getKey();
            Optional optional = entry.getValue();
            if (Objects.equals(key, type) || optional.isEmpty()) {
                continue;
            }
            return (T) optional.get();
        }
        return null;
    }

    public static <T> T getOrDefault(ItemStackTemplate template, DataComponentType<T> type, T defVal) {
        T val = get(template, type);
        return val != null ? val : defVal;
    }

    public static class ModifierImpl implements Modifier {
        private final ItemStackTemplate template;

        public ModifierImpl(ItemStackTemplate template) {
            this.template = template;
        }

        @Override
        public <T> void set(DataComponentType<T> type, T object) {
            IItemStackTemplateModifier accessor = IItemStackTemplateModifier.of(this.template);
            DataComponentPatch components = template.components();
            DataComponentPatch.Builder builder = DataComponentPatch.builder();
            for (Map.Entry<DataComponentType<?>, Optional<?>> entry : components.entrySet()) {
                DataComponentType key = entry.getKey();
                Optional optional = entry.getValue();
                if (optional.isEmpty()) {
                    continue;
                }
                builder.set(key, optional.get());
            }
            builder.set(type, object);
            accessor.reverie_dreams$setComponents(builder.build());
        }

        @Override
        public void setCount(int count) {
            IItemStackTemplateModifier accessor = IItemStackTemplateModifier.of(this.template);
            if (accessor != null) {
                accessor.reverie_dreams$setCount(count);
            }
        }

        @Override
        public void replace(DataComponentPatch.Builder builder) {
            IItemStackTemplateModifier accessor = IItemStackTemplateModifier.of(this.template);
            if (accessor != null) {
                accessor.reverie_dreams$setComponents(builder.build());
            }
        }

        @Override
        public void replace(DataComponentPatch patch) {
            IItemStackTemplateModifier accessor = IItemStackTemplateModifier.of(this.template);
            if (accessor != null) {
                accessor.reverie_dreams$setComponents(patch);
            }
        }

        @Override
        public void enchant(Holder<Enchantment> enchantment, int level) {
            this.updateEnchantments(enchantments -> enchantments.upgrade(enchantment, level));
        }

        @SuppressWarnings("UnusedReturnValue")
        public ItemEnchantments updateEnchantments(Consumer<ItemEnchantments.Mutable> consumer) {
            DataComponentType<ItemEnchantments> componentType = getComponentType(this.template);
            ItemEnchantments oldEnchantments = get(this.template, componentType);
            if (oldEnchantments == null) {
                return ItemEnchantments.EMPTY;
            } else {
                ItemEnchantments.Mutable mutableEnchantments = new ItemEnchantments.Mutable(oldEnchantments);
                consumer.accept(mutableEnchantments);
                ItemEnchantments newEnchantments = mutableEnchantments.toImmutable();
                this.set(componentType, newEnchantments);
                return newEnchantments;
            }
        }

        public static DataComponentType<ItemEnchantments> getComponentType(ItemStackTemplate itemStack) {
            return itemStack.is(Items.ENCHANTED_BOOK) ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
        }
    }

    public interface Modifier {
        <T> void set(DataComponentType<T> type, T object);

        void setCount(int count);

        void enchant(Holder<Enchantment> enchantment, int level);

        void replace(DataComponentPatch builder);

        void replace(DataComponentPatch.Builder builder);
    }

}
