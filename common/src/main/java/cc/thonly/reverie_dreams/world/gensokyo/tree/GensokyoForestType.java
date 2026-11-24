package cc.thonly.reverie_dreams.world.gensokyo.tree;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

/**
 * Forest variants; their distribution is controlled by the map data.
 */
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
    MAPLE_FOREST(4, 0.6F, mapleMix()),
    JUNGLE(5, 0.9F, new Mix(GensokyoTreeProfile.JUNGLE_CANOPY, 0.10F),
            new Mix(GensokyoTreeProfile.JUNGLE_FORKED, 0.22F), new Mix(GensokyoTreeProfile.JUNGLE_TIERED, 0.18F),
            new Mix(GensokyoTreeProfile.JUNGLE_LEANING, 0.16F), new Mix(GensokyoTreeProfile.JUNGLE_SPREADING, 0.16F),
            new Mix(GensokyoTreeProfile.JUNGLE_YOUNG, 0.18F));

    private final int densityScale;
    private final float downfall;
    private final List<Mix> mix;

    GensokyoForestType(int densityScale, float downfall, Mix... mix) {
        this.densityScale = densityScale;
        this.downfall = downfall;
        this.mix = List.of(mix);
    }

    public record Mix(GensokyoTreeProfile profile, float weight) {
    }

    private record MaplePair(GensokyoTreeProfile original, GensokyoTreeProfile addition, float weight) {
    }

    private static Mix[] mapleMix() {
        var choices = new ArrayList<Mix>();
        for (var pair : List.of(
                new MaplePair(GensokyoTreeProfile.YOUNG_IROHA_MAPLE, GensokyoTreeProfile.AUTUMN_YOUNG_IROHA_MAPLE, 0.07F),
                new MaplePair(GensokyoTreeProfile.YOUNG_ITAYA_MAPLE, GensokyoTreeProfile.AUTUMN_YOUNG_ITAYA_MAPLE, 0.05F),
                new MaplePair(GensokyoTreeProfile.YOUNG_HAUCHIWA_MAPLE, GensokyoTreeProfile.AUTUMN_YOUNG_HAUCHIWA_MAPLE, 0.06F),
                new MaplePair(GensokyoTreeProfile.IROHA_MAPLE, GensokyoTreeProfile.AUTUMN_IROHA_MAPLE, 0.28F),
                new MaplePair(GensokyoTreeProfile.HAUCHIWA_MAPLE, GensokyoTreeProfile.AUTUMN_HAUCHIWA_MAPLE, 0.24F),
                new MaplePair(GensokyoTreeProfile.OLD_ITAYA_MAPLE, GensokyoTreeProfile.AUTUMN_OLD_ITAYA_MAPLE, 0.06F),
                new MaplePair(GensokyoTreeProfile.ITAYA_MAPLE, GensokyoTreeProfile.AUTUMN_ITAYA_MAPLE, 0.24F))) {
            choices.add(new Mix(pair.original(), pair.weight() * 0.43F));
            choices.add(new Mix(pair.addition(), pair.weight() * 0.43F));
        }
        choices.add(new Mix(GensokyoTreeProfile.FOREST_OAK, 0.06F));
        choices.add(new Mix(GensokyoTreeProfile.SPRUCE, 0.04F));
        choices.add(new Mix(GensokyoTreeProfile.YOUNG_SPRUCE, 0.04F));
        return choices.toArray(Mix[]::new);
    }

    public int densityScale() {
        return densityScale;
    }

    public float downfall() {
        return downfall;
    }

    public List<Mix> mix() {
        return mix;
    }

    public Identifier biomeId() {
        return ReverieDreams.id(biomePath());
    }

    public ResourceKey<Biome> biomeKey() {
        return ResourceKey.create(Registries.BIOME, biomeId());
    }

    public String biomePath() {
        return "gensokyo_" + name().toLowerCase(Locale.ROOT);
    }

    public String treesPath() {
        return "gensokyo/" + name().toLowerCase(Locale.ROOT) + "_trees";
    }
}
