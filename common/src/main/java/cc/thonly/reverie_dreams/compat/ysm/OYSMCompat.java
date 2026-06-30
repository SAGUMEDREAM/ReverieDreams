package cc.thonly.reverie_dreams.compat.ysm;

import cc.thonly.reverie_dreams.compat.ReverieDreamsCompats;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.platform.ModInfo;

import java.util.Optional;

public class OYSMCompat {
    public static void bootstrap() {
        Optional<ModInfo> yesSteveModelOptional =
                Balm.platform().getModInfo("yes_steve_model");
        if (yesSteveModelOptional.isEmpty()) {
            return;
        }
        ModInfo modInfo = yesSteveModelOptional.get();
        String name = modInfo.name();
        if (name.contains("Fox Model Loader")) {
            // is Fox Model Loader Fork
            ReverieDreamsCompats.load("yes_steve_model", "cc.thonly.reverie_dreams.compat.ysm.FoxModelLoader");
        } else {
            // is Vanilla YSM
        }
    }
}

