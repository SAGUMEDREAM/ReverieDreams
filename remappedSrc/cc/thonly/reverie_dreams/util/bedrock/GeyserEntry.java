package cc.thonly.reverie_dreams.util.bedrock;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.util.IdentifierGetter;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.event.EventRegistrar;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomItemsEvent;
import org.geysermc.geyser.api.item.custom.NonVanillaCustomItemData;

import java.nio.file.Path;
import java.util.Map;

@Deprecated
public class GeyserEntry implements EventRegistrar {
    public static Path PACKS_FOLDER;
    public static Path GEYSER_PACK;
    public static GeyserApi geyser;

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> {
            geyser = GeyserApi.api();

            EventRegistrar registrar = new GeyserEntry();
            geyser.eventBus().register(registrar, registrar);
        });
    }

    @Subscribe
    public void onGeyserDefineCustomItemsEvent(GeyserDefineCustomItemsEvent event) {
        for (Map.Entry<ResourceKey<Item>, Item> mapEntry : BuiltInRegistries.ITEM.entrySet()) {
            ResourceKey<Item> key = mapEntry.getKey();
            if (!key.location().getNamespace().equals(Touhou.MOD_ID)) continue;
            Item item = mapEntry.getValue();
            if(item instanceof IdentifierGetter) {
                int id = BuiltInRegistries.ITEM.getId(item);
                ResourceLocation identifier = ((IdentifierGetter) item).getIdentifier();
                NonVanillaCustomItemData.Builder customItemData = NonVanillaCustomItemData.builder()
                        .displayName(Component.translatable(item.getDescriptionId()).getString())
                        .name(Component.translatable(item.getDescriptionId()).getString())
                        .javaId(id)
                        .stackSize(item.getDefaultMaxStackSize())
                        .identifier(identifier.toString())
                        .translationString(item.getDescriptionId())
                        .allowOffhand(true)
                        .displayHandheld(true)
                        .icon(identifier.toString())
                        .creativeCategory(3)
                        ;

                event.register(customItemData.build());
            }
        }
    }
    public static boolean isPlayerOnBedrock(ServerPlayer player) {
        if (geyser == null || player == null) return false;
        return geyser.isBedrockPlayer(player.getUUID());
    }
}
