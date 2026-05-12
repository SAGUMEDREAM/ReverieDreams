package cc.thonly.reverie_dreams.gui.block;

import cc.thonly.reverie_dreams.block.entity.SpeakerBlockEntity;
import eu.pb4.sgui.api.gui.SignGui;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SpeakerGui extends SignGui {
    private final SpeakerBlockEntity blockEntity;

    public SpeakerGui(SpeakerBlockEntity blockEntity, ServerPlayer player) {
        super(player);
        this.blockEntity = blockEntity;
        this.init();
    }

    public void init() {
        for (int i = 0; i < 4; i++) {
            this.setLine(i, Component.literal(""));
        }
        List<String> texts = this.blockEntity.getTexts();
        for (int i = 0; i < texts.size(); i++) {
            if (i > 3) {
                break;
            }
            String line = texts.get(i);
            this.setLine(i, Component.literal(line));
        }
    }

    @Override
    public void onManualClose() {
        super.onManualClose();
        try {
            List<String> lines = new ArrayList<>(4);
            for (int i = 0; i < 4; i++) {
                Component line = this.getLine(i);
                lines.add(line.getString());
            }
            this.blockEntity.setTexts(lines);
            this.blockEntity.setChanged();
        } catch (Exception err) {
            log.error("Error: ", err);
        }
    }

}
