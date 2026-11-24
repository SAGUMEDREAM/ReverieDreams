package cc.thonly.reverie_dreams.fabric.gametest;

import cc.thonly.reverie_dreams.block.entity.BoardGameBlockEntity;
import cc.thonly.reverie_dreams.board.*;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import com.google.gson.GsonBuilder;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.*;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;
import java.nio.charset.StandardCharsets;

/** Real client input and server callbacks. Server writes below only arrange test fixtures. */
@SuppressWarnings("UnstableApiUsage")
public final class BoardClientGameTest implements FabricClientGameTest {
    private final Path output=Path.of(System.getProperty("reverie.boardClientTest.output"));
    private final List<String> passed=new ArrayList<>(),failures=new ArrayList<>();
    private final List<Map<String,Object>> layouts=new ArrayList<>();
    private ClientGameTestContext context;
    private TestSingleplayerContext world;
    private BlockPos position;
    private UUID playerId;
    private String kind;

    @Override public void runTest(ClientGameTestContext context) {
        if (Boolean.getBoolean("reverie.boardClientTest.renderOnly")) {
            new BoardRenderGameTest().runTest(context);
            return;
        }
        this.context=context;
        try {
            Path expected=Path.of(System.getProperty("reverie.boardClientTest.root"),"client").toAbsolutePath().normalize();
            require(FabricLoader.getInstance().getGameDir().toAbsolutePath().normalize().equals(expected),"isolated game directory");
            require(!FabricLoader.getInstance().isModLoaded("reverie_dreams_polymerify"),"native client without Polymer addon");
            Files.createDirectories(output);
            Files.writeString(output.resolve("loaded-mods.txt"),FabricLoader.getInstance().getAllMods().stream()
                    .map(m->m.getMetadata().getId()+"="+m.getMetadata().getVersion().getFriendlyString()).sorted().reduce("",(a,b)->a+b+"\n"));
            try(TestSingleplayerContext singleplayer=context.worldBuilder().create()) {
                world=singleplayer;
                context.getInput().resizeWindow(1280,690);
                context.runOnClient(mc->{mc.options.guiScale().set(2);mc.options.renderDistance().set(4);mc.options.hideGui=false;mc.options.pauseOnLostFocus=false;mc.resizeGui();});
                var reload=context.computeOnClient(mc->{mc.getLanguageManager().setSelected("zh_cn");mc.options.languageCode="zh_cn";return mc.reloadResourcePacks();});
                context.waitFor(mc->reload.isDone(),1200);
                world.getClientLevel().waitForChunksRender();
                world.getServer().runCommand("difficulty peaceful");
                world.getServer().runOnServer(server->{
                    var level=server.overworld();
                    for(int x=-5;x<=5;x++)for(int z=-3;z<=6;z++)level.setBlock(new BlockPos(x,64,z),Blocks.STONE.defaultBlockState(),3);
                    level.setBlock(new BlockPos(-2,65,0),RDBlocks.XIANGQI_BOARD.value().defaultBlockState(),3);
                    level.setBlock(new BlockPos(0,65,0),RDBlocks.CHESS_BOARD.value().defaultBlockState(),3);
                    level.setBlock(new BlockPos(2,65,0),RDBlocks.GO_BOARD.value().defaultBlockState(),3);
                    server.getPlayerList().getPlayers().getFirst().setGameMode(GameType.CREATIVE);
                });
                world.getServer().runCommand("tp @p 0.5 65 3.5 180 30");
                playerId=world.getServer().computeOnServer(s->s.getPlayerList().getPlayers().getFirst().getUUID());
                context.waitTicks(5);
                for(var board:BoardKind.values()) {
                    kind=board.name().toLowerCase(Locale.ROOT);
                    position=new BlockPos(board==BoardKind.XIANGQI?-2:board==BoardKind.CHESS?0:2,65,0);
                    try {exercise(board);}catch(Throwable failure) {
                        failures.add(kind+": "+failure);
                        failure.printStackTrace();
                        screenshot(kind+"-failure");
                        context.setScreen(()->null);
                    }
                }
            }
        } catch(Throwable failure) {
            failures.add("setup/teardown: "+failure);failure.printStackTrace();
        } finally {writeReport();}
        if(!failures.isEmpty())throw new AssertionError("Board client failures: "+failures);
        System.out.println("BOARD_CLIENT_GAMETEST_PASS checks="+passed.size());
    }

