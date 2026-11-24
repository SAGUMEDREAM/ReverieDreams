package cc.thonly.reverie_dreams.util.item;

import cc.thonly.reverie_dreams.registry.content.BeverageProperties;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import net.minecraft.world.item.ItemStack;

/**
 * 旧版本会把物品默认的食物/饮料属性自动写进 ItemStack 组件。
 * 这里在物品堆反序列化时把“与当前默认值完全相同”的冗余组件清掉。
 *
 * <p>用独立的 holder 类保存 ready 标记，避免在 ItemStack 构造早期
 * 触发 FoodProperties / RDItems 等注册类的静态初始化。</p>
 */
public final class ItemTagMigration {
    private static volatile boolean ready = false;

    private ItemTagMigration() {
    }

    public static void setReady() {
        ready = true;
    }

    public static void normalize(ItemStack stack) {
        if (!ready || stack.isEmpty()) {
            return;
        }
        if (!stack.has(RDDataComponentTypes.FOOD_PROPERTIES.value())
                && !stack.has(RDDataComponentTypes.BEVERAGE_PROPERTIES.value())) {
            return;
        }
        FoodProperties.normalize(stack);
        BeverageProperties.normalize(stack);
    }
}
