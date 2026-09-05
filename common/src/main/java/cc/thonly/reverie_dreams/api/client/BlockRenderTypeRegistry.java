package cc.thonly.reverie_dreams.api.client;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BlockRenderTypeRegistry {
    public static final List<Entry> ENTRIES = new CopyOnWriteArrayList<>();

    public static void setRenderLayer(Holder<Block> block, ChunkSectionLayer layer) {
        ENTRIES.add(new Entry(block, layer));
    }

    public static void register(Holder<Block> block, ChunkSectionLayer layer) {
        ENTRIES.add(new Entry(block, layer));
    }

    public record Entry(Holder<Block> block, ChunkSectionLayer layer) {

    }
}
