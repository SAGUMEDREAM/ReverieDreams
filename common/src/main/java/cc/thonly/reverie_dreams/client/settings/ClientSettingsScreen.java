package cc.thonly.reverie_dreams.client.settings;

import cc.thonly.reverie_dreams.client.audio.fumo.FumoAudioScreen;
import cc.thonly.reverie_dreams.client.audio.radio.RadioScreen;
import cc.thonly.reverie_dreams.client.renderer.danmaku.DanmakuRenderOptions;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class ClientSettingsScreen extends SettingsMenuScreen {
    ClientSettingsScreen(Screen parent) {
        super(parent, "reverie_dreams.settings.client");
    }

    @Override
    protected void addOptions() {
        addExplanation("reverie_dreams.settings.client.scope");
        addHeading("reverie_dreams.settings.client.rendering");
        addToggle(Component.translatable("reverie_dreams.settings.client.batched_orbs"),
                DanmakuRenderOptions.batchOrbs(), DanmakuRenderOptions::setBatchOrbs);
        addHeading("reverie_dreams.settings.client.audio");
        addAction(Component.translatable("reverie_dreams.fumo.audio.title"), () ->
                minecraft.setScreen(new FumoAudioScreen(this)));
        addAction(RadioScreen.text("title"), () ->
                minecraft.setScreen(new RadioScreen(this)));
    }
}
