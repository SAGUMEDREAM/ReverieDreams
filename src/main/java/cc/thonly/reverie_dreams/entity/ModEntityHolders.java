package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.item.ModItems;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ModEntityHolders {
    public static final List<Item> HOLDERS = new ArrayList<>();
    public static final Item YOUSEI_WINGS = register("holder/yousei_wing_holder",Item::new,new Item.Settings().translationKey("Entity Holder"));
    public static final Item KNIFE_DISPLAY = register("holder/knife_display",Item::new,new Item.Settings().maxCount(1).component(ModDataComponentTypes.Danmaku.TEMPLATE, Touhou.id("single").toString()).component(ModDataComponentTypes.Danmaku.DAMAGE, 2.0f).component(ModDataComponentTypes.Danmaku.SPEED, 0.5f).component(ModDataComponentTypes.Danmaku.SCALE, 0.8f).component(ModDataComponentTypes.Danmaku.COUNT, 1).component(ModDataComponentTypes.Danmaku.TILE, false).component(ModDataComponentTypes.Danmaku.INFINITE, false).translationKey("Entity Holder"));
    public static final Item MAGIC_BROOM_DISPLAY = register("holder/magic_broom_display", Item::new,new Item.Settings().maxCount(1).translationKey("Entity Holder"));

    public static void registerHolders() {
    }

    public static Item register(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
        Item item = ModItems.registerSimpleItem(name, factory, settings);
        HOLDERS.add(item);
        return item;
    }

}
