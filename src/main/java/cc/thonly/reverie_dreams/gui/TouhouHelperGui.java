package cc.thonly.reverie_dreams.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TouhouHelperGui extends SimpleGui implements GuiCommon {
    public static final List<Map<Item, List<Text>>> PAGES = new ArrayList<>();

    public TouhouHelperGui(ServerPlayerEntity player, boolean manipulatePlayerSlots) {
        super(ScreenHandlerType.GENERIC_9X6, player, manipulatePlayerSlots);
        this.init();
    }

    @Override
    public void init() {

    }
}
