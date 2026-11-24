package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.gensokyo.GensokyoLandmark;
import cc.thonly.reverie_dreams.data.gensokyo.LandmarkKind;
import cc.thonly.reverie_dreams.fabric.util.DataProviderHelper;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Source of truth for the Gensokyo compass landmark registry.
 *
 * <p>Coordinates come from the map reserve dataset
 * ({@code data/roads-and-places-reserve-20261103/landmarks.csv}). The CSV's
 * Y fields are terrain context only and intentionally not stored: compass
 * guidance only needs horizontal X/Z. The shrine and mansion coordinates were
 * refined against the actual terrain/blueprint in data/shrine-mansion-20261119.</p>
 */
public class GensokyoLandmarkProvider implements DataProvider {
    private final FabricPackOutput output;
    private final CompletableFuture<HolderLookup.Provider> future;
    private final Map<Identifier, GensokyoLandmark> landmarks = new Object2ObjectLinkedOpenHashMap<>(64);

    public GensokyoLandmarkProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        this.output = output;
        this.future = future;
    }

    @Override
    public CompletableFuture<?> run(@NonNull CachedOutput cache) {
        return this.future.thenAcceptAsync(provider -> {
            this.configured();
            DataProviderHelper.outputFile(cache, this.landmarks, GensokyoLandmark.CODEC, "gensokyo_landmark");
        });
    }

    private void configured() {
        this.add("higan", LandmarkKind.REALM_REGION, 448, 2176);
        this.add("sanzu_river", LandmarkKind.WATER_REGION, 1408, 2256);
        this.add("sai_no_kawara", LandmarkKind.REGION, 2496, 2736);
        this.add("youkai_mountain_peak", LandmarkKind.TERRAIN, 4192, 5616);
        this.add("tengu_settlement", LandmarkKind.SITE, 4880, 4880);
        this.add("lake_suwako", LandmarkKind.WATER_REGION, 5319, 4780);
        this.add("moriya_shrine", LandmarkKind.SITE, 5267, 4911);
        this.add("nine_heaven_waterfall", LandmarkKind.WATERFALL, 5382, 4978);
        this.add("tenjo_roof", LandmarkKind.UNCERTAIN_LABEL, 4112, 6272);
        this.add("rainbow_dragon_cave", LandmarkKind.ENTRANCE, 4368, 6048);
        this.add("secret_heaven_cliff", LandmarkKind.TERRAIN, 4288, 6480);
        this.add("giant_toad_pond", LandmarkKind.WATER_REGION, 4704, 6832);
        this.add("scarlet_devil_mansion", LandmarkKind.SITE, 4920, 7144);
        this.add("cirno_house", LandmarkKind.WATER_SITE, 5826, 6891);
        this.add("youkai_mountain_geyser", LandmarkKind.ENTRANCE, 3376, 8656);
        this.add("hell_valley_entrance", LandmarkKind.ENTRANCE, 3376, 8656);
        this.add("road_of_the_middle", LandmarkKind.SUBTERRANEAN, 1968, 8912);
        this.add("abandoned_mansion", LandmarkKind.SITE, 4960, 9904);
        this.add("nameless_hill", LandmarkKind.REGION, 6016, 9120);
        this.add("lily_of_the_valley_field", LandmarkKind.REGION, 6272, 9424);
        this.add("sunflower_field", LandmarkKind.REGION, 7248, 8096);
        this.add("senkai_entrance_ibara", LandmarkKind.REALM_PORTAL, 5616, 3584);
        this.add("sacred_asanagi_mountain", LandmarkKind.SITE, 7444, 1676);
        this.add("youkai_tree_sea", LandmarkKind.REGION, 7136, 3680);
        this.add("lost_house", LandmarkKind.SITE, 8992, 2832);
        this.add("human_village", LandmarkKind.SITE, 7072, 5120);
        this.add("myouren_temple", LandmarkKind.SITE, 7712, 4336);
        this.add("senkai_entrance_shinreibyou", LandmarkKind.REALM_PORTAL, 8128, 3920);
        this.add("youkai_tanuki_forest", LandmarkKind.REGION, 8816, 3696);
        this.add("human_village_plains", LandmarkKind.REGION, 8656, 4704);
        this.add("willow_canal", LandmarkKind.WATER_REGION, 8992, 5680);
        this.add("beast_path", LandmarkKind.ROUTE_REGION, 10224, 5584);
        this.add("bamboo_forest_of_the_lost", LandmarkKind.REGION, 9120, 8304);
        this.add("shining_needle_castle", LandmarkKind.HEIGHT_UNKNOWN_SITE, 10112, 7296);
        this.add("hakurei_shrine", LandmarkKind.SITE, 11386, 5013);
        this.add("hakurei_shrine_back_mountain", LandmarkKind.REGION, 11419, 4883);
        this.add("hakurei_geyser", LandmarkKind.ENTRANCE, 12208, 5440);
        this.add("kourindou", LandmarkKind.SITE, 10398, 3686);
        this.add("three_fairies_new_house", LandmarkKind.SITE_LABEL, 11392, 3968);
        this.add("ghost_mansion", LandmarkKind.SITE, 10608, 2960);
        this.add("genbu_ravine", LandmarkKind.TERRAIN, 10160, 2304);
        this.add("kirisame_residence", LandmarkKind.SITE, 11454, 2642);
        this.add("okihori", LandmarkKind.WATER_REGION, 11136, 2816);
        this.add("margatroid_residence", LandmarkKind.SITE, 11685, 3401);
        this.add("earthly_rainbow_cave", LandmarkKind.ENTRANCE, 12032, 3024);
        this.add("forest_of_magic", LandmarkKind.REGION, 12592, 3168);
        this.add("road_of_rethinking", LandmarkKind.ROUTE_REGION, 11936, 2192);
        this.add("muenzuka", LandmarkKind.REGION, 12208, 1680);
        this.add("netherworld_barrier", LandmarkKind.AERIAL, 11536, 816);
        this.add("phantom_meadow", LandmarkKind.REGION, 11072, 9648);
        this.add("house_of_eternity", LandmarkKind.SITE, 9120, 8304);
    }

    private void add(String id, LandmarkKind kind, int x, int z) {
        Identifier identifier = ReverieDreams.id(id);
        String key = "landmark.reverie_dreams." + id;
        GensokyoLandmark landmark = new GensokyoLandmark(
                this.landmarks.size() + 1,
                kind,
                x,
                z,
                key,
                Optional.of(key + ".note")
        );
        this.landmarks.put(identifier, landmark);
        BuiltInRegistryProviders.register(BuiltInRegistryProviders.GENSOKYO_LANDMARK, identifier, landmark);
    }

    @Override
    public @NonNull String getName() {
        return "Gensokyo Landmark Provider";
    }
}
