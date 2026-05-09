package cc.thonly.reverie_dreams.recipe;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Slf4j
public class RecipeItemTag {
    private static final Map<Identifier, RecipeItemTag> INSTANCES = new ConcurrentHashMap<>();

    @Getter
    private final Identifier recipeTagId;

    private final Set<Item> entries = new HashSet<>();
    private final Set<Identifier> preparingItemIdentifiers = new HashSet<>();

    private Set<Item> cachedResolvedEntries = null;

    private RecipeItemTag(Identifier recipeTagId) {
        this.recipeTagId = recipeTagId;
    }

    public synchronized RecipeItemTag addItem(Item... items) {
        Collections.addAll(entries, items);
        this.invalidateCache();
        return this;
    }

    public synchronized RecipeItemTag removeItem(Item... items) {
        for (Item item : items) {
            entries.remove(item);
        }
        this.invalidateCache();
        return this;
    }

    public synchronized RecipeItemTag addItemIdentifier(Item... items) {
        for (Item item : items) {
            this.preparingItemIdentifiers.add(BuiltInRegistries.ITEM.getKey(item));
        }
        this.invalidateCache();
        return this;
    }

    public synchronized RecipeItemTag addItemIdentifier(Identifier... items) {
        Collections.addAll(preparingItemIdentifiers, items);
        this.invalidateCache();
        return this;
    }

    public synchronized RecipeItemTag addItemIdentifier(String... items) {
        for (String s : items) {
            this.preparingItemIdentifiers.add(Identifier.parse(s));
        }
        this.invalidateCache();
        return this;
    }

    public synchronized RecipeItemTag removeItemIdentifier(Identifier... items) {
        for (Identifier id : items) {
            this.preparingItemIdentifiers.remove(id);
        }
        this.invalidateCache();
        return this;
    }

    public synchronized RecipeItemTag addFromTagKey(RegistryAccess registryManager, TagKey<Item> itemTagKey) {
        Optional<Registry<Item>> optionalRegistry = registryManager.lookup(Registries.ITEM);
        if (optionalRegistry.isPresent()) {
            Registry<Item> registry = optionalRegistry.get();
            for (Holder<Item> itemEntry : registry.getTagOrEmpty(itemTagKey)) {
                this.addItemIdentifier(BuiltInRegistries.ITEM.getKey(itemEntry.value()));
            }
            this.invalidateCache();
        } else {
            log.error("Can't read item tag id {} ", itemTagKey.location());
        }
        return this;
    }

    public void forEach(Consumer<? super Item> action) {
        this.getEntries().forEach(action);
    }

    public Stream<Item> stream() {
        return this.getEntries().stream();
    }

    public List<Item> asList() {
        return new ArrayList<>(this.getEntries());
    }

    public synchronized Set<Item> getEntries() {
        if (this.cachedResolvedEntries == null) {
            Set<Item> result = new HashSet<>(this.entries);
            for (Identifier id : this.preparingItemIdentifiers) {
                Item item = BuiltInRegistries.ITEM.getValue(id);
                if (item != Items.AIR) {
                    result.add(item);
                }
            }
            this.cachedResolvedEntries = result;
        }
        return this.cachedResolvedEntries;
    }

    private void invalidateCache() {
        this.cachedResolvedEntries = null;
    }

    public static RecipeItemTag of(Identifier recipeTagId) {
        return INSTANCES.computeIfAbsent(recipeTagId, RecipeItemTag::new);
    }

    public static RecipeItemTag of(ResourceKey<Item> registryKey) {
        return of(registryKey.identifier());
    }
}
