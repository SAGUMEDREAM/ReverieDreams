package cc.thonly.reverie_dreams.inf;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface IServerAdvancementLoaderAccessor {
    Map<ResourceLocation, AdvancementHolder> getAdvancements();
}
