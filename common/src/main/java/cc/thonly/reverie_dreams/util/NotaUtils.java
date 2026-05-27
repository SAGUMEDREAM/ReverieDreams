package cc.thonly.reverie_dreams.util;

import cc.thonly.reverie_dreams.block.MusicBlock;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SuppressWarnings({"deprecation", "resource"})
public final class NotaUtils {
    public static final String STR_PATH = "config/reverie_dreams/nota";
    public static final Path PATH = Paths.get(STR_PATH);
    public static final Map<String, SongPlayer> id2SongCache = new HashMap<>();
    public static final Map<Level, Map<Long, SongPlayer>> blockMusicPlayCache = new HashMap<>();
    public static int MAX_DISTANCE = 64;

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
        if (select == null) {
            return;
        }

        String filename = select.intern();
        MinecraftServer server = world.getServer();

        assert server != null;
        PlayerList playerManager = server.getPlayerList();
        Song song;
        try {
            song = NBSDecoderPlus.parse(getFilePath(filename).toFile());
        } catch (Exception e) {
            log.error("读取音乐失败: {}", filename, e);
            return;
        }

        DelayedTask.create(server, 2, () -> {
            Map<Long, SongPlayer> blockPos2SongPlayer = blockMusicPlayCache.computeIfAbsent(world, k -> new HashMap<>());
            SongPlayer songPlayer = blockPos2SongPlayer.get(pos.asLong());
            if (songPlayer != null) {
                songPlayer.setPlaying(false);
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
                if (world.hasChunkAt(pos)) {
                    return false;
                }
                BlockState blockState = world.getBlockState(pos);
                return !(blockState.getBlock() instanceof MusicBlock);
            }, 4, () -> {
                psp.setPlaying(false);
                blockPos2SongPlayer.remove(pos.asLong());
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
                        if (psp.hasPlayer(player)) continue;
                        double squaredDistance = pos.distToCenterSqr(player.position());
                        if (squaredDistance > MAX_DISTANCE * MAX_DISTANCE) continue;
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
            song = NBSDecoderPlus.parse(getFilePath(filename).toFile(), noteBlockInstrument);
        } catch (Exception e) {
            log.error("Can't read NBS: {}", playingMusic, e);
            if (user instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.literal("§c无法读取音乐：" + playingMusic), false);
            }
            return;
        }

        String playId = getPlayId(user);

        SongPlayer prev = id2SongCache.get(playId);
        if (prev != null) {
            prev.setPlaying(false);
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
            ItemStack handStack = user.getMainHandItem();
            ItemStack offStack = user.getOffhandItem();
            ItemStack headStack = user.getItemBySlot(EquipmentSlot.HEAD);
            if (!esp.isPlaying()) {
                return false;
            }
            return !(headStack.getItem() instanceof MusicalInstrumentItem)
                    && !(handStack.getItem() instanceof MusicalInstrumentItem)
                    && !(offStack.getItem() instanceof MusicalInstrumentItem);
        }, 3, () -> {
            esp.setPlaying(false);
            id2SongCache.remove(playId);
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
        SongPlayer songPlayer = id2SongCache.get(playId);
        songPlayer.destroy();
        id2SongCache.remove(playId);
    }

    public static boolean isPlaying(LivingEntity entity) {
        return id2SongCache.containsKey(getPlayId(entity));
    }

    public static String getPlayId(LivingEntity entity) {
        return "music_" + entity.getStringUUID();
    }

    public static List<String> getFileNames() {
        try {
            return Files.list(PATH)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".nbs"))
                    .map(path -> path.getFileName().toString())
                    .toList();
        } catch (Exception e) {
            log.warn("扫描音乐目录失败", e);
            return new ArrayList<>();
        }
    }

    public static Path getFilePath(String filename) {
        return PATH.resolve(filename);
    }

}
