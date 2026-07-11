package cc.thonly.reverie_dreams.compat.ysm;

import cc.thonly.reverie_dreams.compat.ReverieDreamsCompats;
import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;

import java.util.Optional;

public class OYSMCompat {
    public static void bootstrap() {
        Optional<Mod> yesSteveModelOptional =
                Platform.getOptionalMod("yes_steve_model");
        if (yesSteveModelOptional.isEmpty()) {
            return;
        }
        Mod modInfo = yesSteveModelOptional.get();
        String name = modInfo.getName();
        if (name.contains("Fox Model Loader")) {
            // is Fox Model Loader Fork
            ReverieDreamsCompats.load("yes_steve_model", "cc.thonly.reverie_dreams.compat.ysm.FoxModelLoader");
        } else {
            // is Vanilla YSM
        }
    }
}

