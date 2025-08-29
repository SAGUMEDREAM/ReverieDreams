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

    @Comment(name = "SOCK5 Proxy Address")
    @Entry
    public static String SOCK_5_PROXY_ADDRESS = "";

    @Comment(name = "SOCK5 Proxy PORT")
    @Entry
    public static Integer SOCK_5_PROXY_PORT = -1;

//    @Comment(name = "Polymer Patch")
//    @Entry
    public static boolean POLYMER_PATCH = true;

    @Comment(name = "Enable Debug Mode")
    @Entry
    public static boolean DEBUG_MODE = false;

    public static boolean hasProxy() {
        return !SOCK_5_PROXY_ADDRESS.isEmpty() && SOCK_5_PROXY_PORT != -1;
    }
}
