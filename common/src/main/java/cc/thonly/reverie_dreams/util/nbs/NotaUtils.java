package cc.thonly.reverie_dreams.util.nbs;

import cc.thonly.reverie_dreams.block.props.MusicBlock;
import cc.thonly.reverie_dreams.item.prop.MusicalInstrumentItem;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.server.nota.model.RepeatMode;
import cc.thonly.reverie_dreams.server.nota.model.Song;
import cc.thonly.reverie_dreams.server.nota.player.EntitySongPlayer;
import cc.thonly.reverie_dreams.server.nota.player.PositionSongPlayer;
import cc.thonly.reverie_dreams.server.nota.player.SongPlayer;
import cc.thonly.reverie_dreams.server.nota.utils.NBSDecoderPlus;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.Vec3;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;

@Slf4j
@SuppressWarnings({"deprecation", "resource"})
public final class NotaUtils {
    public static final String STR_PATH = "config/reverie_dreams/nota";
    public static final Path PATH = Paths.get(STR_PATH);
    public static final Map<String, SongPlayer> id2SongCache = new HashMap<>();
    public static final Map<Level, Map<Long, SongPlayer>> blockMusicPlayCache = new HashMap<>();
    public static int MAX_DISTANCE = 64;
    private static List<String> fileListCache = List.of();
    private static long fileListExpires;

    private record ParsedSong(Path file, long size, long modified, NoteBlockInstrument instrument) {
    }

