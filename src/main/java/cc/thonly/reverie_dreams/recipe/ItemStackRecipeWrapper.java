package cc.thonly.reverie_dreams.recipe;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

@SuppressWarnings("MethodDoesntCallSuperMethod")
@Getter
@ToString
public class ItemStackRecipeWrapper {
    public static final Gson GSON = new Gson();
    public static final Codec<Item> ITEM_CODEC_ALLOWING_AIR = Codec.STRING.xmap(
            id -> {
                Identifier identifier = Identifier.tryParse(id);
                if (identifier == null) {
                    return Items.AIR;
                }
                Item item = Registries.ITEM.get(identifier);
                if (item == null) {
                    return Items.AIR;
                }
                return item;
            },
            item -> Registries.ITEM.getId(item).toString()
    );
    public static final Codec<ItemStack> FLEXIBLE_ITEMSTACK_CODEC = Codec.lazyInitialized(() ->
            RecordCodecBuilder.create(instance -> instance.group(
                    ITEM_CODEC_ALLOWING_AIR.fieldOf("id").forGetter(ItemStack::getItem),
                    Codec.INT.optionalFieldOf("count", 0).forGetter(ItemStack::getCount),
                    ComponentChanges.CODEC.optionalFieldOf("components", ComponentChanges.EMPTY)
                            .forGetter(stack -> stack.components.getChanges())
            ).apply(instance, (item, count, components) -> {
                ItemStack stack = new ItemStack(item, count);
                stack.components.setChanges(components);
//                if (stack.isEmpty()) {
//                    stack = Items.WHITE_DYE.getDefaultStack();
//                    stack.set(DataComponentTypes.ITEM_MODEL, Registries.ITEM.getId(Items.BARRIER));
//                    stack.set(DataComponentTypes.ITEM_NAME, Text.literal("§cError Item"));
//                    stack.set(DataComponentTypes.LORE, new LoreComponent(new ArrayList<>(List.of(Text.literal("§cThis item failed to be serialized")))));
//                }
                return stack;
            }))
    );
    public static final Codec<List<ItemStack>> LIST_CODEC = ItemStackRecipeWrapper.FLEXIBLE_ITEMSTACK_CODEC.listOf();

    public static final Codec<ItemStackRecipeWrapper> CODEC =
            FLEXIBLE_ITEMSTACK_CODEC.xmap(ItemStackRecipeWrapper::new, ItemStackRecipeWrapper::getItemStack);
    public static final ItemStackRecipeWrapper EMPTY = new ItemStackRecipeWrapper(ItemStack.EMPTY);

    private final ItemStack itemStack;

    public ItemStackRecipeWrapper(ItemStack itemStack) {
        if (itemStack == null) {
            itemStack = ItemStack.EMPTY;
        }
        this.itemStack = itemStack;
    }

    public boolean isEmpty() {
        return this == EMPTY || this.itemStack.isEmpty();
    }

    public static ItemStackRecipeWrapper empty() {
        return EMPTY;
    }

    public static ItemStackRecipeWrapper of(ItemStack itemStack) {
        return new ItemStackRecipeWrapper(itemStack);
    }

    public static ItemStackRecipeWrapper of(Item item) {
        return of(new ItemStack(item));
    }

    public static ItemStackRecipeWrapper of(Item item, int amount) {
        return of(new ItemStack(item, amount));
    }

    public static ItemStackRecipeWrapper of(Item item, int amount, ComponentChanges components) {
        return of(new ItemStack(Registries.ITEM.getEntry(item), amount, components));
    }

    public ItemStackRecipeWrapper copy() {
        return this.clone();
    }

    @Override
    public ItemStackRecipeWrapper clone() {
        return new ItemStackRecipeWrapper(this.itemStack.copy());
    }

    public Item getItem() {
        return this.itemStack.getItem();
    }

    public Integer getCount() {
        return this.itemStack.getCount();
    }

    public Boolean test(ItemStack other) {
        return ItemStack.areEqual(this.itemStack, other);
    }

    public Boolean greaterThan(ItemStack other) {
        if (this.itemStack == other) {
            return true;
        }
        if (this.itemStack.isEmpty()) {
            return true;
        }
        if (this.itemStack.getItem() != other.getItem()) {
            return false;
        }

        return ItemStack.areItemsAndComponentsEqual(this.itemStack, other) && (other.getCount() >= this.itemStack.getCount());
    }

    public static ItemStackRecipeWrapper findEquivalentKey(Map<ItemStackRecipeWrapper, ?> map, ItemStackRecipeWrapper key) {
        for (ItemStackRecipeWrapper candidate : map.keySet()) {
            if (candidate.test(key.getItemStack())) {
                return candidate;
            }
        }
        return key;
    }


    public boolean matchesAndSufficient(ItemStack other) {
        if (other == null) return false;
        if (!other.isOf(itemStack.getItem())) return false;
        if (!Objects.equals(other.components, itemStack.components)) return false;
        return other.getCount() >= itemStack.getCount();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ItemStackRecipeWrapper other)) return false;
        return ItemStack.areEqual(this.itemStack, other.itemStack);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + Item.getRawId(itemStack.getItem());
        result = 31 * result + (itemStack.getComponents() != null ? itemStack.getComponents().hashCode() : 0);
        result = 31 * result + itemStack.getCount();
        return result;
    }

    public ItemStack getOrThrow() {
        assert !this.itemStack.isEmpty();
        return Optional.of(this.itemStack).get();
    }

    public Optional<ItemStack> getOrNullable() {
        return this.itemStack.isEmpty() ? Optional.empty() : Optional.of(this.itemStack);
    }

    public static Optional<String> toJson(ItemStackRecipeWrapper wrapper, Class<?> empty) {
        return Optional.ofNullable(toJson(wrapper));
    }

    public static String toJson(ItemStackRecipeWrapper wrapper) {
        DataResult<JsonElement> dataResult = ItemStackRecipeWrapper.CODEC.encodeStart(JsonOps.INSTANCE, wrapper);
        Optional<JsonElement> result = dataResult.result();
        if (result.isPresent()) {
            JsonElement element = result.get();
            return GSON.toJson(element);
        }
        return null;
    }

    public static Optional<ItemStackRecipeWrapper> toWrapper(String json) {
        if (json == null || json.isEmpty()) {
            return Optional.empty();
        }
        JsonElement jsonElement = JsonParser.parseString(json);
        Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, jsonElement);
        DataResult<ItemStackRecipeWrapper> parse = ItemStackRecipeWrapper.CODEC.parse(input);
        return parse.result();
    }


}
