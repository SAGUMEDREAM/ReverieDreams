package cc.thonly.reverie_dreams.item;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.item.other.GuiSlotItem;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ModGuiItems {
    public static final List<Item> GUI_ITEM_LIST = new ArrayList<>();
    public static final Item NEXT = registerItem("sgui/elements/next", GuiSlotItem::new);
    public static final Item PREV = registerItem("sgui/elements/prev", GuiSlotItem::new);
    public static final Item BACK = registerItem("sgui/elements/back_slot", GuiSlotItem::new);
    public static final Item EMPTY_SLOT = registerItem("sgui/elements/empty_slot", GuiSlotItem::new);
    public static final Item PROGRESS_TO_RESULT = registerItem("sgui/elements/progress_to_result", GuiSlotItem::new);
    public static final Item PROGRESS_TO_RESULT_REVERSE = registerItem("sgui/elements/progress_to_result_reverse", GuiSlotItem::new);

    public static void init() {

    }

    public static Item registerItem(String name, Function<Item.Settings, Item> factory) {
        Item.Settings itemSettings = createSlotItemSettings();
        Item item = factory.apply(itemSettings.registryKey(ModItems.keyOf(name)));
        Registry.register(Registries.ITEM, Touhou.id(name), item);
        GUI_ITEM_LIST.add(item);
        return item;
    }

    public static Item.Settings createSlotItemSettings() {
        return new Item.Settings()
                .maxCount(1)
                .translationKey("")
                .component(DataComponentTypes.ITEM_NAME, Text.of(""))
                .component(DataComponentTypes.TOOLTIP_DISPLAY, new TooltipDisplayComponent(
                                true,
                                ReferenceSortedSets.emptySet()
                        )
                );
    }

    public static List<Item> getGuiItemList() {
        return List.copyOf(GUI_ITEM_LIST);
    }
}