    private static final Map<ParsedSong, Song> parsedSongs = new LinkedHashMap<>(32, .75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<ParsedSong, Song> eldest) {
            return size() > 32;
        }
    };

    private static synchronized Song readSong(Path file, NoteBlockInstrument instrument) throws Exception {
        var key = new ParsedSong(file.toAbsolutePath().normalize(), Files.size(file), Files.getLastModifiedTime(file).toMillis(), instrument);
        var song = parsedSongs.get(key);
        if (song == null) {
            song = NBSDecoderPlus.parse(file.toFile(), instrument);
            if (song != null)
                parsedSongs.put(key, song);
        }
        return song;
    }

    static {
        try {
            if (!Files.exists(PATH)) {
                Files.createDirectories(PATH);
            }
        } catch (IOException e) {
            log.error("Failed to create directory: " + STR_PATH, e);
        }
    }

    public static void playAt(Level world, BlockPos pos, String select) {
        if (world.isClientSide() || select == null || select.isEmpty()) {
            return;
        }

        MinecraftServer server = world.getServer();
        if (server == null) {
            return;
        }
        // 区块未加载时绝不能读取方块状态，否则 getBlockState 会把区块重新加载。
        if (!world.hasChunkAt(pos)) {
            return;
        }

        String filename = select.intern();
        PlayerList playerManager = server.getPlayerList();
        Song song;
        try {
            song = NBSDecoderPlus.parse(getFilePath(filename).toFile());
        } catch (FileNotFoundException e) {
            return;
        } catch (Exception e) {
            log.error("Failed to read music: {}", filename, e);
            return;
        }

        DelayedTask.create(server, 2, () -> {
            if (song == null || !world.hasChunkAt(pos)) {
                return;
            }
            Map<Long, SongPlayer> blockPos2SongPlayer = blockMusicPlayCache.computeIfAbsent(world, k -> new HashMap<>());
            SongPlayer songPlayer = blockPos2SongPlayer.get(pos.asLong());
            if (songPlayer != null) {
                songPlayer.destroy();
                blockPos2SongPlayer.remove(pos.asLong());
            }

            PositionSongPlayer psp = new PositionSongPlayer(song, world);
            psp.setBlockPos(new BlockPos(pos));
            psp.setDistance(MAX_DISTANCE);
            psp.setRepeatMode(RepeatMode.ALL);
            for (var sPlayer : playerManager.getPlayers()) {
                psp.addPlayer(sPlayer);
            }
            psp.setPlaying(true);
            blockPos2SongPlayer.put(pos.asLong(), psp);
            AtomicInteger age = new AtomicInteger();
            DelayedTask.whenTick(server, () -> {
                // 已经停止：不要再读区块，直接结束定时任务。
                if (!psp.isPlaying()) {
                    return true;
                }
                // 区块已卸载：立即结束，绝不能调用 getBlockState，否则会重新加载区块。
                if (!world.hasChunkAt(pos)) {
                    return true;
                }
                BlockState blockState = world.getBlockState(pos);
                return !(blockState.getBlock() instanceof MusicBlock);
            }, 4, () -> {
                // destroy() 会取消 Timer 线程，并从 NotaAPI 的玩家列表里移除自己。
                psp.destroy();
                blockPos2SongPlayer.remove(pos.asLong(), psp);
            }, () -> {
                if (age.get() <= 4) {
                    age.getAndIncrement();
                } else {
                    age.set(0);
                }
                if (psp.isPlaying()) {
                    ServerLevel serverWorld = (ServerLevel) world;
                    ParticleOptions particleEffect = ParticleTypes.NOTE;
                    List<ServerPlayer> players = serverWorld.players();
                    for (ServerPlayer player : players) {
                        if (psp.hasPlayer(player))
                            continue;
                        double squaredDistance = pos.distToCenterSqr(player.position());
                        if (squaredDistance > MAX_DISTANCE * MAX_DISTANCE)
                            continue;
                        psp.addPlayer(player);
                    }

                    double px = pos.getX() + 0.5;
                    double py = pos.getY() + 1;
                    double pz = pos.getZ() + 0.5;

                    serverWorld.sendParticles(
                            particleEffect,
                            px, py, pz,
                            1,
                            0, 0, 0,
                            0.01
                    );
                }
            });
        });
    }

    public static void play(LivingEntity user, String playingMusic, NoteBlockInstrument noteBlockInstrument) {
        if (user.level().isClientSide()) {
            return;
        }
        String filename = playingMusic;
        playingMusic = playingMusic.replaceAll(" ", "_");
        playingMusic = playingMusic.toLowerCase();
        MinecraftServer server = user.level().getServer();
        if (server == null) {
            return;
        }
        PlayerList playerManager = server.getPlayerList();
        Song song;
        try {
            song = readSong(getFilePath(filename), noteBlockInstrument);
        } catch (Exception e) {
            log.error("Can't read NBS: {}", playingMusic, e);
            if (user instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.literal("§c无法读取音乐：" + playingMusic), false);
            }
            return;
        }

        if (song == null) {
            return;
        }

        String playId = getPlayId(user);

        SongPlayer prev = id2SongCache.get(playId);
        if (prev != null) {
            prev.destroy();
            id2SongCache.remove(playId);
        }

        EntitySongPlayer esp = new EntitySongPlayer(song);
        esp.setId(Identifier.parse(UUID.randomUUID().toString()));
        esp.setEntity(user);
        esp.setDistance(MAX_DISTANCE);
        esp.setRepeatMode(RepeatMode.NONE);
        for (var sPlayer : playerManager.getPlayers()) {
            esp.addPlayer(sPlayer);
        }
        esp.setPlaying(true);
        id2SongCache.put(playId, esp);
        DelayedTask.whenTick(server, () -> {
            // 歌曲自然结束、玩家下线或已经没有乐器时，结束并清理定时任务。
            boolean managedNPC = user instanceof BaseNPCLikeEntity npc
                    && npc.getWorkMode() == NPCWorkModes.PLAYING_MUSIC
                    && npc.getWorkController().working();
            // Keep a finished NPC player until its work goal observes completion and advances the playlist.
            if ((!esp.isPlaying() && !managedNPC) || user.isRemoved()) {
                return true;
            }
            ItemStack handStack = user.getMainHandItem();
            ItemStack offStack = user.getOffhandItem();
            ItemStack headStack = user.getItemBySlot(EquipmentSlot.HEAD);
            return !(headStack.getItem() instanceof MusicalInstrumentItem)
                    && !(handStack.getItem() instanceof MusicalInstrumentItem)
                    && !(offStack.getItem() instanceof MusicalInstrumentItem);
        }, 3, () -> {
            esp.destroy();
            id2SongCache.remove(playId, esp);
        }, () -> {
            if (esp.isPlaying()) {
                ServerLevel serverWorld = (ServerLevel) user.level();
                ParticleOptions particleEffect = ParticleTypes.NOTE;

                Vec3 frontVec = user.getViewVector(1.66F);

                double px = user.getX() + frontVec.x * 0.5;
                double py = user.getY() + user.getEyeHeight() - 0.1;
                double pz = user.getZ() + frontVec.z * 0.5;

                serverWorld.sendParticles(
                        particleEffect,
                        px, py, pz,
                        2,
                        0.05, 0.05, 0.05,
                        0.01
                );
            }
        });
    }

    public static void stop(LivingEntity entity) {
        Level level = entity.level();
        MinecraftServer server = level.getServer();
        if (server == null || level.isClientSide()) {
            return;
        }
        String playId = getPlayId(entity);
        SongPlayer songPlayer = id2SongCache.remove(playId);
        if (songPlayer != null) {
            songPlayer.destroy();
        }
    }

    /**
     * 服务器停止时清空所有音乐播放器和缓存，避免 Timer 线程、Level 对象跨存档残留。
     */
    public static synchronized void clearCaches() {
        parsedSongs.clear();
        fileListCache = List.of();
        fileListExpires = 0;
        for (SongPlayer player : new HashSet<>(id2SongCache.values())) {
            player.destroy();
        }
        id2SongCache.clear();
        for (Map<Long, SongPlayer> players : blockMusicPlayCache.values()) {
            for (SongPlayer player : players.values()) {
                player.destroy();
            }
        }
        blockMusicPlayCache.clear();
    }

    public static boolean isPlaying(LivingEntity entity) {
        return id2SongCache.containsKey(getPlayId(entity));
    }

    public static String getPlayId(LivingEntity entity) {
        return "music_" + entity.getStringUUID();
    }

    public static List<String> getFileNames() {
        long now = System.nanoTime();
        if (now < fileListExpires)
            return fileListCache;
        try (var stream = Files.list(PATH)) {
            fileListCache = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".nbs"))
                    .map(path -> path.getFileName().toString())
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
            fileListExpires = now + 2_000_000_000L;
            return fileListCache;
        } catch (Exception e) {
            log.warn("Failed to scan music directory", e);
            return new ArrayList<>();
        }
    }

    public static Path getFilePath(String filename) {
        return PATH.resolve(filename);
    }

    public static boolean activelyPlaying(LivingEntity user) {
        var player = id2SongCache.get(getPlayId(user));
        return player != null && player.isPlaying();
    }

    public static int playbackTick(LivingEntity user) {
        var player = id2SongCache.get(getPlayId(user));
        return player == null ? -1 : player.getTick();
    }

    public static void configurePlayback(LivingEntity user, int range, int volume, int resumeTick) {
        var player = id2SongCache.get(getPlayId(user));
        if (player == null)
            return;
        player.setVolume((byte) Math.clamp(volume, 0, 100));
        if (player instanceof EntitySongPlayer entityPlayer)
            entityPlayer.setDistance(Math.clamp(range, 8, 128));
        if (resumeTick >= 0)
            player.setTick((short) Math.min(Short.MAX_VALUE, resumeTick));
    }

    public static void pause(LivingEntity user, boolean paused) {
        var player = id2SongCache.get(getPlayId(user));
        if (player != null)
            player.setPlaying(!paused);
    }

}
