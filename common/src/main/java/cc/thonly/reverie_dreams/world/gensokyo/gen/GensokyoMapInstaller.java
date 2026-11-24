package cc.thonly.reverie_dreams.world.gensokyo.gen;

import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapData;
import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 把 template 类型锚点放进 Gensokyo 的安装器。
 *
 * <p>建筑本体在区块FEATURES阶段完成。这里仅在周围建筑墙体就绪后处理装饰实体，
 * 不在玩家已看到的区块里慢慢拼墙，也不请求同步生成新区块。</p>
 */
public final class GensokyoMapInstaller {
    private static final Logger LOGGER = LoggerFactory.getLogger(GensokyoMapInstaller.class);
    private static final int INITIAL_DELAY_TICKS = 20;
    private static final long BATCH_BUDGET_NANOS = 8_000_000L;

    /**
     * 保存每个服务器实例的安装队列。WeakHashMap 避免服务器关闭后继续持有服务器对象。
     */
    private static final Map<MinecraftServer, InstallQueue> INSTALL_QUEUES = new WeakHashMap<>();

    private GensokyoMapInstaller() {
    }

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> repairCommand() {
        return net.minecraft.commands.Commands.literal("repair")
                .then(net.minecraft.commands.Commands.argument("anchor", net.minecraft.commands.arguments.IdentifierArgument.id())
                        .executes(context -> {
                            var source = context.getSource();
                            var level = source.getServer().getLevel(RDBuiltinLevels.getGensokyo());
                            if (level == null || !(level.getChunkSource().getGenerator() instanceof GensokyoChunkGenerator generator))
                                return 0;
                            try {
                                var id = net.minecraft.commands.arguments.IdentifierArgument.getId(context, "anchor");
                                generator.authoredStructures(level).repair(id);
                                synchronized (INSTALL_QUEUES) {
                                    var old = INSTALL_QUEUES.remove(source.getServer());
                                    if (old != null)
                                        old.finished = true;
                                }
                                install(source.getServer());
                                source.sendSuccess(() -> net.minecraft.network.chat.Component.literal("已排队持久修复建筑 " + id + "：请离开建筑及可视范围；备份并安全卸载后恢复结构，保留容器和NPC状态。使用 structure status 查看进度。"), true);
                                return 1;
                            } catch (RuntimeException error) {
                                source.sendFailure(net.minecraft.network.chat.Component.literal(error.getMessage()));
                                return 0;
                            }
                        }));
    }

    /**
     * 请求支撑就绪后的装饰收尾；建筑本体由生成器在FEATURES阶段完成。
     */
    public static void install(MinecraftServer server) {
        if (server == null) {
            return;
        }
        synchronized (INSTALL_QUEUES) {
            if (INSTALL_QUEUES.containsKey(server)) {
                return;
            }
            InstallQueue queue = new InstallQueue(server);
            INSTALL_QUEUES.put(server, queue);
            LOGGER.info("Queued {} Gensokyo anchor(s) for support-safe decoration finalization", GensokyoMapLoader.get(GensokyoChunkGenerator.DEFAULT_MAP).anchors().size());
            DelayedTask.create(server, INITIAL_DELAY_TICKS, queue::tick);
        }
    }

    /**
     * Voxel-resamples a template into a scaled template. Block states and block
     * entity NBT are copied; structure entity positions are scaled so entities
     * are still spawned (entity models themselves are not enlarged).
     */
    static StructureTemplate scaledTemplate(ServerLevel level, StructureTemplate source, float scale) {
        CompoundTag saved = source.save(new CompoundTag());
        ListTag sizeTag = saved.getListOrEmpty(StructureTemplate.SIZE_TAG);
        if (sizeTag.size() != 3) {
            return null;
        }
        int sizeX = sizeTag.getIntOr(0, 0);
        int sizeY = sizeTag.getIntOr(1, 0);
        int sizeZ = sizeTag.getIntOr(2, 0);
        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) {
            return null;
        }

