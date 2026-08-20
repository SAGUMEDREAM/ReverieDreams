package cc.thonly.reverie_dreams.gui.block;

import cc.thonly.reverie_dreams.block.entity.RemoteBlockEntity;
import eu.pb4.sgui.api.gui.SignGui;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

@Slf4j
@SuppressWarnings("resource")
public class RemoteGui extends SignGui {
    private final RemoteBlockEntity remoteBlockEntity;

    public RemoteGui(RemoteBlockEntity remoteBlockEntity, ServerPlayer player) {
        super(player);
        this.remoteBlockEntity = remoteBlockEntity;
        this.init();
    }

    public void init() {
        if (this.remoteBlockEntity.getSignalName().isEmpty()) {
            this.setLine(0, Component.literal("SignalName?"));
        } else {
            this.setLine(0, Component.literal(this.remoteBlockEntity.getSignalName()));
        }
        if (this.remoteBlockEntity.getSignalToken().isEmpty()) {
            this.setLine(1, Component.literal("SignalToken?"));
        } else {
            this.setLine(1, Component.literal(this.remoteBlockEntity.getSignalToken()));
        }
    }

    @Override
    public void onPlayerClose(boolean success) {
        super.onPlayerClose(success);
        this.player.level().playSound(null, this.player.getOnPos(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS);
        try {
            Component name = this.getLine(0);
            Component token = this.getLine(1);
            if (name == null || token == null) {
                this.player.sendSystemMessage(Component.literal("§Unknown Input!"));
                return;
            }
            if (name.getString().isEmpty() || name.getString().isEmpty()) {
                this.player.sendSystemMessage(Component.literal("§cInput is empty!!"));
                return;
            }
            if (name.getString().length() > 30 || name.getString().length() > 30) {
                this.player.sendSystemMessage(Component.literal("§cLength is Long!"));
                return;
            }
            this.remoteBlockEntity.setSignalName(name.getString());
            this.remoteBlockEntity.setSignalToken(token.getString());
            this.remoteBlockEntity.setChanged();
        } catch (Exception err) {
            log.error("Error: ", err);
        }
    }

}
