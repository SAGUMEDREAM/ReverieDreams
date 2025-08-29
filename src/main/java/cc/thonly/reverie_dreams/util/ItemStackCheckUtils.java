package cc.thonly.reverie_dreams.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

@Slf4j
public class ItemStackCheckUtils {
    public static void test() {
        Registries.ITEM
                .stream()
                .parallel()
                .filter(item -> item != Items.AIR)
                .forEach(item -> {
                    try {
                        ItemStack stack = item.getDefaultStack();
                        DataResult<JsonElement> result = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, stack);
                        result.getOrThrow();
                    } catch (Exception e) {
                        log.error("Serialization error for item {}: {}", item, e.toString());
                    }
                });
    }
}
