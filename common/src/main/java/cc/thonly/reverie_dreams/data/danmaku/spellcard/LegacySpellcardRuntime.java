package cc.thonly.reverie_dreams.data.danmaku.spellcard;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.function.KeyframeFunction;
import cc.thonly.reverie_dreams.data.danmaku.stg.runtime.SpellCastOrigin;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Server-side execution and cleanup for one cast of a legacy spellcard.
 */
public final class LegacySpellcardRuntime {
    private static final Set<LegacySpellcardRuntime> ACTIVE = new LinkedHashSet<>();

    private final ServerLevel world;
    private final List<List<SpellCardFrameConfig>> frames;
    private final int maxTick;
    private final Map<DanmakuType, Map<Integer, ItemStack>> stackCache = new Object2ObjectOpenHashMap<>();
    private final SequencedSet<Entity> ownedBullets = new LinkedHashSet<>();
    private @Nullable Entity source;
    private Vec3 position;
    private @Nullable SpellCastOrigin origin;
    private int tick;
    private boolean canceled;

    private LegacySpellcardRuntime(LegacySpellcardDefinition definition, ServerLevel world,
                                   @Nullable Entity source, Vec3 position) {
        this.world = world;
        this.source = source;
        this.position = position;
        this.frames = definition.copyFrames();
        this.maxTick = definition.durationTicks();
    }

    public static void start(LegacySpellcardDefinition definition, ServerLevel world,
                             @Nullable Entity source, Vec3 position) {
        ACTIVE.add(new LegacySpellcardRuntime(definition, world, source, position));
    }

    public static void startFromPlayer(LegacySpellcardDefinition definition, ServerLevel world, Player player) {
        var origin = SpellCastOrigin.fromPlayer(world, player);
        var cast = new LegacySpellcardRuntime(definition, world, player, origin.position());
        cast.origin = origin;
        ACTIVE.add(cast);
    }

    /**
     * 取消并清理所有仍在运行的旧版符卡。
     *
     * <p>避免服务器切换存档时保留旧的 {@code ServerLevel} 或继续推进旧符卡。
     */
    public static void cancelAll() {
        for (LegacySpellcardRuntime cast : ACTIVE) {
            cast.cancel();
        }
        ACTIVE.clear();
    }

