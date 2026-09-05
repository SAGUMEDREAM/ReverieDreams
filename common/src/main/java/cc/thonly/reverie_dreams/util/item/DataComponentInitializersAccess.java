package cc.thonly.reverie_dreams.util.item;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.mixin.accessor.ItemAccessor;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
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
import java.util.function.Function;

@SuppressWarnings("rawtypes")
public class DataComponentInitializersAccess {
    private static final List<Consumer<Registry<?>>> TASKS = new ArrayList<>();
    static {
        ReverieDreams.COMMON_LATE_INIT.add(DataComponentInitializersAccess::apply);
    }

    private static void apply() {
        for (Consumer<Registry<?>> task : TASKS) {
            for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
                task.accept(registry);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void modifyEntry(
            ResourceKey<T> key,
            Consumer<ComponentModifier> modifier
    ) {
        TASKS.add(registry -> {
            T value = (T) registry.getValue((ResourceKey) key);

            if (!(value instanceof Item item)) {
                return;
            }

            ItemAccessor accessor = (ItemAccessor) item;

            modifier.accept(new ComponentModifier() {
                @Override
                public <ComponentType> void set(
                        DataComponentType<ComponentType> type,
                        ComponentType value
                ) {
                    DataComponentMap old = item.components;
                    DataComponentMap.Builder builder = DataComponentMap.builder();

                    builder.set(type, value);

                    accessor.reverie_dreams$setComponents(
                            DataComponentMap.composite(old, builder.build())
                    );
                }
            });
        });
    }

//    @SuppressWarnings({"unchecked"})
//    public static <T> Optional<DataComponentInitializers.Initializer<T>> accessInitializerEntry(ResourceKey<T> item) {
//        DataComponentInitializers init = getInit();
//        DataComponentInitializersAccessor accessor = (DataComponentInitializersAccessor) init;
//        List<DataComponentInitializers.InitializerEntry<?>> list = accessor.getInitializers();
//        for (DataComponentInitializers.InitializerEntry<?> entry : list) {
//            if (Objects.equals(entry.key(), item)) {
//                return Optional.of((DataComponentInitializers.Initializer<T>) entry.initializer());
//            }
//        }
//        return Optional.empty();
//    }

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

//    public static DataComponentInitializers getInit() {
//        return BuiltInRegistries.DATA_COMPONENT_INITIALIZERS;
//    }

    @FunctionalInterface
    public interface ComponentModifier {
        <ComponentType> void set(DataComponentType<ComponentType> type, ComponentType value);
    }
}
