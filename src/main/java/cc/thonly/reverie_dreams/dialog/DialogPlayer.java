package cc.thonly.reverie_dreams.dialog;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.server.DelayedTask;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.CommonButtonData;
import net.minecraft.server.dialog.CommonDialogData;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.dialog.NoticeDialog;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Slf4j
@Getter
public class DialogPlayer {
    public static final Map<String, DialogPlayer> INSTANCES = new Object2ObjectOpenHashMap<>();
    public static final Map<String, String> FILENAME2UID = new Object2ObjectOpenHashMap<>();
    public static final Map<String, Boolean> LOADED = new Object2ObjectOpenHashMap<>();

    public static synchronized DialogPlayer play(ServerPlayer player, String filename, @Nullable SoundEvent soundEvent) {
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
        ServerTickRateManager tickManager = server.tickRateManager();
        if (!tickManager.isFrozen()) {
            for (Map.Entry<String, DialogPlayer> entry : INSTANCES.entrySet()) {
                entry.getValue().tick();
            }
        }
    }

    public static DialogPlayer getInstance(String uuid) {
        return INSTANCES.get(uuid);
    }

    private final ServerPlayer player;
    private final String uuid;
    private LinkedList<LinkedList<String>> source = new LinkedList<>();
    private ListIterator<LinkedList<String>> iterator = null;
    private int state = 0;
    @Setter
    private boolean paused = true;
    @Nullable
    private SoundEvent soundEvent;

    public DialogPlayer(ServerPlayer player, String fileId) {
        this.player = player;
        this.uuid = this.player.getUUID().toString();
        this.parse(fileId);
        INSTANCES.put(this.uuid, this);
    }

    public DialogPlayer(ServerPlayer player, DialogFiles.Entry entry) {
        this(player, entry.getFileId());
    }

    public DialogPlayer(ServerPlayer player, DialogFiles.Entry entry, @Nullable SoundEvent soundEvent) {
        this(player, entry.getFileId());
        this.soundEvent = soundEvent;
    }

    public synchronized void start() {
        this.iterator = this.source.listIterator();
        if (this.soundEvent != null) {
            this.player.makeSound(this.soundEvent);
            System.out.println(111);
        }
        DelayedTask.createFromSecond(ReverieDreams.getServer(), 0.75f, () -> {
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
        if (this.player.hasDisconnected()) {
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
        CompoundTag nbtCompound = new CompoundTag();
        nbtCompound.putString("uid", this.player.getStringUUID());

        NoticeDialog result = new NoticeDialog(
                new CommonDialogData(
                        Component.literal(""),
                        Optional.of(Component.empty()),
                        true, false,
                        DialogAction.CLOSE,
                        new ArrayList<>(List.of()),
                        new ArrayList<>(List.of())
                ),
                new ActionButton(
                        new CommonButtonData(Component.empty().append(Component.translatable("dialog.text.back")), 200),
                        Optional.of(new StaticAction(new ClickEvent.Custom(ReverieDreams.id("stop/dialog_video"), Optional.of(nbtCompound))))
                )
        );

        if (this.iterator.hasNext()) {
            LinkedList<String> next = this.iterator.next();
            MutableComponent mutableText = Component.empty();
            for (String line : next) {
                mutableText.append(Component.literal(line + "\n"));
            }
            result.common().body().add(
                    new PlainMessage(mutableText, 1024)
            );
            this.player.openDialog(Holder.direct(result));
        } else {
            INSTANCES.remove(this.uuid);
//            System.out.println("removed");
        }
    }

    public void remove() {
        INSTANCES.remove(this.uuid);
    }
}
