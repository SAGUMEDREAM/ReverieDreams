package cc.thonly.reverie_dreams.compat.distanthorizons;

import cc.thonly.reverie_dreams.world.gensokyo.GensokyoBoundary;
import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapData;
import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoLodSampler;
import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.enums.worldGeneration.EDhApiDistantGeneratorMode;
import com.seibel.distanthorizons.api.enums.worldGeneration.EDhApiGeneratorPlan;
import com.seibel.distanthorizons.api.enums.worldGeneration.EDhApiWorldGeneratorReturnType;
import com.seibel.distanthorizons.api.interfaces.block.IDhApiBiomeWrapper;
import com.seibel.distanthorizons.api.interfaces.block.IDhApiBlockStateWrapper;
import com.seibel.distanthorizons.api.interfaces.override.worldGenerator.IDhApiWorldGenerator;
import com.seibel.distanthorizons.api.interfaces.world.IDhApiLevelWrapper;
import com.seibel.distanthorizons.api.objects.data.DhApiTerrainDataPoint;
import com.seibel.distanthorizons.api.objects.data.IDhApiFullDataSource;
import com.seibel.distanthorizons.core.api.internal.SharedApi;
import com.seibel.distanthorizons.core.config.Config;
import com.seibel.distanthorizons.core.generation.DhWorldGenerator;
import com.seibel.distanthorizons.core.level.IDhLevel;
import com.seibel.distanthorizons.core.level.IDhServerLevel;
import com.seibel.distanthorizons.core.world.AbstractDhWorld;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * Distant Horizons 数据源生成器：地图内的最细一级（detail 0）交给 DH 自带的分块生成，其余精度按 LOD 列中心
 * 采样幻想乡基础地形，写入 DH 的 API 数据源。
 *
 * <p>只有 detail 0 能触发 DH 的区块生成流程（含生物群系装饰），这是远处 LOD 出现树木等地物的唯一途径；
 * 更粗的精度 DH 自身也只走粗糙生成器，直接用采样器反而更接近真实地形。地图外只有装饰性边界山脉，
 * 因此 detail-0 区块生成限制在地图内，过渡环与界外都用采样或空列，避免把算力花在无用的区块上。</p>
 */
@Slf4j
public final class GensokyoDhWorldGenerator implements IDhApiWorldGenerator {
    /**
     * DH 的数据列数量固定为 64（每个 LOD 段比数据列细 6 级）。
     */
    private static final int DETAIL_OFFSET = 6;
    private static final byte SMALLEST_DATA_DETAIL = 0;
    private static final byte LARGEST_DATA_DETAIL = 12;
    /**
     * 地图外保留的过渡宽度，超出后 DH 任务直接返回空列，不再采样无尽的边界山脉。与玩家可探索范围共用同一常量。
     */
    private static final int BORDER_TRANSITION_BLOCKS = GensokyoBoundary.BORDER_TRANSITION_BLOCKS;

    private final IDhApiLevelWrapper levelWrapper;
    private final GensokyoLodSampler sampler;
    private final int skyLight;
    private final ConcurrentHashMap<BlockState, IDhApiBlockStateWrapper> blockWrappers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Holder<Biome>, IDhApiBiomeWrapper> biomeWrappers = new ConcurrentHashMap<>();
    private DhWorldGenerator delegate;
    private boolean delegateUnavailable;

    public GensokyoDhWorldGenerator(IDhApiLevelWrapper levelWrapper, GensokyoLodSampler sampler) {
        this.levelWrapper = levelWrapper;
        this.sampler = sampler;
        this.skyLight = levelWrapper.hasSkyLight() ? 15 : 0;
    }

    @Override
    public byte getSmallestDataDetailLevel() {
        return SMALLEST_DATA_DETAIL;
    }

    @Override
    public byte getLargestDataDetailLevel() {
        return LARGEST_DATA_DETAIL;
    }

