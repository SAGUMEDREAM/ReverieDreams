package cc.thonly.reverie_dreams.entity.ai;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.entity.ai.work.CustomerQueue;
import cc.thonly.reverie_dreams.entity.ai.work.WorkArea;
import cc.thonly.reverie_dreams.entity.ai.work.WorkClaims;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.inventory.WorkToolLease;
import cc.thonly.reverie_dreams.inventory.storage.StorageClaims;
import cc.thonly.reverie_dreams.registry.content.NPCStates;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Per-entity work context. Inventory, search, leases and movement stay independent components.
 */
public final class NPCWorkController {
    public record Demand(Predicate<ItemStack> accepts, int amount, String reason) {
    }

    private final BaseNPCLikeEntity npc;
    private final WorkToolLease tools = new WorkToolLease();
    private final Map<Object, Long> rejected = new HashMap<>();
    private Object movement;
    private Object lastLevel;
    private NPCWorkMode lastMode;
    private Object lastState;
    private WorkArea lastArea;
    private String status = "ready";
    private Demand demand;
    private long actionAt;
    private boolean logistics;
    private boolean musicPaused;
    private int musicIndex;
    private boolean nextSong;
    private boolean syncSong;
    private String requestedSong;
    private String resumeTrack = "";
    private int resumeTick = -1;

    public NPCWorkController(BaseNPCLikeEntity npc) {
        this.npc = npc;
    }

    public WorkArea area() {
        var config = ReverieDreams.config();
        return new WorkArea(npc.getWorkingPos(), config.npcWorkRange, config.npcWorkVerticalRange);
    }

    public boolean working(NPCWorkMode mode) {
        return npc.isAlive() && npc.isTame() && npc.getNPCState() == NPCStates.WORKING && npc.getWorkMode() == mode;
    }

    public boolean working() {
        return npc.isAlive() && npc.isTame() && npc.getNPCState() == NPCStates.WORKING;
    }

    public WorkToolLease tools() {
        return tools;
    }

    public String status() {
        return status;
    }

    public void status(String status) {
        this.status = status;
    }

    public Demand demand() {
        return demand;
    }

    public void demand(Predicate<ItemStack> accepts, int amount, String reason) {
        demand = new Demand(accepts, Math.clamp(amount, 1, 64), reason);
        status = reason;
    }

    public void clearDemand() {
        demand = null;
    }

    public long idleFor(long now) {
        return Math.max(0, now - actionAt);
    }

    public boolean logistics() {
        return logistics;
    }

    public void logistics(boolean active) {
        logistics = active;
    }

    public boolean available(Object resource, long now) {
        return rejected.getOrDefault(resource, Long.MIN_VALUE) <= now && WorkClaims.of(npc.level()).available(resource, npc.getUUID(), now);
    }

    public void reject(Object resource, long now) {
        rejected.put(resource, now + 200);
    }

    public void navigation(Object owner, boolean active) {
        if (active)
            movement = owner;
        else if (movement == owner)
            movement = null;
    }

    public boolean navigating() {
        return movement != null && working();
    }

    public void action() {
        actionAt = npc.level().getGameTime();
        demand = null;
        status = "ready";
        if (npc instanceof NPCSimpleEntity simple)
            simple.getSkillTask().recordAction();
    }

    public void sync() {
        var area = area();
        if (lastLevel != npc.level() || lastMode != npc.getWorkMode() || lastState != npc.getNPCState() || !area.equals(lastArea)) {
            reset();
            lastLevel = npc.level();
            lastMode = npc.getWorkMode();
            lastState = npc.getNPCState();
            lastArea = area;
            actionAt = npc.level().getGameTime();
        }
        rejected.values().removeIf(until -> until <= npc.level().getGameTime());
    }

    public void reset() {
        tools.restore(npc.getInventory());
        if (lastLevel != null) {
            WorkClaims.of(lastLevel).release(npc.getUUID());
            StorageClaims.forWorld(lastLevel).release(npc.getUUID());
            CustomerQueue.of(lastLevel).leave(npc.getUUID());
        }
        movement = null;
        demand = null;
        logistics = false;
        rejected.clear();
        status = "ready";
    }

    public boolean musicPaused() {
        return musicPaused;
    }

    public void toggleMusicPaused() {
        musicPaused = !musicPaused;
    }

    public int musicIndex() {
        return musicIndex;
    }

    public void songFinished() {
        musicIndex++;
    }

    public void nextSong() {
        nextSong = true;
    }

    public boolean consumeNextSong() {
        boolean requested = nextSong;
        nextSong = false;
        return requested;
    }

    public void syncMusic() {
        syncSong = true;
    }

    public boolean consumeSyncMusic() {
        boolean request = syncSong;
        syncSong = false;
        return request;
    }

    public void selectMusic(String song) {
        requestedSong = song;
        musicIndex = 0;
        resumeTick = -1;
    }

    public String consumeSelectedMusic() {
        String song = requestedSong;
        requestedSong = null;
        return song;
    }

    public void resume(String track, int tick) {
        resumeTrack = track == null ? "" : track;
        resumeTick = tick;
    }

    public int resumeTick(String track) {
        return resumeTrack.equals(track) ? resumeTick : -1;
    }

    public void save(ValueOutput out) {
        tools.save(out);
        out.putBoolean("Work.MusicPaused", musicPaused);
        out.putInt("Work.MusicIndex", musicIndex);
        out.putString("Work.MusicTrack", resumeTrack);
        out.putInt("Work.MusicTick", resumeTick);
    }

    public void load(ValueInput in) {
        tools.load(in, npc.getInventory());
        musicPaused = in.getBooleanOr("Work.MusicPaused", false);
        musicIndex = Math.max(0, in.getIntOr("Work.MusicIndex", 0));
        resumeTrack = in.getStringOr("Work.MusicTrack", "");
        resumeTick = in.getIntOr("Work.MusicTick", -1);
    }
}
