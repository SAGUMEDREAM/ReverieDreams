package cc.thonly.reverie_dreams.item.other;

import cc.thonly.polymer.item.IBasicPolymerItem;
import net.minecraft.world.item.Item;

public class GuiSlotItem extends Item implements IBasicPolymerItem {

    public GuiSlotItem(Properties settings) {
        super(settings.stacksTo(1));
    }

}