        long sourceVolume = (long) sizeX * sizeY * sizeZ;
        double maxScale = Math.cbrt(1_500_000.0 / Math.max(1L, sourceVolume));
        double effectiveScale = Math.min(scale, Math.max(0.1, maxScale));
        int targetX = Math.max(1, (int) Math.round(sizeX * effectiveScale));
        int targetY = Math.max(1, (int) Math.round(sizeY * effectiveScale));
        int targetZ = Math.max(1, (int) Math.round(sizeZ * effectiveScale));
        if ((long) targetX * targetY * targetZ > 1_600_000L) {
            return null;
        }

        ListTag palette = saved.getListOrEmpty(StructureTemplate.PALETTE_TAG);
        if (palette.size() == 0) {
            ListTag palettes = saved.getListOrEmpty(StructureTemplate.PALETTE_LIST_TAG);
            if (palettes.size() > 0) {
                palette = palettes.getListOrEmpty(0);
            }
        }
        if (palette.size() == 0) {
            return null;
        }
        boolean[] skipped = new boolean[palette.size()];
        for (int i = 0; i < palette.size(); i++) {
            String name = palette.getCompoundOrEmpty(i).getStringOr("Name", "minecraft:air");
            skipped[i] = isSkippedBlockName(name);
        }

        ListTag blocks = saved.getListOrEmpty(StructureTemplate.BLOCKS_TAG);
        Map<BlockPos, Integer> states = new HashMap<>();
        Map<BlockPos, CompoundTag> blockNbt = new HashMap<>();
        for (int i = 0; i < blocks.size(); i++) {
            CompoundTag block = blocks.getCompoundOrEmpty(i);
            ListTag position = block.getListOrEmpty("pos");
            if (position.size() != 3) {
                continue;
            }
            int blockX = position.getIntOr(0, 0);
            int blockY = position.getIntOr(1, 0);
            int blockZ = position.getIntOr(2, 0);
            int state = block.getIntOr("state", -1);
            if (state < 0 || state >= skipped.length || skipped[state]) {
                continue;
            }
            BlockPos sourcePos = new BlockPos(blockX, blockY, blockZ);
            states.put(sourcePos, state);
            if (block.contains("nbt")) {
                blockNbt.put(sourcePos, block.getCompoundOrEmpty("nbt").copy());
            }
        }
        if (states.isEmpty()) {
            return null;
        }

        ListTag targetBlocks = new ListTag();
        for (int targetBlockY = 0; targetBlockY < targetY; targetBlockY++) {
            int sourceBlockY = Math.min(sizeY - 1, Math.max(0, (int) Math.floor(targetBlockY / effectiveScale)));
            for (int targetBlockX = 0; targetBlockX < targetX; targetBlockX++) {
                int sourceBlockX = Math.min(sizeX - 1, Math.max(0, (int) Math.floor(targetBlockX / effectiveScale)));
                for (int targetBlockZ = 0; targetBlockZ < targetZ; targetBlockZ++) {
                    int sourceBlockZ = Math.min(sizeZ - 1, Math.max(0, (int) Math.floor(targetBlockZ / effectiveScale)));
                    BlockPos sourcePos = new BlockPos(sourceBlockX, sourceBlockY, sourceBlockZ);
                    Integer state = states.get(sourcePos);
                    if (state == null) {
                        continue;
                    }
                    CompoundTag target = new CompoundTag();
                    ListTag targetPosition = new ListTag();
                    targetPosition.add(IntTag.valueOf(targetBlockX));
                    targetPosition.add(IntTag.valueOf(targetBlockY));
                    targetPosition.add(IntTag.valueOf(targetBlockZ));
                    target.put("pos", targetPosition);
                    target.putInt("state", state);
                    CompoundTag nbt = blockNbt.get(sourcePos);
                    if (nbt != null) {
                        target.put("nbt", nbt.copy());
                    }
                    targetBlocks.add(target);
                }
            }
        }

