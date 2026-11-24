package cc.thonly.reverie_dreams.client.audio.radio;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.decor.RecordPlayerBlock;
import cc.thonly.reverie_dreams.block.entity.RecordPlayerBlockEntity;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Same URL shares a channel; different URLs remain independent and may play simultaneously.
 */
public final class RadioAudioClient {
    private static final Set<BlockPos> SOURCES = new HashSet<>();

    private record Occlusion(boolean blocked, int checked) {
    }

    private static final Map<BlockPos, Occlusion> OBSTRUCTION = new HashMap<>();
    private static final Map<String, Channel> CHANNELS = new HashMap<>();
    private static final Map<Identifier, Channel> AUDIO = new ConcurrentHashMap<>();
    private static Level world;
    private static boolean initialized, bookmarksDirty;
    private static int ticks;
    private static long serial;
    private static final AudioStream ENDED = new AudioStream() {
        public AudioFormat getFormat() {
            return new AudioFormat(RadioPcm.RATE, 16, 1, true, false);
        }

        public ByteBuffer read(int size) {
            return ByteBuffer.allocateDirect(0);
        }

        public void close() {
        }
    };

    private static final class Channel {
        final String url;
        final Identifier audio = ReverieDreams.id("radio/session_" + ++serial);
        final Identifier path = ReverieDreams.id("sounds/" + audio.getPath() + ".ogg");
        final long resume;
        volatile float volume;
        float target;
        RadioSound sound;
        volatile RadioStream stream;
        boolean allowed = true, finished;
        int started;

        Channel(String url) {
            this.url = url;
            resume = Math.max(0, config().positions.getOrDefault(url, 0L));
            AUDIO.put(path, this);
        }

        synchronized AudioStream audio() {
            if (!allowed)
                return ENDED;
            if (stream == null || stream.isClosed())
                stream = new RadioStream(url, resume);
            return stream;
        }

        synchronized void close() {
            allowed = false;
            AUDIO.remove(path, this);
            if (stream != null) {
                bookmark(this);
                stream.close();
                stream = null;
            }
            if (sound != null) {
                Minecraft.getInstance().getSoundManager().stop(sound);
                sound = null;
            }
        }
    }

    private RadioAudioClient() {
    }

