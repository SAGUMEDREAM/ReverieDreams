package cc.thonly.reverie_dreams.registry.content.item;

import cc.thonly.reverie_dreams.item.other.GuiSlotItem;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import net.blay09.mods.balm.world.item.BalmItemRegistrar;
import net.blay09.mods.balm.world.item.DeferredItem;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.ArrayList;
import java.util.List;

public class RDGuiItems {
    public static final List<Holder<Item>> GUI_ITEM_LIST = new ArrayList<>();
    public static DeferredItem NEXT;
    public static DeferredItem PREV;
    public static DeferredItem BACK;
    public static DeferredItem EMPTY_SLOT;
    public static DeferredItem ENABLE;
    public static DeferredItem DISABLE;
    public static DeferredItem DONE;
    public static DeferredItem CLOSE;
    public static DeferredItem PROGRESS_TO_RESULT;
    public static DeferredItem PROGRESS_TO_RESULT_REVERSE;
    public static DeferredItem PROGRESS_TO_RESULT_UP;
    public static DeferredItem PROGRESS_TO_RESULT_DOWN;

    public static void initialize(BalmItemRegistrar registrar) {
        NEXT = registerItem(registrar, "sgui/elements/next");
        PREV = registerItem(registrar, "sgui/elements/prev");
        BACK = registerItem(registrar, "sgui/elements/back_slot");
        EMPTY_SLOT = registerItem(registrar, "sgui/elements/empty_slot");
        ENABLE = registerItem(registrar, "sgui/elements/enable");
        DISABLE = registerItem(registrar, "sgui/elements/disable");
        DONE = registerItem(registrar, "sgui/elements/done");
        CLOSE = registerItem(registrar, "sgui/elements/close");
        PROGRESS_TO_RESULT = registerItem(registrar, "sgui/elements/progress_to_result");
        PROGRESS_TO_RESULT_REVERSE = registerItem(registrar, "sgui/elements/progress_to_result_reverse");
        PROGRESS_TO_RESULT_UP = registerItem(registrar, "sgui/elements/progress_to_result_up");
        PROGRESS_TO_RESULT_DOWN = registerItem(registrar, "sgui/elements/progress_to_result_down");
    }

    public static DeferredItem registerItem(BalmItemRegistrar registrar, String name) {
        DeferredItem item = registrar.register(name, GuiSlotItem::new).asDeferredItem();
        GUI_ITEM_LIST.add(item);
        return item;
    }

    public static List<Holder<Item>> getGuiItemList() {
        return List.copyOf(GUI_ITEM_LIST);
    }

}
