package cc.thonly.reverie_dreams.util;

import cc.thonly.reverie_dreams.block.MusicBlock;
import cc.thonly.reverie_dreams.item.prop.MusicalInstrumentItem;
import cc.thonly.reverie_dreams.server.DelayedTask;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import nota.model.RepeatMode;
import nota.model.Song;
import nota.player.EntitySongPlayer;
import nota.player.PositionSongPlayer;
import nota.player.SongPlayer;
import nota.utils.NBSDecoderPlus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SuppressWarnings("deprecation")
public final class TouhouNotaUtils {
    public static final String STR_PATH = "config/reverie_dreams/nota";
    public static final Path PATH = Paths.get(STR_PATH);
    public static final Map<String, SongPlayer> id2SongCache = new HashMap<>();
    public static final Map<World, Map<Long, SongPlayer>> blockMusicPlayCache = new HashMap<>();
    public static int MAX_DISTANCE = 32;

    static {
        try {
            if (!Files.exists(PATH)) {
                Files.createDirectories(PATH);
            }
        } catch (IOException e) {
            log.error("Failed to create directory: " + STR_PATH, e);
        }
    }

    public static void playAt(World world, BlockPos pos, String select) {
        if (select == null) {
            return;
        }

        String filename = select.intern();
        MinecraftServer server = world.getServer();

        assert server != null;
        PlayerManager playerManager = server.getPlayerManager();
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
            for (var sPlayer : playerManager.getPlayerList()) {
                psp.addPlayer(sPlayer);
            }
            psp.setPlaying(true);
            blockPos2SongPlayer.put(pos.asLong(), psp);
            AtomicInteger age = new AtomicInteger();
            DelayedTask.whenTick(server, () -> {
                if (world.isChunkLoaded(pos)) {
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
                    ServerWorld serverWorld = (ServerWorld) world;
                    ParticleEffect particleEffect = ParticleTypes.NOTE;
                    List<ServerPlayerEntity> players = serverWorld.getPlayers();
                    for (ServerPlayerEntity player : players) {
                        if (psp.hasPlayer(player)) continue;
                        double squaredDistance = pos.getSquaredDistance(player.getPos());
                        if (squaredDistance > MAX_DISTANCE * MAX_DISTANCE) continue;
                        psp.addPlayer(player);
                    }

                    double px = pos.getX() + 0.5;
                    double py = pos.getY() + 1;
                    double pz = pos.getZ() + 0.5;

                    serverWorld.spawnParticles(
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
        if (user.getWorld().isClient) {
            return;
        }
        String filename = playingMusic;
        playingMusic = playingMusic.replaceAll(" ", "_");
        playingMusic = playingMusic.toLowerCase();
        MinecraftServer server = user.getServer();
        assert server != null;
        PlayerManager playerManager = server.getPlayerManager();
        Song song;
        try {
            song = NBSDecoderPlus.parse(getFilePath(filename).toFile(), noteBlockInstrument);
        } catch (Exception e) {
            log.error("读取音乐失败: {}", playingMusic, e);
            if (user instanceof ServerPlayerEntity player) {
                player.sendMessage(Text.literal("§c无法读取音乐：" + playingMusic), false);
            }
            return;
        }

        String id = "music_" + user.getUuidAsString();

        SongPlayer prev = id2SongCache.get(id);
        if (prev != null) {
            prev.setPlaying(false);
            id2SongCache.remove(id);
        }

        EntitySongPlayer esp = new EntitySongPlayer(song);
        esp.setId(Identifier.of(UUID.randomUUID().toString()));
        esp.setEntity(user);
        esp.setDistance(32);
        esp.setRepeatMode(RepeatMode.NONE);
        for (var sPlayer : playerManager.getPlayerList()) {
            esp.addPlayer(sPlayer);
        }
        esp.setPlaying(true);
        id2SongCache.put(id, esp);
        DelayedTask.whenTick(server, () -> {
            ItemStack handStack = user.getMainHandStack();
            ItemStack offStack = user.getOffHandStack();
            return !(handStack.getItem() instanceof MusicalInstrumentItem) && !(offStack.getItem() instanceof MusicalInstrumentItem);
        }, 2, () -> {
            esp.setPlaying(false);
            id2SongCache.remove(id);
        }, () -> {
            if (esp.isPlaying()) {
                ServerWorld serverWorld = (ServerWorld) user.getWorld();
                ParticleEffect particleEffect = ParticleTypes.NOTE;

                Vec3d frontVec = user.getRotationVec(1.0F);

                double px = user.getX() + frontVec.x * 0.5;
                double py = user.getY() + user.getStandingEyeHeight() - 0.1;
                double pz = user.getZ() + frontVec.z * 0.5;

                serverWorld.spawnParticles(
                        particleEffect,
                        px, py, pz,
                        2,
                        0.05, 0.05, 0.05,
                        0.01
                );
            }
        });
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
