package cc.thonly.reverie_dreams.interfaces;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.core.component.DataComponentType;

public interface IComponentMapBuilder {
    public Reference2ObjectMap<DataComponentType<?>, Object> getComponents();
}
