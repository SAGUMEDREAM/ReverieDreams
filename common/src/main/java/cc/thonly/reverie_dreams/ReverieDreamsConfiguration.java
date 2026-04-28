package cc.thonly.reverie_dreams;


import net.blay09.mods.balm.platform.config.reflection.Comment;
import net.blay09.mods.balm.platform.config.reflection.Config;

@Config(value = ReverieDreams.MOD_ID, type = "common")
public class ReverieDreamsConfiguration {
    @Comment("Configuration File Version (Do not modify)")
    public int configVersion = 1;
    @Comment("Enable Update Checker\n# If enabled, the mod will automatically check for new versions.")
    public boolean checkUpdate = false;
    @Comment("Enable Debug Mode\n# Shows extra debug logs and developer-only information.")
    public boolean debugMode = false;
    @Comment("Enable Glow Effect for Danmaku Items\n# Replaces vanilla torch item overlay to display a glowing outline.")
    public boolean enableDanmakuGlow = true;
    @Comment("Maximum Upgraded Health\n# Defines the highest total health value players can reach through upgrades.")
    public int maxUpgradedHealthValue = 1024;
    @Comment("Enable Yousei Spawning\n# Toggle to allow Yousei to spawn naturally.")
    public boolean enableYouseiSpawn = true;
    @Comment("Enable Ghost Spawning\n# Toggle to allow Ghosts to spawn naturally.")
    public boolean enableGhostSpawn = false;
}