package cc.thonly.reverie_dreams.registry.content.item;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import net.blay09.mods.balm.world.item.BalmItemRegistrar;
import net.blay09.mods.balm.world.item.DeferredItem;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RDEntityHolderItems {
    public static final List<DeferredItem> HOLDERS = new ArrayList<>();
    public static DeferredItem YOUSEI_WINGS;
    public static DeferredItem KNIFE_DISPLAY;
    public static DeferredItem MAGIC_BROOM_DISPLAY;

    public static void initialize(BalmItemRegistrar registrar) {
        YOUSEI_WINGS = register(registrar, "holder/yousei_wing_holder", factory(), new Item.Properties());
        KNIFE_DISPLAY = register(registrar, "holder/knife_display", props->new Item(props.component(RDDataComponents.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault()
                .withSpeed(0.5f)
                .withScale(0.8f)
        ).stacksTo(1).overrideDescription("Entity Holder")), new Item.Properties());
        MAGIC_BROOM_DISPLAY = register(registrar, "holder/magic_broom_display", factory(), new Item.Properties());

    }

    public static Function<Item.Properties, Item> factory() {
        return props -> new Item(props.stacksTo(1)
                .overrideDescription("Entity Holder"));
    }

    public static DeferredItem register(BalmItemRegistrar registrar, String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        DeferredItem item = RDItems.registerSimpleItem(registrar, name, factory, settings);
        HOLDERS.add(item);
        return item;
    }

}
