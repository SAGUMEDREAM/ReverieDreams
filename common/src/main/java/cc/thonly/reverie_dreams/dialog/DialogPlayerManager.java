package cc.thonly.reverie_dreams.dialog;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DialogPlayerManager {
    public static final Map<String, DialogPlayer> PLAYER_INSTANCES = new Object2ObjectOpenHashMap<>();
    public static final Map<String, String> FILENAME2UID = new Object2ObjectOpenHashMap<>();
    public static final Map<String, Boolean> LOADED = new Object2ObjectOpenHashMap<>();

    public static void handleStopVideoDialog(ServerPlayer player, ServerboundCustomClickActionPacket packet) {
        Optional<Tag> payload = packet.payload();
        if (payload.isEmpty()) {
            return;
        }
        Tag element = payload.get();
        if (!(element instanceof CompoundTag compound)) {
            return;
        }
        Optional<String> uidOptional = compound.getString("uid");
        if (uidOptional.isEmpty()) {
            return;
        }
        String uid = uidOptional.get();
        for (Map.Entry<String, DialogPlayer> entry : DialogPlayerManager.PLAYER_INSTANCES.entrySet()) {
            DialogPlayer dialogPlayer = entry.getValue();
            ServerPlayer serverPlayer = dialogPlayer.getPlayer();
            if (!player.equals(serverPlayer)) {
                continue;
            }
            if (player.getStringUUID().equals(uid)) {
                dialogPlayer.remove();
                SoundEvent soundEvent = dialogPlayer.getSoundEvent();
                if (soundEvent != null) {
                    ClientboundStopSoundPacket stopSoundS2CPacket = new ClientboundStopSoundPacket(soundEvent.location(), SoundSource.PLAYERS);
                    player.connection.send(stopSoundS2CPacket);
                }
            }
        }
    }

    public static synchronized DialogPlayer play(ServerPlayer player, String filename, @Nullable SoundEvent soundEvent) {
        if (DialogFiles.contain(filename)) {
            DialogFiles.Entry entry = DialogFiles.getEntry(filename);
            if (!LOADED.getOrDefault(filename, false)) {
                LOADED.put(filename, true);
                String uid = UUID.randomUUID().toString();
                FILENAME2UID.put(filename, uid);
                entry = DialogFiles.add(new DialogFiles.Entry(filename, uid));
            }
            if (entry == null) {
                return null;
            }
            DialogPlayer dialogPlayer = new DialogPlayer(player, entry, soundEvent);
            dialogPlayer.start();
            return dialogPlayer;
        }
        return null;
    }

    public static void reload() {
        LOADED.clear();
        FILENAME2UID.clear();
        PLAYER_INSTANCES.clear();
        DialogFiles.reload();
    }

    public static synchronized void tick(MinecraftServer server) {
        ServerTickRateManager tickManager = server.tickRateManager();
        if (!tickManager.isFrozen()) {
            for (Map.Entry<String, DialogPlayer> entry : PLAYER_INSTANCES.entrySet()) {
                entry.getValue().tick();
            }
        }
    }

    public static DialogPlayer getPlayerInstance(String uuid) {
        return PLAYER_INSTANCES.get(uuid);
    }
}
