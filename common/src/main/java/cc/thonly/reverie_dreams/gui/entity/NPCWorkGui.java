package cc.thonly.reverie_dreams.gui.entity;

import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.util.NotaUtils;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;

public class NPCWorkGui extends SimpleGui implements GuiCommon {
    private final ServerPlayer player;
    private final BaseNPCLikeEntity npcEntity;

    public NPCWorkGui(ServerPlayer player, BaseNPCLikeEntity npcEntity) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.npcEntity = npcEntity;
        this.init();
    }

    @Override
    public void init() {
        this.setTitle(Component.translatable("gui.npc.work.mode"));
        NPCWorkMode workMode = this.npcEntity.getWorkMode();
        Iterator<NPCWorkMode> iterator = RegistryImpls.NPC_WORK_MODE.iterator();
        for (int i = 0; i < this.size; i++) {
            if (!iterator.hasNext()) {
                break;
            }
            NPCWorkMode next = iterator.next();
            ItemStack icon = next.getItemDisplay().value().getDefaultInstance();
            GuiElementBuilder builder = new GuiElementBuilder(icon);
            builder.setName(next.translationKey());
            if (workMode.equals(next)) {
                builder.glow();
            }
            builder.setCallback((index, clickType, input, slotGuiInterface) -> {
                this.npcEntity.setWorkMode(next);
                if (NotaUtils.isPlaying(this.npcEntity)) {
                    NotaUtils.stop(this.npcEntity);
                }
                SoundEventPlayUtils.playUISound(player, 1.0f, 1.0f);
                this.init();
            });
            this.setSlot(i, builder);
        }
    }

    @Override
    public void onManualClose() {
        super.onManualClose();
        NPCGui npcGui = new NPCGui(this.player, this.npcEntity);
        npcGui.open();
    }
}
