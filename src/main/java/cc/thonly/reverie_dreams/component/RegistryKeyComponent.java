package cc.thonly.reverie_dreams.component;

import com.mojang.serialization.Codec;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;

@AllArgsConstructor
@Setter
@Getter
public class RegistryKeyComponent {
    public static final Codec<ResourceLocation> CODEC = ResourceLocation.CODEC.optionalFieldOf("id", ResourceLocation.parse("minecraft:stone")).codec();
}
