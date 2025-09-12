package cc.thonly.reverie_dreams.component;

import com.mojang.serialization.Codec;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Identifier;

@AllArgsConstructor
@Setter
@Getter
public class RegistryKeyComponent {
    public static final Codec<Identifier> CODEC = Identifier.CODEC.optionalFieldOf("id", Identifier.of("minecraft:stone")).codec();
}
