package cc.thonly.reverie_dreams.registry;

import cc.thonly.reverie_dreams.registry.content.ItemColor;
import cc.thonly.reverie_dreams.registry.impl.PairRegistryImpl;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.Map;

public class PairRegistryImpls {
    public static final Map<Identifier, PairRegistryImpl<?, ?>> ROOT = new Object2ObjectLinkedOpenHashMap<>();
    public static final PairRegistryImpl<Item, ItemColor> ITEM_COLOR = PairRegistryImpl.createRegister(Identifier.parse("item_color"), Item.class, ItemColor.class);
    
    public static void bootstrap() {

    }

    public static <K, V> V register(PairRegistryImpl<K, V> registry, K key, V value) {
        registry.add(key, value);
        return value;
    }

}
