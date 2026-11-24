package cc.thonly.reverie_dreams.registry.content.item;

import cc.thonly.reverie_dreams.registry.delegate.ItemDelegate;
import net.minecraft.world.item.Item;
import java.util.List;

public final class RDWorkshopItems {
    public static final ItemDelegate DRIED_HERBS=RDItems.registerItem("dried_herbs",Item::new,new Item.Properties());
    public static final ItemDelegate HERBAL_POWDER=RDItems.registerItem("herbal_powder",Item::new,new Item.Properties());
    public static final ItemDelegate EMA=register("ema");
    public static final ItemDelegate OFUDA=register("ofuda");
    public static final ItemDelegate OMAMORI=register("omamori");
    public static final List<ItemDelegate> ALL=List.of(DRIED_HERBS,HERBAL_POWDER,EMA,OFUDA,OMAMORI);
    public static final List<ItemDelegate> DECORATIONS=List.of(EMA,OFUDA,OMAMORI);
    private static ItemDelegate register(String name){return RDItems.registerSimpleItem(name,Item::new,new Item.Properties());}
    public static void initialize(){}
    private RDWorkshopItems(){}
}
