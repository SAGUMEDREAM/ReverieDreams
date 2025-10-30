package cc.thonly.reverie_dreams.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;

public class TouhouHelperGui extends SimpleGui implements GuiCommon {
    public static final List<Map<Item, List<Component>>> PAGES = new ArrayList<>();

    public TouhouHelperGui(ServerPlayer player, boolean manipulatePlayerSlots) {
        super(MenuType.GENERIC_9x6, player, manipulatePlayerSlots);
        this.init();
    }

    @Override
    public void init() {

    }
}
