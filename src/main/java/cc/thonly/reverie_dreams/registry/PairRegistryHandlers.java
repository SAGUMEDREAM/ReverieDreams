package cc.thonly.reverie_dreams.registry;

import cc.thonly.reverie_dreams.registry.content.ItemColor;
import cc.thonly.reverie_dreams.registry.impl.PairRegistryHandler;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class PairRegistryHandlers {
    public static final Map<ResourceLocation, PairRegistryHandler<?, ?>> ROOT = new Object2ObjectLinkedOpenHashMap<>();
    public static final PairRegistryHandler<Item, ItemColor> ITEM_COLOR = PairRegistryHandler.createRegister(ResourceLocation.parse("item_color"), Item.class, ItemColor.class);
    
    public static void bootstrap() {

    }

    public static <K, V> V register(PairRegistryHandler<K, V> registry, K key, V value) {
        registry.add(key, value);
        return value;
    }

}
