package cc.thonly.reverie_dreams.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.stream.Collectors;

public interface SerializableProvider<T> {
    Codec<Item> ITEM_CODEC = Identifier.CODEC.xmap(
            BuiltInRegistries.ITEM::getValue,
            BuiltInRegistries.ITEM::getKey
    );
    Codec<List<Item>> ITEMS_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(Identifier.CODEC)
                            .fieldOf("values")
                            .forGetter(items -> items.stream()
                                    .map(BuiltInRegistries.ITEM::getKey)
                                    .collect(Collectors.toList()))
            ).apply(instance, identifiers ->
                    identifiers.stream()
                            .map(BuiltInRegistries.ITEM::getValue)
                            .collect(Collectors.toList())
            )
    );


    Codec<T> getCodec();
}
