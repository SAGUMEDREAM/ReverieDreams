package cc.thonly.reverie_dreams.inf;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;

import java.util.Map;

public interface IServerAdvancementLoaderAccessor {
    Map<Identifier, AdvancementHolder> getAdvancements();
}
