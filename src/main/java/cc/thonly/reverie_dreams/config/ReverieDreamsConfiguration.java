package cc.thonly.reverie_dreams.config;

import eu.midnightdust.lib.config.MidnightConfig;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReverieDreamsConfiguration extends MidnightConfig {
    @Comment(name = "Configuration Version")
    @Entry
    public static int CONFIG_VERSION = 1;

    @Comment(name = "Check Update")
    @Entry
    public static boolean CHECK_UPDATE = true;

    @Comment(name = "Enable Danmaku Item Glowing (Torch replace item overlay)")
    public static boolean ENABLE_DANMAKU_GLOW = true;

    @Comment(name = "Enable Debug Mode")
    @Entry
    public static boolean DEBUG_MODE = false;

}
