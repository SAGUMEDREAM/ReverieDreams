package cc.thonly.reverie_dreams.inf;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.core.component.DataComponentType;

public interface IComponentMapBuilder {
    Reference2ObjectMap<DataComponentType<?>, Object> getComponents();
}