    private void exercise(BoardKind board) throws Exception {
        openBoard();
        assertFits("lobby",board);
        clickLink("quick_ai",2);
        awaitGame(g->g.started,"one-click AI start");
        require(snapshot().side(playerId)==1,kind+" quick start chooses first side");
        assertFits("playing",board);
        screenshot(kind+"-playing");
        if(board!=BoardKind.GO) {
            var move=snapshot().position.legalMoves().getFirst();
            clickCell(move.from());
            require(context.computeOnClient(mc->grid(mc).stream().anyMatch(b->b.getMessage().getString().equals("+"))),kind+" visible legal destination markers");
            clickCell(move.from());
            require(context.computeOnClient(mc->grid(mc).stream().noneMatch(b->b.getMessage().getString().equals("+"))),kind+" second selection click cancels");
            clickCell(move.from());
            assertFits("selected",board);
            screenshot(kind+"-selected");
            clickCell(move.to());
        } else clickCell(40);
        awaitGame(g->g.moves.size()>=2&&g.position.turn()==1,"real human move and AI reply");
        assertFits("after-reply",board);
        clickLink("resign",0);
        require(snapshot().position.phase()==BoardPosition.Phase.PLAY,kind+" resignation question does not end match");
        assertFits("confirm-resign",board);
        clickLink("cancel",0);
        require(snapshot().position.phase()==BoardPosition.Phase.PLAY,kind+" resignation can be cancelled");
        clickLink("resign",0);clickLink("resign",1);
        awaitGame(g->g.position.phase()==BoardPosition.Phase.FINISHED,"confirmed resignation");
        UUID match=snapshot().id;
        clickLink("rematch_ai",0);
        awaitGame(g->g.started&&!g.id.equals(match),"same-settings AI rematch");
        require(snapshot().difficulty==BoardGame.Difficulty.HARD,kind+" rematch retains difficulty");
        if(board==BoardKind.CHESS) {promotionFixture();secondSeat();}
        if(board==BoardKind.GO) scoringFixture();
        testClose(board);
    }

    private void promotionFixture() throws Exception {
        world.getServer().runOnServer(server->{
            var host=host(server);var game=host.game();int[] pieces=new int[64];
            pieces[63]=BoardPosition.KING;pieces[7]=-BoardPosition.KING;pieces[8]=BoardPosition.PAWN;
            game.position=BoardPosition.restore(new BoardPosition.Saved(BoardKind.CHESS,pieces,1,0,-1,0,0,0,-1,-1,BoardPosition.Phase.PLAY,"",List.of(),new int[0]));
            game.moves.clear();game.revision++;host.publish();
        });
        context.waitTicks(3);
        clickCell(8);clickCell(0);
        assertFits("promotion",BoardKind.CHESS);screenshot("chess-promotion");
        clickLink("promote",BoardPosition.KNIGHT);
        awaitGame(g->g.position.at(0)==BoardPosition.KNIGHT,"native underpromotion choice");
    }

    private void secondSeat() throws Exception {
        if(snapshot().position.phase()!=BoardPosition.Phase.FINISHED){clickLink("resign",0);clickLink("resign",1);}
        clickLink("restart",0);clickLink("leave",0);clickLink("join",1);clickLink("quick_ai",1);
        awaitGame(g->g.side(playerId)==-1&&g.moves.size()>=1&&g.position.turn()==-1,"second-side AI opening");
        var expected=snapshot().position.pieceLabel(63);
        require(context.computeOnClient(mc->grid(mc).getFirst().getMessage().getString().equals(expected)),"chess board rotates for second side");
        assertFits("second-side",BoardKind.CHESS);screenshot("chess-second-side");
    }

