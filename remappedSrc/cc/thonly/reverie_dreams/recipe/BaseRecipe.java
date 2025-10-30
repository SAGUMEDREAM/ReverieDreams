package cc.thonly.reverie_dreams.recipe;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;

@Setter
@Getter
public abstract class BaseRecipe {
    private ResourceLocation id;
    private Integer rawId;
    private boolean isVirtual;
    public abstract ItemStackWrapper getOutput();
}
