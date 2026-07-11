package cc.thonly.reverie_dreams.data.craftengine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.utils.GameInstance;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SuppressWarnings("deprecation")
public class CraftEngineProvider {
    public static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    public static ItemDefinitionList generateItems(List<Item> entries) {
        return new ItemDefinitionList(entries.stream().map(item -> (ItemLike) item).toList());
    }

    public static BlockDefinitionList generateBlocks(List<Block> entries) {
        return new BlockDefinitionList(entries);
    }

    public static CraftEngineDefinition fromNamespace(String namespace) {
        MinecraftServer server = GameInstance.getServer();
        if (server == null) {
            log.error("The server must be running.");
            return null;
        }
        List<Item> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            Holder.Reference<Item> reference = item.builtInRegistryHolder();
            ResourceKey<Item> key = reference.key();
            Identifier identifier = key.identifier();
            if (!identifier.getNamespace().equalsIgnoreCase(namespace)) {
                continue;
            }
            items.add(item);
        }
        List<Block> blocks = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            Holder.Reference<Block> reference = block.builtInRegistryHolder();
            ResourceKey<Block> key = reference.key();
            Identifier identifier = key.identifier();
            if (!identifier.getNamespace().equalsIgnoreCase(namespace)) {
                continue;
            }
            blocks.add(block);
        }
        return new CraftEngineDefinition(generateItems(items), generateBlocks(blocks));
    }
}
