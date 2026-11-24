package cc.thonly.reverie_dreams.world.gensokyo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 幻想乡固定地图的总开关。
 *
 * <p>正式环境也生效，不再依赖开发环境判断。把 {@link #ENABLED} 改成
 * {@code false} 即可关闭维度创建、区块生成和相关命令；已存在的区域文件不会删除。</p>
 */
public final class GensokyoFeature {
    /**
     * Public static final switch: true = create/generate Gensokyo in all
     * environments, false = leave the dimension uncreated.
     */
    public static final boolean ENABLED = true;

    private static final Logger LOGGER = LoggerFactory.getLogger("Gensokyo");
    private static volatile boolean disabledLogged;

    private GensokyoFeature() {
    }

    public static boolean isEnabled() {
        return ENABLED;
    }

    public static void logDisabledOnce() {
        if (disabledLogged) {
            return;
        }
        disabledLogged = true;
        LOGGER.info("Gensokyo generation is disabled by GensokyoFeature.ENABLED; "
                + "existing region files are left untouched.");
    }
}
