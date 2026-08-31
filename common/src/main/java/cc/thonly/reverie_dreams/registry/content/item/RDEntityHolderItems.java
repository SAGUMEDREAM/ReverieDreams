package cc.thonly.reverie_dreams.registry.content.item;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.delegate.ItemDelegate;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RDEntityHolderItems {
    public static final List<ItemDelegate> HOLDERS = new ArrayList<>();
    public static final ItemDelegate YOUSEI_WINGS = register("holder/yousei_wing_holder", factory(), new Item.Properties());
    public static final ItemDelegate ICE_FAIRY_WINGS = register("holder/ice_fairy_wings", factory(), new Item.Properties());
    public static final ItemDelegate KNIFE_DISPLAY = register("holder/knife_display", props -> new Item(
            props.component(
                    RDDataComponentTypes.DANMAKU_PROPERTIES.value(),
                    DanmakuProperties.ofDefault()
                                     .withSpeed(0.5f)
                                     .withScale(0.8f))
                 .stacksTo(1).overrideDescription("Entity Holder")), new Item.Properties()
    );
    public static final ItemDelegate MAGIC_BROOM_DISPLAY = register("holder/magic_broom_display", factory(), new Item.Properties());

    public static void initialize() {

    }

    public static Function<Item.Properties, Item> factory() {
        return props -> new Item(props.stacksTo(1)
                                      .overrideDescription("Entity Holder"));
    }

    public static ItemDelegate register(String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        ItemDelegate item = RDItems.registerSimpleItem(name, factory, settings);
        HOLDERS.add(item);
        return item;
    }

}
