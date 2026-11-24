package cc.thonly.reverie_dreams.gui.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.npc.NPCState;
import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
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

import java.util.Iterator;

public class NPCStateGui extends SimpleGui implements GuiCommon {
    private final ServerPlayer player;
    private final NPCSimpleEntity npc;

    public NPCStateGui(ServerPlayer player, NPCSimpleEntity npc) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.npc = npc;
        this.init();
    }

    @Override
    public void init() {
        this.setTitle(Component.empty()
                .append(Component.translatable("space.-8"))
                .append(Component.literal("\ue005")
                        .withStyle(Style.EMPTY
                                .withColor(ChatFormatting.WHITE)
                                .withFont(new FontDescription.Resource(
                                        ReverieDreams.id("reverie_dreams")
                                ))))
                .append(Component.translatable("space.-168"))
                .append(Component.translatable("gui.npc.state.button", this.npc.getNPCState().translationKey())
                        .withStyle(ChatFormatting.WHITE)));
        NPCState workMode = this.npc.getNPCState();
        Iterator<NPCState> iterator = BuiltInRegistryProviders.NPC_STATE.iterator();
        for (int i = 0; i < this.size; i++) {
            if (!iterator.hasNext()) {
                break;
            }
            NPCState next = iterator.next();
            ItemStack icon = next.getItemDisplay().value().getDefaultInstance();
            GuiElementBuilder builder = new GuiElementBuilder(icon);
            builder.setName(next.translationKey());
            if (workMode.equals(next)) {
                builder.glow();
            }
            builder.setCallback((index, clickType, input, slotGuiInterface) -> {
                if (!this.npc.isAlive() || !this.npc.isOwnedBy(player) || !player.isAlive() || player.level() != this.npc.level()
                        || this.npc.distanceToSqr(player) > 1024)
                    return;
                this.npc.cancelChatRequests();
                if (this.npc.getSkillTask().active()) {
                    if (!this.npc.isOwnedBy(player) || !player.isAlive() || player.level() != this.npc.level()
                            || this.npc.distanceToSqr(player) > 1024)
                        return;
                    this.npc.getSkillTask().cancel(this.npc, "Task replaced by manual state control.");
                }
                this.npc.setNPCState(next);
                if (NotaUtils.isPlaying(this.npc)) {
                    NotaUtils.stop(this.npc);
                }
                SoundEventPlayUtils.playUISound(player, 1.0f, 1.0f);
                this.init();
            });
            this.setSlot(i, builder);
        }
    }

    @Override
    public void onPlayerClose(boolean success) {
        super.onPlayerClose(success);
        DelayedTask.create(ReverieDreams.getServer(), 2, () -> {
            NPCGui npcGui = new NPCGui(this.player, this.npc);
            npcGui.open();
        });
    }
}
