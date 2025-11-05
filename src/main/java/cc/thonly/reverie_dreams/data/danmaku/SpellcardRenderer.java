package cc.thonly.reverie_dreams.data.danmaku;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.SpellCardFrameConfig;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class SpellcardRenderer {
    public static final Codec<List<List<SpellCardFrameConfig>>> FRAMES_CODEC =
            Codec.list(Codec.list(SpellCardFrameConfig.CODEC));
    public static final Codec<SpellcardRenderer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FRAMES_CODEC.fieldOf("frames").forGetter(SpellcardRenderer::getFrames)
    ).apply(instance, SpellcardRenderer::new));

    private static final Set<SpellcardRenderer> TICKER = new LinkedHashSet<>();
    private static final int MAX_SPAWN_PER_TICK = 64;

    @Nullable
    @Setter
    private Entity source;
    @Setter
    private Vec3 position;
    @Setter
    @Nullable
    private PositionGetter positionGetter;
    @Setter
    private ServerLevel world;
    private final List<List<SpellCardFrameConfig>> frames;
    private final Random random = new Random();

    private final Map<DanmakuType, Map<Integer, ItemStack>> STACK_CACHED = new Object2ObjectOpenHashMap<>();

    private boolean canceled = false;
    private int tick = 0;
    private int maxTick = 100;

    public SpellcardRenderer(@Nullable Entity source, @NotNull ServerLevel world, List<List<SpellCardFrameConfig>> frames, int tick, int maxTick) {
        this.source = source;
        this.position = source != null ? source.getEyePosition() : Vec3.ZERO;
        this.world = world;
        this.frames = copyFramesConfig(frames);
        this.tick = tick;
        this.maxTick = maxTick;
    }

    public SpellcardRenderer(@NotNull Vec3 position, @NotNull ServerLevel world, List<List<SpellCardFrameConfig>> frames, int tick, int maxTick) {
        this.source = null;
        this.position = position;
        this.world = world;
        this.frames = this.copyFramesConfig(frames);
        this.tick = tick;
        this.maxTick = maxTick;
    }

    public SpellcardRenderer(List<List<SpellCardFrameConfig>> frames) {
        this.frames = this.copyFramesConfig(frames);
        this.maxTick = this.searchMaxTick(frames);
    }

    public static SpellcardRenderer addRenderer(SpellcardRenderer renderer) {
        TICKER.add(renderer);
        return renderer;
    }

    public static void tick(MinecraftServer server) {
        Iterator<SpellcardRenderer> iterator = TICKER.iterator();
        while (iterator.hasNext()) {
            SpellcardRenderer renderer = iterator.next();
            renderer.update();
            if (renderer.canceled || renderer.tick > renderer.maxTick) {
                iterator.remove();
                renderer.cleanup();
            }
        }
    }

    private int searchMaxTick(List<List<SpellCardFrameConfig>> frames) {
        int maxTick = 0;
        for (List<SpellCardFrameConfig> frameList : frames) {
            for (SpellCardFrameConfig config : frameList) {
                int endTick = config.getTickDelay() + config.getTickDuration();
                if (endTick > maxTick) {
                    maxTick = endTick;
                }
            }
        }
        return maxTick;
    }

    private List<List<SpellCardFrameConfig>> copyFramesConfig(List<List<SpellCardFrameConfig>> frames) {
        List<List<SpellCardFrameConfig>> copy = new ArrayList<>();
        for (List<SpellCardFrameConfig> frame : frames) {
            List<SpellCardFrameConfig> frameCopy = new ArrayList<>();
            for (SpellCardFrameConfig config : frame) {
                frameCopy.add(config.copy());
            }
            copy.add(frameCopy);
        }
        return copy;
    }

    protected void update() {
        if (this.canceled) {
            return;
        }

        this.tick++;
        if (this.tick > this.maxTick) {
            this.cancel();
            return;
        }
        if (this.source != null && this.source.isRemoved()) {
            this.cancel();
            return;
        }

        int spawned = 0;

        for (List<SpellCardFrameConfig> frame : this.frames) {
            for (SpellCardFrameConfig config : frame) {
                int relativeTick = this.tick - config.getTickDelay();
                if (relativeTick < 0 || relativeTick > config.getTickDuration()) continue;
                if (relativeTick % config.getTickInterval() != 0) continue;

                float pitchStart = config.getPitchRange().getStart();
                float pitchEnd = config.getPitchRange().getEnd();
                float yawStart = config.getYawRange().getStart();
                float yawEnd = config.getYawRange().getEnd();

                int count = Math.max(1, config.getDensity());
                float pitchStep = (pitchEnd - pitchStart) / count;
                float yawStep = (yawEnd - yawStart) / count;

                for (int i = 0; i < count && spawned < MAX_SPAWN_PER_TICK; i++) {
                    float pitch = pitchStart + pitchStep * i;
                    float yaw = yawStart + yawStep * i;
                    if (config.isRandomColor()) {
                        this.spawnDanmaku(config.getType(), config.getSpeed(), pitch, yaw);
                    } else {
                        this.spawnDanmaku(config.getType(), config.getColor(), pitch, yaw);
                    }
                    spawned++;
                }
                if (spawned >= MAX_SPAWN_PER_TICK) break;
            }
            if (spawned >= MAX_SPAWN_PER_TICK) break;
        }
    }

    private void spawnDanmaku(DanmakuType type, float speed, float pitch, float yaw) {
        int r = 128 + ThreadLocalRandom.current().nextInt(128);
        int g = 128 + ThreadLocalRandom.current().nextInt(128);
        int b = 128 + ThreadLocalRandom.current().nextInt(128);
        int color = (r << 16) | (g << 8) | b;
        ItemStack danmakuStack = getDanmakuStack(type, color);
        spawnDanmaku(danmakuStack, speed, pitch, yaw);
    }

    private void spawnDanmaku(DanmakuType type, float speed, int color, float pitch, float yaw) {
        ItemStack danmakuStack = getDanmakuStack(type, color);
        spawnDanmaku(danmakuStack, speed, pitch, yaw);
    }

    private void spawnDanmaku(ItemStack itemStack, float speed, float pitch, float yaw) {
        DanmakuProperties properties = itemStack.get(RDDataComponentTypes.DANMAKU_PROPERTIES);
        if (properties == null) {
            return;
        }
        properties = properties.withSpeed(speed);
        if (this.source != null) {
            this.position = new Vec3(this.source.getX(), this.source.getEyeY(), this.source.getZ());
        }
        if (this.position == null && this.positionGetter != null) {
            this.position = this.positionGetter.getPosition(this.tick);
        }
        if (this.position == null) {
            return;
        }

        DanmakuEntity danmakuEntity = new DanmakuEntity(
                this.source,
                this.world,
                this.position.x, this.position.y, this.position.z,
                itemStack,
                properties,
                pitch, yaw,
                0f, 0f,
                false
        );
        this.world.addFreshEntity(danmakuEntity);
    }

    private ItemStack getDanmakuStack(DanmakuType type, int color) {
        Map<Integer, ItemStack> colorStackMap = STACK_CACHED.computeIfAbsent(type, t -> new Object2ObjectOpenHashMap<>());

        if (colorStackMap.size() > 64) {
            colorStackMap.clear(); // 防止无限增长
        }

        ItemStack itemStack = colorStackMap.get(color);
        if (itemStack != null) {
            return itemStack.copy();
        }

        Item item = type.getItem();
        itemStack = item.getDefaultInstance();
        itemStack.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
        colorStackMap.put(color, itemStack);
        return itemStack;
    }

    public void cancel() {
        this.canceled = true;
        this.cleanup();
    }

    private void cleanup() {
        this.frames.clear();
        this.STACK_CACHED.clear();
        this.source = null;
    }

    public SpellcardRenderer copy() {
        return new SpellcardRenderer(this.frames);
    }
}
