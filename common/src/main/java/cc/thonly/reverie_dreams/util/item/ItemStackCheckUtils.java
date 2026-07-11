package cc.thonly.reverie_dreams.util.item;

import cc.thonly.reverie_dreams.util.PlatformContext;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

@Slf4j
public class ItemStackCheckUtils {
    private static final Gson GSON = new Gson();
    public static void test() {
        if (!PlatformContext.isFabric()) {
            return;
        }
        BuiltInRegistries.ITEM
                .stream()
                .parallel()
                .filter(item -> item != Items.AIR)
                .forEach(item -> {
                    try {
                        ItemStackTemplate template = new ItemStackTemplate(item);
                        DataResult<JsonElement> result = ItemStackTemplate.CODEC.encodeStart(JsonOps.INSTANCE, template);
                        JsonElement element = result.getOrThrow();
                        String json = GSON.toJson(element);
                    } catch (Exception e) {
                        log.error("Serialization error for item {}: {}", item, e.toString());
                    }
                });
    }
}
