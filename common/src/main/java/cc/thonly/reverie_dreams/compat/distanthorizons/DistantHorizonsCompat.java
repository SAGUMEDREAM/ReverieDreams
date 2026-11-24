package cc.thonly.reverie_dreams.compat.distanthorizons;

import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoChunkGenerator;
import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoLodSampler;
import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.interfaces.world.IDhApiLevelWrapper;
import com.seibel.distanthorizons.api.methods.events.DhApiEventRegister;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiLevelLoadEvent;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiEventParam;
import com.seibel.distanthorizons.api.objects.DhApiResult;
import com.seibel.distanthorizons.core.api.internal.SharedApi;
import com.seibel.distanthorizons.core.file.fullDatafile.GeneratedFullDataSourceProvider;
import com.seibel.distanthorizons.core.generation.queues.LodRequestModule;
import com.seibel.distanthorizons.core.level.AbstractDhServerLevel;
import com.seibel.distanthorizons.core.level.IDhLevel;
import com.seibel.distanthorizons.core.level.ServerLevelModule;
import com.seibel.distanthorizons.core.world.AbstractDhWorld;
import com.seibel.distanthorizons.core.wrapperInterfaces.world.ILevelWrapper;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.server.level.ServerLevel;

/**
 * 为幻想乡维度注册 Distant Horizons 世界生成覆盖。
 *
 * <p>只在服务端维度生效：DH 客户端的 LOD 永远走远端请求，重写客户端生成器不会产生效果。
 * 事件注册必须早于 DH 创建该维度的 LodRequestState，因此放在模组初始化阶段。</p>
 */
@Slf4j
public final class DistantHorizonsCompat {
    private static final long LEVEL_WAIT_MILLIS = 10_000L;
    private static final long WORLD_GEN_WAIT_MILLIS = 10_000L;
    private static final int REGISTER_RETRY_ATTEMPTS = 50;
    private static final long REGISTER_RETRY_MILLIS = 100L;
    private static boolean bound;

    private DistantHorizonsCompat() {
    }

    public static synchronized void bootstrap() {
        if (bound) {
            return;
        }
        bound = true;
        cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateLod.register((level,chunk)->{
            if(DhApi.Delayed.terrainRepo==null)return "UNAVAILABLE: DH terrain API is not ready";
            var wrapper=com.seibel.distanthorizons.common.wrappers.world.ServerLevelWrapper.getWrapper(level);
            var refreshed=DhApi.Delayed.terrainRepo.overwriteChunkDataAsync(wrapper,new Object[]{chunk,level});
            return refreshed.success?"QUEUED":"FAILED: "+refreshed.message;
        });
        DhApiResult<Void> result = DhApiEventRegister.on(DhApiLevelLoadEvent.class, new DhApiLevelLoadEvent() {
            @Override
            public void onLevelLoad(DhApiEventParam<EventParam> event) {
                register(event.value);
            }
        });
        if (result.success) {
            log.info("Bound Distant Horizons level-load event for Gensokyo LOD generation");
        } else {
            log.warn("Distant Horizons rejected the Gensokyo LOD event: {}", result.message);
        }
    }

    private static void register(DhApiLevelLoadEvent.EventParam param) {
        if (!(param.levelWrapper.getWrappedMcObject() instanceof ServerLevel level)) {
            return;
        }
        if (!(level.getChunkSource().getGenerator() instanceof GensokyoChunkGenerator generator)) {
            return;
        }
        GensokyoDhWorldGenerator worldGenerator = new GensokyoDhWorldGenerator(
                param.levelWrapper, new GensokyoLodSampler(generator));
        DhApiResult<Void> result = DhApi.worldGenOverrides.registerWorldGeneratorOverride(
                param.levelWrapper, worldGenerator);
        if (result.success) {
            log.info("Distant Horizons will generate Gensokyo LODs for level [{}]", param.levelWrapper.getDhIdentifier());
        } else {
            // 事件与 DH 的等级构造并发，注入器的 bind 偶发抛出不带消息的异常；队列一旦建好就不再读覆盖，
            // 因此必须在后台补注册，不能等下次等级加载。
            log.warn("Distant Horizons refused the Gensokyo LOD generator for [{}] [{}]; retrying in the background",
                    param.levelWrapper.getDhIdentifier(), result.message);
        }
        startBindingThread(param.levelWrapper, worldGenerator, result.success);
    }

