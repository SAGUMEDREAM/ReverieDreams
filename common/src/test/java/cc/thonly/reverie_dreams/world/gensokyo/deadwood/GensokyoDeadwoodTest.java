package cc.thonly.reverie_dreams.world.gensokyo.deadwood;

import cc.thonly.reverie_dreams.block.nature.SplinteredLogBlock;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.MapColor;

import static org.junit.jupiter.api.Assertions.*;

class GensokyoDeadwoodTest {
    static SplinteredLogBlock split;
    @BeforeAll static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        // Same isolated registry fixture as the existing decoration tests; restore it immediately.
        var registry=BuiltInRegistries.BLOCK;
        var holders=MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
        var frozen=MappedRegistry.class.getDeclaredField("frozen");
        holders.setAccessible(true);frozen.setAccessible(true);
        Object previous=holders.get(registry);boolean wasFrozen=frozen.getBoolean(registry);
        try {
            frozen.setBoolean(registry,false);holders.set(registry,new IdentityHashMap<>());
            split = new SplinteredLogBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)
                    .mapColor(MapColor.WOOD).noOcclusion()
                    .setId(ResourceKey.create(Registries.BLOCK, Identifier.parse("test:split_log"))));
        } finally {holders.set(registry,previous);frozen.setBoolean(registry,wasFrozen);}
    }
    static GensokyoDeadwoodConfiguration config(GensokyoForestType theme, GensokyoDeadwoodConfiguration.Kind kind) {
        return new GensokyoDeadwoodConfiguration(theme,kind,List.of(Blocks.OAK_LOG.defaultBlockState()),
                List.of(split.defaultBlockState()),List.of(),List.of());
    }
    @Test void everyThemeAndKindPlacesOneCompleteSpecimenOnFlatSoil() {
        for (var theme : GensokyoForestType.values()) for (var kind : GensokyoDeadwoodConfiguration.Kind.values()) {
            var fixture = new Site(); long seed = 17;
            assertTrue(GensokyoDeadwoodFeature.placeDeadwood(fixture.level,BlockPos.ZERO,config(theme,kind),seed),theme+" "+kind);
            assertEquals(GensokyoDeadwoodShape.create(theme,kind,seed).cells().size(),fixture.writes.size());
            assertTrue(fixture.writes.keySet().stream().allMatch(p -> p.getY() >= 0),"No excavation");
        }
    }
    @Test void anObstacleAtTheLastCellRejectsTheWholeSpecimenWithoutWrites() {
        for (var kind : GensokyoDeadwoodConfiguration.Kind.values()) {
            long seed=91;
            var shape=GensokyoDeadwoodShape.create(GensokyoForestType.MAGIC_FOREST,kind,seed);
            var p=shape.cells().getLast().point();
            Direction forward=Direction.Plane.HORIZONTAL.getRandomDirection(RandomSource.create(seed));
            Direction side=forward.getClockWise();
            var blocked=new BlockPos(forward.getStepX()*p.x()+side.getStepX()*p.z(),p.y(),forward.getStepZ()*p.x()+side.getStepZ()*p.z());
            var fixture=new Site();fixture.existing.put(blocked,Blocks.BEDROCK.defaultBlockState());
            assertFalse(GensokyoDeadwoodFeature.placeDeadwood(fixture.level,BlockPos.ZERO,config(GensokyoForestType.MAGIC_FOREST,kind),seed));
            assertTrue(fixture.writes.isEmpty());
            assertTrue(fixture.existing.get(blocked).is(Blocks.BEDROCK));
        }
    }
    @Test void waterRoadsSteepGroundAndUnwritablePositionsRejectWithoutWrites() {
        for (int mode=0;mode<4;mode++) {
            var fixture=new Site();fixture.mode=mode+1;
            assertFalse(GensokyoDeadwoodFeature.placeDeadwood(fixture.level,BlockPos.ZERO,
                    config(GensokyoForestType.DARK_FOREST,GensokyoDeadwoodConfiguration.Kind.FALLEN_LOG),123));
            assertTrue(fixture.writes.isEmpty(),"Mode "+mode);
        }
    }
    @Test void cutTopsAreLevelWhileImpactScenesHaveOpenSplitsAndAlignedDebris() {
        for (var theme : GensokyoForestType.values()) for (long seed=0;seed<12;seed++) {
            var cut=GensokyoDeadwoodShape.create(theme,GensokyoDeadwoodConfiguration.Kind.CUT_STUMP,seed);
            assertTrue(cut.cells().stream().noneMatch(GensokyoDeadwoodShape.Cell::splinter));
            var splitShape=GensokyoDeadwoodShape.create(theme,GensokyoDeadwoodConfiguration.Kind.SPLINTERED_TRUNK,seed);
            assertTrue(splitShape.cells().stream().anyMatch(GensokyoDeadwoodShape.Cell::splinter));
            assertTrue(splitShape.cells().stream().anyMatch(c -> c.point().x()>splitShape.width()+1 && c.point().y()==0));
        }
        for (int variant=0;variant<3;variant++) {
            var state=split.defaultBlockState().setValue(SplinteredLogBlock.VARIANT,variant);
            var shape=state.getCollisionShape(new Site().level,BlockPos.ZERO);
            double volume=shape.toAabbs().stream().mapToDouble(b -> b.getXsize()*b.getYsize()*b.getZsize()).sum();
            assertTrue(volume>.3 && volume<.85,"Split must have substantial wood and a real open notch");
        }
    }
    @Test void fallenLogAxesFollowTheRotatedTrunkAndBranches() {
        for (long seed=0;seed<8;seed++) {
            var fixture=new Site();var theme=GensokyoForestType.CHERRY_GROVE;
            var kind=GensokyoDeadwoodConfiguration.Kind.FALLEN_LOG;
            assertTrue(GensokyoDeadwoodFeature.placeDeadwood(fixture.level,BlockPos.ZERO,config(theme,kind),seed));
            var forward=Direction.Plane.HORIZONTAL.getRandomDirection(RandomSource.create(seed));
            var side=forward.getClockWise();
            for (var c : GensokyoDeadwoodShape.create(theme,kind,seed).cells()) {
                var p=c.point();var pos=new BlockPos(forward.getStepX()*p.x()+side.getStepX()*p.z(),p.y(),forward.getStepZ()*p.x()+side.getStepZ()*p.z());
                var expected=c.axis()=='Y'?Direction.Axis.Y:c.axis()=='X'?forward.getAxis():side.getAxis();
                assertEquals(expected,fixture.writes.get(pos).getValue(RotatedPillarBlock.AXIS));
            }
        }
    }
    @Test void allPreviewConfigurationsAndModelsArePackagedWithNoScatterModifiers() throws Exception {
        assertEquals(21,GensokyoDeadwoodBootstrap.PROFILES.size());
        for (var profile : GensokyoDeadwoodBootstrap.PROFILES) {
            String path=profile.path();
            var configured=json("data/reverie_dreams/worldgen/configured_feature/"+path+".json");
            assertEquals("reverie_dreams:gensokyo_deadwood",configured.get("type").getAsString());
            var cfg=configured.getAsJsonObject("config");
            assertEquals(cfg.getAsJsonArray("logs").size(),cfg.getAsJsonArray("splinters").size());
            assertEquals(0,json("data/reverie_dreams/worldgen/placed_feature/"+path+".json").getAsJsonArray("placement").size());
        }
        for (String wood : List.of("oak","birch","spruce","cherry","dark_oak","iroha_maple","itaya_maple","hauchiwa_maple")) {
            assertEquals(12,json("assets/reverie_dreams/blockstates/splintered_"+wood+"_log.json").getAsJsonArray("multipart").size());
            for (String language:List.of("zh_cn","zh_tw","en_us","ja_jp"))
                assertTrue(json("assets/reverie_dreams/lang/"+language+".json").has("block.reverie_dreams.splintered_"+wood+"_log"));
        }
        for (int variant=0;variant<3;variant++) {
            var elements=json("assets/reverie_dreams/models/block/deadwood/split_"+variant+".json").getAsJsonArray("elements");
            var boxes=SplinteredLogBlock.boxes(variant);assertEquals(boxes.size(),elements.size());
            for(int n=0;n<boxes.size();n++) {
                var b=boxes.get(n);var e=elements.get(n).getAsJsonObject();
                assertEquals(List.of(b.x0(),b.y0(),b.z0()),e.getAsJsonArray("from").asList().stream().map(v->v.getAsInt()).toList());
                assertEquals(List.of(b.x1(),b.y1(),b.z1()),e.getAsJsonArray("to").asList().stream().map(v->v.getAsInt()).toList());
            }
        }
    }
    static JsonObject json(String name) throws Exception {
        var resource=GensokyoDeadwoodTest.class.getClassLoader().getResourceAsStream(name);
        // Loader jars merge generated resources; common's standalone tests also read their source tree.
        if(resource==null) resource=Files.newInputStream(Path.of("src/generated/resources").resolve(name));
        try(var input=resource) {
            assertNotNull(input,name);return JsonParser.parseReader(new InputStreamReader(input,StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
    static final class Site {
        final Map<BlockPos,BlockState> existing=new HashMap<>(),writes=new HashMap<>(); int mode;
        BlockState state(BlockPos p) {
            if(writes.containsKey(p))return writes.get(p);
            if(existing.containsKey(p))return existing.get(p);
            if(mode==1 && p.getY()==0)return Blocks.WATER.defaultBlockState();
            if(mode==2 && p.equals(new BlockPos(1,-1,0)))return Blocks.DIRT_PATH.defaultBlockState();
            int surface=mode==3 && (p.getX()!=0 || p.getZ()!=0)?-4:-1;
            return p.getY()==surface?Blocks.GRASS_BLOCK.defaultBlockState():p.getY()<surface?Blocks.DIRT.defaultBlockState():Blocks.AIR.defaultBlockState();
        }
        final WorldGenLevel level=(WorldGenLevel)Proxy.newProxyInstance(WorldGenLevel.class.getClassLoader(),new Class[]{WorldGenLevel.class},(proxy,method,args)->{
            return switch(method.getName()) {
                case "getBlockState" -> state((BlockPos)args[0]);
                case "getFluidState" -> state((BlockPos)args[0]).getFluidState();
                case "getBlockEntity" -> null;
                case "ensureCanWrite" -> mode!=4 || ((BlockPos)args[0]).getX()==0 && ((BlockPos)args[0]).getZ()==0;
                case "isOutsideBuildHeight" -> false;
                case "getMinY" -> -64;
                case "getHeight" -> 384;
                case "setBlock" -> {writes.put(((BlockPos)args[0]).immutable(),(BlockState)args[1]);yield true;}
                case "getSeed" -> 42L;
                default -> throw new UnsupportedOperationException(method.toString());
            };
        });
    }
}
