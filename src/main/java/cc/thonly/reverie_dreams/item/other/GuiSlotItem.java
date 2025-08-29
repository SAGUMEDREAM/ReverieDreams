package cc.thonly.reverie_dreams.item.other;

import net.minecraft.item.Item;

public class GuiSlotItem extends Item {

    public GuiSlotItem(Settings settings) {
        super(settings.maxCount(1));
    }

}
