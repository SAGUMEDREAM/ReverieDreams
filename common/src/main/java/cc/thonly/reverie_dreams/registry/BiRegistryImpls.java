package cc.thonly.reverie_dreams.registry;

import cc.thonly.reverie_dreams.registry.content.ItemColor;
import cc.thonly.reverie_dreams.registry.impl.BiRegistryImpl;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.Map;

public class BiRegistryImpls {
    public static final Map<Identifier, BiRegistryImpl<?, ?>> ROOT = new Object2ObjectLinkedOpenHashMap<>();
    public static final BiRegistryImpl<Item, ItemColor> ITEM_COLOR = BiRegistryImpl.createRegister(Identifier.parse("item_color"), Item.class, ItemColor.class);
    
    public static void bootstrap() {

    }

    public static <K, V> V register(BiRegistryImpl<K, V> registry, K key, V value) {
        registry.add(key, value);
        return value;
    }

}
