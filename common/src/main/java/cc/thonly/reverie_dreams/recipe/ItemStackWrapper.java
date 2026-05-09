package cc.thonly.reverie_dreams.recipe;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.ItemLike;

import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings("MethodDoesntCallSuperMethod")
@Getter
@Setter
@ToString
public class ItemStackWrapper {
    public static final Gson GSON = new Gson();
    public static final ItemStackWrapper EMPTY = new ItemStackWrapper(ItemStack.EMPTY);
    public static final ItemStackWrapper ERROR = new ItemStackWrapper(createErrorItem());
    public static final Codec<Item> ITEM_CODEC = Codec.STRING.xmap(
            id -> {
                Identifier identifier = Identifier.tryParse(id);
                if (identifier == null) {
                    return Items.AIR;
                }
                return BuiltInRegistries.ITEM.getValue(identifier);
            },
            item -> BuiltInRegistries.ITEM.getKey(item).toString()
    );
    public static final Codec<TagKey<Item>> TAG_KEY_CODEC =
            Identifier.CODEC.xmap(
                    id -> TagKey.create(Registries.ITEM, id),
                    TagKey::location
            );
    public static final Codec<ItemStack> FLEXIBLE_ITEMSTACK_CODEC = Codec.lazyInitialized(() ->
            RecordCodecBuilder.create(instance -> instance.group(
                    ITEM_CODEC.fieldOf("id").forGetter(ItemStack::getItem),
                    Codec.INT.optionalFieldOf("count", 0).forGetter(ItemStack::getCount),
                    DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                            .forGetter(stack -> stack.components.asPatch())
            ).apply(instance, (item, count, components) -> {
                ItemStack stack = new ItemStack(item, count);
                stack.components.restorePatch(components);
                return stack;
            }))
    );
    public static final Codec<ItemStackWrapper> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ITEM_CODEC.fieldOf("id").forGetter(w -> w.itemStack.getItem()),
            Codec.INT.optionalFieldOf("count", 1).forGetter(w -> w.itemStack.getCount()),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                    .forGetter(w -> w.itemStack.components.asPatch()),
            TAG_KEY_CODEC.listOf()
                    .optionalFieldOf("tags", new ArrayList<>())
                    .forGetter(ItemStackWrapper::getTags)
    ).apply(instance, (item, count, components, tags) -> {
        ItemStack stack = new ItemStack(item, count);
        stack.components.restorePatch(components);

        return new ItemStackWrapper(stack, tags);
    }));
    public static final Codec<List<ItemStackWrapper>> LIST_CODEC = CODEC.listOf();
    public static final Codec<List<ItemStack>> ITEM_STACK_LIST_CODEC = ItemStackWrapper.FLEXIBLE_ITEMSTACK_CODEC.listOf();
    public static final StreamCodec<RegistryFriendlyByteBuf,ItemStackWrapper> TRUSTED_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(CODEC);
    public static final EntityDataSerializer<ItemStackWrapper> SERIALIZER = EntityDataSerializer.forValueType(TRUSTED_STREAM_CODEC);

    private final ItemStack itemStack;
    private final List<TagKey<Item>> tags;

    public ItemStackWrapper(ItemStack itemStack) {
        if (itemStack == null) {
            itemStack = ItemStack.EMPTY;
        }
        this.itemStack = itemStack;
        this.tags = new ArrayList<>();
    }

    public ItemStackWrapper(ItemStack itemStack, TagKey<Item> tags) {
        this(itemStack, List.of(tags));
    }

    public ItemStackWrapper(ItemStack itemStack, List<TagKey<Item>> tags) {
        if (itemStack == null) {
            itemStack = ItemStack.EMPTY;
        }
        this.itemStack = itemStack;
        this.tags = new ArrayList<>(tags);
    }

    @SuppressWarnings("deprecation")
    private List<TagKey<Item>> getTagKeys(ItemStack itemStack) {
        Item item = itemStack.getItem();
        Holder.Reference<Item> itemReference = item.builtInRegistryHolder();
        Set<TagKey<Item>> tagKeySet = itemReference.tags;
        if (tagKeySet == null) {
            return new ArrayList<>();
        }
        return tagKeySet.stream().toList();
    }

    public boolean isEmpty() {
        return (Objects.equals(this, EMPTY) || this.itemStack.isEmpty()) && this.tags.isEmpty();
    }

    public static ItemStackWrapper empty() {
        return EMPTY;
    }

    public static ItemStackWrapper error() {
        return ERROR;
    }

    public static ItemStackWrapper of(ItemStack itemStack) {
        return new ItemStackWrapper(itemStack);
    }

    public static ItemStackWrapper of(ItemLike item) {
        return of(item.asItem());
    }

    public static ItemStackWrapper of(ItemStack itemStack, List<TagKey<Item>> tagKey) {
        return new ItemStackWrapper(itemStack, tagKey);
    }

    public static ItemStackWrapper of(Item item) {
        return of(new ItemStack(item));
    }

    public static ItemStackWrapper of(Item item, List<TagKey<Item>> tagKey) {
        return of(new ItemStack(item), tagKey);
    }

    public static ItemStackWrapper of(ItemLike item, int amount) {
        return of(new ItemStack(item, amount));
    }

    public static ItemStackWrapper of(Item item, int amount) {
        return of(new ItemStack(item, amount));
    }

    public static ItemStackWrapper of(Item item, int amount, List<TagKey<Item>> tagKey) {
        return of(new ItemStack(item, amount), tagKey);
    }

    public static ItemStackWrapper of(Item item, int amount, DataComponentPatch components) {
        return of(new ItemStack(BuiltInRegistries.ITEM.wrapAsHolder(item), amount, components));
    }

    @SafeVarargs
    public static ItemStackWrapper of(Item item, int amount, DataComponentPatch components, TagKey<Item>... tagKey) {
        return of(new ItemStack(BuiltInRegistries.ITEM.wrapAsHolder(item), amount, components), Arrays.stream(tagKey).toList());
    }

    public static ItemStackWrapper of(TagKey<Item> tagKey) {
        return new ItemStackWrapper(ItemStack.EMPTY, tagKey);
    }

    public static ItemStackWrapper of(ItemStack itemStack, TagKey<Item> tagKey) {
        return new ItemStackWrapper(itemStack, tagKey);
    }

    public static ItemStackWrapper of(Item item, TagKey<Item> tagKey) {
        return new ItemStackWrapper(item.getDefaultInstance(), tagKey);
    }

    public static ItemStack createErrorItem() {
        ItemStack stack = Items.WHITE_DYE.getDefaultInstance();
        stack.set(DataComponents.ITEM_MODEL, BuiltInRegistries.ITEM.getKey(Items.BARRIER));
        stack.set(DataComponents.ITEM_NAME, Component.literal("§cError Item"));
        stack.set(DataComponents.LORE, new ItemLore(
                new ArrayList<>(List.of(Component.literal("§cThis item failed to be serialized")))
        ));
        return stack;
    }

    public ItemStackWrapper copy() {
        return this.clone();
    }

    public <T> T get(DataComponentType<T> type) {
        return this.itemStack.get(type);
    }

    public <T> T getOrCreate(DataComponentType<T> type, Supplier<T> supplier) {
        T val = this.get(type);
        if (val == null) {
            T newVal = supplier.get();
            this.itemStack.set(type, newVal);
            val = newVal;
        }
        return val;
    }

    public <T> T getOrDefault(DataComponentType<T> type, T value) {
        return this.itemStack.getOrDefault(type, value);
    }

    @Override
    public ItemStackWrapper clone() {
        return new ItemStackWrapper(this.itemStack.copy(), this.tags);
    }

    public Item getItem() {
        return this.itemStack.getItem();
    }

    public Integer getCount() {
        return this.itemStack.getCount();
    }

    public boolean test(ItemStack other) {
        return this.test(ItemStackWrapper.of(other));
    }

    @SuppressWarnings("deprecation")
    public boolean test(ItemStackWrapper other) {
        if (!ItemStack.isSameItem(this.itemStack, other.itemStack)) {
            for (TagKey<Item> tag : this.tags) {
                Holder.Reference<Item> itemReference = other.itemStack.getItem().builtInRegistryHolder();
                Set<TagKey<Item>> tags = itemReference.tags;
                if (tags != null && tags.contains(tag)) {
                    return true;
                }
            }
            return false;
        }

        if (!this.itemStack.getComponentsPatch().isEmpty()) {
            return this.isSameItemSameComponents(this.itemStack, other.itemStack);
        }
        return true;
    }

    public Boolean greaterThan(ItemStack other) {
        if (other == null || other.isEmpty()) {
            return false;
        }

        if (this.itemStack.isEmpty() && this.tags.isEmpty()) {
            return true;
        }

        if (!this.test(other)) {
            return false;
        }

        return other.getCount() >= this.itemStack.getCount();
    }

    private boolean isSameItemSameComponents(ItemStack a, ItemStack b) {
        if (!a.is(b.getItem())) {
            return false;
        } else {
            return (a.isEmpty() && b.isEmpty()) || Objects.equals(a.components.asPatch(), b.components.asPatch());
        }
    }

    public static ItemStackWrapper findEquivalentKey(Map<ItemStackWrapper, ?> map, ItemStackWrapper key) {
        for (ItemStackWrapper candidate : map.keySet()) {
            if (candidate.test(ItemStackWrapper.of(key.getItemStack()))) {
                return candidate;
            }
        }
        return key;
    }


    public boolean matchesAndSufficient(ItemStack other) {
        return this.greaterThan(other);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ItemStackWrapper other)) return false;

        return ItemStack.matches(this.itemStack, other.itemStack)
                && Objects.equals(this.tags, other.tags);
    }

    public boolean matches(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ItemStackWrapper other)) {
            return false;
        }
        if (ItemStack.matches(this.itemStack, other.itemStack)) {
            return true;
        }
        for (TagKey<Item> tagA : other.tags) {
            for (TagKey<Item> tagB : this.tags) {
                if (Objects.equals(tagA, tagB)) {
                    if (Objects.equals(this.itemStack.components, other.itemStack.components)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.itemStack.getItem(),
                this.itemStack.getComponents(),
                this.itemStack.getCount(),
                this.tags
        );
    }

    public ItemStack getOrThrow() {
        assert !this.itemStack.isEmpty();
        return Optional.of(this.itemStack).get();
    }

    public Optional<ItemStack> getOrNullable() {
        return this.itemStack.isEmpty() ? Optional.empty() : Optional.of(this.itemStack);
    }

    public static Optional<String> toJson(ItemStackWrapper wrapper, Class<?> empty) {
        return Optional.ofNullable(toJson(wrapper));
    }

    public static String toJson(ItemStackWrapper wrapper) {
        DataResult<JsonElement> dataResult = ItemStackWrapper.CODEC.encodeStart(JsonOps.INSTANCE, wrapper);
        Optional<JsonElement> result = dataResult.result();
        if (result.isPresent()) {
            JsonElement element = result.get();
            return GSON.toJson(element);
        }
        return null;
    }

    public static String toJsonArray(List<ItemStackWrapper> wrappers) {
        DataResult<JsonElement> dataResult = ItemStackWrapper.LIST_CODEC.encodeStart(JsonOps.INSTANCE, wrappers);
        Optional<JsonElement> result = dataResult.result();
        if (result.isPresent()) {
            JsonElement array = result.get();
            return GSON.toJson(array);
        }
        return null;
    }

    public static Optional<ItemStackWrapper> toWrapper(String json) {
        if (json == null || json.isEmpty()) {
            return Optional.empty();
        }
        JsonElement jsonElement = JsonParser.parseString(json);
        return toWrapper(jsonElement);
    }

    public static Optional<ItemStackWrapper> toWrapper(JsonElement element) {
        Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, element);
        DataResult<ItemStackWrapper> parse = ItemStackWrapper.CODEC.parse(input);
        return parse.result();
    }

    public static List<ItemStackWrapper> toWrappers(JsonArray array) {
        List<ItemStackWrapper> list = new ArrayList<>();
        for (JsonElement element : array) {
            toWrapper(element).map(list::add);
        }
        return list;
    }


}
