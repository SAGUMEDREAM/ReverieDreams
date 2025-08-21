package cc.thonly.reverie_dreams.dialog;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.dialog.AfterAction;
import net.minecraft.dialog.DialogActionButtonData;
import net.minecraft.dialog.DialogButtonData;
import net.minecraft.dialog.DialogCommonData;
import net.minecraft.dialog.action.SimpleDialogAction;
import net.minecraft.dialog.body.PlainMessageDialogBody;
import net.minecraft.dialog.type.NoticeDialog;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Slf4j
@Getter
public class DialogPlayer {
    public static final Map<String, DialogPlayer> INSTANCES = new Object2ObjectOpenHashMap<>();
    public static final Map<String, String> FILENAME2UID = new Object2ObjectOpenHashMap<>();
    public static final Map<String, Boolean> LOADED = new Object2ObjectOpenHashMap<>();

    public static synchronized DialogPlayer play(ServerPlayerEntity player, String filename, @Nullable SoundEvent soundEvent) {
        if (DialogFiles.contain(filename)) {
            DialogFiles.Entry entry = DialogFiles.getEntry(filename);
            ;
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
        INSTANCES.clear();
        DialogFiles.reload();
    }

    public static synchronized void tick(MinecraftServer server) {
        ServerTickManager tickManager = server.getTickManager();
        if (!tickManager.isFrozen()) {
            for (Map.Entry<String, DialogPlayer> entry : INSTANCES.entrySet()) {
                entry.getValue().tick();
            }
        }
    }

    public static DialogPlayer getInstance(String uuid) {
        return INSTANCES.get(uuid);
    }

    private final ServerPlayerEntity player;
    private final String uuid;
    private LinkedList<LinkedList<String>> source = new LinkedList<>();
    private ListIterator<LinkedList<String>> iterator = null;
    private int state = 0;
    @Setter
    private boolean paused = true;
    @Nullable
    private SoundEvent soundEvent;

    public DialogPlayer(ServerPlayerEntity player, String fileId) {
        this.player = player;
        this.uuid = this.player.getUuid().toString();
        this.parse(fileId);
        INSTANCES.put(this.uuid, this);
    }

    public DialogPlayer(ServerPlayerEntity player, DialogFiles.Entry entry) {
        this(player, entry.getFileId());
    }

    public DialogPlayer(ServerPlayerEntity player, DialogFiles.Entry entry, @Nullable SoundEvent soundEvent) {
        this(player, entry.getFileId());
        this.soundEvent = soundEvent;
    }

    public synchronized void start() {
        this.iterator = this.source.listIterator();
        if (this.soundEvent != null) {
            this.player.getWorld().playSound(null, this.player.getBlockPos(), this.soundEvent, SoundCategory.PLAYERS);
        }
        DelayedTask.createFromSecond(Touhou.getServer(), 0.75f, () -> {
            this.paused = false;
        });
    }

    public synchronized void parse(String fileId) {
        DialogFiles.Entry file = DialogFiles.getFile(fileId);
        if (file == null) {
            return;
        }
        LinkedList<LinkedList<String>> data = new LinkedList<>();
        try {
            JsonElement json = file.json();
            if (json instanceof JsonArray main) {
                for (JsonElement element : main) {
                    LinkedList<String> frame = new LinkedList<>();
                    if (element instanceof JsonArray frameElement) {
                        for (JsonElement lineStr : frameElement) {
                            String line = lineStr.getAsString();
                            line = line.replaceAll("■", "■■");
                            frame.add(line);
                        }
                    }
                    data.add(frame);
                }
            }
            this.source = new LinkedList<>(data);
        } catch (Exception err) {
            log.error("Can't parse dialog video {}", fileId, err);
        }
    }

    public synchronized void next() {
        next(20);
    }

    public synchronized void next(int frame) {
        if (iterator == null) return;
        int moved = 0;
        while (moved < frame && iterator.hasNext()) {
            iterator.next();
            moved++;
        }
    }

    public synchronized void previous() {
        previous(20);
    }

    public synchronized void previous(int frame) {
        if (this.iterator == null) return;
        int moved = 0;
        while (moved < frame && this.iterator.hasPrevious()) {
            this.iterator.previous();
            moved++;
        }
    }

    public synchronized void tick() {
        if (this.player.isDisconnected()) {
            INSTANCES.remove(this.uuid);
            return;
        }
        if (this.iterator == null) {
            return;
        }
        if (this.paused) {
            return;
        }
//        if (this.state <= 1) {
//            this.state++;
//            return;
//        } else {
//            this.state = 0;
//        }
        this.state++;
//        System.out.println(this.state);
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putString("uid", this.player.getUuidAsString());

        NoticeDialog result = new NoticeDialog(
                new DialogCommonData(
                        Text.literal(""),
                        Optional.of(Text.empty()),
                        true, false,
                        AfterAction.CLOSE,
                        new ArrayList<>(List.of()),
                        new ArrayList<>(List.of())
                ),
                new DialogActionButtonData(
                        new DialogButtonData(Text.empty().append(Text.translatable("dialog.text.back")), 200),
                        Optional.of(new SimpleDialogAction(new ClickEvent.Custom(Touhou.id("stop/dialog_video"), Optional.of(nbtCompound))))
                )
        );

        if (this.iterator.hasNext()) {
            LinkedList<String> next = this.iterator.next();
            MutableText mutableText = Text.empty();
            for (String line : next) {
                mutableText.append(Text.literal(line + "\n"));
            }
            result.common().body().add(
                    new PlainMessageDialogBody(mutableText, 1024)
            );
            this.player.openDialog(RegistryEntry.of(result));
        } else {
            INSTANCES.remove(this.uuid);
//            System.out.println("removed");
        }
    }

    public void remove() {
        INSTANCES.remove(this.uuid);
    }
}