    public static void tick(MinecraftServer server) {
        Iterator<LegacySpellcardRuntime> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            LegacySpellcardRuntime cast = iterator.next();
            cast.update();
            if (cast.canceled || (cast.tick >= cast.maxTick && cast.ownedBullets.isEmpty())) {
                iterator.remove();
                cast.cleanup();
            }
        }
    }

    private void update() {
        if (this.canceled)
            return;

        if (this.origin == null && this.source != null) {
            this.origin = new SpellCastOrigin(this.world, this.source, this.source.getEyePosition());
        }
        if (this.origin != null) {
            if (!this.origin.update()) {
                this.cancel();
                return;
            }
            this.position = this.origin.position();
        }
        this.tick++;
        if (this.tick > this.maxTick)
            return;
        Vec3 spawnPosition = this.position;
        if (spawnPosition == null || !this.world.isPositionEntityTicking(BlockPos.containing(spawnPosition)))
            return;

        for (List<SpellCardFrameConfig> frame : this.frames) {
            for (SpellCardFrameConfig config : frame) {
                int delay = config.getTickDelay();
                int duration = config.getTickDuration();
                int interval = Math.max(1, config.getTickInterval());

                // ⏱️ 时间窗口
                if (this.tick < delay || this.tick > delay + duration)
                    continue;

                int localTick = this.tick - delay;

                // ⏱️ 间隔控制
                if (localTick % interval != 0)
                    continue;

                int density = Math.max(1, config.getDensity());

                KeyframeFunction pitchFunc = config.getPitchRange().createFunction();
                KeyframeFunction yawFunc = config.getYawRange().createFunction();

                // 🎯 当前进度
                float baseProgress = duration > 0 ? (float) localTick / duration : 1f;

                // 旧配置以 density / interval 表示每次齐射的弹数。
                int perVolley = Math.max(1, density / interval);

                for (int i = 0; i < perVolley; i++) {
                    float progress;

                    if (config.isSync()) {
                        // 同步：统一进度
                        progress = baseProgress;
                    } else {
                        // 异步：均匀错开（无随机数开销）
                        float offset = ((float) i / (float) perVolley);
                        progress = (baseProgress + offset) % 1.0f;
                    }

                    float pitch = pitchFunc.sample(progress);
                    float yaw = yawFunc.sample(progress);

                    // 🎨 颜色逻辑
                    if (config.isRandomColor()) {
                        spawnDanmaku(config.getType(), config.getSpeed(), pitch, yaw, spawnPosition);
                    } else {
                        int color = config.getColor();
                        if (color < 0) {
                            spawnDanmaku(config.getType(), config.getSpeed(), pitch, yaw, spawnPosition);
                        } else {
                            spawnDanmaku(config.getType(), config.getSpeed(), color, pitch, yaw, spawnPosition);
                        }
                    }
                }
            }
        }
    }


    private void spawnDanmaku(DanmakuType type, float speed, float pitch, float yaw, Vec3 spawnPosition) {
        int r = 128 + ThreadLocalRandom.current().nextInt(128);
        int g = 128 + ThreadLocalRandom.current().nextInt(128);
        int b = 128 + ThreadLocalRandom.current().nextInt(128);
        int color = (r << 16) | (g << 8) | b;
        ItemStack stack = type.getItemHolder().asItem().getDefaultInstance();
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
        spawnDanmaku(stack, speed, pitch, yaw, spawnPosition);
    }

    private void spawnDanmaku(DanmakuType type, float speed, int color, float pitch, float yaw, Vec3 spawnPosition) {
        spawnDanmaku(this.getDanmakuStack(type, color).copy(), speed, pitch, yaw, spawnPosition);
    }

    private void spawnDanmaku(ItemStack shotStack, float speed, float xRot, float yRot, Vec3 spawnPosition) {
        DanmakuProperties properties = shotStack.get(RDDataComponentTypes.DANMAKU_PROPERTIES.value());
        if (properties == null) {
            return;
        }
        // 旧符卡的 speed 是“组件语义”（实际速度 = speed / 2）；显式标记为 0 以保持旧行为
        properties = properties.withSpeed(speed).withSpeedVersion(0);
        shotStack.set(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), properties);
        DanmakuEntity bullet = DanmakuEntity.create(this.world, shotStack, spawnPosition.x, spawnPosition.y,
                spawnPosition.z, xRot, yRot, 0);
        if (this.origin != null && this.origin.entity() instanceof LivingEntity livingEntity) {
            bullet.setOwner(livingEntity);
        }
        if (this.world.addFreshEntity(bullet)) {
            bullet.setLegacySpell(this);
            this.ownedBullets.add(bullet);
        }
    }

    private ItemStack getDanmakuStack(DanmakuType type, int color) {
        Map<Integer, ItemStack> colorStackMap = stackCache.computeIfAbsent(type, t -> new Object2ObjectOpenHashMap<>());

        if (colorStackMap.size() > 64) {
            colorStackMap.clear(); // 防止无限增长
        }

        ItemStack itemStack = colorStackMap.get(color);
        if (itemStack != null) {
            return itemStack;
        }

        Item item = type.getItemHolder().asItem();
        itemStack = item.getDefaultInstance();
        itemStack.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
        colorStackMap.put(color, itemStack);
        return itemStack;
    }

    public void cancel() {
        this.canceled = true;
        while (!this.ownedBullets.isEmpty()) {
            this.ownedBullets.removeFirst().discard();
        }
        this.cleanup();
    }

    public boolean isCasterDefeated() {
        return SpellCastOrigin.wasDefeated(this.source);
    }

    public void untrack(Entity entity) {
        this.ownedBullets.remove(entity);
    }

    private void cleanup() {
        this.frames.clear();
        this.stackCache.clear();
        this.source = null;
        this.origin = null;
    }

}
