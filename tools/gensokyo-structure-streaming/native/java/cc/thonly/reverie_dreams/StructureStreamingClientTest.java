package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoChunkGenerator;
import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoStructureChunkState;
import com.google.gson.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/** No whole-building forceload: examine first client chunk visibility and dependent decorations. */
@SuppressWarnings("UnstableApiUsage")
public final class StructureStreamingClientTest implements FabricClientGameTest {
    private final Path out=Path.of(System.getProperty("reverie.building.evidence"));
    private final String mode=System.getProperty("reverie.structure.mode","native");
    private final ResourceKey<Level> dimension=ResourceKey.create(Registries.DIMENSION,Identifier.parse("reverie_dreams:gensokyo"));
    private final List<String> checks=new ArrayList<>();
    private final Map<String,Object> metrics=new LinkedHashMap<>();
    private final AtomicReference<String> visibleFailure=new AtomicReference<>();
    private final Set<String> seen=new HashSet<>();
    private volatile boolean watch;
    private ClientGameTestContext context;
    private TestSingleplayerContext world;
    private JsonObject samples;
    private void check(boolean yes,String label){if(!yes)throw new AssertionError(label);checks.add(label);}

    @Override public void runTest(ClientGameTestContext context){
        this.context=context;Throwable error=null;long[] start={0};List<Double> ticks=Collections.synchronizedList(new ArrayList<>());
        try {
            Files.createDirectories(out.resolve("screenshots"));
            samples=JsonParser.parseString(Files.readString(Path.of("D:/Gradle/ReverieDreams/data/structure-streaming-20261119/probe-points.json"))).getAsJsonObject();
            ClientTickEvents.END_CLIENT_TICK.register(mc->{
                if(!watch||mc.level==null||!mc.level.dimension().equals(dimension))return;
                for(var entry:samples.getAsJsonObject("chunks").entrySet()){
                    if(seen.contains(entry.getKey()))continue;
                    String[] c=entry.getKey().split(",");int cx=Integer.parseInt(c[0]),cz=Integer.parseInt(c[1]);
                    if(mc.level.getChunk(cx,cz,ChunkStatus.FULL,false)==null)continue;
                    seen.add(entry.getKey());
                    for(JsonElement value:entry.getValue().getAsJsonArray()){
                        JsonObject q=value.getAsJsonObject();BlockPos p=pos(q);var state=mc.level.getBlockState(p);String expected=q.get("block").getAsString();
                        boolean yes=expected.equals("air")?state.isAir():BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().equals(expected);
                        if(!yes)visibleFailure.compareAndSet(null,"First visible chunk "+entry.getKey()+" at "+p+" expected "+expected+" got "+state);
                    }
                }
            });
            ServerTickEvents.START_SERVER_TICK.register(server->{if(watch)start[0]=System.nanoTime();});
            ServerTickEvents.END_SERVER_TICK.register(server->{if(watch&&start[0]!=0)ticks.add((System.nanoTime()-start[0])/1e6);});
            {
                world=context.worldBuilder().create();
                context.getInput().resizeWindow(1280,800);
                context.runOnClient(mc->{mc.options.hideGui=true;mc.options.renderDistance().set(4);mc.options.simulationDistance().set(3);mc.options.autoJump().set(false);mc.options.pauseOnLostFocus=false;mc.options.broadcastOptions();});
                command("difficulty peaceful");command("gamemode spectator @p");command("weather clear");command("time set noon");
                watch=true;
                for(double x:new double[]{5180,5110,5050,5000,4940,4880}){
                    command(String.format(Locale.ROOT,"execute in reverie_dreams:gensokyo run tp @p %.1f 1090 7144 90 15",x));
                    context.waitFor(mc->mc.player!=null&&mc.level.dimension().equals(dimension)&&Math.abs(mc.player.getX()-x)<1,4000);
                    awaitNeighborhood();
                    context.waitTicks(30);
                    check(visibleFailure.get()==null,"newly visible chunks already contain the complete building at approach X="+x+": "+visibleFailure.get());
                    world.getServer().runOnServer(server->{var level=server.getLevel(dimension);
                        check(level.getChunkSource().getForceLoadedChunks().isEmpty(),"no whole-building forced chunk loading");
                        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){
                            var chunk=level.getChunkSource().getChunkNow(((int)x>>4)+dx,(7144>>4)+dz);
                            if(chunk!=null&&chunk.getPos().getMinBlockX()<5031&&chunk.getPos().getMaxBlockX()>=4810)
                                check(GensokyoStructureChunkState.markers(chunk).stream().anyMatch(k->k.endsWith(":blocks")),"FULL structure chunk was generated before becoming visible "+chunk.getPos());
                        }
                    });
                }
                check(seen.size()>12,"first-visible audit covered multiple structure chunks: "+seen.size());
                command("execute in reverie_dreams:gensokyo run tp @p 4915 1072 7144 90 0");awaitNeighborhood();context.waitTicks(160);
                Set<UUID> before=hangers();checkAttachedBlocks();check(!before.isEmpty(),"paintings/item frames actually spawned after their support walls");
                context.waitTicks(180);Set<UUID> stable=hangers();checkAttachedBlocks();check(before.equals(stable),"wall decorations survive multiple support checks without dropping or duplication");
                Object[] oldChunk={null};
                world.getServer().runOnServer(server->{var level=server.getLevel(dimension);oldChunk[0]=level.getChunkSource().getChunkNow(307,446);check(oldChunk[0]!=null,"reload test starts with an actually loaded chunk");});
                watch=false;
                // A new integrated server cannot reuse a retained ChunkMap/pending-unload object.
                // This also tests persistence across a complete game save/reopen, not just cache eviction.
                var savedWorld=world.getWorldSave();closeWorld();
                world=savedWorld.open();
                command("execute in reverie_dreams:gensokyo run tp @p 4915 1072 7144 90 0");awaitNeighborhood();context.waitTicks(200);
                world.getServer().runOnServer(server->{var level=server.getLevel(dimension);var chunk=level.getChunkSource().getChunkNow(307,446);
                    check(chunk!=oldChunk[0],"reloaded a different chunk object from storage");check(GensokyoStructureChunkState.markers(chunk).stream().anyMatch(k->k.endsWith(":blocks")),"generation marker survives actual chunk serialization");});
                check(before.equals(hangers()),"unload/reload retains exactly the same hanging entity UUIDs");
                checkAttachedBlocks();
                oldChunk[0]=null;
                JsonObject stair=samples.getAsJsonObject("stair");BlockPos air=pos(stair);BlockPos chest=new BlockPos(4950,1068,7122);
                world.getServer().runOnServer(server->{var level=server.getLevel(dimension);
                    check(level.getBlockState(air).isAir(),"reported stair neighborhood contains authored air before repair");
                    level.setBlock(air,Blocks.STONE.defaultBlockState(),3);
                    var be=level.getBlockEntity(chest);check(be instanceof Container,"actual source chest available for repair protection");((Container)be).setItem(0,new ItemStack(Items.DIAMOND,7));
                });
                command("touhou gensokyo repair reverie_dreams:scarlet_devil_mansion");
                boolean repaired=false;
                for(int attempt=0;attempt<120&&!repaired;attempt++){
                    boolean[] done={false};world.getServer().runOnServer(server->done[0]=server.getLevel(dimension).getBlockState(air).isAir());
                    repaired=done[0];if(!repaired)context.waitTicks(5);
                }
                check(repaired,"explicit repair completes within the bounded wait");context.waitTicks(100);
                world.getServer().runOnServer(server->{var level=server.getLevel(dimension);
                    check(level.getBlockState(air).isAir(),"explicit legacy repair removes a simulated natural-stone stair blockage");
                    var container=(Container)level.getBlockEntity(chest);check(container.getItem(0).is(Items.DIAMOND)&&container.getItem(0).getCount()==7,"repair preserves existing container inventory");
                });
                check(before.equals(hangers()),"repair does not respawn duplicate paintings or item frames");
                command("touhou gensokyo repair reverie_dreams:scarlet_devil_mansion");context.waitTicks(100);
                check(before.equals(hangers()),"repeated repair remains idempotent for hanging entities");
                command("execute in reverie_dreams:gensokyo run tp @p 4879 1044 7113 -90 -10");context.waitTicks(30);
                Files.copy(context.takeScreenshot("stair-repaired-"+mode),out.resolve("screenshots/stair-repaired-"+mode+".png"),StandardCopyOption.REPLACE_EXISTING);
                metrics.put("stableHangingEntityCount",before.size());metrics.put("firstVisibleChunks",seen.size());metrics.put("viewDistanceChunks",4);
            }
        }catch(Throwable failure){error=failure;failure.printStackTrace();}
        finally{
            watch=false;
            if(world!=null){try{closeWorld();}catch(Throwable closeError){if(error==null)error=closeError;else error.addSuppressed(closeError);}world=null;}
            if(!ticks.isEmpty()){List<Double> sorted=new ArrayList<>(ticks);Collections.sort(sorted);metrics.put("tickMsMedian",sorted.get(sorted.size()/2));metrics.put("tickMsP95",sorted.get((int)((sorted.size()-1)*.95)));metrics.put("tickMsMax",sorted.getLast());}
            try{Files.writeString(out.resolve(mode+"-results.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status",error==null?"PASS":"FAIL","failure",error==null?"":error.toString(),"checks",checks,"metrics",metrics,"polymer","not involved; not run")));}catch(Exception e){throw new RuntimeException(e);}
        }
        if(error!=null)throw new AssertionError("Streaming structure validation failed",error);
        System.out.println("STRUCTURE_STREAMING_NATIVE_PASS checks="+checks.size());
    }
    private Set<UUID> hangers(){Set<UUID> result=new HashSet<>();world.getServer().runOnServer(server->{var level=server.getLevel(dimension);for(var entity:level.getEntitiesOfClass(BlockAttachedEntity.class,new AABB(4884,1004,7114,4946,1170,7176))){check(entity.survives(),"hanging entity has loaded support "+entity.getUUID());result.add(entity.getUUID());}});return result;}
    private void checkAttachedBlocks(){world.getServer().runOnServer(server->{var level=server.getLevel(dimension);int count=0,boundary=0;
        for(var item:samples.getAsJsonArray("attachedBlocks")){var q=item.getAsJsonObject();BlockPos p=pos(q);
            if(level.getChunkSource().getChunkNow(p.getX()>>4,p.getZ()>>4)==null)continue;
            String expected=q.get("block").getAsString();check(BuiltInRegistries.BLOCK.getKey(level.getBlockState(p).getBlock()).toString().equals(expected),"authored attached block survives at "+p);
            count++;if((p.getX()&15)==0||(p.getX()&15)==15||(p.getZ()&15)==0||(p.getZ()&15)==15)boundary++;
        }check(count>0&&boundary>0,"attached block audit includes actual chunk boundaries");metrics.put("attachedBlocksChecked",count);metrics.put("attachedBlocksOnChunkEdges",boundary);
    });}
    private static BlockPos pos(JsonObject q){var a=q.getAsJsonArray("pos");return new BlockPos(a.get(0).getAsInt(),a.get(1).getAsInt(),a.get(2).getAsInt());}
    private void closeWorld() throws ReflectiveOperationException {
        if(!mode.equals("combined")){world.close();world=null;return;}
        // Reuse the existing DH test teardown: render-thread halt otherwise deadlocks on
        // Fabric GameTest's phase gate. This is a harness adaptation, not a game lifecycle hook.
        var server=world.getServer().computeOnServer(s->s);
        context.runOnClient(mc->{var field=net.minecraft.client.Minecraft.class.getDeclaredField("singleplayerServer");field.setAccessible(true);field.set(mc,null);
            var saving=net.minecraft.network.chat.Component.translatable("menu.savingLevel");
            if(mc.level!=null)mc.level.disconnect(saving);
            mc.disconnect(new net.minecraft.client.gui.screens.GenericMessageScreen(saving),false);
            mc.setScreen(new net.minecraft.client.gui.screens.TitleScreen());});
        context.waitFor(mc->mc.level==null,1200);
        world.getServer().runOnServer(s->s.halt(false));
        context.waitFor(mc->server.isShutdown(),1200);context.waitTicks(2);
        context.setScreen(net.minecraft.client.gui.screens.TitleScreen::new);world=null;
    }
    private void awaitNeighborhood(){context.waitFor(mc->{if(mc.player==null||mc.level==null)return false;var p=mc.player.chunkPosition();for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)if(mc.level.getChunk(p.x()+dx,p.z()+dz,ChunkStatus.FULL,false)==null)return false;return true;},4000);}
    private void command(String text){world.getServer().runCommand(text);}
}
