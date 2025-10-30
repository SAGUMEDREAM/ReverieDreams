package cc.thonly.reverie_dreams.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.world.level.block.state.properties.WoodType;

@Mixin(WoodType.class)
public interface WoodTypeAccessor {
    @Accessor("VALUES")
    static Map<String, WoodType> getValues() {
        throw new UnsupportedOperationException();
    }
}
