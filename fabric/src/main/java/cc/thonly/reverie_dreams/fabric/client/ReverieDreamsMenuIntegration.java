package cc.thonly.reverie_dreams.fabric.client;

import cc.thonly.reverie_dreams.client.settings.ReverieSettingsScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ReverieDreamsMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ReverieSettingsScreen::new;
    }
}
