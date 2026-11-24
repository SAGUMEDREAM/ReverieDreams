package cc.thonly.reverie_dreams.paper.world.forest;

import java.util.List;
import java.util.Locale;

/** Source forest catalog; the adapter activates only types with nonzero source-map columns. */
public enum GensokyoForestType {
    FOREST(5, 0.8F, new Mix(GensokyoTreeProfile.UNDERSTORY_OAK, 0.15F),
            new Mix(GensokyoTreeProfile.FOREST_BIRCH, 0.2F), new Mix(GensokyoTreeProfile.FOREST_OAK, 0.65F)),
    BIRCH_FOREST(5, 0.6F, new Mix(GensokyoTreeProfile.YOUNG_BIRCH, 0.18F),
            new Mix(GensokyoTreeProfile.TALL_BIRCH, 0.12F), new Mix(GensokyoTreeProfile.FOREST_BIRCH, 0.70F)),
    TAIGA(3, 0.8F, new Mix(GensokyoTreeProfile.YOUNG_SPRUCE, 0.2F), new Mix(GensokyoTreeProfile.SPRUCE, 0.8F)),
    CHERRY_GROVE(3, 0.8F, new Mix(GensokyoTreeProfile.YOUNG_CHERRY, 0.18F), new Mix(GensokyoTreeProfile.CHERRY, 0.82F)),
    DARK_FOREST(3, 0.8F, new Mix(GensokyoTreeProfile.UNDERSTORY_OAK, 0.15F),
            new Mix(GensokyoTreeProfile.FOREST_OAK, 0.10F), new Mix(GensokyoTreeProfile.DARK_OAK, 0.75F)),
    MAGIC_FOREST(3, 0.95F, new Mix(GensokyoTreeProfile.UNDERSTORY_OAK, 0.15F),
            new Mix(GensokyoTreeProfile.MAGIC_DARK_OAK, 0.25F), new Mix(GensokyoTreeProfile.MAGIC_OAK, 0.6F)),
    MAPLE_FOREST(4, 0.6F, new Mix(GensokyoTreeProfile.YOUNG_IROHA_MAPLE, 0.07F),
            new Mix(GensokyoTreeProfile.YOUNG_ITAYA_MAPLE, 0.05F), new Mix(GensokyoTreeProfile.YOUNG_HAUCHIWA_MAPLE, 0.06F),
            new Mix(GensokyoTreeProfile.IROHA_MAPLE, 0.28F), new Mix(GensokyoTreeProfile.HAUCHIWA_MAPLE, 0.24F),
            new Mix(GensokyoTreeProfile.OLD_ITAYA_MAPLE, 0.06F), new Mix(GensokyoTreeProfile.ITAYA_MAPLE, 0.24F));

    private final int densityScale;
    private final float downfall;
    private final List<Mix> mix;

    GensokyoForestType(int densityScale, float downfall, Mix... mix) {
        this.densityScale = densityScale;
        this.downfall = downfall;
        this.mix = List.of(mix);
    }

    public record Mix(GensokyoTreeProfile profile, float weight) {}
    public int densityScale() { return densityScale; }
    public float downfall() { return downfall; }
    public List<Mix> mix() { return mix; }
    public String biomePath() { return "gensokyo_" + name().toLowerCase(Locale.ROOT); }
    public String treesPath() { return "gensokyo/" + name().toLowerCase(Locale.ROOT) + "_trees"; }
}