    public static void initialize() {
        if (!initialized) {
            AutoConfig.register(RadioConfiguration.class, GsonConfigSerializer::new);
            initialized = true;
            if (config().positions == null)
                config().positions = new LinkedHashMap<>();
            config().positions.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue() < 0);
            trimBookmarks();
        }
    }

    public static RadioConfiguration config() {
        return AutoConfig.getConfigHolder(RadioConfiguration.class).getConfig();
    }

    public static void save() {
        AutoConfig.getConfigHolder(RadioConfiguration.class).save();
        tick(Minecraft.getInstance());
    }

    public static float volume() {
        return CHANNELS.values().stream().map(channel -> channel.volume).max(Float::compare).orElse(0f);
    }

    public static void activationHint(Player player) {
        if (initialized && !config().enabled)
            player.sendOverlayMessage(Component.translatable("reverie_dreams.radio.disabled_hint"));
    }

    public static String status() {
        if (!config().enabled)
            return "disabled";
        if (CHANNELS.isEmpty())
            return "idle";
        if (CHANNELS.values().stream().anyMatch(channel -> channel.stream != null && channel.stream.status() == RadioStream.Status.PLAYING))
            return "playing";
        if (CHANNELS.values().stream().anyMatch(channel -> channel.stream != null && channel.stream.status() == RadioStream.Status.RECONNECTING))
            return "reconnecting";
        return CHANNELS.values().stream().allMatch(channel -> channel.finished) ? "finished" : "connecting";
    }

    public static void source(Level level, BlockPos pos) {
        if (!initialized || !config().enabled)
            return;
        if (world != level) {
            reset();
            world = level;
        }
        SOURCES.add(pos.immutable());
    }

    public static void tick(Minecraft client) {
        if (!initialized)
            return;
        if (world != client.level) {
            reset();
            world = client.level;
        }
        ticks++;
        if (!config().enabled || client.level == null || client.player == null || client.isPaused()
                || client.options.getSoundSourceVolume(SoundSource.MASTER) <= 0 || client.options.getSoundSourceVolume(SoundSource.RECORDS) <= 0) {
            stop();
            return;
        }
        SOURCES.removeIf(pos -> !client.level.hasChunkAt(pos) || !(client.level.getBlockState(pos).getBlock() instanceof RecordPlayerBlock)
                || !client.level.getBlockState(pos).getValue(RecordPlayerBlock.PLAYING));
        Vec3 listener = client.player.getEyePosition();
        var nearby = SOURCES.stream().filter(pos -> speaker(pos).distanceToSqr(listener) < RadioVolume.RANGE * RadioVolume.RANGE)
                .sorted(Comparator.comparingDouble(pos -> speaker(pos).distanceToSqr(listener))).toList();
        OBSTRUCTION.keySet().retainAll(SOURCES);
        if (config().occlusion && ticks % 10 == 0) {
            nearby.stream().sorted(Comparator.comparingInt(pos -> OBSTRUCTION.getOrDefault(pos, new Occlusion(true, Integer.MIN_VALUE)).checked()))
                    .limit(8).forEach(pos -> OBSTRUCTION.put(pos, new Occlusion(client.level.clip(new ClipContext(speaker(pos), listener,
                            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, client.player)).getType() != HitResult.Type.MISS, ticks)));
        }
        Map<String, Float> gains = new HashMap<>();
        for (var pos : nearby) {
            if (!(client.level.getBlockEntity(pos) instanceof RecordPlayerBlockEntity entity))
                continue;
            float gain = RadioVolume.atDistance(speaker(pos).distanceTo(listener));
            if (config().occlusion && OBSTRUCTION.getOrDefault(pos, new Occlusion(true, 0)).blocked())
                gain *= .45f;
            gains.merge(entity.mediaUrl(), gain, RadioVolume::merge);
        }
        gains.keySet().forEach(url -> CHANNELS.computeIfAbsent(url, Channel::new));
        var iterator = CHANNELS.values().iterator();
        while (iterator.hasNext()) {
            Channel channel = iterator.next();
            channel.target = gains.getOrDefault(channel.url, 0f);
            channel.volume = RadioVolume.smooth(channel.volume, channel.target);
            if (channel.target == 0 && (channel.volume < .002f || channel.finished)) {
                channel.close();
                iterator.remove();
                continue;
            }
            if (channel.stream != null && channel.stream.isFinished()) {
                channel.finished = true;
                bookmark(channel);
            }
            if (!channel.finished && (channel.sound == null || ticks - channel.started > 20 && !client.getSoundManager().isActive(channel.sound))) {
                // A resource/device reload may discard a native channel. Preserve its file bookmark.
                if (channel.sound != null) {
                    String url = channel.url;
                    float gain = channel.volume;
                    channel.close();
                    channel = new Channel(url);
                    channel.volume = gain;
                    // Defer replacement to avoid modifying this iterator's backing map.
                    iterator.remove();
                    pendingReplacement = channel;
                    break;
                }
                Identifier path = channel.path;
                channel.sound = new RadioSound(channel.audio, () -> gainFor(path));
                channel.started = ticks;
                client.getSoundManager().play(channel.sound);
            }
            if (ticks % 600 == 0)
                bookmark(channel);
        }
        if (pendingReplacement != null) {
            CHANNELS.put(pendingReplacement.url, pendingReplacement);
            pendingReplacement = null;
        }
        if (bookmarksDirty)
            flushBookmarks();
    }

    private static Channel pendingReplacement;

    private static double gainFor(Identifier path) {
        Channel channel = AUDIO.get(path);
        return channel == null ? 0 : channel.volume;
    }

    /**
     * Only our generated paths are intercepted. Late callbacks cannot resurrect a stopped URL.
     */
    public static AudioStream audio(Identifier path) {
        Channel channel = AUDIO.get(path);
        if (channel != null)
            return channel.audio();
        return path.getNamespace().equals("reverie_dreams") && path.getPath().startsWith("sounds/radio/session_") ? ENDED : null;
    }

    private static void bookmark(Channel channel) {
        RadioStream stream = channel.stream;
        if (stream == null || stream.framesDecoded() == 0)
            return;
        if (stream.isFinite() && !channel.finished && !stream.isFinished()) {
            Long previous = config().positions.put(channel.url, stream.positionSamples());
            bookmarksDirty |= previous == null || previous != stream.positionSamples();
        } else {
            bookmarksDirty |= config().positions.remove(channel.url) != null;
        }
        trimBookmarks();
    }

    private static void trimBookmarks() {
        while (config().positions.size() > 64) {
            config().positions.remove(config().positions.keySet().iterator().next());
            bookmarksDirty = true;
        }
    }

    private static void flushBookmarks() {
        if (initialized && bookmarksDirty) {
            AutoConfig.getConfigHolder(RadioConfiguration.class).save();
            bookmarksDirty = false;
        }
    }

    public static void stop() {
        CHANNELS.values().forEach(Channel::close);
        CHANNELS.clear();
        flushBookmarks();
    }

    private static Vec3 speaker(BlockPos pos) {
        return Vec3.atLowerCornerOf(pos).add(.5, .6, .5);
    }

    public static void reset() {
        stop();
        SOURCES.clear();
        OBSTRUCTION.clear();
        world = null;
    }
}
