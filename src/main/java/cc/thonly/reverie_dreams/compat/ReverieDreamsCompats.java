package cc.thonly.reverie_dreams.compat;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.util.ConstantInfo;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.goal.Goal;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.Pattern;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ReverieDreamsCompats {
    public static void init() {
        load("polydex", "cc.thonly.reverie_dreams.compat.PolydexCompatImpl");
        load("polydex2eiv", "cc.thonly.reverie_dreams.compat.Polydex2EIVCompatImpl");
        load("minecraft", "cc.thonly.reverie_dreams.compat.VanillaCompat");
        load("borukva-food", "cc.thonly.reverie_dreams.compat.BorukvaFoodCompatImpl");
        load("borukva-food-exotic", "cc.thonly.reverie_dreams.compat.BorukvaFoodExoticCompatImpl");
        load("borukva-fish", "cc.thonly.reverie_dreams.compat.BorukvaFishCompatImpl");
        load("farmersdelight", "cc.thonly.reverie_dreams.compat.FarmersdelightCompatImpl");
        load("moredelight", "cc.thonly.reverie_dreams.compat.MoreDelightCompatImpl");
        load("oceansdelight-port", "cc.thonly.reverie_dreams.compat.OceansdelightCompatImpl");
        load("spanishdelight", "cc.thonly.reverie_dreams.compat.SpanishDelightCompatImpl");
        load("go-fish", "cc.thonly.reverie_dreams.compat.GoFishingCompatImpl");
        load("fishing101", "cc.thonly.reverie_dreams.compat.Fishing101CompatImpl");
        load("polyfactory", "cc.thonly.reverie_dreams.compat.PolyFactoryCompatImpl");
        load("create", "cc.thonly.reverie_dreams.compat.CreateFlyCompatImpl");
        load("appleskin", "cc.thonly.reverie_dreams.compat.AppleskinCompatImpl");
    }

    public static List<Tuple<Integer, Goal>> getCompatGoals(BaseNPCLikeEntity npcLikeEntity) {
        List<Tuple<Integer, Goal>> goals = new ArrayList<>();
        if (ConstantInfo.hasCreateFly()) {
            goals.addAll(CreateFlyCompatImpl.getGoals(npcLikeEntity));
        }
        if (ConstantInfo.hasPolyfactory()) {
            goals.addAll(PolyFactoryCompatImpl.getGoals(npcLikeEntity));
        }
        return goals;
    }

    public static void load(String modId,
                            @Pattern("[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*") String compatClassName
    ) {
        if (!FabricLoader.getInstance().isModLoaded(modId)) return;

        try {
            Class<?> clazz = Class.forName(compatClassName);
            clazz.getMethod("bootstrap").invoke(null);
            ReverieDreams.LOGGER.info("Loaded Compat for {}", modId);
        } catch (Throwable e) {
            log.warn("Can't load compat plugin {}", compatClassName, e);
        }
    }

}
