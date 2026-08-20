package cc.thonly.reverie_dreams.registry;

import cc.thonly.reverie_dreams.registry.content.ItemColor;
import cc.thonly.reverie_dreams.registry.impl.BiRegistryProvider;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.Map;

public class BuiltInBiRegistryProviders {
    public static final Map<Identifier, BiRegistryProvider<?, ?>> ROOT = new Object2ObjectLinkedOpenHashMap<>();
    public static final BiRegistryProvider<Item, ItemColor> ITEM_COLOR = BiRegistryProvider.createRegister(Identifier.parse("item_color"), Item.class, ItemColor.class);
    
    public static void bootstrap() {

    }

    public static <K, V> V register(BiRegistryProvider<K, V> registry, K key, V value) {
        registry.add(key, value);
        return value;
    }

}
