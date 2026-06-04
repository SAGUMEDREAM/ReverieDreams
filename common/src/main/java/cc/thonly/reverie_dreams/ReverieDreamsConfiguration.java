package cc.thonly.reverie_dreams;


import net.blay09.mods.balm.platform.config.reflection.Comment;
import net.blay09.mods.balm.platform.config.reflection.Config;
import net.blay09.mods.balm.platform.config.reflection.NestedType;

import java.util.Arrays;
import java.util.List;

@Config(value = ReverieDreams.MOD_ID, type = "common")
public class ReverieDreamsConfiguration {
    @Comment("Do not modify")
    public int configVersion = 1;
    @Comment("If enabled, the mod will automatically check for new versions.")
    public boolean checkUpdate = true;
    @Comment("Shows extra debug logs and developer-only information.")
    public boolean debugMode = false;
    @Comment("Replaces vanilla torch item overlay to display a glowing outline.")
    public boolean enableDanmakuGlow = true;
    @Comment("Defines the highest total health value players can reach through upgrades.")
    public int maxUpgradedHealthValue = 1024;
    @Comment("Toggle to allow Yousei to spawn naturally.")
    public boolean enableYouseiSpawn = true;
    @Comment("Toggle to allow Ghosts to spawn naturally.")
    public boolean enableGhostSpawn = false;
    @Comment("Toggle to replace general chat by AIChat")
    public boolean enableAIReplacesGeneralChat = false;
    @Comment("Set Chat API url")
    public String apiUrl = "https://api.deepseek.com/v1/chat/completions";
    @Comment("Set Chat API key")
    public String apiKey = "";
    @Comment("Set Chat API Model")
    public String model = "deepseek-chat";
    @Comment("Set Chat API Type: openai/deepseek/gemini/claude")
    public String chatType = "deepseek";
}