    /**
     * 事件触发时 DH 可能仍在构造该等级的 LOD 队列，注册失败不能直接放弃。
     * 线程负责补注册、按等级实际持有的 wrapper 再绑定一次，并重启已用回退生成器建好的队列。
     */
    private static void startBindingThread(IDhApiLevelWrapper wrapper, GensokyoDhWorldGenerator worldGenerator,
                                           boolean initiallyBound) {
        Thread thread = new Thread(() -> {
            boolean registered = initiallyBound;
            String lastMessage = "";
            try {
                for (int attempt = 1; !registered && attempt <= REGISTER_RETRY_ATTEMPTS; attempt++) {
                    Thread.sleep(REGISTER_RETRY_MILLIS);
                    DhApiResult<Void> retry = DhApi.worldGenOverrides.registerWorldGeneratorOverride(
                            wrapper, worldGenerator);
                    registered = retry.success;
                    lastMessage = retry.message;
                    if (registered) {
                        log.info("Registered the Gensokyo LOD generator for [{}] on retry {}",
                                wrapper.getDhIdentifier(), attempt);
                    }
                }
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            } catch (Throwable failure) {
                log.warn("Could not register the Gensokyo LOD generator for [{}]", wrapper.getDhIdentifier(), failure);
            }
            if (!registered) {
                worldGenerator.close();
                log.warn("Distant Horizons never accepted the Gensokyo LOD generator for [{}] [{}]",
                        wrapper.getDhIdentifier(), lastMessage);
                return;
            }
            restartWorldGeneration(wrapper, worldGenerator);
        }, "reverie-dreams-dh-gensokyo");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * DH 在等级构造期间就启动了 LOD 请求 ticker，等本事件触发时，ticker 往往已经用内置的
     * 回退生成器建好了队列；队列持有 final 的生成器，之后注册的覆盖不会替换它。
     * 这里停掉旧队列，ticker 下一轮会用刚注册的覆盖生成器重建。
     * 这些成员不在 DH 公开 API 里，DH 改内部结构时由 {@link cc.thonly.reverie_dreams.compat.ReverieDreamsCompats} 的加载兜底。
     */
    private static void restartWorldGeneration(IDhApiLevelWrapper wrapper, GensokyoDhWorldGenerator worldGenerator) {
        try {
            AbstractDhWorld world = SharedApi.getAbstractDhWorld();
            IDhLevel dhLevel = waitForDhLevel(world, wrapper);
            if (dhLevel == null) {
                log.warn("Distant Horizons level [{}] was not bound to a world in time; the Gensokyo generator "
                        + "applies on the next level load", wrapper.getDhIdentifier());
                return;
            }
            if (!(dhLevel instanceof AbstractDhServerLevel serverLevel)) {
                log.warn("Distant Horizons level [{}] is not a server level; the Gensokyo generator "
                        + "applies on the next level load", wrapper.getDhIdentifier());
                return;
            }
            // 事件参数可能是与 level 持有的不同 wrapper 实例；按 level 实际使用的 wrapper 再绑定一次，
            // 保证 ticker 重建队列时查到的是覆盖生成器而不是内置回退。
            DhApi.worldGenOverrides.registerWorldGeneratorOverride(serverLevel.getLevelWrapper(), worldGenerator);
            ServerLevelModule module = serverLevel.serverside;
            LodRequestModule requests = module.lodRequestModule;
            GeneratedFullDataSourceProvider provider = module.fullDataFileHandler;
            if (!waitForWorldGenRunning(requests)) {
                log.info("Distant Horizons has not started LOD generation for level [{}] yet; the Gensokyo "
                        + "generator applies to its first run", wrapper.getDhIdentifier());
                return;
            }
            requests.stopWorldGen(provider);
            log.info("Restarted Distant Horizons LOD generation for level [{}] with the Gensokyo generator",
                    wrapper.getDhIdentifier());
        } catch (Throwable failure) {
            log.warn("Could not restart Distant Horizons LOD generation for level [{}]",
                    wrapper.getDhIdentifier(), failure);
        }
    }

    private static IDhLevel waitForDhLevel(AbstractDhWorld world, IDhApiLevelWrapper wrapper) throws InterruptedException {
        if (world == null) {
            return null;
        }
        long deadline = System.currentTimeMillis() + LEVEL_WAIT_MILLIS;
        while (true) {
            IDhLevel level = world.getLevel((ILevelWrapper) wrapper);
            if (level != null || System.currentTimeMillis() >= deadline) {
                return level;
            }
            Thread.sleep(50L);
        }
    }

    /**
     * ticker 的第一轮启动与本事件几乎同时发生，单次检查常会看到“还没开始”而放弃重启。
     * 轮询到它真正启动后再停下，才能保证旧的回退队列被替换。
     *
     * @return ticker 是否已启动 LOD 生成
     */
    private static boolean waitForWorldGenRunning(LodRequestModule requests) throws InterruptedException {
        long deadline = System.currentTimeMillis() + WORLD_GEN_WAIT_MILLIS;
        while (true) {
            if (requests.isWorldGenRunning()) {
                return true;
            }
            if (System.currentTimeMillis() >= deadline) {
                return false;
            }
            Thread.sleep(50L);
        }
    }
}
