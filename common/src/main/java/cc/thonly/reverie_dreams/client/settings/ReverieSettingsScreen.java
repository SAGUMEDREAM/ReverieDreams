package cc.thonly.reverie_dreams.client.settings;

import cc.thonly.reverie_dreams.ReverieDreamsConfiguration;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ReverieSettingsScreen extends SettingsMenuScreen {
    public ReverieSettingsScreen(Screen parent) {
        super(parent, "reverie_dreams.settings.title");
    }

    @Override
    protected void addOptions() {
        addExplanation("reverie_dreams.settings.general.scope");
        addAction(Component.translatable("reverie_dreams.settings.general"), () ->
                minecraft.setScreen(AutoConfigClient.getConfigScreen(ReverieDreamsConfiguration.class, this).get()));
        addAction(Component.translatable("reverie_dreams.settings.client"), () ->
                minecraft.setScreen(new ClientSettingsScreen(this)));
    }
}