    private void scoringFixture() throws Exception {
        world.getServer().runOnServer(server->{
            var host=host(server);var game=host.game();int[] pieces=new int[81];pieces[40]=1;pieces[41]=-1;
            game.position=BoardPosition.restore(new BoardPosition.Saved(BoardKind.GO,pieces,1,0,-1,0,0,2,-1,-1,BoardPosition.Phase.SCORING,"",List.of(),new int[0]));
            // A disconnected opponent is a fixture for the local player's confirmation UI,
            // not a claim of two real clients playing together.
            game.aiSide=0;game.players[1]=UUID.nameUUIDFromBytes("score-fixture".getBytes(StandardCharsets.UTF_8));
            game.names[1]="ScoreFixture";Arrays.fill(game.scoreAccepted,false);game.moves.clear();game.revision++;host.publish();
        });
        context.waitTicks(3);
        require(context.computeOnClient(mc->{var cells=grid(mc);return cells.get(40).getMessage().getString().equals("●")&&cells.get(41).getMessage().getString().equals("●")
                &&cells.get(40).getMessage().getStyle().getColor().getValue()==ChatFormatting.BLACK.getColor()
                &&cells.get(41).getMessage().getStyle().getColor().getValue()==ChatFormatting.WHITE.getColor();}),"go real widget labels use solid black/white stones");
        assertFits("scoring",BoardKind.GO);screenshot("go-scoring");
        clickCell(40);awaitGame(g->g.position.dead(40),"mark Go dead group");
        clickLink("score",0);awaitGame(g->g.scoreAccepted[0]&&!g.scoreAccepted[1],"local score confirmation waits for opponent");
        require(context.computeOnClient(mc->body(mc).getMessage().getString().contains(I18n.get("board.reverie_dreams.score_wait"))),"go confirmation waiting text is visible");
        screenshot("go-score-confirmed");
        clickCell(41);awaitGame(g->!g.scoreAccepted[0]&&!g.scoreAccepted[1],"changing dead groups clears previous confirmation");
        clickLink("resume",0);awaitGame(g->g.position.phase()==BoardPosition.Phase.PLAY,"resume Go from scoring");
        assertFits("paused",BoardKind.GO);
    }

