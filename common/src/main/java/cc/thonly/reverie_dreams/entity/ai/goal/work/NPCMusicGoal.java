package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.ai.work.MusicPlaylist;
import cc.thonly.reverie_dreams.entity.ai.work.ScheduledWorkGoal;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.inventory.NPCWorkInventory;
import cc.thonly.reverie_dreams.item.prop.MusicalInstrumentItem;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.util.nbs.NotaUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

import java.util.List;
import java.util.Objects;

public final class NPCMusicGoal extends ScheduledWorkGoal {
    private String track;
    private List<String> available = List.of();
    private boolean paused;

    public NPCMusicGoal(BaseNPCLikeEntity npc) {
        super(npc, NPCWorkModes.PLAYING_MUSIC);
    }

    @Override
    protected int searchInterval() {
        return 40;
    }

    private static boolean instrument(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof MusicalInstrumentItem;
    }

    private ItemStack equipped() {
        if (instrument(npc.getMainHandItem()))
            return npc.getMainHandItem();
        if (instrument(npc.getOffhandItem()))
            return npc.getOffhandItem();
        int slot = NPCWorkInventory.find(npc.getInventory(), NPCMusicGoal::instrument);
        return slot < 0 ? ItemStack.EMPTY : npc.getInventory().getItem(slot);
    }

    @Override
    protected boolean findTask(ServerLevel level) {
        if (equipped().isEmpty()) {
            work().status("need_instrument");
            return false;
        }
        available = NotaUtils.getFileNames();
        String selected = equipped().get(RDDataComponentTypes.PLAYING_MUSIC.value());
        track = MusicPlaylist.select(ReverieDreams.config().npcMusicPlaylist, available, selected, work().musicIndex(), ReverieDreams.config().npcMusicLoop);
        if (track == null) {
            work().status(available.isEmpty() ? "need_music_file" : "music_finished");
            return false;
        }
        return true;
    }

    @Override
    protected boolean validTask(ServerLevel level) {
        return track != null && !equipped().isEmpty();
    }

    @Override
    protected void clearTask() {
        if (track != null && NotaUtils.activelyPlaying(npc) || track != null && work().musicPaused())
            work().resume(track, NotaUtils.playbackTick(npc));
        NotaUtils.stop(npc);
        track = null;
        paused = false;
    }

    @Override
    protected void runTask(ServerLevel level) {
        var hand = work().tools().ensure(npc.getInventory(), NPCMusicGoal::instrument);
        if (hand == null) {
            finish("need_instrument", 40);
            return;
        }
        var stack = npc.getItemInHand(hand);
        String selected = work().consumeSelectedMusic();
        if (selected != null && available.contains(selected)) {
            NotaUtils.stop(npc);
            track = selected;
            stack.set(RDDataComponentTypes.PLAYING_MUSIC.value(), selected);
        }
        if (work().consumeNextSong()) {
            String next = MusicPlaylist.next(MusicPlaylist.tracks(ReverieDreams.config().npcMusicPlaylist, available, null), track);
            if (next != null) {
                work().selectMusic(next);
                work().resume(next, -1);
                NotaUtils.stop(npc);
                track = next;
                stack.set(RDDataComponentTypes.PLAYING_MUSIC.value(), next);
            }
        }
        if (work().consumeSyncMusic()) {
            for (var other : level.getEntitiesOfClass(BaseNPCLikeEntity.class, npc.getBoundingBox().inflate(ReverieDreams.config().npcMusicRange),
                    n -> n.isAlive() && n.getWorkMode() == mode && n.getWorkController().working() && Objects.equals(n.getOwnerUuid(), npc.getOwnerUuid()))) {
                other.getWorkController().selectMusic(track);
                other.getWorkController().resume(track, -1);
            }
            NotaUtils.stop(npc);
        }
        if (work().musicPaused()) {
            NotaUtils.pause(npc, true);
            paused = true;
            work().status("music_paused");
            return;
        }
        if (paused && NotaUtils.isPlaying(npc)) {
            NotaUtils.pause(npc, false);
            paused = false;
        }
        if (NotaUtils.isPlaying(npc) && !NotaUtils.activelyPlaying(npc)) {
            NotaUtils.stop(npc);
            work().resume(track, -1);
            work().songFinished();
            finish("music_finished", 10);
            return;
        }
        if (!NotaUtils.isPlaying(npc)) {
            NotaUtils.play(npc, track, stack.getOrDefault(RDDataComponentTypes.NOTE_TYPE.value(), NoteBlockInstrument.PLING));
            NotaUtils.configurePlayback(npc, ReverieDreams.config().npcMusicRange, ReverieDreams.config().npcMusicVolume, work().resumeTick(track));
            if (!NotaUtils.isPlaying(npc)) {
                finish("need_music_file", 100);
                return;
            }
        }
        work().status("playing_music");
        if (level.getGameTime() % 20 == 0)
            work().resume(track, NotaUtils.playbackTick(npc));
    }
}
