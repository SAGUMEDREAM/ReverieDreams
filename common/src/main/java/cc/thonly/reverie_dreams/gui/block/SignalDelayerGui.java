package cc.thonly.reverie_dreams.gui.block;

import cc.thonly.reverie_dreams.block.entity.SignalDelayerBlockEntity;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.ContainerInput;

@SuppressWarnings("resource")
public class SignalDelayerGui extends AnvilInputGui {
    private final SignalDelayerBlockEntity signalDelayerBlockEntity;

    public SignalDelayerGui(ServerPlayer player, SignalDelayerBlockEntity signalDelayerBlockEntity) {
        super(player, true);
        this.signalDelayerBlockEntity = signalDelayerBlockEntity;
        this.init();
    }

    public void init() {
        this.setDefaultInputValue(String.valueOf(this.signalDelayerBlockEntity.getMaxDelayTick()));
    }

    @Override
    public boolean onClick(int index, ClickType type, ContainerInput action, GuiElement element) {
        try {
            int num = Integer.parseInt(this.getInput());
            this.signalDelayerBlockEntity.setMaxDelayTick(num);
        } catch (Exception ignored) {
            this.player.sendSystemMessage(Component.literal("§cError Input Value"));
        } finally {
            this.player.level().playSound(null, this.player.getOnPos(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS);
            this.close();
        }
        return false;
    }

    @Override
    public void onManualClose() {
        super.onManualClose();
        try {
            int num = Integer.parseInt(this.getInput());
            this.signalDelayerBlockEntity.setMaxDelayTick(num);
            this.signalDelayerBlockEntity.setChanged();
        } catch (Exception ignored) {

        }
    }

}