    private void openBoard() {
        context.setScreen(()->null);
        context.runOnClient(mc->{
            Vec3 target=Vec3.atCenterOf(position).add(0,-0.47,0),delta=target.subtract(mc.player.getEyePosition());
            mc.player.setYRot((float)(Math.toDegrees(Math.atan2(delta.z,delta.x))-90));
            mc.player.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,Math.sqrt(delta.x*delta.x+delta.z*delta.z))));
        });
        context.waitTicks(4);context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        context.waitForScreen(DialogScreen.class);context.waitTick();
        require(context.computeOnClient(mc->mc.screen instanceof DialogScreen<?>),kind+" opens by actual right click");
    }

    private void clickLink(String action,int value) {
        var old=context.computeOnClient(mc->mc.screen);
        int[] point=context.computeOnClient(mc->{
            var text=body(mc);
            for(int y=text.getY()+8;y<text.getBottom()-4;y+=9)for(int x=text.getX()+4;x<text.getRight()-4;x++) {
                var finder=new ActiveTextCollector.ClickableStyleFinder(mc.font,x,y);text.visitLines(finder);
                var style=finder.result();
                if(style!=null&&style.getClickEvent() instanceof ClickEvent.Custom event&&event.payload().orElse(null) instanceof CompoundTag tag
                        &&tag.getString("action").orElse("").equals(action)&&tag.getIntOr("value",-1)==value)return new int[]{x,y};
            }
            throw new AssertionError("Missing clickable link "+action+"/"+value+" in "+text.getMessage().getString());
        });
        click(point);
        context.waitFor(mc->mc.screen!=old,200);context.waitTicks(2);
    }

    private void clickCell(int square) {
        var state=snapshot();int index=state.side(playerId)<0?state.position.size()-1-square:square;
        var old=context.computeOnClient(mc->mc.screen);
        int[] point=context.computeOnClient(mc->{var button=grid(mc).get(index);return new int[]{button.getX()+button.getWidth()/2,button.getY()+button.getHeight()/2};});
        click(point);context.waitFor(mc->mc.screen!=old,200);context.waitTicks(2);
    }

    private void click(int[] logical) {
        double[] physical=context.computeOnClient(mc->new double[]{(logical[0]+0.5)*mc.getWindow().getScreenWidth()/mc.screen.width,
                (logical[1]+0.5)*mc.getWindow().getScreenHeight()/mc.screen.height});
        context.getInput().setCursorPos(physical[0],physical[1]);context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
    }

    private void testClose(BoardKind board) {
        var state=snapshot();int side=state.side(playerId);
        int[] point=context.computeOnClient(mc->{var button=widgets(mc.screen).stream().filter(w->w instanceof Button&&w.getWidth()==180).findFirst().orElseThrow();return new int[]{button.getX()+90,button.getY()+10};});
        click(point);context.waitTicks(8);
        if(!context.computeOnClient(mc->mc.screen==null)) {
            failures.add(kind+": footer Close button did not dismiss native dialog");screenshot(kind+"-close-stuck");
            context.getInput().pressKey(GLFW.GLFW_KEY_ESCAPE);context.waitTicks(2);
        } else require(true,kind+" footer closes native dialog");
        require(snapshot().side(playerId)==side,kind+" closing retains seat");
    }

    private void assertFits(String stage,BoardKind board) {
        Map<String,Object> measurement=context.computeOnClient(mc->{
            var all=widgets(mc.screen);var cells=grid(mc);var scroll=all.stream().filter(w->w instanceof AbstractScrollArea).map(w->(AbstractScrollArea)w).findFirst().orElseThrow();
            boolean bounds=cells.size()==board.width*board.height&&cells.stream().allMatch(w->w.getY()>=scroll.getY()&&w.getBottom()<=scroll.getBottom()&&w.getX()>=0&&w.getRight()<=mc.screen.width);
            return Map.of("board",kind,"stage",stage,"width",mc.screen.width,"height",mc.screen.height,"gridCells",cells.size(),"maxScroll",scroll.maxScrollAmount(),"scroll",scroll.scrollAmount(),"allCellsInBounds",bounds,"textHeight",body(mc).getHeight());
        });
        layouts.add(measurement);
        require(Boolean.TRUE.equals(measurement.get("allCellsInBounds"))&&((Number)measurement.get("maxScroll")).intValue()==0,kind+" "+stage+" full board visible without scrolling: "+measurement);
    }

    private static List<AbstractWidget> widgets(ContainerEventHandler root) {
        List<AbstractWidget> result=new ArrayList<>();
        for(var child:root.children()) {if(child instanceof AbstractWidget widget)result.add(widget);if(child instanceof ContainerEventHandler container)result.addAll(widgets(container));}
        return result;
    }
    private static FocusableTextWidget body(Minecraft mc) {return widgets(mc.screen).stream().filter(w->w instanceof FocusableTextWidget).map(w->(FocusableTextWidget)w).findFirst().orElseThrow();}
    private static List<Button> grid(Minecraft mc) {return widgets(mc.screen).stream().filter(w->w instanceof Button&&w.getWidth()==24).map(w->(Button)w).toList();}
    private BoardGameBlockEntity host(MinecraftServer server) {return (BoardGameBlockEntity)server.overworld().getBlockEntity(position);}
    private BoardGame snapshot() {return world.getServer().computeOnServer(server->BoardGame.decode(host(server).game().encode()));}
    private void awaitGame(Predicate<BoardGame> predicate,String label) {
        for(int i=0;i<1200;i++){if(world.getServer().computeOnServer(server->predicate.test(host(server).game()))){require(true,kind+" "+label);return;}context.waitTick();}
        throw new AssertionError("Timeout: "+kind+" "+label);
    }
    private void require(boolean condition,String message) {if(!condition)throw new AssertionError(message);passed.add(message);System.out.println("BOARD_CLIENT_CHECK "+message);}
    private void screenshot(String name) {
        try {context.waitTicks(2);Path file=context.takeScreenshot(name);Files.createDirectories(output.resolve("screenshots"));Files.copy(file,output.resolve("screenshots/"+name+".png"),StandardCopyOption.REPLACE_EXISTING);}
        catch(Throwable failure){failures.add("screenshot "+name+": "+failure);}
    }
    private void writeReport() {
        try {Files.createDirectories(output);Files.writeString(output.resolve("client-results.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status",failures.isEmpty()?"PASS":"FAIL","passed",passed,"failures",failures,"layouts",layouts,"provenance","Real Fabric Client GameTest input, native Dialog widgets and integrated server; controlled promotion/scoring fixtures; not human or two-client multiplayer validation")));}
        catch(Exception failure){throw new RuntimeException(failure);}
    }
}
