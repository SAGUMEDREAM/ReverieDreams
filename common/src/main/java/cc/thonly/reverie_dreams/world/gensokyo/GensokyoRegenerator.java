package cc.thonly.reverie_dreams.world.gensokyo;

import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoBlockStates;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 安全重新生成幻想乡维度。所有环境可用，由 GensokyoFeature.ENABLED 控制是否启用。
 *
 * <p>命令只写入一个待处理标记并保存世界；真正的目录移动发生在下一次服务器启动、
 * {@code MinecraftServer.createLevels()} 之前。此时维度尚未加载，区域文件没有打开，
 * 可以安全地把整个维度目录移动到 {@code world/backups/}，然后由新的 map.json 重新生成。</p>
 */
public final class GensokyoRegenerator {
    public static final String MARKER_FILE = "reverie_dreams_gensokyo_regenerate.json";

    private static final Logger LOGGER = LoggerFactory.getLogger("Gensokyo");

    private GensokyoRegenerator() {
    }

    public static Path worldRoot(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT);
    }

    public static Path markerPath(MinecraftServer server) {
        return worldRoot(server).resolve(MARKER_FILE);
    }

    public static Path dimensionPath(MinecraftServer server) {
        Identifier id = RDBuiltinLevels.getGensokyo().identifier();
        return worldRoot(server)
                .resolve("dimensions")
                .resolve(id.getNamespace())
                .resolve(id.getPath());
    }

    public static Path backupRoot(MinecraftServer server) {
        return worldRoot(server).resolve("backups");
    }

    public static Path request(MinecraftServer server, String requestedBy) throws IOException {
        if (!GensokyoFeature.isEnabled()) {
            throw new IllegalStateException("Gensokyo is disabled by GensokyoFeature.ENABLED");
        }
        Path marker = markerPath(server);
        Files.createDirectories(marker.getParent());
        JsonObject json = new JsonObject();
        json.addProperty("requestedAt", Instant.now().toString());
        json.addProperty("requestedBy", requestedBy == null || requestedBy.isBlank() ? "unknown" : requestedBy);
        json.addProperty("dimension", RDBuiltinLevels.getGensokyo().identifier().toString());
        json.addProperty("world", worldRoot(server).toString());
        String content = new GsonBuilder().setPrettyPrinting().create().toJson(json) + "\n";
        Files.writeString(marker, content, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        LOGGER.info("Gensokyo regeneration requested by {}; marker={}", requestedBy, marker);
        return marker;
    }

    public static boolean cancel(MinecraftServer server) throws IOException {
        boolean removed = Files.deleteIfExists(markerPath(server));
        if (removed) {
            LOGGER.info("Gensokyo regeneration marker cancelled.");
        }
        return removed;
    }

    /**
     * 在 createLevels 之前调用。成功移动后删除标记；失败则保留标记，下次启动再试。
     * 任何异常都不能阻止服务器启动。
     */
    public static void processPending(MinecraftServer server) {
        try {
            processPendingInternal(server);
        } catch (Throwable t) {
            LOGGER.error("Gensokyo regeneration check failed; marker kept for next startup.", t);
        }
    }

    private static void processPendingInternal(MinecraftServer server) {
        if (!GensokyoFeature.isEnabled()) {
            return;
        }
        Path marker = markerPath(server);
        if (!Files.isRegularFile(marker)) {
            return;
        }
        Path dimension = dimensionPath(server);
        if (!Files.exists(dimension)) {
            deleteMarkerQuietly(marker);
            LOGGER.info("Gensokyo dimension folder does not exist; regeneration marker cleared (fresh generation).");
            return;
        }
        List<Path> stranded = playersInGensokyo(worldRoot(server));
        if (!stranded.isEmpty()) {
            LOGGER.warn("Gensokyo regeneration paused: {} player data file(s) still reference Gensokyo. "
                    + "Log in with the old dimension, leave it, save, then restart. Marker kept.", stranded.size());
            return;
        }
        Path backup = nextBackupPath(server);
        try {
            Files.createDirectories(backup.getParent());
            try {
                Files.move(dimension, backup, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(dimension, backup);
            }
            deleteMarkerQuietly(marker);
            LOGGER.info("Gensokyo dimension moved to {}; it will regenerate from the current map data.", backup);
        } catch (IOException e) {
            LOGGER.error("Failed to move Gensokyo dimension to {}; marker kept for next startup.", backup, e);
        }
    }

    public static List<String> describe(MinecraftServer server) {
        List<String> lines = new ArrayList<>();
        Path marker = markerPath(server);
        Path dimension = dimensionPath(server);
        lines.add("幻想乡维度目录: " + dimension);
        lines.add("维度目录存在: " + Files.exists(dimension));
        lines.add("待处理重新生成: " + Files.isRegularFile(marker));
        if (Files.isRegularFile(marker)) {
            lines.add("标记文件: " + marker);
        }
        Path backups = backupRoot(server);
        if (Files.isDirectory(backups)) {
            try (Stream<Path> stream = Files.list(backups)) {
                stream.filter(Files::isDirectory)
                        .map(path -> path.getFileName().toString())
                        .filter(name -> name.startsWith("gensokyo-"))
                        .sorted()
                        .reduce((first, second) -> second)
                        .ifPresent(name -> lines.add("最近维度备份: " + backups.resolve(name)));
            } catch (IOException e) {
                lines.add("读取备份目录失败: " + e.getMessage());
            }
        }
        lines.add("执行 regenerate confirm 后请完全重启服务器；下次启动时旧维度会移动到 world/backups/。");
        return lines;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> command() {
        return Commands.literal("gensokyo")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("status").executes(GensokyoRegenerator::statusCommand))
                .then(cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoMapInstaller.repairCommand())
                .then(cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateCommands.command())
                .then(Commands.literal("regenerate")
                        .executes(GensokyoRegenerator::regenerateHelp)
                        .then(Commands.literal("confirm").executes(GensokyoRegenerator::regenerateCommand)))
                .then(Commands.literal("blockstate")
                        .then(Commands.argument("value", StringArgumentType.greedyString())
                                .executes(GensokyoRegenerator::blockStateCommand)))
                .then(Commands.literal("cancel").executes(GensokyoRegenerator::cancelCommand));
    }

    private static int statusCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!GensokyoFeature.isEnabled()) {
            source.sendFailure(Component.literal("幻想乡生成器已被 GensokyoFeature.ENABLED 关闭。"));
            return 0;
        }
        for (String line : describe(source.getServer())) {
            source.sendSuccess(() -> Component.literal(line), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int regenerateHelp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal(
                "安全重新生成幻想乡维度：请先让所有玩家离开幻想乡，并确认地图已保存/同步/重新构建。"
                        + "确认执行：/touhou gensokyo regenerate confirm"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int regenerateCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!GensokyoFeature.isEnabled()) {
            source.sendFailure(Component.literal("幻想乡生成器已被 GensokyoFeature.ENABLED 关闭。"));
            return 0;
        }
        MinecraftServer server = source.getServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.level().dimension().equals(RDBuiltinLevels.getGensokyo())) {
                source.sendFailure(Component.literal("有玩家仍在幻想乡，请先让所有玩家离开该维度再执行。"));
                return 0;
            }
        }
        try {
            server.saveEverything(false, true, true);
            List<Path> stranded = playersInGensokyo(worldRoot(server));
            if (!stranded.isEmpty()) {
                source.sendFailure(Component.literal(
                        "有 " + stranded.size() + " 个玩家的存档位置仍在幻想乡。"
                                + "请让他们登录并离开该维度、保存后再执行。"));
                return 0;
            }
            Path marker = request(server, source.getTextName());
            Path backups = backupRoot(server);
            source.sendSuccess(() -> Component.literal(
                    "已安排重新生成幻想乡维度。请完全重启服务器；下次启动时旧区域文件会移动到 "
                            + backups + "，然后从当前 map.json 重新生成。标记: " + marker), true);
            return Command.SINGLE_SUCCESS;
        } catch (IOException | RuntimeException e) {
            source.sendFailure(Component.literal("安排重新生成失败: " + e.getMessage()));
            return 0;
        }
    }

    private static int blockStateCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String value = StringArgumentType.getString(context, "value");
        try {
            BlockState state = GensokyoBlockStates.parse(value);
            source.sendSuccess(() -> Component.literal("解析结果: " + state), false);
            return Command.SINGLE_SUCCESS;
        } catch (RuntimeException e) {
            source.sendFailure(Component.literal("解析失败: " + e.getMessage()));
            return 0;
        }
    }

    private static int cancelCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!GensokyoFeature.isEnabled()) {
            source.sendFailure(Component.literal("幻想乡生成器已被 GensokyoFeature.ENABLED 关闭。"));
            return 0;
        }
        try {
            boolean removed = cancel(source.getServer());
            source.sendSuccess(() -> Component.literal(
                    removed ? "已取消待处理的幻想乡重新生成。" : "当前没有待处理的重新生成标记。"), false);
            return Command.SINGLE_SUCCESS;
        } catch (IOException e) {
            source.sendFailure(Component.literal("取消重新生成失败: " + e.getMessage()));
            return 0;
        }
    }

    private static List<Path> playersInGensokyo(Path worldRoot) {
        Path playerDir = worldRoot.resolve("playerdata");
        if (!Files.isDirectory(playerDir)) {
            return List.of();
        }
        List<Path> result = new ArrayList<>();
        try (Stream<Path> stream = Files.list(playerDir)) {
            for (Path file : stream.filter(path -> path.getFileName().toString().endsWith(".dat")).toList()) {
                try {
                    CompoundTag tag = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
                    String dimension = tag.getStringOr("Dimension", "");
                    if (RDBuiltinLevels.getGensokyo().identifier().toString().equals(dimension)) {
                        result.add(file);
                    }
                } catch (IOException e) {
                    LOGGER.warn("Failed to read player data {} while checking Gensokyo positions", file, e);
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to list player data in {} while checking Gensokyo positions", playerDir, e);
        }
        return result;
    }

    private static Path nextBackupPath(MinecraftServer server) {
        Path root = backupRoot(server);
        String base = "gensokyo-regenerate-" + System.currentTimeMillis();
        Path candidate = root.resolve(base);
        int suffix = 1;
        while (Files.exists(candidate)) {
            candidate = root.resolve(base + "-" + suffix++);
        }
        return candidate;
    }

    private static void deleteMarkerQuietly(Path marker) {
        try {
            Files.deleteIfExists(marker);
        } catch (IOException e) {
            LOGGER.warn("Failed to delete Gensokyo regeneration marker {}", marker, e);
        }
    }
}
