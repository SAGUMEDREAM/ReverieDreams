package cc.thonly.reverie_dreams.world.gensokyo.gen;

import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapData;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * 为远景 LOD 采样幻想乡方块列。直接复用 {@link GensokyoChunkGenerator#emitColumn}，
 * 避免在兼容层重复一份地形填充规则；道路净空和稀疏地下覆盖属于近处原版区块的细节。
 */
public final class GensokyoLodSampler {
    @FunctionalInterface
    public interface SpanConsumer {
        void accept(int bottomY, int topY, BlockState state);
    }

    /**
     * @param bottomY 含
     * @param topY    不含
     */
    public record Span(int bottomY, int topY, BlockState state) {
        public Span {
            if (topY <= bottomY) {
                throw new IllegalArgumentException("Empty LOD span [" + bottomY + ", " + topY + ")");
            }
        }
    }

    public record Column(Holder<Biome> biome, List<Span> spans) {
        public Column {
            spans = List.copyOf(spans);
        }
    }

    private final GensokyoChunkGenerator generator;
    private final int minY;
    private final int height;

    public GensokyoLodSampler(GensokyoChunkGenerator generator) {
        this.generator = generator;
        this.minY = generator.getMinY();
        this.height = generator.getGenDepth();
    }

    public Column sample(int x, int z) {
        List<Span> spans = new ArrayList<>(8);
        this.forEachSpan(x, z, (bottomY, topY, state) -> spans.add(new Span(bottomY, topY, state)));
        return new Column(this.biomeAt(x, z), spans);
    }

    public void forEachSpan(int x, int z, SpanConsumer consumer) {
        this.generator.emitColumn(x, z, consumer::accept);
    }

    public Holder<Biome> biomeAt(int x, int z) {
        return this.generator.getBiomeSource().getNoiseBiome(
                QuartPos.fromBlock(x), 0, QuartPos.fromBlock(z), null);
    }

    public GensokyoMapData map() {
        return this.generator.mapData();
    }

    public int minY() {
        return this.minY;
    }

    public int height() {
        return this.height;
    }
}
