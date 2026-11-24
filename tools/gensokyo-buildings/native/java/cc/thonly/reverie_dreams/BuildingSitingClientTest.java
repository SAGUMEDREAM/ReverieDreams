package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapInstallState;
import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoChunkGenerator;
import com.google.gson.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.nio.file.*;
import java.util.*;

/** Real integrated-server installation and gate/road walking, in a disposable world. */
@SuppressWarnings("UnstableApiUsage")
public final class BuildingSitingClientTest implements FabricClientGameTest {
    private final Path output=Path.of(System.getProperty("reverie.building.evidence"));
    private final ResourceKey<Level> dimension=ResourceKey.create(Registries.DIMENSION,Identifier.parse("reverie_dreams:gensokyo"));
    private final List<String> checks=new ArrayList<>();
    private final Map<String,Object> metrics=new LinkedHashMap<>();
    private ClientGameTestContext context;
    private TestSingleplayerContext world;
    private void check(boolean success,String name){if(!success)throw new AssertionError(name);checks.add(name);}

    @Override public void runTest(ClientGameTestContext context){
        this.context=context;Throwable failed=null;
        try{
            Files.createDirectories(output.resolve("screenshots"));
            check(!FabricLoader.getInstance().isModLoaded("reverie_dreams_polymerify"),"native validation without Polymer");
            try(var single=context.worldBuilder().create()){
                world=single;
                context.getInput().resizeWindow(1600,900);
                context.runOnClient(mc->{mc.options.hideGui=true;mc.options.autoJump().set(false);mc.options.pauseOnLostFocus=false;
                    mc.options.renderDistance().set(10);mc.options.simulationDistance().set(3);mc.options.fov().set(75);mc.options.broadcastOptions();});
                command("difficulty peaceful");command("time set noon");command("weather clear");command("gamemode spectator @p");
                command("execute in reverie_dreams:gensokyo run forceload add 4794 7030 5046 7258");
                command("execute in reverie_dreams:gensokyo run tp @p 5038 1074 7144 90 0");
                boolean installed=false;long began=System.nanoTime();
                for(int attempt=0;attempt<360&&!installed;attempt++){
                    boolean[] done={false};
                    world.getServer().runOnServer(server->{var level=server.getLevel(dimension);
                        checkGenerator(level);
                        done[0]=level.getDataStorage().computeIfAbsent(GensokyoMapInstallState.TYPE).isInstalled("reverie_dreams:scarlet_devil_mansion");});
                    installed=done[0];if(!installed)context.waitTicks(20);
                }
                check(installed,"entire mansion installed through the existing chunk-safe anchor installer");
                metrics.put("installationWaitSeconds",(System.nanoTime()-began)/1e9);
                JsonObject samples=JsonParser.parseString(Files.readString(Path.of("D:/Gradle/ReverieDreams/data/shrine-mansion-20261119/native-samples.json"))).getAsJsonObject();
                world.getServer().runOnServer(server->{var level=server.getLevel(dimension);
                    for(JsonElement element:samples.getAsJsonArray("building")){
                        JsonObject q=element.getAsJsonObject();JsonArray a=q.getAsJsonArray("pos");var pos=new BlockPos(a.get(0).getAsInt(),a.get(1).getAsInt(),a.get(2).getAsInt());
                        String expected=q.get("block").getAsString();String actual=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString();
                        check(actual.equals(expected),"mansion source block "+expected+" at "+pos);
                        if(q.has("block_entity"))check(level.getBlockEntity(pos)!=null,"block entity retained at "+pos);
                    }
                    for(int x=5028;x<=5033;x++)for(int z=7142;z<=7146;z++)for(int y=1056;y<=1060;y++)
                        check(level.getBlockState(new BlockPos(x,y,z)).isAir(),"full gate headroom "+x+","+y+","+z);
                });
                camera(5037.5,1056.05,7144.5,90,0);
                command("gamemode adventure @p");context.waitTicks(20);double[] before=position();
                context.getInput().holdKeyFor(o->o.keyUp,75);context.waitTicks(3);double[] after=position();
                check(after[0]<5026,"walks from access road through the mansion gate with auto-jump disabled");
                context.getInput().holdKeyFor(o->o.keyDown,75);context.waitTicks(3);double[] back=position();
                check(back[0]>5031,"walks back from the courtyard to the access road");
                metrics.put("gateWalk",Map.of("start",before,"inside",after,"return",back));
                command("gamemode spectator @p");camera(5098,1080,7174,105,-9);photo("mansion-front-native");
                camera(4918,1245,7248,180,61);photo("mansion-overhead-native");
                command("execute in reverie_dreams:gensokyo run forceload remove all");
                for(JsonElement element:samples.getAsJsonArray("roads")){
                    JsonObject q=element.getAsJsonObject();JsonArray a=q.getAsJsonArray("pos");int x=a.get(0).getAsInt(),y=a.get(1).getAsInt(),z=a.get(2).getAsInt();
                    camera(x+.5,y+3,z+.5,0,20);
                    world.getServer().runOnServer(server->{var level=server.getLevel(dimension);var pos=new BlockPos(x,y,z);
                        check(level.getBlockState(pos).is(Blocks.DIRT_PATH)||level.getBlockState(pos).is(Blocks.ANDESITE_SLAB),"new road paving survives decoration at "+pos);
                        for(int dy=1;dy<=5;dy++)check(level.getBlockState(pos.above(dy)).isAir(),"road clearance at "+pos.above(dy));});
                }
                camera(11520,1212,5130,160,45);photo("shrine-site-native");
                camera(11568,1140,5748,30,29);photo("back-mountain-trail-native");
                context.waitTicks(40);
            }
        }catch(Throwable failure){failed=failure;failure.printStackTrace();}
        finally{try{Files.writeString(output.resolve("native-results.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status",failed==null?"PASS":"FAIL","failure",failed==null?"":failed.toString(),"checks",checks,"metrics",metrics,"polymer","not involved; not run")));}catch(Exception e){throw new RuntimeException(e);}}
        if(failed!=null)throw new AssertionError("Building siting validation failed",failed);
        System.out.println("BUILDING_SITING_NATIVE_PASS checks="+checks.size());
    }
    private void checkGenerator(ServerLevel level){if(level==null||!(level.getChunkSource().getGenerator() instanceof GensokyoChunkGenerator))throw new AssertionError("Expected fixed-map generator");}
    private void command(String text){world.getServer().runCommand(text);}
    private double[] position(){return context.computeOnClient(mc->new double[]{mc.player.getX(),mc.player.getY(),mc.player.getZ()});}
    private void camera(double x,double y,double z,float yaw,float pitch){
        command(String.format(Locale.ROOT,"execute in reverie_dreams:gensokyo run tp @p %.3f %.3f %.3f %.2f %.2f",x,y,z,yaw,pitch));
        context.waitFor(mc->mc.player!=null&&mc.level.dimension().equals(dimension)&&Math.abs(mc.player.getX()-x)<.1&&Math.abs(mc.player.getY()-y)<.1,3000);
        context.runOnClient(mc->{mc.player.setYRot(yaw);mc.player.setXRot(pitch);});
        context.waitFor(mc->{int cx=mc.player.chunkPosition().x(),cz=mc.player.chunkPosition().z();for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)if(mc.level.getChunk(cx+dx,cz+dz,ChunkStatus.FULL,false)==null)return false;return true;},3000);
        context.runOnClient(mc->mc.levelRenderer.allChanged());world.getClientLevel().waitForChunksRender(false,3000);context.waitTicks(30);
    }
    private void photo(String name)throws Exception{Files.copy(context.takeScreenshot(name),output.resolve("screenshots/"+name+".png"),StandardCopyOption.REPLACE_EXISTING);}
}
