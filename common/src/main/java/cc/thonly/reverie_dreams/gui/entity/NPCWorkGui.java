package cc.thonly.reverie_dreams.gui.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.util.nbs.NotaUtils;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Iterator;
import cc.thonly.reverie_dreams.entity.ai.goal.work.NPCWorkLogisticsGoal;
import cc.thonly.reverie_dreams.entity.ai.work.CustomerQueue;

public class NPCWorkGui extends SimpleGui implements GuiCommon {
    private final ServerPlayer player;
    private final NPCSimpleEntity npc;

    public NPCWorkGui(ServerPlayer player, NPCSimpleEntity npc) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.npc = npc;
        this.init();
    }

    @Override
    public void init() {
        this.setTitle(Component.empty()
                .append(Component.translatable("space.-8"))
                .append(Component.literal("\ue004")
                        .withStyle(Style.EMPTY
                                .withColor(ChatFormatting.WHITE)
                                .withFont(new FontDescription.Resource(
                                        ReverieDreams.id("reverie_dreams")
                                ))))
                .append(Component.translatable("space.-168"))
                .append(Component.translatable("gui.npc.work.mode").withStyle(ChatFormatting.WHITE)));
        for (int slot = 0; slot < this.size; slot++) this.clearSlot(slot);
        NPCWorkMode workMode = this.npc.getWorkMode();
        Iterator<NPCWorkMode> iterator = BuiltInRegistryProviders.NPC_WORK_MODE.iterator();
        for (int i = 0; i < this.size && iterator.hasNext();) {
            NPCWorkMode next = iterator.next();
            if (!NPCWorkModes.canAssign(next)) continue;
            ItemStack icon = next.getItemDisplay().value().getDefaultInstance();
            GuiElementBuilder builder = new GuiElementBuilder(icon);
            builder.setName(next.translationKey());
            if (next == NPCWorkModes.CHEST_CLASSIFICATION) {
                builder.addLoreLine(Component.translatable("gui.reverie_dreams.storage.help.item"));
                builder.addLoreLine(Component.translatable("gui.reverie_dreams.storage.help.rotation"));
                builder.addLoreLine(Component.translatable("gui.reverie_dreams.storage.help.range"));
                if (workMode == next) builder.addLoreLine(Component.translatable("gui.reverie_dreams.task_reason."
                        + this.npc.getChestClassificationGoal().inspect(this.npc.getWorkingPos())));
            }
            if (workMode.equals(next)) {
                builder.glow();
            }
            builder.setCallback((index, clickType, input, slotGuiInterface) -> {
                if (!this.allowed() || !NPCWorkModes.canAssign(next)) return;
                this.npc.cancelChatRequests();
                var previousState = this.npc.getNPCState();
                boolean taskActive = this.npc.getSkillTask().active();
                if (taskActive) {
                    if (!this.npc.isOwnedBy(player) || !player.isAlive() || player.level() != this.npc.level()
                            || this.npc.distanceToSqr(player) > 1024) return;
                    this.npc.getSkillTask().cancel(this.npc, "Task replaced by manual work control.");
                }
                this.npc.setWorkMode(next);
                if (taskActive) this.npc.setNPCState(previousState);
                if (NotaUtils.isPlaying(this.npc)) {
                    NotaUtils.stop(this.npc);
                }
                SoundEventPlayUtils.playUISound(player, 1.0f, 1.0f);
                this.init();
            });
            this.setSlot(i++, builder);
        }
        var work = this.npc.getWorkController();
        var status = new GuiElementBuilder(Items.COMPASS).setName(Component.translatable("gui.reverie_dreams.work.status"));
        status.addLoreLine(Component.translatable(work.status().equals("restaurant_counter_unbound")
                ? "workshop.reverie_dreams.counter_unbound" : "gui.reverie_dreams.task_reason." + work.status()));
        status.addLoreLine(Component.translatable("gui.reverie_dreams.work.range", work.area().radius(), work.area().height()));
        if (NPCWorkLogisticsGoal.production(workMode)) {
            status.addLoreLine(Component.translatable("gui.reverie_dreams.work.logistics"));
        }
        if (workMode == NPCWorkModes.FARM) status.addLoreLine(Component.translatable("gui.reverie_dreams.work.same_crop"));
        if (this.npc.getCustomerContainer().isCustomerMode()) {
            var customer = this.npc.getCustomerContainer();
            status.addLoreLine(Component.translatable(customer.getCustomer().isGeneric()
                    ? "restaurant.reverie_dreams.guest_generic" : "restaurant.reverie_dreams.guest_defined"));
            int position = CustomerQueue.of(this.npc.level()).position(this.npc.getUUID(), this.npc.level().getGameTime());
            if (position > 0) status.addLoreLine(Component.translatable("gui.reverie_dreams.work.queue", position));
            else status.addLoreLine(Component.translatable(customer.getCooldownTickTime() >= 0
                    ? "gui.reverie_dreams.work.eating_time" : "gui.reverie_dreams.work.order_time",
                    (Math.max(0, customer.getCooldownTickTime() >= 0 ? customer.getCooldownTickTime() : customer.getTimeout()) + 19) / 20));
        }
        this.setSlot(49, status);
        if (workMode == NPCWorkModes.PLAYING_MUSIC) {
            this.setSlot(45, new GuiElementBuilder(Items.JUKEBOX).setName(Component.translatable(work.musicPaused()
                    ? "gui.reverie_dreams.work.music_resume" : "gui.reverie_dreams.work.music_pause"))
                    .setCallback((a,b,c,d) -> { if (allowed()) { work.toggleMusicPaused(); init(); } }));
            this.setSlot(46, new GuiElementBuilder(Items.ARROW).setName(Component.translatable("gui.reverie_dreams.work.music_next"))
                    .setCallback((a,b,c,d) -> { if (allowed()) { work.nextSong(); init(); } }));
            this.setSlot(47, new GuiElementBuilder(Items.NOTE_BLOCK).setName(Component.translatable("gui.reverie_dreams.work.music_sync"))
                    .setCallback((a,b,c,d) -> { if (allowed()) { work.syncMusic(); init(); } }));
        }
    }

    private boolean allowed() { return this.npc.isAlive() && this.npc.isOwnedBy(player) && player.isAlive()
            && player.level() == this.npc.level() && this.npc.distanceToSqr(player) <= 1024; }

    @Override public void onTick() {
        super.onTick();
        if (!allowed()) { this.close(); return; }
        if (this.npc.level().getGameTime() % 20 == 0) this.init();
    }

    @Override
    public void onPlayerClose(boolean success) {
        super.onPlayerClose(success);
        DelayedTask.create(ReverieDreams.getServer(), 2, ()->{
            if (!allowed()) return;
            NPCGui npcGui = new NPCGui(this.player, this.npc);
            npcGui.open();
        });
    }

}
