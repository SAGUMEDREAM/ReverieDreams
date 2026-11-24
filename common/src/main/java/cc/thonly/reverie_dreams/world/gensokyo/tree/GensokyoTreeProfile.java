package cc.thonly.reverie_dreams.world.gensokyo.tree;

import java.util.Locale;

/**
 * Total heights include the crown; the original sapling configurations are untouched.
 */
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
    YOUNG_HAUCHIWA_MAPLE(GensokyoTreeShape.Species.HAUCHIWA_MAPLE, 8, 12, 1, 3),

    AUTUMN_IROHA_MAPLE(GensokyoTreeShape.Species.IROHA_MAPLE, 16, 24, 1, 4, GensokyoTreeShape.CrownStyle.TALL_MAPLE),
    AUTUMN_YOUNG_IROHA_MAPLE(GensokyoTreeShape.Species.IROHA_MAPLE, 8, 12, 1, 2, GensokyoTreeShape.CrownStyle.TALL_MAPLE),
    AUTUMN_ITAYA_MAPLE(GensokyoTreeShape.Species.ITAYA_MAPLE, 20, 28, 2, 5, GensokyoTreeShape.CrownStyle.TALL_MAPLE),
    AUTUMN_YOUNG_ITAYA_MAPLE(GensokyoTreeShape.Species.ITAYA_MAPLE, 8, 12, 1, 3, GensokyoTreeShape.CrownStyle.TALL_MAPLE),
    AUTUMN_OLD_ITAYA_MAPLE(GensokyoTreeShape.Species.ITAYA_MAPLE, 26, 34, 2, 6, GensokyoTreeShape.CrownStyle.TALL_MAPLE),
    AUTUMN_HAUCHIWA_MAPLE(GensokyoTreeShape.Species.HAUCHIWA_MAPLE, 18, 26, 1, 5, GensokyoTreeShape.CrownStyle.TALL_MAPLE),
    AUTUMN_YOUNG_HAUCHIWA_MAPLE(GensokyoTreeShape.Species.HAUCHIWA_MAPLE, 8, 12, 1, 3, GensokyoTreeShape.CrownStyle.TALL_MAPLE),

    JUNGLE_CANOPY(GensokyoTreeShape.Species.JUNGLE, 26, 36, 2, 6, GensokyoTreeShape.CrownStyle.JUNGLE_CANOPY),
    JUNGLE_FORKED(GensokyoTreeShape.Species.JUNGLE, 18, 26, 2, 4, GensokyoTreeShape.CrownStyle.JUNGLE_FORKED),
    JUNGLE_TIERED(GensokyoTreeShape.Species.JUNGLE, 20, 30, 1, 4, GensokyoTreeShape.CrownStyle.JUNGLE_TIERED),
    JUNGLE_LEANING(GensokyoTreeShape.Species.JUNGLE, 14, 22, 1, 3, GensokyoTreeShape.CrownStyle.JUNGLE_LEANING),
    JUNGLE_SPREADING(GensokyoTreeShape.Species.JUNGLE, 12, 18, 1, 5, GensokyoTreeShape.CrownStyle.JUNGLE_SPREADING),
    JUNGLE_YOUNG(GensokyoTreeShape.Species.JUNGLE, 8, 12, 1, 2, GensokyoTreeShape.CrownStyle.JUNGLE_YOUNG);

    private final GensokyoTreeShape.Settings settings;

    GensokyoTreeProfile(GensokyoTreeShape.Species species, int minHeight, int maxHeight, int width, int radius) {
        this(species, minHeight, maxHeight, width, radius, GensokyoTreeShape.CrownStyle.BRANCHING);
    }

    GensokyoTreeProfile(GensokyoTreeShape.Species species, int minHeight, int maxHeight, int width, int radius,
                        GensokyoTreeShape.CrownStyle crownStyle) {
        this.settings = new GensokyoTreeShape.Settings(species, minHeight, maxHeight, width, radius, crownStyle);
    }

    public GensokyoTreeShape.Settings settings() {
        return settings;
    }

    public boolean isAutumnMaple() {
        return settings.crownStyle() == GensokyoTreeShape.CrownStyle.TALL_MAPLE;
    }

    public String path() {
        return "gensokyo/" + name().toLowerCase(Locale.ROOT);
    }
}
