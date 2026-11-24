import cc.thonly.reverie_dreams.world.gensokyo.tree.*;
import com.google.gson.*;
import java.nio.file.*;
import java.util.Random;

/** Geometry evidence only; never writes game assets or substitutes for Fabric datagen. */
public final class ExportJunglePreview {
    public static void main(String[] args) throws Exception {
        JsonArray result = new JsonArray();
        for (var entry : GensokyoForestType.JUNGLE.mix()) {
            for (int sample = 0; sample < 3; sample++) {
                var profile = entry.profile();
                long seed = 20261119L + sample * 104729L;
                var tree = GensokyoTreeShape.create(profile.settings(), new Random(seed));
                var item = new JsonObject();
                item.addProperty("profile", profile.name()); item.addProperty("species", profile.settings().species().name());
                item.addProperty("form", tree.form().name()); item.addProperty("weight", entry.weight());
                item.addProperty("seed", seed); item.addProperty("height", tree.height());
                var voxels = new JsonArray();
                for (var v : tree.voxels()) {
                    var cell = new JsonArray();
                    cell.add(v.point().x()); cell.add(v.point().y()); cell.add(v.point().z());
                    cell.add(v.log() ? 1 : 0); cell.add(v.axis().name()); cell.add(v.leafDistance());
                    voxels.add(cell);
                }
                item.add("voxels", voxels); result.add(item);
            }
        }
        Files.writeString(Path.of(args[0]), new GsonBuilder().create().toJson(result));
    }
}
