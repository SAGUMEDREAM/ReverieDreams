package cc.thonly.reverie_dreams.util.item;

import cc.thonly.reverie_dreams.mixin.accessor.DataComponentInitializersAccessor;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class DataComponentInitializersAccess {
    @SuppressWarnings("unchecked")
    public static <T> void modifyEntry(ResourceKey<T> key, Consumer<ComponentModifier> modifier) {
        DataComponentInitializers init = getInit();
        DataComponentInitializersAccessor accessor = (DataComponentInitializersAccessor) init;

        List<DataComponentInitializers.InitializerEntry<?>> list = accessor.getInitializers();

        for (int i = 0; i < list.size(); i++) {
            DataComponentInitializers.InitializerEntry<?> entry = list.get(i);

            if (Objects.equals(entry.key(), key)) {

                DataComponentInitializers.Initializer<T> original =
                        (DataComponentInitializers.Initializer<T>) entry.initializer();

                List<DataComponentInitializers.Initializer<T>> additions = new ArrayList<>();

                modifier.accept(new ComponentModifier() {
                    @Override
                    public <ComponentType> void set(DataComponentType<ComponentType> type, ComponentType value) {
                        additions.add((builder, ctx, k) -> builder.set(type, value));
                    }
                });

                DataComponentInitializers.Initializer<T> combined = original;
                for (var add : additions) {
                    combined = combined.andThen(add);
                }

                list.set(i, new DataComponentInitializers.InitializerEntry<>(key, combined));

                return;
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public static <T> Optional<DataComponentInitializers.Initializer<T>> accessInitializerEntry(ResourceKey<T> item) {
        DataComponentInitializers init = getInit();
        DataComponentInitializersAccessor accessor = (DataComponentInitializersAccessor) init;
        List<DataComponentInitializers.InitializerEntry<?>> list = accessor.getInitializers();
        for (DataComponentInitializers.InitializerEntry<?> entry : list) {
            if (Objects.equals(entry.key(), item)) {
                return Optional.of((DataComponentInitializers.Initializer<T>) entry.initializer());
            }
        }
        return Optional.empty();
    }

    public static Component getNameByNonEmpty(ItemLike itemLike) {
        Item item = itemLike.asItem();
        return item.getName(item.getDefaultInstance());
    }

    public static <Entity extends net.minecraft.world.entity.Entity> ResourceKey<EntityType<?>> getEntityTypeId(EntityType<Entity> type) {
        return BuiltInRegistries.ENTITY_TYPE.getResourceKey(type).orElseGet(() -> ResourceKey.create(Registries.ENTITY_TYPE, BuiltInRegistries.ENTITY_TYPE.getDefaultKey()));
    }

    public static ResourceKey<Block> getBlockId(Block block) {
        return BuiltInRegistries.BLOCK.getResourceKey(block).orElseGet(() -> ResourceKey.create(Registries.BLOCK, BuiltInRegistries.BLOCK.getDefaultKey()));
    }

    public static ResourceKey<Item> getItemId(Item item) {
        return BuiltInRegistries.ITEM.getResourceKey(item).orElseGet(() -> ResourceKey.create(Registries.ITEM, BuiltInRegistries.ITEM.getDefaultKey()));
    }

    public static DataComponentInitializers getInit() {
        return BuiltInRegistries.DATA_COMPONENT_INITIALIZERS;
    }

    @FunctionalInterface
    public interface ComponentModifier {
        <ComponentType> void set(DataComponentType<ComponentType> type, ComponentType value);
    }
}
