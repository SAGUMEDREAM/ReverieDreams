package cc.thonly.reverie_dreams.recipe;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Identifier;

@Setter
@Getter
public abstract class BaseRecipe {
    private Identifier id;
    private Integer rawId;
    private boolean isVirtual;
    public abstract ItemStackWrapper getOutput();
}
