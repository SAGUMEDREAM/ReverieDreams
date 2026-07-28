package cc.thonly.reverie_dreams.data.skin;

import cc.thonly.reverie_dreams.registry.RegistryImpls;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;

@Getter
public class CustomType extends SkinType {
    public static final Codec<CustomType> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(x -> x.group(
            Identifier.CODEC.fieldOf("SkinType").forGetter(SkinType::getId)
    ).apply(x, RegistryImpls.CUSTOM_SKIN_TYPE::getValue)));
    final Item icon;

    public CustomType(Identifier id, Item icon) {
        super(id);
        this.icon = icon;
    }

    public String getDescriptionId() {
        return Util.makeDescriptionId("entity", this.getId());
    }

    @Override
    public String toString() {
        return "CustomType{" +
                "icon=" + this.icon +
                ", id=" + this.id +
                ", value='" + this.value + '\'' +
                ", signature='" + this.signature + '\'' +
                '}';
    }
}
