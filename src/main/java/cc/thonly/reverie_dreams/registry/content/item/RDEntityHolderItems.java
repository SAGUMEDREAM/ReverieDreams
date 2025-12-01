package cc.thonly.reverie_dreams.registry.content.item;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RDEntityHolderItems {
    public static final List<Item> HOLDERS = new ArrayList<>();
    public static final Item YOUSEI_WINGS = register("holder/yousei_wing_holder", Item::new, new Item.Properties().overrideDescription("Entity Holder"));
    public static final Item KNIFE_DISPLAY = register("holder/knife_display", Item::new, new Item.Properties().stacksTo(1)
            .component(RDDataComponents.DANMAKU_PROPERTIES, DanmakuProperties.ofDefault()
                    .withSpeed(0.5f)
                    .withScale(0.8f)
            )
            .overrideDescription("Entity Holder"));
    public static final Item MAGIC_BROOM_DISPLAY = register("holder/magic_broom_display", Item::new, new Item.Properties().stacksTo(1).overrideDescription("Entity Holder"));

    public static void registerHolders() {
    }

    public static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        Item item = RDItems.registerSimpleItem(name, factory, settings);
        HOLDERS.add(item);
        return item;
    }

}