        ListTag targetEntities = new ListTag();
        ListTag entities = saved.getListOrEmpty(StructureTemplate.ENTITIES_TAG);
        for (int i = 0; i < entities.size(); i++) {
            CompoundTag entity = entities.getCompoundOrEmpty(i).copy();
            ListTag position = entity.getListOrEmpty("pos");
            if (position.size() == 3) {
                ListTag targetPosition = new ListTag();
                targetPosition.add(DoubleTag.valueOf(position.getDoubleOr(0, 0.0) * effectiveScale));
                targetPosition.add(DoubleTag.valueOf(position.getDoubleOr(1, 0.0) * effectiveScale));
                targetPosition.add(DoubleTag.valueOf(position.getDoubleOr(2, 0.0) * effectiveScale));
                entity.put("pos", targetPosition);
            }
            ListTag blockPosition = entity.getListOrEmpty("blockPos");
            if (blockPosition.size() == 3) {
                ListTag targetBlockPosition = new ListTag();
                targetBlockPosition.add(IntTag.valueOf((int) Math.floor(blockPosition.getIntOr(0, 0) * effectiveScale)));
                targetBlockPosition.add(IntTag.valueOf((int) Math.floor(blockPosition.getIntOr(1, 0) * effectiveScale)));
                targetBlockPosition.add(IntTag.valueOf((int) Math.floor(blockPosition.getIntOr(2, 0) * effectiveScale)));
                entity.put("blockPos", targetBlockPosition);
            }
            targetEntities.add(entity);
        }

        ListTag targetSize = new ListTag();
        targetSize.add(IntTag.valueOf(targetX));
        targetSize.add(IntTag.valueOf(targetY));
        targetSize.add(IntTag.valueOf(targetZ));
        CompoundTag scaledTag = new CompoundTag();
        scaledTag.put(StructureTemplate.SIZE_TAG, targetSize);
        scaledTag.put(StructureTemplate.PALETTE_TAG, palette.copy());
        scaledTag.put(StructureTemplate.BLOCKS_TAG, targetBlocks);
        scaledTag.put(StructureTemplate.ENTITIES_TAG, targetEntities);

        StructureTemplate scaled = new StructureTemplate();
        scaled.load(level.holderLookup(Registries.BLOCK), scaledTag);
        LOGGER.info(
                "Scaled Gensokyo template from {}x{}x{} to {}x{}x{} (scale {}, effective {})",
                sizeX, sizeY, sizeZ, targetX, targetY, targetZ, scale, effectiveScale
        );
        return scaled;
    }

    private static boolean isSkippedBlockName(String name) {
        return "minecraft:structure_void".equals(name);
    }

    private static Rotation parseRotation(String value) {
        if (value == null) {
            return Rotation.NONE;
        }
        try {
            String normalized = value.trim().toLowerCase(Locale.ROOT);
            // Backward compatibility: older editor versions stored 180 as "180".
            if (normalized.equals("180")) {
                return Rotation.CLOCKWISE_180;
            }
            return Rotation.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            LOGGER.warn("Unknown structure rotation '{}', using NONE", value);
            return Rotation.NONE;
        }
    }

    private static Mirror parseMirror(String value) {
        if (value == null) {
            return Mirror.NONE;
        }
        try {
            return Mirror.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            LOGGER.warn("Unknown structure mirror '{}', using NONE", value);
            return Mirror.NONE;
        }
    }

    private static final class InstallQueue {
        private final java.lang.ref.WeakReference<MinecraftServer> server;
        private boolean finished;

        private InstallQueue(MinecraftServer server) {
            this.server = new java.lang.ref.WeakReference<>(server);
        }

        private void tick() {
            if (this.finished)
                return;
            MinecraftServer actual = this.server.get();
            if (actual == null) {
                this.finished = true;
                return;
            }
            ServerLevel level = actual.getLevel(RDBuiltinLevels.getGensokyo());
            if (level == null || !(level.getChunkSource().getGenerator() instanceof GensokyoChunkGenerator generator)) {
                this.finished = true;
                return;
            }
            try {
                var updates = generator.authoredStructures(level).updates();
                updates.tick();
                if (actual.getPlayerList().getPlayers().stream().noneMatch(p -> p.level() == level)) {
                    DelayedTask.create(actual, updates.hasWork() ? 1 : 40, this::tick);
                    return;
                }
                generator.authoredStructures(level).finishReady(System.nanoTime() + BATCH_BUDGET_NANOS);
                generator.authoredStructures(level).tickAdventure();
            } catch (Exception error) {
                LOGGER.error("Could not finalize fixed structure decoration", error);
            }
            DelayedTask.create(actual, 1, this::tick);
        }
    }
}
