package cc.thonly.reverie_dreams;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = ReverieDreams.MOD_ID)
public class ReverieDreamsConfiguration implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public int configVersion = 1;

    @ConfigEntry.Gui.Tooltip
    public boolean checkUpdate = true;


    @ConfigEntry.Gui.Tooltip
    public boolean debugMode = false;


    @ConfigEntry.Gui.Tooltip
    public boolean enableDanmakuGlow = true;


    @ConfigEntry.Gui.Tooltip
    public int maxUpgradedHealthValue = 1024;


    @ConfigEntry.Gui.Tooltip
    public boolean enableYouseiSpawn = true;


    @ConfigEntry.Gui.Tooltip
    public boolean enableGhostSpawn = false;


    @ConfigEntry.Gui.Tooltip
    public boolean enableAIReplacesGeneralChat = false;


    @ConfigEntry.Gui.Tooltip
    public String apiUrl =
            "https://api.deepseek.com/v1/chat/completions";


    @ConfigEntry.Gui.Tooltip
    public String apiKey = "";


    @ConfigEntry.Gui.Tooltip
    public String model =
            "deepseek-chat";


    @ConfigEntry.Gui.Tooltip
    public String chatType =
            "deepseek";
}