package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.util.CardboardWarning;
import cc.thonly.reverie_dreams.util.ConstantInfo;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class TouhouPreload implements PreLaunchEntrypoint {
    public static final Logger LOGGER = LoggerFactory.getLogger(Touhou.MOD_ID);

    @Override
    public void onPreLaunch() {
        this.preload();
        try {
            Class.forName("cc.thonly.reverie_dreams.util.FKMod");
            Class.forName("cc.thonly.reverie_dreams.util.ConstantInfo");
        } catch (ClassNotFoundException e) {
            log.error("Can't load packaging mod", e);
        }
    }

    private void preload() {
        List<String> args = Arrays.stream(FabricLoader.getInstance().getLaunchArguments(true)).toList();
        for (String arg : args) {
            if (arg.contains("--output") || arg.contains("--input") || arg.contains("--mod") || arg.contains("--all")) {
                ConstantInfo.IS_DATAGEN = true;
                break;
            }
        }
        CardboardWarning.checkAndAnnounce();
        if (ConstantInfo.isDevMode()) {
            LOGGER.warn("=====================================================");
            LOGGER.warn("You are using development version of Gensokyo: Reverie of Lost Dreams!");
            LOGGER.warn("Support is limited, as features might be unfinished!");
            LOGGER.warn("You are on your own!");
            LOGGER.warn("=====================================================");
        }
        if (ConstantInfo.hasForgeApi()) {
            LOGGER.warn("No? Dude, are you serious?");
        }
        if (ConstantInfo.hasOptifine()) {
            LOGGER.warn("Are you kidding me? Install Optifine?!");
        }
        if (ConstantInfo.isHasConnector()) {
            LOGGER.warn("(Neo)Forge not supported");
        }
    }
}
