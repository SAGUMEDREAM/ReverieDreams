package cc.thonly.reverie_dreams.api.registry.block;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry-key based lookup also accepts Architectury delegates and vanilla holders.
 */
@SuppressWarnings("deprecation")
public final class CuisineBlockBindings {
    public record Binding(Holder<Item> item, Holder<Block> block) {
    }

    private static final Map<ResourceKey<Item>, Binding> BINDINGS = new LinkedHashMap<>();

    private CuisineBlockBindings() {
    }

    public static void bind(Holder<Item> item, Holder<Block> block) {
        ResourceKey<Item> key = item.unwrapKey().orElseThrow(() -> new IllegalArgumentException("Item must have a registry key"));
        block.unwrapKey().orElseThrow(() -> new IllegalArgumentException("Block must have a registry key"));
        if (BINDINGS.putIfAbsent(key, new Binding(item, block)) != null) {
            throw new IllegalArgumentException("Duplicate cuisine binding: " + key.identifier());
        }
    }

    public static Optional<Holder<Block>> find(Holder<Item> item) {
        return item.unwrapKey().map(BINDINGS::get).map(Binding::block);
    }

    public static Optional<Holder<Block>> find(ItemStack stack) {
        return stack.isEmpty() ? Optional.empty() : find(stack.getItem().builtInRegistryHolder());
    }

    public static Collection<Binding> bindings() {
        return Collections.unmodifiableCollection(BINDINGS.values());
    }
}
