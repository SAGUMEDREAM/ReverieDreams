package cc.thonly.reverie_dreams.recipe;

import cc.thonly.reverie_dreams.util.item.ItemStackTemplateHelper;
import cc.thonly.reverie_dreams.util.item.ItemUtils;
import com.google.gson.Gson;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public interface ItemWrapper {
    Gson GSON = new Gson();
    ItemStackTemplateWrapper EMPTY = ItemStackTemplateWrapper.of(ItemUtils.createUnsafeTemplate((Void)null));
    ItemStackTemplateWrapper ERROR = ItemStackTemplateWrapper.of(createErrorItem());
    Codec<Item> ITEM_CODEC = Codec.STRING.xmap(
            id -> {
                Identifier itemId = Identifier.tryParse(id.toLowerCase());
                if (itemId == null) {
                    return Items.AIR;
                }
                return BuiltInRegistries.ITEM.getValue(itemId);
            },
            item -> BuiltInRegistries.ITEM.getKey(item).toString()
    );
    Codec<Holder<Item>> ITEM_HOLDER_CODEC = Codec.STRING.xmap(
            id -> {
                Identifier itemId = Identifier.tryParse(id.toLowerCase());
                if (itemId == null) {
                    return BuiltInRegistries.ITEM.wrapAsHolder(Items.AIR);
                }

                Item item = BuiltInRegistries.ITEM.getValue(itemId);
                return BuiltInRegistries.ITEM.wrapAsHolder(item);
            },
            holder -> BuiltInRegistries.ITEM.getKey(holder.value()).toString()
    );
    Codec<TagKey<Item>> TAG_KEY_CODEC =
            Identifier.CODEC.xmap(
                    id -> TagKey.create(Registries.ITEM, id),
                    TagKey::location
            );

    static ItemStackTemplate createErrorItem() {
        ItemStackTemplate template = new ItemStackTemplate(Items.WHITE_DYE);
        ItemStackTemplateHelper.modify(template, (template1, modifier) -> {
            modifier.set(DataComponents.ITEM_MODEL, BuiltInRegistries.ITEM.getKey(Items.BARRIER));
            modifier.set(DataComponents.ITEM_NAME, Component.literal("§cError Item"));
            modifier.set(DataComponents.LORE, new ItemLore(
                    new ArrayList<>(List.of(Component.literal("§cThis item failed to be serialized")))
            ));
        });
        return template;
    }

    static ItemStackTemplateWrapper empty() {
        return EMPTY;
    }

    static ItemStackTemplateWrapper error() {
        return ERROR;
    }

    Item getItem();

    int getCount();

    <T> T get(DataComponentType<T> type);

    <T> T getOrCreate(DataComponentType<T> type, Supplier<T> supplier);

    <T> T getOrDefault(DataComponentType<T> type, T value);

    <T> T set(DataComponentType<T> type, T value);

    ItemWrapper copy();


}
