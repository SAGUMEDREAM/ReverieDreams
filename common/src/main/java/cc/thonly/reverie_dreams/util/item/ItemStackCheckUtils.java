package cc.thonly.reverie_dreams.util.item;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Slf4j
public class ItemStackCheckUtils {
    private static final Gson GSON = new Gson();
    public static void test() {
        BuiltInRegistries.ITEM
                .stream()
                .parallel()
                .filter(item -> item != Items.AIR)
                .forEach(item -> {
                    try {
                        ItemStack stack = item.getDefaultInstance();
                        DataResult<JsonElement> result = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, stack);
                        JsonElement element = result.getOrThrow();
                        String json = GSON.toJson(element);
                    } catch (Exception e) {
                        log.error("Serialization error for item {}: {}", item, e.toString());
                    }
                });
    }
}
