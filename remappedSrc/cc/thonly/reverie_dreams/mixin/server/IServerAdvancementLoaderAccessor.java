package cc.thonly.reverie_dreams.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;

@Mixin(ServerAdvancementManager.class)
public interface IServerAdvancementLoaderAccessor extends cc.thonly.reverie_dreams.interfaces.IServerAdvancementLoaderAccessor {
    @Accessor("advancements")
    public Map<ResourceLocation, AdvancementHolder> getAdvancements();
}
