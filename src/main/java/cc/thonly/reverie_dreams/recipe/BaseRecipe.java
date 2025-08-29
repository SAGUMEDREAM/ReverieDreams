package cc.thonly.reverie_dreams.recipe;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Identifier;

@Setter
@Getter
public abstract class BaseRecipe implements PolymerObject {
    private Identifier id;
    private Integer rawId;
    private boolean isVirtual;
    public abstract ItemStackWrapper getOutput();
}
