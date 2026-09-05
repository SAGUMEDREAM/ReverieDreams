package cc.thonly.reverie_dreams.item;

import cc.thonly.reverie_dreams.mixin.accessor.HolderSetDirectAccessor;
import cc.thonly.reverie_dreams.util.CodecMerger;
import cc.thonly.reverie_dreams.util.LazySupplier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import cc.thonly.keine.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings({"ALL","deprecation"})
public class IngredientStackSet implements Collection<IngredientStack>,
        Predicate<IngredientStack>,
        RandomAccess {
    public static final Codec<TagKey<Item>> TAG_KEY_CODEC = Codec.lazyInitialized(() -> Identifier.CODEC.xmap(
            id -> TagKey.create(Registries.ITEM, id),
            TagKey::location
    ));
    public static final List<Codec<IngredientStackSet>> CODEC_LIST = new ArrayList<>();
    public static final Codec<IngredientStackSet> SET_CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(
            instance -> instance.group(
                    IngredientStack.CODEC.listOf().optionalFieldOf("values", new ArrayList<>()).forGetter(IngredientStackSet::values)
            ).apply(instance, IngredientStackSet::new)
    ));
    public static final Codec<IngredientStackSet> TAG_SET_CODEC = Codec.lazyInitialized(() ->
            RecordCodecBuilder.create(instance -> instance.group(
                    Identifier.CODEC.listOf().fieldOf("tag").forGetter(stackSet -> {
                        List<Identifier> list = new ArrayList<>();
                        for (IngredientStack stack : stackSet.values) {
                            stack.tags().map(TagKey::location).forEach(list::add);
                        }
                        return list;
                    })
            ).apply(instance, (tags) -> {
                List<IngredientStack> stacks = new ArrayList<>();
                for (Identifier tagId : tags) {
                    Iterable<Holder<Item>> tagIterator = BuiltInRegistries.ITEM.getTagOrEmpty(TagKey.create(Registries.ITEM, tagId));
                    Iterator<Holder<Item>> iterator = tagIterator.iterator();
                    while (iterator.hasNext()) {
                        Holder<Item> next = iterator.next();
                        stacks.add(IngredientStack.of(next.value()));
                    }
                }
                return new IngredientStackSet(stacks);
            }))
    );

    static {
        CODEC_LIST.add(SET_CODEC);
        CODEC_LIST.add(TAG_SET_CODEC);
    }

    public static final Codec<IngredientStackSet> CODEC = CodecMerger.mergeLazyInitialized(LazySupplier.of(() -> {
        return CODEC_LIST;
    }));
    final List<IngredientStack> values;

    public IngredientStackSet() {
        this.values = Collections.synchronizedList(new ArrayList<>());
        this.dedupe();
    }

    public IngredientStackSet(Collection<IngredientStack> stacks) {
        this();
        this.values.addAll(stacks);
    }

    public IngredientStackSet(List<Item> stacks) {
        this();
        this.values.addAll(stacks.stream().map(IngredientStack::of).toList());
    }

    public IngredientStackSet(IngredientStack stack) {
        this(List.of(stack));
    }

    public IngredientStackSet(IngredientStack... stacks) {
        this(Arrays.stream(stacks).toList());
    }

    public IngredientStackSet(HolderSet<Item> tag) {
        this();
        tag.unwrap().ifRight((directValues) -> {
            for (Holder<Item> directValue : directValues) {
                this.values.add(IngredientStack.of(directValue.value()));
            }
        });
    }

    protected List<IngredientStack> values() {
        return this.values;
    }

    public IngredientStackSet dedupe() {
        List<IngredientStack> unique = new ArrayList<>(this.values.size());
        for (IngredientStack stack : this.values) {
            if (!unique.contains(stack)) {
                unique.add(stack);
            }
        }
        this.values.clear();
        this.values.addAll(unique);
        return this;
    }

    public IngredientStackSet removeEmpty() {
        Iterator<IngredientStack> iterator = this.values.iterator();
        while (iterator.hasNext()) {
            IngredientStack next = iterator.next();
            if (next.isEmpty()) {
                iterator.remove();
            }
        }
        return this;
    }

    public HolderSet<IngredientStack> toHolderSet() {
        return HolderSetDirectAccessor.invokeInit(this.values
                .stream()
                .filter(Objects::nonNull)
                .map(stack -> new Holder.Direct<>(stack))
                .map(direct -> (Holder<IngredientStack>) direct)
                .toList());
    }

    public static IngredientStackSet of(ItemLike itemLike) {
        return new IngredientStackSet(new IngredientStack(itemLike.asItem()));
    }

    public static IngredientStackSet of(ItemLike... items) {
        return of(Arrays.stream(items));
    }

    public static IngredientStackSet of(Stream<? extends ItemLike> stream) {
        return new IngredientStackSet(stream.map(ItemLike::asItem).toList());
    }

    public static IngredientStackSet of(HolderSet<Item> set) {
        return new IngredientStackSet(set);
    }

    @Override
    public boolean test(IngredientStack input) {
        for (IngredientStack stack : this.values) {
            if (stack.is(input)) {
                return true;
            }
        }
        return false;
    }

    public Stream<Item> items() {
        return this.values.stream().map(IngredientStack::asItem);
    }

    public Stream<Holder<Item>> holders() {
        return this.values.stream().map(IngredientStack::asItem).map(Item::builtInRegistryHolder);
    }

    public Stream<Holder.Reference<Item>> holderReferences() {
        return this.values.stream().map(IngredientStack::asItem).map(Item::builtInRegistryHolder);
    }

    public Stream<IngredientStack> ingredientStacks() {
        return this.values.stream();
    }

    public Stream<ItemStack> itemStacks() {
        return this.values.stream().map(IngredientStack::build);
    }

    public Stream<ItemStackTemplate> itemStackTemplates() {
        return this.values.stream().map(IngredientStack::buildTemplate);
    }

    @Override
    public int size() {
        return this.values.size();
    }

    @Override
    public boolean isEmpty() {
        return this.values.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.values.contains(o);
    }

    @Override
    public @NonNull Iterator<IngredientStack> iterator() {
        return this.values.iterator();
    }

    @Override
    public @NonNull Object[] toArray() {
        return this.values.toArray();
    }

    @Override
    public @NonNull <T> T[] toArray(@NonNull T[] a) {
        return this.values.toArray(a);
    }

    @Override
    public boolean add(IngredientStack stack) {
        if (this.values.contains(stack)) {
            return false;
        }
        return this.values.add(stack);
    }

    @Override
    public boolean remove(Object o) {
        return this.values.remove(o);
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        return new HashSet<>(this.values).containsAll(c);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends IngredientStack> c) {
        boolean modified = false;
        for (IngredientStack stack : c) {
            if (!this.values.contains(stack)) {
                this.values.add(stack);
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        return this.values.removeAll(c);
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        return this.values.retainAll(c);
    }

    @Override
    public void clear() {
        this.values.clear();
    }

    @Override
    public int hashCode() {
        return this.values.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IngredientStackSet other))
            return false;
        return this == obj
                || (this.hashCode() == other.hashCode())
                || this.values.equals(other.values)
                || super.equals(other);
    }

    public IngredientStackSet copy() {
        return new IngredientStackSet(this.values);
    }

    @Override
    public Object clone() {
        return new IngredientStackSet(this.values);
    }
}
