package cc.thonly.reverie_dreams.neoforge.impl;

import cc.thonly.reverie_dreams.api.loot.ILootTableModificationContextExtension;
import cc.thonly.reverie_dreams.neoforge.mixin.accessor.LootPoolAccessor;
import dev.architectury.event.events.common.LootEvent;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * NeoForge 侧的“修改已有战利品池”实现。
 *
 * <p>Architectury 的 NeoForge 实现会在加载期暴露原始 {@code LootTable} 与它的 {@code pools} 列表；
 * {@link LootPool} 本身的内容字段不可直接修改，因此这里用 {@link LootPoolAccessor}
 * 读出原池的字段，把新增条目拼接进去后，通过私有构造器重建一个池并替换回列表。
 */
@Slf4j
public class NeoTableModifier implements ILootTableModificationContextExtension.TableModifier {
    private static final LootPoolConstructor LOOT_POOL_CONSTRUCTOR = new LootPoolConstructor();

    private static final class FieldsCache {
        private final Field tableField;
        private final Field poolsField;

        private FieldsCache(Field tableField, Field poolsField) {
            this.tableField = tableField;
            this.poolsField = poolsField;
        }

        private static final ConcurrentHashMap<Class<?>, FieldsCache> CACHE = new ConcurrentHashMap<>();

        private static FieldsCache resolve(Class<?> clazz) {
            return CACHE.computeIfAbsent(clazz, type -> {
                try {
                    Field tableField = findField(type, "table");
                    Field poolsField = findField(type, "pools");
                    tableField.setAccessible(true);
                    poolsField.setAccessible(true);
                    return new FieldsCache(tableField, poolsField);
                } catch (ReflectiveOperationException exception) {
                    throw new IllegalStateException("Failed to resolve LootTableModificationContext fields: " + type.getName(), exception);
                }
            });
        }

        private static Field findField(@NotNull Class<?> clazz, String name) throws NoSuchFieldException {
            Class<?> current = clazz;
            while (current != null) {
                try {
                    return current.getDeclaredField(name);
                } catch (NoSuchFieldException ignored) {
                    current = current.getSuperclass();
                }
            }
            throw new NoSuchFieldException(clazz.getName() + "#" + name);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void modifyPool(ILootTableModificationContextExtension extension, Consumer<LootPool.Builder> modifier) {
        LootEvent.LootTableModificationContext context = extension.getAccessor().get();
        FieldsCache cache = FieldsCache.resolve(context.getClass());
        try {
            List<LootPool> pools = (List<LootPool>) cache.poolsField.get(context);
            for (int i = 0; i < pools.size(); i++) {
                pools.set(i, appendEntries(pools.get(i), modifier));
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to modify loot pools of " + context, exception);
        }
    }

    private static LootPool appendEntries(LootPool pool, Consumer<LootPool.Builder> modifier) throws ReflectiveOperationException {
        // 先用同一份修改逻辑构建出“新增条目”池，再把它拼进原池
        LootPool.Builder extraBuilder = LootPool.lootPool();
        modifier.accept(extraBuilder);
        LootPool extra = extraBuilder.build();

        LootPoolAccessor original = (LootPoolAccessor) pool;
        LootPoolAccessor addition = (LootPoolAccessor) extra;

        List<LootPoolEntryContainer> entries = new ArrayList<>(original.fabric_getEntries());
        entries.addAll(addition.fabric_getEntries());

        return LOOT_POOL_CONSTRUCTOR.create(
                entries,
                original.fabric_getConditions(),
                original.fabric_getFunctions(),
                original.fabric_getRolls(),
                original.fabric_getBonusRolls(),
                Optional.ofNullable(original.reverie_dreams$getName())
        );
    }

    /**
     * 缓存 {@link LootPool} 的私有构造器（字段顺序在映射间可能变化，因此按参数个数定位）。
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final class LootPoolConstructor {
        private Constructor<LootPool> constructor;

        @SuppressWarnings("unchecked")
        private LootPoolConstructor() {
            for (Constructor<?> candidate : LootPool.class.getDeclaredConstructors()) {
                if (candidate.getParameterCount() == 6) {
                    candidate.setAccessible(true);
                    this.constructor = (Constructor<LootPool>) candidate;
                    return;
                }
            }
            throw new IllegalStateException("LootPool constructor with 6 parameters not found");
        }

        private LootPool create(List<LootPoolEntryContainer> entries,
                                List<LootItemCondition> conditions,
                                List<LootItemFunction> functions,
                                NumberProvider rolls,
                                NumberProvider bonusRolls,
                                Optional<String> name) throws ReflectiveOperationException {
            return this.constructor.newInstance(entries, conditions, functions, rolls, bonusRolls, name);
        }
    }
}