    @Override
    public EDhApiWorldGeneratorReturnType getReturnType() {
        return EDhApiWorldGeneratorReturnType.API_DATA_SOURCES;
    }

    @Override
    public boolean runApiValidation() {
        return true;
    }

    @Override
    public void preGeneratorTaskStart() {
        if (!chunkGenAllowed(Config.Common.WorldGenerator.chunkGeneratorMode.get()))
            return;
        DhWorldGenerator chunkGenerator = this.delegate();
        if (chunkGenerator != null) {
            chunkGenerator.preGeneratorTaskStart();
        }
    }

    @Override
    public CompletableFuture<Void> generateLod(int chunkX, int chunkZ, int sectionX, int sectionZ, byte detailLevel,
                                               IDhApiFullDataSource dataSource,
                                               EDhApiDistantGeneratorMode generatorMode,
                                               ExecutorService executor, Consumer<IDhApiFullDataSource> resultConsumer) {
        int sectionWidth = 64 << detailLevel;
        int minX = sectionX << (detailLevel + DETAIL_OFFSET);
        int minZ = sectionZ << (detailLevel + DETAIL_OFFSET);
        if (detailLevel == 0 && !this.outsideMap(minX, minZ, sectionWidth) && chunkGenAllowed(generatorMode)) {
            DhWorldGenerator chunkGenerator = this.delegate();
            if (chunkGenerator != null) {
                return chunkGenerator.generateLod(chunkX, chunkZ, sectionX, sectionZ, detailLevel, dataSource,
                        generatorMode, executor, resultConsumer);
            }
        }
        return CompletableFuture.runAsync(
                () -> this.generate(minX, minZ, detailLevel, dataSource, resultConsumer), executor);
    }

    /**
     * 区块生成只用于地图内的真实地形；过渡环和界外走采样，避免给装饰山脉生成区块与地物。
     */
    private boolean outsideMap(int minX, int minZ, int sectionWidth) {
        GensokyoMapData map = this.sampler.map();
        return minX + sectionWidth <= map.originX()
                || minX >= map.originX() + map.width()
                || minZ + sectionWidth <= map.originZ()
                || minZ >= map.originZ() + map.depth();
    }

    /**
     * 地图外的边界山脉延伸到无穷远，但玩家看不到；列落在过渡环之外时直接给空气。
     */
    private boolean beyondBorder(int blockX, int blockZ) {
        GensokyoMapData map = this.sampler.map();
        int margin = BORDER_TRANSITION_BLOCKS;
        return blockX < map.originX() - margin
                || blockX >= map.originX() + map.width() + margin
                || blockZ < map.originZ() - margin
                || blockZ >= map.originZ() + map.depth() + margin;
    }

    private List<DhApiTerrainDataPoint> emptyColumn(IDhApiBiomeWrapper biome) {
        int minY = this.sampler.minY();
        return List.of(DhApiTerrainDataPoint.create(
                (byte) 0, 0, this.skyLight, minY, minY + this.sampler.height(),
                this.blockWrapper(Blocks.AIR.defaultBlockState()), biome));
    }

    private void generate(int minX, int minZ, byte detailLevel, IDhApiFullDataSource dataSource,
                          Consumer<IDhApiFullDataSource> resultConsumer) {
        int width = dataSource.getWidthInDataColumns();
        int columnWidth = 1 << detailLevel;
        int columnCenterOffset = columnWidth >> 1;
        // The section origin is quart-aligned. Up to four adjacent samples along either
        // axis have the same biome even though their terrain spans are independent.
        int samplesPerQuart = Math.max(1, 4 / columnWidth);
        var biomesAtZ = new IDhApiBiomeWrapper[(width + samplesPerQuart - 1) / samplesPerQuart];

        for (int columnX = 0; columnX < width; columnX++) {
            int blockX = minX + (columnX << detailLevel) + columnCenterOffset;
            for (int columnZ = 0; columnZ < width; columnZ++) {
                int blockZ = minZ + (columnZ << detailLevel) + columnCenterOffset;
                int biomeIndex = columnZ / samplesPerQuart;
                if (columnX % samplesPerQuart == 0 && columnZ % samplesPerQuart == 0) {
                    biomesAtZ[biomeIndex] = this.biomeWrappers.computeIfAbsent(this.sampler.biomeAt(blockX, blockZ),
                            holder -> DhApi.Delayed.wrapperFactory.getBiomeWrapper(new Object[]{holder}, this.levelWrapper));
                }
                dataSource.setApiDataPointColumn(columnX, columnZ, this.dataPoints(blockX, blockZ, biomesAtZ[biomeIndex]));
            }
        }
        resultConsumer.accept(dataSource);
    }

