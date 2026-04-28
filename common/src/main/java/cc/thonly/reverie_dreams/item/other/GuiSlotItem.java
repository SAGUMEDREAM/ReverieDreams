package cc.thonly.reverie_dreams.item.other;

import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.TooltipDisplay;

public class GuiSlotItem extends Item {

    public GuiSlotItem(Properties settings) {
        super(settings.stacksTo(1)
                .overrideDescription("")
                .component(DataComponents.ITEM_NAME, Component.nullToEmpty(""))
                .component(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(
                                true,
                                ReferenceSortedSets.emptySet()
                        )
                ));
    }

}
