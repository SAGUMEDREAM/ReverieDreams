package cc.thonly.reverie_dreams.config;

import eu.midnightdust.lib.config.MidnightConfig;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReverieDreamsConfiguration extends MidnightConfig {

    @Comment(name = "Configuration File Version (Do not modify)")
    @Entry
    public static int CONFIG_VERSION = 1;

    @Comment(name = "Enable Update Checker\nIf enabled, the mod will automatically check for new versions.")
    @Entry
    public static boolean CHECK_UPDATE = true;

    @Comment(name = "Enable Glow Effect for Danmaku Items\nReplaces vanilla torch item overlay to display a glowing outline.")
    @Entry
    public static boolean ENABLE_DANMAKU_GLOW = true;

    @Comment(name = "Maximum Upgraded Health\nDefines the highest total health value players can reach through upgrades.")
    @Entry
    public static int MAX_UPGRADED_HEALTH_VALUE = 46;

    @Comment(name = "Enable Yousei Spawning\nToggle to allow Yousei to spawn naturally.")
    @Entry
    public static boolean ENABLE_YOUSEI_SPAWN = false;

    @Comment(name="Enable Ghost Spawning\nToggle to allow Yousei to spawn naturally.")
    @Entry
    public static boolean ENABLE_GHOST_SPAWN = false;

    @Comment(name = "Enable Debug Mode\nShows extra debug logs and developer-only information.")
    @Entry
    public static boolean DEBUG_MODE = false;
}
