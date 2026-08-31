package cc.thonly.reverie_dreams.proxy;

import cc.thonly.keine.api.proxy.PlatformProxy;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ByModsPlatformProxy {
    public static final Optional<PlateBlockEntityTicker> PLATE_BLOCK_ENTITY_TICKER =
            PlatformProxy.<PlateBlockEntityTicker>builder()
                         .withPolymer("cc.thonly.reverie_dreams.polymer.proxy.PlateBlockEntityTickerImpl")
                         .buildOrNull();
    public static final Optional<PlateBlockEntityUpdater> PLATE_BLOCK_ENTITY_UPDATER =
            PlatformProxy.<PlateBlockEntityUpdater>builder()
                         .withPolymer("cc.thonly.reverie_dreams.polymer.proxy.PlateBlockEntityUpdaterImpl")
                         .buildOrNull();
    public static final Optional<TenguCameraItemUse> TENGU_CAMERA_ITEM_USE =
            PlatformProxy.<TenguCameraItemUse>builder()
                         .withPolymer("cc.thonly.reverie_dreams.polymer.proxy.TenguCameraItemUseImpl")
                         .buildOrNull();
    public static final Optional<GensokyoAltarBlockEntityTicker> GENSOKYO_ALTAR_BLOCK_ENTITY_TICKER =
            PlatformProxy.<GensokyoAltarBlockEntityTicker>builder()
                         .withPolymer("cc.thonly.reverie_dreams.polymer.proxy.GensokyoAltarBlockEntityTickerImpl")
                         .buildOrNull();
}
