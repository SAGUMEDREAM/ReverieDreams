package cc.thonly.reverie_dreams.compat;

import cc.thonly.reverie_dreams.ReverieDreams;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.loader.api.FabricLoader;

@Slf4j
public class ReverieDreamsCompats {
    public static void init() {
        load("polydex", PolydexCompatImpl::bootstrap);
        load("polydex2eiv", Polydex2EIVCompatImpl::bootstrap);
        load("minecraft", VanillaCompat::bootstrap);
        load("borukva-food", BorukvaFoodCompatImpl::bootstrap);
        load("borukva-food-exotic", BorukvaFoodExoticCompatImpl::bootstrap);
        load("borukva-fish", BorukvaFishCompatImpl::bootstrap);
        load("farmersdelight", FarmersdelightCompatImpl::bootstrap);
        load("moredelight", MoreDelightCompatImpl::bootstrap);
        load("oceansdelight-port", OceansdelightCompatImpl::bootstrap);
        load("spanishdelight", SpanishDelightCompatImpl::bootstrap);
        load("go-fish", GoFishingCompatImpl::bootstrap);
        load("fishing101", Fishing101CompatImpl::bootstrap);
    }

    public static void load(String modId, CompatApplication application) {
        try {
            if (FabricLoader.getInstance().isModLoaded(modId)) {
                application.apply();
                ReverieDreams.LOGGER.info("Loaded Compat for {}", modId);
            }
        } catch (Exception e) {
            log.warn("Can't load compat plugin", e);
        }
    }

    @FunctionalInterface
    public interface CompatApplication {
        void apply();
    }
}
