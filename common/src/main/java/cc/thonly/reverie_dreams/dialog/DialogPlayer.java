package cc.thonly.reverie_dreams.dialog;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.server.DelayedTask;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.dialog.*;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("resource")
@Slf4j
@Getter
public class DialogPlayer {
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
        DialogPlayerManager.PLAYER_INSTANCES.put(this.uuid, this);
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
            this.player.level().playSound(null, this.player.getOnPos(), this.soundEvent, SoundSource.PLAYERS, 1.0f, 1.0f);
//            System.out.println(111);
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
            DialogPlayerManager.PLAYER_INSTANCES.remove(this.uuid);
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
                    new PlainMessage(mutableText, 512)
            );
            this.player.openDialog(Holder.direct(result));
        } else {
            DialogPlayerManager.PLAYER_INSTANCES.remove(this.uuid);
//            System.out.println("removed");
        }
    }

    public void remove() {
        DialogPlayerManager.PLAYER_INSTANCES.remove(this.uuid);
    }
}
