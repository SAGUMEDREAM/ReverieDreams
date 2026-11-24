package cc.thonly.reverie_dreams.paper.world.forest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Random;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;

class GensokyoForestTest {
    private JsonObject goldens() throws Exception {
        try(var reader=new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/worldgen/source-forest-goldens.json")),StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
    @Test void sourceOracleVoxelHashesRemainIdenticalForEveryProfile() throws Exception {
        for(var value:goldens().getAsJsonArray("geometry")) {
            var row=value.getAsJsonObject();var profile=GensokyoTreeProfile.valueOf(row.get("profile").getAsString());
            var shape=GensokyoTreeShape.create(profile.settings(),new Random(row.get("seed").getAsLong()));
            assertEquals(row.get("height").getAsInt(),shape.height());var text=new StringBuilder();int logs=0,leaves=0;
            for(var voxel:shape.voxels()) {
                text.append(voxel.point().x()).append(',').append(voxel.point().y()).append(',').append(voxel.point().z())
                        .append(':').append(voxel.log()?"log/"+voxel.axis():"leaf/"+voxel.leafDistance()).append('\n');
                if(voxel.log())logs++;else leaves++;
            }
            assertEquals(row.get("logs").getAsInt(),logs);assertEquals(row.get("leaves").getAsInt(),leaves);
            assertEquals(row.get("sha256").getAsString(),HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(text.toString().getBytes(StandardCharsets.UTF_8))),profile.name());
        }
    }
    @Test void nativeDensityNoiseAndCountsMatchCapturedRuntimeBits() throws Exception {
        var noise=new GensokyoForestNoise();
        for(var value:goldens().getAsJsonArray("density")) {
            var row=value.getAsJsonObject();int x=row.get("x").getAsInt(),z=row.get("z").getAsInt();
            assertEquals(row.get("native_bits").getAsString(),Long.toHexString(Double.doubleToLongBits(noise.value(x/96d,z/96d))));
            assertEquals(row.get("forest_count").getAsInt(),noise.attempts(GensokyoForestType.FOREST,x,z));
            assertEquals(row.get("cherry_count").getAsInt(),noise.attempts(GensokyoForestType.CHERRY_GROVE,x,z));
        }
    }
    @Test void nativeSelectorAndShapeSeedChainRemainExact() throws Exception {
        for(var value:goldens().getAsJsonArray("native_selector_rng")) {
            var row=value.getAsJsonObject();var random=new GensokyoForestRandom(0);
            long seed=random.decorationSeed(row.get("world_seed").getAsLong(),row.get("chunk_x").getAsInt()<<4,row.get("chunk_z").getAsInt()<<4);
            random.sourceFeatureSeed(seed,row.get("feature_index").getAsInt());
            assertEquals(row.get("profile").getAsString(),GensokyoForestPlanner.select(GensokyoForestType.valueOf(row.get("type").getAsString()),random).name());
            assertEquals(row.get("shape_seed").getAsLong(),random.nextLong());
        }
    }
    @Test void obstructedTreeNeverWritesAnEarlierUnobstructedVoxel() {
        for(var profile:GensokyoTreeProfile.values())for(int failure=1;failure<=5;failure++) {
            var planned=GensokyoForestPlanner.create(profile,21,0,1025,0);var memory=new Region();memory.failure=failure;
            memory.blocked=planned.writes().getLast().point();
            assertFalse(GensokyoForestPlanner.place(memory,profile,21,0,1025,0));assertTrue(memory.writes.isEmpty());
        }
    }
    private static final class Region implements GensokyoForestPlanner.Region {
        int failure;GensokyoForestPlanner.Point blocked;
        final Map<GensokyoForestPlanner.Point,GensokyoForestPlanner.Write> writes=new HashMap<>();
        public int minY(){return 0;}public int maxY(){return 1536;}
        public boolean canWrite(int x,int y,int z){return failure!=1 || x<0;}
        public boolean substrate(int x,int y,int z){return failure!=2;}
        public boolean validTreePosition(int x,int y,int z){return failure!=3 || !new GensokyoForestPlanner.Point(x,y,z).equals(blocked);}
        public boolean fluid(int x,int y,int z){return failure==4 && new GensokyoForestPlanner.Point(x,y,z).equals(blocked);}
        public boolean blockEntity(int x,int y,int z){return failure==5 && new GensokyoForestPlanner.Point(x,y,z).equals(blocked);}
        public void write(GensokyoTreeProfile profile,GensokyoForestPlanner.Write value){writes.put(value.point(),value);}
    }
}
