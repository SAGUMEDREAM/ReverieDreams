package cc.thonly.reverie_dreams.block.decor.workshop;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum WorkshopKind implements StringRepresentable {
    EMA_RACK(Mode.DECOR, true),
    OFUDA_RACK(Mode.DECOR, true),
    OMAMORI_CABINET(Mode.STORAGE, true),
    KOMAINU_A(Mode.DECOR, true),
    KOMAINU_UN(Mode.DECOR, true),
    FUMIZUKUE(Mode.DESK, true),
    SUZURI(Mode.DECOR, false),
    SCROLL_RACK(Mode.STORAGE, true),
    BOOK_STACK(Mode.DECOR, false),
    BOOK_BOX(Mode.STORAGE, true),
    MEDICINE_CABINET(Mode.STORAGE, true),
    YAGEN(Mode.DECOR, false),
    STONE_MILL(Mode.DECOR, true),
    HERB_DRYING_TRAY(Mode.DECOR, false),
    MEDICINE_JAR_RACK(Mode.STORAGE, true),
    VERMILION_RAILING(Mode.RAILING, true),
    TAIKO_BRIDGE_APPROACH(Mode.BRIDGE, true),
    TAIKO_BRIDGE_RISE(Mode.BRIDGE, true),
    TAIKO_BRIDGE_CROWN(Mode.BRIDGE, true),
    FLOWER_VASE(Mode.DECOR, false),
    SAKURA_BYOBU(Mode.DECOR, false),
    CONNECTED_COUNTER(Mode.COUNTER, true),
    MERCHANDISE_SHELF(Mode.STORAGE, true),
    SAKE_CASK_RACK(Mode.STORAGE, true),
    INGREDIENT_BASKET(Mode.STORAGE, true),
    SHOP_BENCH(Mode.BENCH, true);

    public enum Mode {DECOR, STORAGE, DESK, COUNTER, BENCH, BRIDGE, RAILING}

    private final Mode mode;
    private final boolean collision;

    WorkshopKind(Mode mode, boolean collision) {
        this.mode = mode;
        this.collision = collision;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getSerializedName() {
        return id();
    }

    public Mode mode() {
        return mode;
    }

    public boolean collision() {
        return collision;
    }

    public boolean storage() {
        return mode == Mode.STORAGE || mode == Mode.DESK || mode == Mode.COUNTER;
    }
}
