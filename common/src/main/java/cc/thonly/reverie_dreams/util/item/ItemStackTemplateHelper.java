package cc.thonly.reverie_dreams.util.item;

import cc.thonly.reverie_dreams.mixin.accessor.ItemStackTemplateAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
public class ItemStackTemplateHelper {
    public static ItemStackTemplate create(Item item) {
        return ItemUtils.createUnsafeTemplate(item);
    }

    public static ItemStackTemplate create(Item item, int count) {
        return ItemUtils.createUnsafeTemplate(item, count);
    }

    public static ItemStackTemplate create(Item item, int count, DataComponentPatch components) {
        return ItemUtils.createUnsafeTemplate(item.builtInRegistryHolder(), count, components);
    }

    public static ItemStackTemplate create(Item item, BiConsumer<ItemStackTemplate, Modifier> consumer) {
        ItemStackTemplate template = ItemUtils.createUnsafeTemplate(item);
        consumer.accept(template, new ModifierImpl(template));
        return template;
    }

    public static ItemStackTemplate create(Item item, int count, BiConsumer<ItemStackTemplate, Modifier> consumer) {
        ItemStackTemplate template = ItemUtils.createUnsafeTemplate(item, count);
        consumer.accept(template, new ModifierImpl(template));
        return template;
    }

    public static ItemStackTemplate create(Item item, int count, DataComponentPatch components, BiConsumer<ItemStackTemplate, Modifier> consumer) {
        ItemStackTemplate template = ItemUtils.createUnsafeTemplate(item.builtInRegistryHolder(), count, components);
        consumer.accept(template, new ModifierImpl(template));
        return template;
    }

    public static void modify(ItemStackTemplate template, BiConsumer<ItemStackTemplate, Modifier> consumer) {
        consumer.accept(template, new ModifierImpl(template));
    }

    public static class ModifierImpl implements Modifier {
        private final ItemStackTemplate template;

        public ModifierImpl(ItemStackTemplate template) {
            this.template = template;
        }

        @Override
        public <T> void set(DataComponentType<T> type, T object) {
            ItemStackTemplateAccessor accessor = (ItemStackTemplateAccessor) (Object) template;
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
            ItemStackTemplateAccessor accessor = (ItemStackTemplateAccessor) (Object) this.template;
            if (accessor != null) {
                accessor.reverie_dreams$setCount(count);
            }
        }

        @Override
        public void replace(DataComponentPatch.Builder builder) {
            ItemStackTemplateAccessor accessor = (ItemStackTemplateAccessor) (Object) this.template;
            if (accessor != null) {
                accessor.reverie_dreams$setComponents(builder.build());
            }
        }

        @Override
        public void replace(DataComponentPatch patch) {
            ItemStackTemplateAccessor accessor = (ItemStackTemplateAccessor) (Object) this.template;
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
            ItemEnchantments oldEnchantments = this.template.get(componentType);
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
