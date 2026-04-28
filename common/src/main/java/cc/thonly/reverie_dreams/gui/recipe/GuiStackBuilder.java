package cc.thonly.reverie_dreams.gui.recipe;

import cc.thonly.reverie_dreams.gui.BasePageGui;

@FunctionalInterface
public interface GuiStackBuilder {
    void apply(BasePageGui gui, int slotIndex);
}