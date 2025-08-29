package cc.thonly.reverie_dreams.registry;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.Map;

public class Key2ValueRegistryManager {
    public static final Map<Identifier, Key2ValueRegister<?, ?>> ROOT = new Object2ObjectLinkedOpenHashMap<>();
    public static final Key2ValueRegister<Item, ItemColor> ITEM_COLOR = Key2ValueRegister.createRegister(Identifier.of("item_color"), Item.class, ItemColor.class);
    
    public static void bootstrap() {

    }

    public static <K, V> V register(Key2ValueRegister<K, V> registry, K key, V value) {
        registry.add(key, value);
        return value;
    }

}
