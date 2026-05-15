package cc.thonly.reverie_dreams.proxy;

import cc.thonly.keine.api.proxy.PlatformProxy;

import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class PlatformProxies {
    public static final Optional<FoodDisplayBlockEntityTicker> FOOD_DISPLAY_BLOCK_ENTITY_TICKER =
            PlatformProxy.<FoodDisplayBlockEntityTicker>builder()
                    .withPolymer("cc.thonly.reverie_dreams.polymer.proxy.FoodDisplayBlockEntityTickerImpl")
                    .buildOrNull();
    public static final Optional<FoodDisplayBlockEntityUpdater> FOOD_DISPLAY_BLOCK_ENTITY_UPDATER =
            PlatformProxy.<FoodDisplayBlockEntityUpdater>builder()
                    .withPolymer("cc.thonly.reverie_dreams.polymer.proxy.FoodDisplayBlockEntityUpdaterImpl")
                    .buildOrNull();
    public static final Optional<TenguCameraItemUse> TENGU_CAMERA_ITEM_USE =
            PlatformProxy.<TenguCameraItemUse>builder()
                    .withPolymer("cc.thonly.reverie_dreams.polymer.proxy.TenguCameraItemUseImpl")
                    .buildOrNull();
    public static final Optional<GensokyoAltarBlockEntityTicker> GENSOKYO_ALTAR_BLOCK_ENTITY_TICKER =
            PlatformProxy.<GensokyoAltarBlockEntityTicker>builder()
                    .withPolymer("cc.thonly.reverie_dreams.polymer.proxy.GensokyoAltarBlockEntityTickerImpl")
                    .buildOrNull();

    public static void initialize() {

    }

    public static <T, R> void access(Optional<T> method, Function<T, R> logic) {
        method.map((Function<T, Object>) logic::apply);
    }
}
