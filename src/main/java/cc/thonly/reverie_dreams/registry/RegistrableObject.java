package cc.thonly.reverie_dreams.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Collectors;

public interface RegistrableObject<T extends RegistrableObject<T>> {
    Codec<List<Item>> ITEMS_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(Identifier.CODEC)
                            .fieldOf("values")
                            .forGetter(items -> items.stream()
                                    .map(Registries.ITEM::getId)
                                    .collect(Collectors.toList()))
            ).apply(instance, identifiers ->
                    identifiers.stream()
                            .map(Registries.ITEM::get)
                            .collect(Collectors.toList())
            )
    );

    public void setId(Identifier id);

    public Identifier getId();

    public Codec<T> getCodec();

    public default Boolean isDirect() {
        return false;
    }
}
