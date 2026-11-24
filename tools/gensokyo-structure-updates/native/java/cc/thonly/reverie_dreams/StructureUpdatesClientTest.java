package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.util.command.FilteredSuggestions;
import cc.thonly.reverie_dreams.world.gensokyo.update.*;
import com.google.gson.*;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Real server chunks/entities, disk backups, engine unload/reload and full world reopen. */
@SuppressWarnings("UnstableApiUsage")
public final class StructureUpdatesClientTest implements FabricClientGameTest {
    private static final Identifier ID=Identifier.parse("reverie_dreams:qa_structure");
    private static final ResourceKey<Level> DIM=ResourceKey.create(Registries.DIMENSION,Identifier.parse("reverie_dreams:gensokyo"));
    private static final BlockPos ORIGIN=new BlockPos(8990,1400,8990);
    private final List<String> checks=new ArrayList<>();
    private final Path output=Path.of(System.getProperty("reverie.building.evidence"));
    private TestSingleplayerContext world;
    private ClientGameTestContext context;
    private StructureUpdateService service;
    private final Catalogue catalogue=new Catalogue();
    private UUID standUuid,frameUuid,npcUuid;
    private final BlockPos chest=ORIGIN.offset(1,1,1),edited=ORIGIN.offset(1,0,2),changed=ORIGIN.offset(2,0,2);
    private void check(boolean ok,String text){if(!ok)throw new AssertionError(text);checks.add(text);}
    private ServerLevel level(MinecraftServer server){return Objects.requireNonNull(server.getLevel(DIM));}
    private void on(java.util.function.Consumer<MinecraftServer> action){world.getServer().runOnServer(server->action.accept(server));}
    private void command(String text){world.getServer().runCommand(text);}
    private void closeWorld()throws ReflectiveOperationException {
        if(!dev.architectury.platform.Platform.isModLoaded("distanthorizons")){world.close();world=null;return;}
        // Existing structure-streaming harness adaptation: let the server stop while the
        // client keeps rendering, rather than blocking both Fabric GameTest phase gates.
        var server=world.getServer().computeOnServer(s->s);
        context.runOnClient(mc->{var field=net.minecraft.client.Minecraft.class.getDeclaredField("singleplayerServer");field.setAccessible(true);field.set(mc,null);
            var saving=net.minecraft.network.chat.Component.translatable("menu.savingLevel");if(mc.level!=null)mc.level.disconnect(saving);
            mc.disconnect(new net.minecraft.client.gui.screens.GenericMessageScreen(saving),false);mc.setScreen(new net.minecraft.client.gui.screens.TitleScreen());});
        context.waitFor(mc->mc.level==null,1200);world.getServer().runOnServer(s->s.halt(false));context.waitFor(mc->server.isShutdown(),1200);context.waitTicks(2);context.setScreen(net.minecraft.client.gui.screens.TitleScreen::new);world=null;
    }
    private static UUID stable(String actor){return UUID.nameUUIDFromBytes((ID+"|"+actor).getBytes(StandardCharsets.UTF_8));}
    private final class Catalogue implements StructureUpdateTargets {
        StructureBlueprint current;
        public List<Identifier> anchors(){return List.of(ID);}
        public CompletableFuture<StructureBlueprint> blueprint(Identifier id){return CompletableFuture.completedFuture(current);}
        public Optional<CompletableFuture<StructureBlueprint>> preparedBlueprint(Identifier id){return Optional.empty();}
        public boolean hasLegacyBlocks(Identifier id){return false;}
        public boolean freshRevision(Identifier id){return false;}
        public void markUpdated(Identifier id,StructureBlueprint revision){ }
    }
    private CompoundTag tag(ServerLevel level,Entity e){TagValueOutput out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());check(e.save(out),"entity serializes "+e.getType());return out.buildResult();}
    private StructureBlueprint blueprint(ServerLevel l,int version,List<StructureBlueprint.Actor> actors){
        Map<Long,List<BlockPos>> positions=new TreeMap<>();Map<BlockPos,BlockState> states=new HashMap<>();
        for(int x=0;x<6;x++)for(int z=0;z<6;z++)for(int y=0;y<5;y++){
            BlockPos p=ORIGIN.offset(x,y,z);BlockState b=y==0?(version==1?Blocks.STONE:Blocks.STONE_BRICKS).defaultBlockState():Blocks.AIR.defaultBlockState();
            if(z==0&&y>0)b=Blocks.STONE_BRICKS.defaultBlockState();if(p.equals(chest))b=Blocks.CHEST.defaultBlockState();
            states.put(p,b);positions.computeIfAbsent(new net.minecraft.world.level.ChunkPos(p.getX()>>4,p.getZ()>>4).pack(),k->new ArrayList<>()).add(p);
        }
        Map<Long,StructureBlueprint.Slice> slices=new TreeMap<>();positions.forEach((key,ps)->slices.put(key,new StructureBlueprint.Slice(ps.stream().mapToInt(StructureBlueprint::pack).toArray(),ps.stream().map(states::get).toArray(BlockState[]::new),Map.of())));
        return new StructureBlueprint(ID.toString(),version,String.valueOf(version).repeat(64),new BoundingBox(ORIGIN.getX(),ORIGIN.getY(),ORIGIN.getZ(),ORIGIN.getX()+5,ORIGIN.getY()+4,ORIGIN.getZ()+5),Map.copyOf(slices),List.copyOf(actors),Map.of("work",ORIGIN.offset(4,1,4)));
    }
    private StructureBlueprint.Actor actor(ServerLevel l,String id,Entity entity){entity.setUUID(stable(id));return new StructureBlueprint.Actor(id,entity instanceof Mob?"npc":"decoration",entity.position(),tag(l,entity),entity.getUUID(),UUID.nameUUIDFromBytes(("legacy|"+id).getBytes(StandardCharsets.UTF_8)));}
    private UUID enqueue(boolean rebuild){command("execute in reverie_dreams:gensokyo run forceload remove all");UUID[] id={null};on(s->{try{id[0]=service.enqueue(ID.toString(),rebuild,false);}catch(Exception e){throw new RuntimeException(e);}});return id[0];}
    private String state(UUID id){String[] text={null};on(s->text[0]=service.jobStatus(id));return text[0];}
    private void waitFor(UUID id,String wanted){
        for(int i=0;i<4500;i++){
            String[] status={null};on(s->{service.tick();status[0]=service.jobStatus(id);});
            if(i%200==0)System.out.println("STRUCTURE_QA "+status[0]);
            if(status[0].contains(" "+wanted+" "))return;
            if(status[0].contains(" BLOCKED ")||status[0].contains(" NEEDS_REVIEW "))throw new AssertionError(status[0]);
            context.waitTicks(1);
        }throw new AssertionError("Timed out: "+state(id));
    }
    private void load(){
        command("execute in reverie_dreams:gensokyo run forceload add 8960 8960 9023 9023");
        for(int n=0;n<1000;n++){boolean[] ready={false};on(s->ready[0]=catalogue.current.neighborhood().stream().allMatch(k->level(s).areEntitiesLoaded(k)));if(ready[0]){context.waitTicks(3);return;}context.waitTicks(1);}throw new AssertionError("Inspection chunks did not load entities");
    }
    private void verifyLod(UUID job)throws Exception {
        if(!dev.architectury.platform.Platform.isModLoaded("distanthorizons"))return;
        check(state(job).contains("QUEUED"),"DH accepts the changed chunks through its terrain API");
        Class<?> delayed=Class.forName("com.seibel.distanthorizons.api.DhApi$Delayed"),wrapperType=Class.forName("com.seibel.distanthorizons.api.interfaces.world.IDhApiLevelWrapper"),cacheType=Class.forName("com.seibel.distanthorizons.api.interfaces.data.IDhApiTerrainDataCache");
        Object repo=delayed.getField("terrainRepo").get(null);var query=repo.getClass().getMethod("getSingleDataPointAtBlockPos",wrapperType,int.class,int.class,int.class,cacheType);
        Object[] wrapper={null};on(s->{try{wrapper[0]=Class.forName("com.seibel.distanthorizons.common.wrappers.world.ServerLevelWrapper").getMethod("getWrapper",ServerLevel.class).invoke(null,level(s));}catch(Exception e){throw new RuntimeException(e);}});
        boolean[] observed={false};
        for(int n=0;n<60&&!observed[0];n++){
            var reply=CompletableFuture.supplyAsync(()->{try(AutoCloseable cache=(AutoCloseable)repo.getClass().getMethod("createSoftCache").invoke(repo)){return query.invoke(repo,wrapper[0],changed.getX(),changed.getY(),changed.getZ(),cache);}catch(Exception e){throw new RuntimeException(e);}});
            long limit=System.nanoTime()+30_000_000_000L;while(!reply.isDone()&&System.nanoTime()<limit)context.waitTicks(5);
            if(!reply.isDone()){reply.cancel(true);throw new AssertionError("DH query timed out off the game threads");}
            Object result=reply.join();Object point=result.getClass().getField("payload").get(result);
            String message=String.valueOf(result.getClass().getField("message").get(result));String serial="no-point";
            if(point!=null){Object block=point.getClass().getField("blockStateWrapper").get(point);serial=(String)block.getClass().getMethod("getSerialString").invoke(block);observed[0]=serial.contains("stone_bricks");}
            if(n<3||n%10==0||observed[0])System.out.println("DH_QA success="+result.getClass().getField("success").get(result)+" serial="+serial+" message="+message);
            if(!observed[0])context.waitTicks(40);
        }
        check(observed[0],"DH queried terrain contains the upgraded building block at its actual height");
        check(dev.architectury.platform.Platform.isModLoaded("c2me"),"combined validation actually has C2ME loaded");
    }
    @Override public void runTest(ClientGameTestContext context){
        this.context=context;Throwable failure=null;
        try{
            Files.createDirectories(output);world=context.worldBuilder().create();command("gamemode spectator @p");command("tick rate 200");
            context.runOnClient(mc->{mc.options.renderDistance().set(2);mc.options.simulationDistance().set(3);mc.options.pauseOnLostFocus=false;mc.options.broadcastOptions();});
            on(server->{var l=level(server);check(!ReverieDreams.config().gensokyoStructureAutoUpgrade,"automatic upgrades default off");
                // Establish an already-FULL world fixture. This is not production generated content.
                for(int cx=560;cx<=564;cx++)for(int cz=560;cz<=564;cz++)l.getChunk(cx,cz);
                ArmorStand stand=EntityType.ARMOR_STAND.create(l,EntitySpawnReason.COMMAND);stand.snapTo(ORIGIN.getX()+4.5,ORIGIN.getY()+1,ORIGIN.getZ()+3.5,0,0);stand.setNoGravity(true);
                ItemFrame frame=new ItemFrame(l,ORIGIN.offset(2,2,1),Direction.SOUTH);
                Entity npc=BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("reverie_dreams:goblin")).create(l,EntitySpawnReason.COMMAND);npc.snapTo(ORIGIN.getX()+2.5,ORIGIN.getY()+1,ORIGIN.getZ()+4.5,0,0);((Mob)npc).setNoAi(true);
                List<StructureBlueprint.Actor> actors=List.of(actor(l,"stand",stand),actor(l,"frame",frame),actor(l,"goblin",npc));catalogue.current=blueprint(l,1,actors);
                for(var entry:catalogue.current.chunks().entrySet())for(int i=0;i<entry.getValue().positions().length;i++)l.setBlock(StructureBlueprint.position(entry.getKey(),entry.getValue().positions()[i]),entry.getValue().states()[i],Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
                check(l.addFreshEntity(stand),"fixture stand is accepted");check(l.addFreshEntity(frame),"fixture frame is accepted");check(l.addFreshEntity(npc),"fixture NPC is accepted inside the map boundary");standUuid=stand.getUUID();frameUuid=frame.getUUID();npcUuid=npc.getUUID();
                l.setBlock(changed,Blocks.AIR.defaultBlockState(),3);((Mob)npc).setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_AXE));
                ((ChestBlockEntity)l.getBlockEntity(chest)).setItem(0,new ItemStack(Items.DIAMOND,3));stand.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SWORD));frame.setItem(new ItemStack(Items.MAP));
                check(frame.survives(),"cross-chunk item frame has a real supporting wall");service=new StructureUpdateService(l,catalogue);
                for(String query:List.of("gensokyo","reverie_dreams:gensokyo")){var b=new SuggestionsBuilder(query,0);FilteredSuggestions.resource(b,Identifier.parse("reverie_dreams:gensokyo_forest"));FilteredSuggestions.resource(b,Identifier.parse("minecraft:plains"));check(b.build().getList().size()==1,"unmatched resource omitted: "+query);}
                var b=new SuggestionsBuilder("\"Scar",0);FilteredSuggestions.file(b,"Scarlet theme.mid");FilteredSuggestions.file(b,"Bad Apple.mid");check(b.build().getList().size()==1&&b.build().getList().getFirst().getText().equals("\"Scarlet theme.mid\""),"quoted filename prefix filters and retains valid quoting");
                var none=new SuggestionsBuilder("not_a_candidate",0);FilteredSuggestions.value(none,"*");check(none.build().isEmpty(),"wildcard is omitted after unrelated input");
                var dispatcher=server.getCommands().getDispatcher();check(dispatcher.parse("touhou gensokyo structure update reverie_dreams:scarlet_devil_mansion",server.createCommandSourceStack()).getReader().getRemaining().isEmpty(),"namespaced landmark command parses unquoted colon");
                check(dispatcher.parse("touhou gensokyo structure update *",server.createCommandSourceStack()).getReader().getRemaining().isEmpty(),"explicit wildcard command parses");
                var completion=dispatcher.getCompletionSuggestions(dispatcher.parse("touhou teleport reverie_dreams:scarlet",server.createCommandSourceStack())).join().getList();check(!completion.isEmpty()&&completion.stream().allMatch(v->v.getText().startsWith("reverie_dreams:scarlet")),"actual landmark provider returns only matching candidates");
            });
            UUID first=enqueue(false);waitFor(first,"SUCCESS");check(state(first).contains("SUCCESS"),"manual install completes with actual unload and reload");load();on(s->check(level(s).getBlockState(changed).is(Blocks.STONE),"missing landmark geometry is built in already-FULL chunks"));
            on(s->{var l=level(s);check(l.getEntity(standUuid)!=null&&l.getEntity(frameUuid)!=null&&l.getEntity(npcUuid)!=null,"existing actors survive engine unload/reload without clones");check(((ChestBlockEntity)l.getBlockEntity(chest)).getItem(0).getCount()==3,"existing chest inventory preserved");check(((ArmorStand)l.getEntity(standUuid)).getMainHandItem().is(Items.IRON_SWORD),"existing display inventory preserved");check(((ItemFrame)l.getEntity(frameUuid)).getItem().is(Items.MAP),"existing frame inventory preserved");
                l.setBlock(edited,Blocks.GOLD_BLOCK.defaultBlockState(),3);catalogue.current=blueprint(l,2,catalogue.current.actors());StructureUpdateService.reloaded(s);
            });
            UUID upgrade=enqueue(false);waitFor(upgrade,"SUCCESS");load();verifyLod(upgrade);on(s->{var l=level(s);check(l.getBlockState(edited).is(Blocks.GOLD_BLOCK),"three-way merge retains player's changed block");check(l.getBlockState(changed).is(Blocks.STONE_BRICKS),"same mod release receives higher structure version");check(((ChestBlockEntity)l.getBlockEntity(chest)).getItem(0).getCount()==3,"version upgrade does not refill or overwrite loot");check(((Mob)l.getEntity(npcUuid)).getMainHandItem().is(Items.IRON_AXE),"NPC retains its changed inventory rather than template equipment");});
            UUID same=enqueue(false);waitFor(same,"UP_TO_DATE");check(true,"same content version/hash is idempotent");
            on(s->{var player=s.getPlayerList().getPlayers().getFirst();check(level(s).getEntity(npcUuid).teleportTo(s.overworld(),player.getX()+2,player.getY()+2,player.getZ(),Set.of(),0,0,true),"owned NPC can leave the original dimension");});
            UUID abroad=enqueue(true);waitFor(abroad,"SUCCESS");load();on(s->{var l=level(s);check(l.getEntity(npcUuid)==null&&s.overworld().getEntity(npcUuid)!=null,"rebuild does not clone or recall an actor in another dimension");Entity npc=s.overworld().getEntity(npcUuid);check(!npc.isInvulnerable()&&((Mob)npc).getMainHandItem().is(Items.IRON_AXE),"actor outside the structure retains flags and inventory");check(npc.teleportTo(l,ORIGIN.getX()+2.5,ORIGIN.getY()+1,ORIGIN.getZ()+4.5,Set.of(),0,0,true),"same NPC UUID can return after maintenance");l.setBlock(edited,Blocks.GOLD_BLOCK.defaultBlockState(),3);});
            command("execute in reverie_dreams:gensokyo run forceload remove all");on(s->{try{service.rollback(upgrade);}catch(Exception e){throw new RuntimeException(e);}});waitFor(upgrade,"ROLLED_BACK");load();on(s->{var l=level(s);check(l.getBlockState(changed).is(Blocks.STONE),"rollback restores only the recorded structural changes");check(l.getBlockState(edited).is(Blocks.GOLD_BLOCK),"rollback leaves retained player edits intact");check(((ChestBlockEntity)l.getBlockEntity(chest)).getItem(0).getCount()==3,"rollback never rewinds current inventory");});
            UUID interrupted=enqueue(false);waitFor(interrupted,"APPLY");
            var save=world.getWorldSave();on(s->StructureUpdateService.flush(s));closeWorld();world=save.open();command("tick rate 200");on(s->service=new StructureUpdateService(level(s),catalogue));waitFor(interrupted,"SUCCESS");load();
            on(s->{var l=level(s);check(l.getBlockState(changed).is(Blocks.STONE_BRICKS),"world reopen resumes an interrupted job");check(l.getEntity(npcUuid)!=null,"NPC keeps its original UUID across world reopen");check(!l.getEntity(npcUuid).isInvulnerable(),"persisted freeze flags are restored");l.getEntity(npcUuid).discard();service.flush();});
            UUID rebuild=enqueue(true);waitFor(rebuild,"SUCCESS");load();on(s->{var l=level(s);check(l.getEntity(npcUuid)==null,"retired NPC is not cloned by rebuild");check(l.getBlockState(edited).is(Blocks.STONE_BRICKS),"explicit rebuild restores structural geometry");check(((ChestBlockEntity)l.getBlockEntity(chest)).getItem(0).getCount()==3,"rebuild preserves chest contents");});
            // External tickets must block unloading; no code may remove their ownership.
            on(s->ReverieDreams.config().gensokyoStructureUnloadTimeoutTicks=100);
            UUID ticket=enqueue(true);command("execute in reverie_dreams:gensokyo run forceload add 8976 8976 8991 8991");waitFor(ticket,"BLOCKED");
            on(s->{var l=level(s);check(l.getForceLoadedChunks().contains(new net.minecraft.world.level.ChunkPos(561,561).pack()),"foreign force-load ticket remains after blocked unload");check(!StructureUpdateGate.blocks(l,new net.minecraft.world.level.ChunkPos(561,561).pack()),"pre-write failure releases publication gate");});
            load();on(s->{var l=level(s);check(((ChestBlockEntity)l.getBlockEntity(chest)).getItem(0).getCount()==3,"blocked unload did not mutate inventory");ReverieDreams.config().gensokyoStructureUnloadTimeoutTicks=1200;});
            command("execute in reverie_dreams:gensokyo run forceload remove all");
            AutoCloseable guard=StructureUpdateProtection.register((l,id,bounds)->{if(id.equals(ID.toString()))throw new IllegalStateException("QA protected claim");});
            try{UUID denied=enqueue(true);waitFor(denied,"BLOCKED");check(state(denied).contains("QA protected claim"),"claim adapter veto blocks the job before mutation");}finally{guard.close();}
            load();on(s->{var l=level(s);l.setBlock(edited,Blocks.CHEST.defaultBlockState(),3);((ChestBlockEntity)l.getBlockEntity(edited)).setItem(0,new ItemStack(Items.EMERALD,7));});
            UUID container=enqueue(true);waitFor(container,"BLOCKED");check(state(container).contains("Protected block entity"),"rebuild refuses to destroy a player's added container");load();on(s->check(((ChestBlockEntity)level(s).getBlockEntity(edited)).getItem(0).getCount()==7,"protected container retains all contents after blocked rebuild"));
            on(s->{var old=catalogue.current;catalogue.current=new StructureBlueprint(old.id(),old.version(),"f".repeat(64),old.bounds(),old.chunks(),old.actors(),old.points());StructureUpdateService.reloaded(s);});
            UUID unversioned=enqueue(false);waitFor(unversioned,"BLOCKED");check(state(unversioned).contains("without incrementing"),"same-version different content hash is rejected");
        }catch(Throwable error){failure=error;error.printStackTrace();}
        finally{if(world!=null)try{closeWorld();}catch(Throwable error){if(failure==null)failure=error;else failure.addSuppressed(error);}world=null;
            try{Files.writeString(output.resolve("native-results.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status",failure==null?"PASS":"FAIL","failure",failure==null?"":failure.toString(),"checks",checks,"polymer","not involved; not run")));}catch(Exception error){throw new RuntimeException(error);}}
        if(failure!=null)throw new AssertionError("Structure update native validation failed",failure);System.out.println("STRUCTURE_UPDATES_NATIVE_PASS checks="+checks.size());
    }
}
