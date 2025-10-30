package cc.thonly.reverie_dreams.interfaces;

import java.util.Map;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;

public interface IServerAdvancementLoaderAccessor {
    Map<ResourceLocation, AdvancementHolder> getAdvancements();
}
