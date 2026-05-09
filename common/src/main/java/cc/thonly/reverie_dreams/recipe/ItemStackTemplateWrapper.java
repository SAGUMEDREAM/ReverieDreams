package cc.thonly.reverie_dreams.recipe;

import cc.thonly.reverie_dreams.mixin.accessor.ItemStackTemplateAccessor;
import cc.thonly.reverie_dreams.util.item.ItemUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;

import java.util.*;
import java.util.function.Supplier;

@Getter
@Setter
@ToString
@Deprecated
public class ItemStackTemplateWrapper implements ItemWrapper {
    public static final Codec<ItemStackTemplate> ITEM_STACK_TEMPLATE_CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(
            instance -> instance.group(
                    ITEM_HOLDER_CODEC.fieldOf("id").forGetter(ItemStackTemplate::item),
                    Codec.INT.optionalFieldOf("count", 0).forGetter(ItemStackTemplate::count),
                    DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                            .forGetter(ItemStackTemplate::components)
            ).apply(instance, ItemUtils::createUnsafeTemplate))
    );
    public static final Codec<ItemStackTemplateWrapper> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
            ITEM_HOLDER_CODEC.fieldOf("id").forGetter(w -> w.template.item()),
            Codec.INT.optionalFieldOf("count", 1).forGetter(w -> w.template.count()),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                    .forGetter(w -> w.template.components()),
            TAG_KEY_CODEC.listOf()
                    .optionalFieldOf("tags", new ArrayList<>())
                    .forGetter(ItemStackTemplateWrapper::getTags)
    ).apply(instance, (item, count, components, tags) -> {
        ItemStackTemplate stack = new ItemStackTemplate(item, count, components);

        return new ItemStackTemplateWrapper(stack, tags);
    })));
    public static final Codec<List<ItemStackTemplateWrapper>> LIST_CODEC = Codec.lazyInitialized(CODEC::listOf);

    private final ItemStackTemplate template;
    private final List<TagKey<Item>> tags;

    public ItemStackTemplateWrapper(ItemStackTemplate template) {
        this(template, template.count(), new ArrayList<>());
    }

    public ItemStackTemplateWrapper(ItemStackTemplate template, TagKey<Item> tags) {
        this(template, new ArrayList<>(Collections.singletonList(tags)));
    }

    public ItemStackTemplateWrapper(ItemStackTemplate template, int count) {
        this(template.withCount(count));
    }

    public ItemStackTemplateWrapper(ItemStackTemplate template, List<TagKey<Item>> tags) {
        this.template = template;
        this.tags = tags;
    }

    public ItemStackTemplateWrapper(ItemStackTemplate template, int count, TagKey<Item> tags) {
        this.template = template.withCount(count);
        this.tags = new ArrayList<>(Collections.singletonList(tags));
    }

    public ItemStackTemplateWrapper(ItemStackTemplate template, int count, List<TagKey<Item>> tags) {
        this.template = template.withCount(count);
        this.tags = tags;
    }

    public static ItemStackTemplateWrapper of(ItemStackTemplate itemStack) {
        return new ItemStackTemplateWrapper(itemStack);
    }

    public static ItemStackTemplateWrapper of(ItemLike item) {
        return of(item.asItem());
    }

    public static ItemStackTemplateWrapper of(ItemStackTemplate itemStack, List<TagKey<Item>> tagKey) {
        return new ItemStackTemplateWrapper(itemStack, tagKey);
    }

    public static ItemStackTemplateWrapper of(Item item) {
        return of(new ItemStackTemplate(item));
    }

    public static ItemStackTemplateWrapper of(Item item, List<TagKey<Item>> tagKey) {
        return of(new ItemStackTemplate(item), tagKey);
    }

    public static ItemStackTemplateWrapper of(ItemLike item, int amount) {
        return of(new ItemStackTemplate(item.asItem(), amount));
    }

    public static ItemStackTemplateWrapper of(Item item, int amount) {
        return of(new ItemStackTemplate(item, amount));
    }

    public static ItemStackTemplateWrapper of(Item item, int amount, List<TagKey<Item>> tagKey) {
        return of(new ItemStackTemplate(item, amount), tagKey);
    }

    public static ItemStackTemplateWrapper of(Item item, int amount, DataComponentPatch components) {
        return of(new ItemStackTemplate(BuiltInRegistries.ITEM.wrapAsHolder(item), amount, components));
    }

    @SafeVarargs
    public static ItemStackTemplateWrapper of(Item item, int amount, DataComponentPatch components, TagKey<Item>... tagKey) {
        return of(new ItemStackTemplate(BuiltInRegistries.ITEM.wrapAsHolder(item), amount, components), Arrays.stream(tagKey).toList());
    }

    public static ItemStackWrapper of(ItemStack itemStack, TagKey<Item> tagKey) {
        return new ItemStackWrapper(itemStack, tagKey);
    }

    public ItemStackTemplateWrapper copy() {
        return this.clone();
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public ItemStackTemplateWrapper clone() {
        return new ItemStackTemplateWrapper(this.template, this.tags);
    }

    public ItemStack createStack() {
        return this.build().getItemStack();
    }

    public ItemStackWrapper build() {
        return new ItemStackWrapper(this.template.create(), this.tags);
    }

    @Override
    public Item getItem() {
        return this.template.item().value();
    }

    @Override
    public int getCount() {
        return this.template.count();
    }

    @Override
    public <T> T get(DataComponentType<T> type) {
        return this.template.get(type);
    }

    @Override
    public <T> T getOrCreate(DataComponentType<T> type, Supplier<T> supplier) {
        T val = this.get(type);
        if (val == null) {
            T newVal = supplier.get();
            this.set(type, newVal);
            val = newVal;
        }
        return val;
    }

    @Override
    public <T> T getOrDefault(DataComponentType<T> type, T value) {
        return this.template.getOrDefault(type, value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> T set(DataComponentType<T> type, T value) {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : this.template.components().entrySet()) {
            DataComponentType key = entry.getKey();
            Optional optional = entry.getValue();
            if (optional.isEmpty()) {
                continue;
            }
            builder.set(key, optional.get());
        }
        ItemStackTemplateAccessor accessor = (ItemStackTemplateAccessor) (Object) this.template;
        accessor.reverie_dreams$setComponents(builder.build());
        return value;
    }
}
