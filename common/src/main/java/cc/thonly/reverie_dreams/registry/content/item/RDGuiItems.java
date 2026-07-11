package cc.thonly.reverie_dreams.registry.content.item;

import cc.thonly.reverie_dreams.item.other.GuiSlotItem;
import cc.thonly.reverie_dreams.registry.ReverieDreamsRegistries;
import cc.thonly.reverie_dreams.registry.impl.ItemDelegate;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class RDGuiItems {
    public static final List<Holder<Item>> GUI_ITEM_LIST = new ArrayList<>();
    public static final ItemDelegate EMPTY_ITEM = registerItem("sgui/elements/empty_item");
    public static final ItemDelegate NEXT = registerItem("sgui/elements/next");
    public static final ItemDelegate PREV = registerItem("sgui/elements/prev");
    public static final ItemDelegate BACK = registerItem("sgui/elements/back_slot");
    public static final ItemDelegate EMPTY_SLOT = registerItem("sgui/elements/empty_slot");
    public static final ItemDelegate HEAD_SLOT = registerItem("sgui/elements/head_slot");
    public static final ItemDelegate CHEST_SLOT = registerItem("sgui/elements/chest_slot");
    public static final ItemDelegate LEG_SLOT = registerItem("sgui/elements/leg_slot");
    public static final ItemDelegate FEET_SLOT = registerItem("sgui/elements/feet_slot");
    public static final ItemDelegate ENABLE = registerItem("sgui/elements/enable");
    public static final ItemDelegate DISABLE = registerItem("sgui/elements/disable");
    public static final ItemDelegate DONE = registerItem("sgui/elements/done");
    public static final ItemDelegate CLOSE = registerItem("sgui/elements/close");
    public static final ItemDelegate PROGRESS_TO_RESULT = registerItem("sgui/elements/progress_to_result");
    public static final ItemDelegate PROGRESS_TO_RESULT_REVERSE = registerItem("sgui/elements/progress_to_result_reverse");
    public static final ItemDelegate PROGRESS_TO_RESULT_UP = registerItem("sgui/elements/progress_to_result_up");
    public static final ItemDelegate PROGRESS_TO_RESULT_DOWN = registerItem("sgui/elements/progress_to_result_down");

    public static void initialize() {

    }

    public static ItemDelegate registerItem(String name) {
        RegistrySupplier<Item> item = ReverieDreamsRegistries.ITEM.register(name, () -> new GuiSlotItem(new Item.Properties().setId(RDItems.keyOf(name))));
        ItemDelegate itemDelegate = ItemDelegate.of(item);
        GUI_ITEM_LIST.add(itemDelegate);
        return itemDelegate;
    }

    public static List<Holder<Item>> getGuiItemList() {
        return List.copyOf(GUI_ITEM_LIST);
    }

}
