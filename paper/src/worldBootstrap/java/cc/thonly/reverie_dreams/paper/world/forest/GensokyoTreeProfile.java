package cc.thonly.reverie_dreams.paper.world.forest;

import java.util.Locale;

/** Total heights include the crown; the original sapling configurations are untouched. */
public enum GensokyoTreeProfile {
    FOREST_OAK(GensokyoTreeShape.Species.OAK, 12, 20, 1, 3),
    FOREST_BIRCH(GensokyoTreeShape.Species.BIRCH, 14, 20, 1, 3),
    TALL_BIRCH(GensokyoTreeShape.Species.BIRCH, 20, 24, 1, 3),
    YOUNG_BIRCH(GensokyoTreeShape.Species.BIRCH, 8, 12, 1, 2),
    SPRUCE(GensokyoTreeShape.Species.SPRUCE, 18, 30, 1, 5),
    YOUNG_SPRUCE(GensokyoTreeShape.Species.SPRUCE, 8, 14, 1, 3),
    CHERRY(GensokyoTreeShape.Species.CHERRY, 10, 18, 1, 4),
    YOUNG_CHERRY(GensokyoTreeShape.Species.CHERRY, 8, 12, 1, 3),
    DARK_OAK(GensokyoTreeShape.Species.DARK_OAK, 16, 26, 2, 5),
    MAGIC_OAK(GensokyoTreeShape.Species.OAK, 18, 26, 2, 4),
    MAGIC_DARK_OAK(GensokyoTreeShape.Species.DARK_OAK, 22, 32, 2, 6),
    UNDERSTORY_OAK(GensokyoTreeShape.Species.OAK, 8, 14, 1, 2),
    IROHA_MAPLE(GensokyoTreeShape.Species.IROHA_MAPLE, 12, 18, 1, 4),
    YOUNG_IROHA_MAPLE(GensokyoTreeShape.Species.IROHA_MAPLE, 8, 12, 1, 2),
    ITAYA_MAPLE(GensokyoTreeShape.Species.ITAYA_MAPLE, 18, 26, 2, 5),
    YOUNG_ITAYA_MAPLE(GensokyoTreeShape.Species.ITAYA_MAPLE, 8, 12, 1, 3),
    OLD_ITAYA_MAPLE(GensokyoTreeShape.Species.ITAYA_MAPLE, 26, 32, 2, 6),
    HAUCHIWA_MAPLE(GensokyoTreeShape.Species.HAUCHIWA_MAPLE, 14, 22, 1, 5),
    YOUNG_HAUCHIWA_MAPLE(GensokyoTreeShape.Species.HAUCHIWA_MAPLE, 8, 12, 1, 3);

    private final GensokyoTreeShape.Settings settings;

    GensokyoTreeProfile(GensokyoTreeShape.Species species, int minHeight, int maxHeight, int width, int radius) {
        this.settings = new GensokyoTreeShape.Settings(species, minHeight, maxHeight, width, radius);
    }

    public GensokyoTreeShape.Settings settings() { return settings; }
    public String path() { return "gensokyo/" + name().toLowerCase(Locale.ROOT); }
}
