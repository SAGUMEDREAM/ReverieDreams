package cc.thonly.reverie_dreams.fabric.impl;

import cc.thonly.reverie_dreams.api.loot.ILootTableModificationContextExtension;
import dev.architectury.event.events.common.LootEvent;
import net.fabricmc.fabric.api.loot.v3.FabricLootTableBuilder;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import java.lang.reflect.Field;
import java.util.function.Consumer;

/**
 * Fabric 侧的“修改已有战利品池”实现。
 *
 * <p>Architectury 的 {@link LootEvent.LootTableModificationContext} 只暴露 {@code addPool}，
 * 但 Fabric API 的 {@link FabricLootTableBuilder#modifyPools(Consumer)} 可以真正修改已经存在的池。
 * 这里通过反射拿到 Architectury Fabric 实现里保存的 {@link LootTable.Builder}，再转交给 Fabric 的扩展接口。
 */
public class FabricTableModifier implements ILootTableModificationContextExtension.TableModifier {
    private static Field tableBuilderField;

    @Override
    public void modifyPool(ILootTableModificationContextExtension extension, Consumer<LootPool.Builder> modifier) {
        LootEvent.LootTableModificationContext context = extension.getAccessor().get();
        try {
            LootTable.Builder builder = (LootTable.Builder) tableBuilderField(context.getClass()).get(context);
            ((FabricLootTableBuilder) builder).modifyPools(modifier);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to access Architectury loot table builder", exception);
        }
    }

    private static Field tableBuilderField(Class<?> contextClass) throws NoSuchFieldException {
        if (tableBuilderField == null) {
            Field field = null;
            for (Class<?> current = contextClass; current != null && field == null; current = current.getSuperclass()) {
                for (Field candidate : current.getDeclaredFields()) {
                    if (candidate.getType() == LootTable.Builder.class) {
                        field = candidate;
                        break;
                    }
                }
            }
            if (field == null) {
                throw new NoSuchFieldException("LootTable.Builder field not found in " + contextClass.getName());
            }
            field.setAccessible(true);
            tableBuilderField = field;
        }
        return tableBuilderField;
    }
}