    private List<DhApiTerrainDataPoint> dataPoints(int blockX, int blockZ, IDhApiBiomeWrapper biome) {
        if (this.beyondBorder(blockX, blockZ)) {
            return this.emptyColumn(biome);
        }
        List<DhApiTerrainDataPoint> points = new ArrayList<>(8);
        this.sampler.forEachSpan(blockX, blockZ, (bottomY, topY, state) -> points.add(DhApiTerrainDataPoint.create(
                (byte) 0, 0, this.skyLight, bottomY, topY, this.blockWrapper(state), biome)));
        return points;
    }

    private IDhApiBlockStateWrapper blockWrapper(BlockState state) {
        return this.blockWrappers.computeIfAbsent(state, key ->
                DhApi.Delayed.wrapperFactory.getBlockStateWrapper(new Object[]{key}, this.levelWrapper));
    }

    /**
     * 与 DH 自身的 detail-0 判定保持一致；配置或调用方不允许区块生成时继续使用采样器。
     */
    private static boolean chunkGenAllowed(EDhApiDistantGeneratorMode generatorMode) {
        if (generatorMode == EDhApiDistantGeneratorMode.PRE_EXISTING_ONLY) {
            return false;
        }
        EDhApiGeneratorPlan plan = Config.Common.WorldGenerator.generatorPlan.get();
        EDhApiDistantGeneratorMode chunkGeneratorMode = Config.Common.WorldGenerator.chunkGeneratorMode.get();
        return plan.chunkGenEnabled
                && !(plan.surfaceGenEnabled && chunkGeneratorMode == EDhApiDistantGeneratorMode.PRE_EXISTING_ONLY);
    }

    private DhWorldGenerator delegate() {
        DhWorldGenerator current = this.delegate;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (this.delegate != null || this.delegateUnavailable) {
                return this.delegate;
            }
            IDhServerLevel serverLevel = this.resolveServerLevel();
            if (serverLevel == null) {
                return null;
            }
            try {
                this.delegate = new DhWorldGenerator(serverLevel);
                log.info("Gensokyo LOD detail 0 now uses the Distant Horizons chunk generator");
            } catch (Throwable failure) {
                this.delegateUnavailable = true;
                log.warn("Distant Horizons chunk generator is unavailable for Gensokyo; LOD detail 0 stays sampled",
                        failure);
            }
            return this.delegate;
        }
    }

    private IDhServerLevel resolveServerLevel() {
        AbstractDhWorld world = SharedApi.getAbstractDhWorld();
        if (world == null) {
            return null;
        }
        Object mcLevel = this.levelWrapper.getWrappedMcObject();
        for (IDhLevel level : world.getAllLoadedLevels()) {
            if (level instanceof IDhServerLevel serverLevel
                    && serverLevel.getServerLevelWrapper().getWrappedMcObject() == mcLevel) {
                return serverLevel;
            }
        }
        return null;
    }

    @Override
    public void close() {
        synchronized (this) {
            if (this.delegate != null) {
                this.delegate.close();
                // DH 重启 LOD 队列时也会关闭覆盖生成器；下次使用时再重建委托。
                this.delegate = null;
            }
        }
        this.blockWrappers.clear();
        this.biomeWrappers.clear();
    }
}
