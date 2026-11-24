package cc.thonly.reverie_dreams.fabric.gametest;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.decor.ChabudaiBlock;
import cc.thonly.reverie_dreams.block.entity.BoardGameBlockEntity;
import cc.thonly.reverie_dreams.board.*;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import com.google.gson.GsonBuilder;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Blocks;
import java.nio.file.*;
import java.util.*;
import net.minecraft.world.item.BlockItem;

/** Actual resource baking and synchronized world displays, independent of the Dialog suite. */
@SuppressWarnings("UnstableApiUsage")
public final class BoardRenderGameTest implements FabricClientGameTest {
    private final Path out=Path.of(System.getProperty("reverie.boardClientTest.output"));
    private final List<String> checks=new ArrayList<>();
    private void require(boolean valid,String label){if(!valid)throw new AssertionError(label);checks.add(label);}
    @Override public void runTest(ClientGameTestContext context) {
        Throwable failure=null;
        try {
            Files.createDirectories(out);
            require(!FabricLoader.getInstance().isModLoaded("reverie_dreams_polymerify"),"native client without Polymer");
            require(FabricLoader.getInstance().getGameDir().toAbsolutePath().normalize().equals(Path.of(System.getProperty("reverie.boardClientTest.root"),"client").toAbsolutePath().normalize()),"disposable test instance");
            for(String phase:new String[]{"initial","reloaded"}) {
            // Reload between controlled worlds: Fabric's test-phase barrier must not
            // race an integrated-server disconnect while a LoadingOverlay is fading.
            if(phase.equals("reloaded")) {
                var reload=context.computeOnClient(Minecraft::reloadResourcePacks);
                context.waitFor(mc->reload.isDone(),1200);reload.join();
                context.waitFor(mc->mc.getOverlay()==null,600);context.waitTicks(20);
            }
            try(var world=context.worldBuilder().create()) {
                context.getInput().resizeWindow(1600,900);
                context.runOnClient(mc->{mc.options.hideGui=true;mc.options.renderDistance().set(4);mc.options.pauseOnLostFocus=false;});
                world.getServer().runCommand("gamemode spectator @p");
                world.getServer().runCommand("time set noon");
                world.getServer().runCommand("weather clear");
                world.getServer().runOnServer(server->{var level=server.overworld();
                    for(int x=-5;x<=9;x++)for(int z=-5;z<=9;z++){
                        for(int y=65;y<=73;y++)level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),2);
                        level.setBlock(new BlockPos(x,64,z),Blocks.SMOOTH_STONE.defaultBlockState(),2);
                    }
                    for(var kind:BoardKind.values())for(int row=0;row<2;row++) {
                        var pos=new BlockPos(kind.ordinal()*3,65,row*3);
                        var block=row==0?BoardGameBlockEntity.boardStack(kind).getItem():null;
                        level.setBlock(pos,row==0?((BlockItem)block).getBlock().defaultBlockState():RDBlocks.CHABUDAI.value().defaultBlockState(),3);
                        var host=(BoardGameBlockEntity)level.getBlockEntity(pos);
                        if(row==1)host.attach(kind,Direction.NORTH);
                        if(kind==BoardKind.GO)host.game().position=host.game().position.play(new BoardPosition.Move(-1,30)).play(new BoardPosition.Move(-1,40));
                        host.publish();
                    }
                });
                context.waitFor(mc->mc.level!=null&&mc.level.getBlockEntity(new BlockPos(6,65,3)) instanceof BoardGameBlockEntity host&&host.game()!=null&&host.game().position.at(40)!=0,600);
                context.waitTicks(20);
                context.runOnClient(mc->verify(mc,phase));
                world.getServer().runCommand("tp @p 3.5 70 -4.5 0 36");context.waitTicks(20);
                context.waitFor(mc->Math.abs(mc.player.getZ()+4.5)<.1,300);
                screenshot(context,"three-boards-"+phase);
                world.getServer().runCommand("tp @p 1.5 67 6.5 180 34");context.waitTicks(15);
                context.waitFor(mc->Math.abs(mc.player.getZ()-6.5)<.1,300);
                screenshot(context,"tabletop-"+phase);
                world.getServer().runCommand("tp @p 1.5 67 -2.5 0 34");context.waitTicks(15);
                context.waitFor(mc->Math.abs(mc.player.getZ()+2.5)<.1,300);
                screenshot(context,"floor-"+phase);
                Files.writeString(out.resolve("render-assertions.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status","PASS","checks",checks,"scope","Rendering assertions complete; teardown tracked separately")));
                context.waitTicks(20);
            }
            }
        }catch(Throwable problem){failure=problem;problem.printStackTrace();}
        finally {
            try{Files.writeString(out.resolve("render-results.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status",failure==null?"PASS":"FAIL","checks",checks,"failure",failure==null?"":failure.toString(),"scope","Actual native client resources, baking, synchronized floor/table displays and resource reload; fixtures, not human playtesting")));}
            catch(Exception problem){throw new RuntimeException(problem);}
        }
        if(failure!=null)throw new AssertionError("Board render regression",failure);
        System.out.println("BOARD_RENDER_GAMETEST_PASS checks="+checks.size());
    }
    private void verify(Minecraft mc,String phase) {
        var ids=new ArrayList<Identifier>();
        for(var kind:BoardKind.values()){
            ids.add(ReverieDreams.id("board/"+kind.id));
            int[] types=switch(kind){case GO->new int[]{1};case CHESS->new int[]{1,2,3,4,5,6};case XIANGQI->new int[]{1,2,3,4,6,7,8};};
            for(String side:new String[]{"first","second"})for(int type:types)ids.add(ReverieDreams.id("board/piece/"+kind.name().toLowerCase(Locale.ROOT)+"_"+side+"_"+type));
        }
        require(ids.size()==31,phase+" 31 dynamic IDs");
        var resolver=new ItemModelResolver(mc.getModelManager());
        for(var id:ids){
            require(mc.getResourceManager().getResource(Identifier.fromNamespaceAndPath(id.getNamespace(),"items/"+id.getPath()+".json")).isPresent(),phase+" definition exists "+id);
            require(!(mc.getModelManager().getItemModel(id) instanceof MissingItemModel),phase+" baked model exists "+id);
            var state=new ItemStackRenderState();resolver.updateForTopItem(state,BoardDisplays.model(id.getPath()),ItemDisplayContext.NONE,mc.level,null,0);
            require(!state.isEmpty()&&!state.pickParticleMaterial(RandomSource.create(1)).sprite().contents().name().getPath().equals("missingno"),phase+" real geometry/material "+id);
            if(!id.getPath().contains("/piece/"))require(state.getModelBoundingBox().getYsize()<.07,phase+" thin board, no full-cube placeholder "+id);
        }
        for(var kind:BoardKind.values())for(int row=0;row<2;row++){
            var host=(BoardGameBlockEntity)mc.level.getBlockEntity(new BlockPos(kind.ordinal()*3,65,row*3));
            require(host!=null&&host.game()!=null&&host.game().position.kind==kind,phase+" synchronized "+kind+" row "+row);
            var parts=BoardDisplays.parts(host);int pieces=kind==BoardKind.GO?2:32;
            require(parts.size()==pieces+row,phase+" expected display count "+kind+" row "+row);
            double top=row==0?0:ChabudaiBlock.TOP_SURFACE;
            if(row==1){var p=parts.getFirst();var state=new ItemStackRenderState();resolver.updateForTopItem(state,p.stack(),ItemDisplayContext.NONE,mc.level,null,0);var bounds=state.getModelBoundingBox();require(Math.abs(p.y()+bounds.minY*p.scale()-top)<.0001&&bounds.getYsize()*p.scale()<.07,phase+" tabletop board seated at actual table height "+kind);}
            for(int i=row;i<parts.size();i++){var p=parts.get(i);var state=new ItemStackRenderState();resolver.updateForTopItem(state,p.stack(),ItemDisplayContext.NONE,mc.level,null,i);var bounds=state.getModelBoundingBox();require(Math.abs(p.y()+bounds.minY*p.scale()-(top+1.05/16))<.0001,phase+" piece base rests on board "+kind+" row "+row+" piece "+i);}
        }
    }
    private void screenshot(ClientGameTestContext context,String name)throws Exception {
        Files.createDirectories(out.resolve("screenshots"));Files.copy(context.takeScreenshot(name),out.resolve("screenshots/"+name+".png"),StandardCopyOption.REPLACE_EXISTING);
    }
}
