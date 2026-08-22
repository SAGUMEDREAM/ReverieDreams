package cc.thonly.reverie_dreams.item;

import cc.thonly.reverie_dreams.api.item.NonPersistentAdditionalData;
import cc.thonly.reverie_dreams.util.LazySupplier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;


@Slf4j
@SuppressWarnings({"LombokSetterMayBeUsed", "LombokGetterMayBeUsed", "deprecation", "OptionalAssignedToNull", "ConstantValue", "rawtypes", "unchecked"})
@ToString
public class IngredientStack implements ItemLike, DataComponentGetter, ItemInstance, TypedInstance<Item>, NonPersistentAdditionalData {
    public static final Codec<Holder<Item>> ITEM_CODEC = Codec.lazyInitialized(() -> Identifier.CODEC.xmap(
            itemId -> {
                return BuiltInRegistries.ITEM.getValue(itemId).builtInRegistryHolder();
            },
            typeHolder -> {
                Item item = typeHolder.value();
                return BuiltInRegistries.ITEM.getKey(item);
            }
    ));
    public static final MapCodec<IngredientStack> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ITEM_CODEC.fieldOf("id").forGetter(IngredientStack::typeHolder),
            Codec.INT.optionalFieldOf("count", 0).forGetter(IngredientStack::getCount),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                                    .forGetter(IngredientStack::getComponents)
    ).apply(instance, (item, count, components) -> {
        IngredientStack stack = new IngredientStack(item, count, components);
        return stack;
    }));
    public static final Codec<IngredientStack> CODEC = MAP_CODEC.codec();
    public static final Codec<List<IngredientStack>> LIST_CODEC = CODEC.listOf();
    public static final StreamCodec<RegistryFriendlyByteBuf, IngredientStack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ITEM),
            IngredientStack::typeHolder,

            ByteBufCodecs.VAR_INT,
            IngredientStack::getCount,

            DataComponentPatch.STREAM_CODEC,
            IngredientStack::getComponents,

            (item, count, components) -> {
                IngredientStack stack = new IngredientStack(item, count, components);
                return stack;
            }
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, List<IngredientStack>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());
    public static final StreamCodec<RegistryFriendlyByteBuf, IngredientStack> TRUSTED_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(CODEC);
    public static final EntityDataSerializer<IngredientStack> SERIALIZER = EntityDataSerializer.forValueType(TRUSTED_STREAM_CODEC);
    public static final EntityDataSerializer<List<IngredientStack>> LIST_SERIALIZER = EntityDataSerializer.forValueType(LIST_STREAM_CODEC);

    private Holder<Item> item;
    private int count;
    private DataComponentPatch components;
    private final LazySupplier<ItemStack> lazyStack = LazySupplier.of(this::build);

    private volatile Map<String, Integer> reverie_dreams$keys = new HashMap<>();
    private volatile Object[] reverie_dreams$values = new Object[0];


    public IngredientStack() {
        this.item = Items.AIR.builtInRegistryHolder();
        this.count = 0;
        this.components = DataComponentPatch.EMPTY;
    }

    public IngredientStack(ItemStack itemStack) {
        this(itemStack.getItem(), itemStack.getCount(), itemStack.components.asPatch());
    }

    public IngredientStack(ItemStackTemplate template) {
        this(template.item(), template.count(), template.components());
    }

    public IngredientStack(Item item) {
        this(item.builtInRegistryHolder(), 1, DataComponentPatch.builder().build());
    }

    public IngredientStack(Item item, int count) {
        this(item.builtInRegistryHolder(), count, DataComponentPatch.builder().build());
    }

    public IngredientStack(Item item, int count, DataComponentPatch components) {
        this(item.builtInRegistryHolder(), count, components);
    }

    public IngredientStack(Holder<Item> item) {
        this(item, 1, DataComponentPatch.builder().build());
    }

    public IngredientStack(Holder<Item> item, int count) {
        this(item, count, DataComponentPatch.builder().build());
    }

    public IngredientStack(Holder<Item> item, int count, DataComponentPatch components) {
        this.item = item;
        this.count = count;
        this.components = components;
    }

    public static IngredientStack empty() {
        return new IngredientStack();
    }

    public static IngredientStack of(ItemStackTemplate template) {
        return new IngredientStack(template.item(), template.count(), template.components());
    }

    public static IngredientStack of(ItemLike item) {
        return of(item.asItem());
    }

    public static IngredientStack of(Item item) {
        return new IngredientStack(item);
    }

    public static IngredientStack of(ItemLike item, int amount) {
        return new IngredientStack(item.asItem(), amount);
    }

    public static IngredientStack of(Item item, int amount) {
        return new IngredientStack(item, amount);
    }

    public static IngredientStack of(Item item, int amount, DataComponentPatch components) {
        return new IngredientStack(BuiltInRegistries.ITEM.wrapAsHolder(item), amount, components);
    }

    public static IngredientStack of(ItemStack itemStack) {
        return new IngredientStack(itemStack);
    }

    private synchronized void reverie_dreams$updateNonPersistentAdditionalDataKey(String name) {
        if (!this.reverie_dreams$keys.containsKey(name)) {
            int index = this.reverie_dreams$keys.size();

            this.reverie_dreams$keys.put(name, index);

            this.reverie_dreams$resizeNonPersistentAdditionalDataKey();
        }
    }

    private synchronized void reverie_dreams$resizeNonPersistentAdditionalDataKey() {
        int size = this.reverie_dreams$keys.size();

        if (this.reverie_dreams$values.length < size) {
            Object[] newValues = new Object[size];

            System.arraycopy(
                    this.reverie_dreams$values,
                    0,
                    newValues,
                    0,
                    this.reverie_dreams$values.length
            );

            this.reverie_dreams$values = newValues;
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public synchronized <Type> @Nullable Type reverie_dreams$getNonPersistentAdditionalData(String name) {
        Integer idx = this.reverie_dreams$keys.get(name);

        if (idx == null) {
            return null;
        }

        if (idx >= this.reverie_dreams$values.length) {
            return null;
        }

        return (Type) this.reverie_dreams$values[idx];
    }


    @Override
    public synchronized <Type> @Nullable Type reverie_dreams$getNonPersistentAdditionalData(
            String name,
            Class<Type> type
    ) {
        Object value = this.reverie_dreams$getNonPersistentAdditionalData(name);

        if (value == null) {
            return null;
        }

        if (!type.isInstance(value)) {
            return null;
        }

        return type.cast(value);
    }


    @Override
    public synchronized <Type> void reverie_dreams$setNonPersistentAdditionalData(
            String name,
            Type data
    ) {

        this.reverie_dreams$updateNonPersistentAdditionalDataKey(name);

        Integer idx = this.reverie_dreams$keys.get(name);

        this.reverie_dreams$values[idx] = data;
    }

    public boolean is(IngredientStack stack) {
        return this.equals(stack);
    }

    @Override
    public boolean is(TagKey<Item> tag) {
        return this.item.value().builtInRegistryHolder().is(tag);
    }

    @Override
    public boolean is(Holder<Item> type) {
        return Objects.equals(this.item, type);
    }

    @Override
    public boolean is(Item rawType) {
        return Objects.equals(this.item.value(), rawType);
    }

    @Override
    public boolean is(HolderSet<Item> set) {
        return set.contains(this.item);
    }

    @Override
    public boolean is(ResourceKey<Item> type) {
        Optional<ResourceKey<Item>> resourceKey = BuiltInRegistries.ITEM.getResourceKey(this.item.value());
        if (resourceKey.isEmpty()) {
            return false;
        }
        ResourceKey<Item> itemResourceKey = resourceKey.get();
        return Objects.equals(itemResourceKey, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof IngredientStack other))
            return false;
        if (this.item != other.item)
            return false;
        if (this.count != other.count)
            return false;
        if (this.isEmpty() && other.isEmpty())
            return true;
        return Objects.equals(this.components, other.components) || this.hashCode() == other.hashCode() || super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.item,
                this.components,
                this.count
        );
    }

    public boolean areComponentsBound() {
        return this.item.areComponentsBound();
    }

    public void setItem(Item item) {
        this.item = item.builtInRegistryHolder();
    }

    public void setItem(Holder<Item> item) {
        this.item = item;
    }

    public Item getItem() {
        return this.item.value();
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return this.count;
    }

    public void setComponents(DataComponentPatch.Builder builder) {
        this.components = builder.build();
    }

    public IngredientStack apply(DataComponentPatch patch) {
        if (patch.isEmpty()) {
            return this.copy();
        }
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : this.components().entrySet()) {
            DataComponentType key = entry.getKey();
            Optional optional = entry.getValue();
            if (optional.isPresent()) {
                Object value = optional.get();
                builder.set(key, value);
            }
        }
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : patch.entrySet()) {
            DataComponentType key = entry.getKey();
            Optional optional = entry.getValue();
            if (optional.isPresent()) {
                Object value = optional.get();
                builder.set(key, value);
            }
        }
        return new IngredientStack(this.item, this.count, builder.build());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> void set(DataComponentType<T> type, T value) {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : this.components.entrySet()) {
            DataComponentType key = entry.getKey();
            Optional optional = entry.getValue();
            if (optional.isEmpty()) {
                continue;
            }
            Object val = optional.get();
            builder.set(key, val);
        }
        builder.set(type, value);
        this.components = builder.build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(DataComponentType<? extends T> type) {
        @Nullable Optional<T> optional = (Optional<T>) this.components.map.get(type);
        if (optional == null) {
            return null;
        }
        return optional.orElse(null);
    }

    public <T> T getOrCreate(DataComponentType<T> type, T value) {
        T val = this.get(type);
        if (val != null) {
            return val;
        }
        this.set(type, value);
        return value;
    }

    public <T> boolean hasComponent(DataComponentType<T> type) {
        return this.get(type) == null;
    }

    public IngredientStack copy() {
        IngredientStack stack = new IngredientStack(this.item, this.count, this.components);
        return stack;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public IngredientStack clone() {
        return this.copy();
    }

    public boolean isEmpty() {
        return this.item.is(Items.AIR.builtInRegistryHolder()) && this.count <= 0;
    }

    public DataComponentPatch getComponents() {
        return this.components;
    }

    @Override
    public Item asItem() {
        return this.item.value();
    }

    @Override
    public Holder<Item> typeHolder() {
        return this.item;
    }

    public ItemStackTemplate buildTemplate() {
        return new ItemStackTemplate(this.item, this.count, this.components);
    }

    private ItemStack getItemStack() {
        return this.buildTemplate().create();
    }

    public ItemStack build() {
        return new ItemStack(this.item, this.count, this.components);
    }

    public ItemStack getLazyStack() {
        return this.lazyStack.get();
    }

    @Override
    public int count() {
        return this.count;
    }

    public DataComponentPatch components() {
        return this.components;
    }

    public Stream<TagKey<Item>> tags() {
        if (this.item.value().builtInRegistryHolder().tags == null) {
            return Stream.empty();
        }
        return this.item.value().builtInRegistryHolder().tags();
    }

    public static Tag encodeArray(RegistryAccess access, List<IngredientStack> stacks) {
        DataResult<Tag> result = LIST_CODEC.encodeStart(
                access.createSerializationContext(NbtOps.INSTANCE),
                stacks
        );

        return result.result().orElseGet(() -> {
            result.error().ifPresent(err -> log.warn("Encode failed: {}", err.message()));
            return new ListTag();
        });
    }

    public static List<IngredientStack> decodeArray(RegistryAccess access, CompoundTag tag) {
        DataResult<List<IngredientStack>> result = LIST_CODEC.parse(
                access.createSerializationContext(NbtOps.INSTANCE),
                tag
        );

        return result.result().orElseGet(() -> {
            result.error().ifPresent(err -> log.warn("Decode failed: {}", err.message()));
            return List.of();
        });
    }

    public static CompoundTag encode(RegistryAccess access, IngredientStack stack) {
        DataResult<Tag> result = CODEC.encodeStart(
                access.createSerializationContext(NbtOps.INSTANCE),
                stack
        );

        return result.resultOrPartial(error -> {
                         throw new RuntimeException("Failed to encode IngredientStack: " + error);
                     }).map(tag -> (CompoundTag) tag)
                     .orElseGet(CompoundTag::new);
    }

    public static IngredientStack decode(RegistryAccess access, CompoundTag tag) {
        DataResult<IngredientStack> result = CODEC.parse(
                access.createSerializationContext(NbtOps.INSTANCE),
                tag
        );

        return result.resultOrPartial(error -> {
            throw new RuntimeException("Failed to decode IngredientStack: " + error);
        }).orElseGet(IngredientStack::new);
    }

    public static IngredientStack findEquivalentKey(
            Map<IngredientStack, ?> map,
            IngredientStack key
    ) {
        ItemComparatorView keyView = ItemComparatorView.of(key);

        for (IngredientStack candidate : map.keySet()) {
            if (ItemComparatorView.of(candidate).test(keyView)) {
                return candidate;
            }
        }
        return key;
    }

}
