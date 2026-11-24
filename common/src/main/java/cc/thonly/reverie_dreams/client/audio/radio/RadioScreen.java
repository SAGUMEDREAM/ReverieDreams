package cc.thonly.reverie_dreams.client.audio.radio;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class RadioScreen extends Screen {
    private final Screen parent;

    public RadioScreen(Screen parent) {
        super(text("title"));
        this.parent = parent;
    }

    public static Component text(String key) {
        return Component.translatable("reverie_dreams.radio." + key);
    }

    @Override
    protected void init() {
        int x = width / 2 - 150, y = Math.max(40, height / 2 - 60);
        addRenderableWidget(Button.builder(Component.translatable("reverie_dreams.radio.enabled", text(RadioAudioClient.config().enabled ? "on" : "off")), b -> {
            RadioAudioClient.config().enabled = !RadioAudioClient.config().enabled;
            RadioAudioClient.save();
            rebuildWidgets();
        }).bounds(x, y, 300, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("reverie_dreams.radio.occlusion", text(RadioAudioClient.config().occlusion ? "on" : "off")), b -> {
            RadioAudioClient.config().occlusion = !RadioAudioClient.config().occlusion;
            RadioAudioClient.save();
            rebuildWidgets();
        }).bounds(x, y + 26, 300, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose()).bounds(width / 2 - 100, height - 28, 200, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 18, 0xffeeeeee);
        int y = Math.max(40, height / 2 - 60) + 58;
        graphics.centeredText(font, text(RadioAudioClient.status()), width / 2, y, 0xffe4cf9b);
        y += 19;
        for (var line : font.split(text("traffic"), Math.max(140, Math.min(width - 24, 370)))) {
            graphics.centeredText(font, line, width / 2, y, 0xffaaaaaa);
            y += 12;
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
