package cc.thonly.reverie_dreams.gui;

import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
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
        Iterator<NPCWorkMode> iterator = RegistryHandlers.NPC_WORK_MODE.iterator();
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
            builder.setCallback((index, clickType, clickType1, slotGuiInterface) -> {
                this.npcEntity.setWorkMode(next);
                this.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                this.init();
            });
            this.setSlot(i, builder);
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        NPCGui npcGui = new NPCGui(this.player, this.npcEntity);
        npcGui.open();
    }
}
