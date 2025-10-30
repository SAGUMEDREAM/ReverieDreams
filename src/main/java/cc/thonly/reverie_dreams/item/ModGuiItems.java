package cc.thonly.reverie_dreams.item;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.item.other.GuiSlotItem;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.TooltipDisplay;

public class ModGuiItems {
    public static final List<Item> GUI_ITEM_LIST = new ArrayList<>();
    public static final Item NEXT = registerItem("sgui/elements/next", GuiSlotItem::new);
    public static final Item PREV = registerItem("sgui/elements/prev", GuiSlotItem::new);
    public static final Item BACK = registerItem("sgui/elements/back_slot", GuiSlotItem::new);
    public static final Item EMPTY_SLOT = registerItem("sgui/elements/empty_slot", GuiSlotItem::new);
    public static final Item ENABLE = registerItem("sgui/elements/enable", GuiSlotItem::new);
    public static final Item DISABLE = registerItem("sgui/elements/disable", GuiSlotItem::new);
    public static final Item DONE = registerItem("sgui/elements/done", GuiSlotItem::new);
    public static final Item CLOSE = registerItem("sgui/elements/close", GuiSlotItem::new);
    public static final Item PROGRESS_TO_RESULT = registerItem("sgui/elements/progress_to_result", GuiSlotItem::new);
    public static final Item PROGRESS_TO_RESULT_REVERSE = registerItem("sgui/elements/progress_to_result_reverse", GuiSlotItem::new);
    public static final Item PROGRESS_TO_RESULT_UP = registerItem("sgui/elements/progress_to_result_up", GuiSlotItem::new);
    public static final Item PROGRESS_TO_RESULT_DOWN = registerItem("sgui/elements/progress_to_result_down", GuiSlotItem::new);

    public static void init() {

    }

    public static Item registerItem(String name, Function<Item.Properties, Item> factory) {
        Item.Properties itemSettings = createSlotItemSettings();
        Item item = factory.apply(itemSettings.setId(ModItems.keyOf(name)));
        Registry.register(BuiltInRegistries.ITEM, Touhou.id(name), item);
        GUI_ITEM_LIST.add(item);
        return item;
    }

    public static Item.Properties createSlotItemSettings() {
        return new Item.Properties()
                .stacksTo(1)
                .overrideDescription("")
                .component(DataComponents.ITEM_NAME, Component.nullToEmpty(""))
                .component(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(
                                true,
                                ReferenceSortedSets.emptySet()
                        )
                );
    }

    public static List<Item> getGuiItemList() {
        return List.copyOf(GUI_ITEM_LIST);
    }
}
