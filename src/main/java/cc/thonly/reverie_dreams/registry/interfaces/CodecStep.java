package cc.thonly.reverie_dreams.registry.interfaces;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public interface CodecStep<T> {
    Codec<List<Item>> ITEMS_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(ResourceLocation.CODEC)
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


    public Codec<T> getCodec();
}
