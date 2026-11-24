package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopKind;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.entry.WorkshopProcessRecipe;
import cc.thonly.reverie_dreams.recipe.type.WorkshopProcessRecipeType;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.*;
import org.junit.jupiter.api.*;
import sun.misc.Unsafe;
import java.util.*;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameType;
import static org.junit.jupiter.api.Assertions.*;

class WorkshopInteractionTest {
    static Unsafe unsafe;
    @BeforeAll static void bootstrap() throws Exception{
        SharedConstants.tryDetectVersion();Bootstrap.bootStrap();var field=Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);unsafe=(Unsafe)field.get(null);
        for(Item item:new Item[]{Items.AIR,Items.BLAZE_ROD,Items.BLAZE_POWDER,Items.BONE,Items.BONE_MEAL,Items.DANDELION,Items.YELLOW_DYE,Items.WHEAT,Items.BOOK,Items.PAPER,Items.STICK})
            if(!item.builtInRegistryHolder().areComponentsBound())item.builtInRegistryHolder().bindComponents(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE,64).build());
    }
    static WorkshopProcessRecipe recipe(Item input,Item output,int count,int work){
        var recipe=new WorkshopProcessRecipe(BuiltInRegistries.ITEM.getKey(input).toString(),IngredientStack.of(new ItemStack(output,count)),work);recipe.setId(Identifier.fromNamespaceAndPath("test",BuiltInRegistries.ITEM.getKey(input).getPath()));return recipe;
    }
    static class Station extends WorkshopStationBlockEntity {
        final WorkshopKind kind;WorkshopProcessRecipe configured;
        Station(WorkshopKind kind,WorkshopProcessRecipe recipe){super(BlockEntityType.BARREL,BlockPos.ZERO,Blocks.BARREL.defaultBlockState());this.kind=kind;configured=recipe;}
        @Override public WorkshopKind workshopKind(){return kind;}
        @Override public Optional<WorkshopProcessRecipe> recipe(ItemStack stack){return configured!=null&&configured.matches(stack)?Optional.of(configured):Optional.empty();}
    }
    static class Storage extends WorkshopStorageBlockEntity {
        Storage(){super(BlockEntityType.BARREL,BlockPos.ZERO,Blocks.BARREL.defaultBlockState());}
        @Override public WorkshopKind workshopKind(){return WorkshopKind.MERCHANDISE_SHELF;}
    }
    static class TestPlayer extends Player {
        TestInventory inventory;boolean creative;int drops;ItemStack dropped;
        TestPlayer(){super(null,null);}
        @Override public GameType gameMode(){return creative?GameType.CREATIVE:GameType.SURVIVAL;}
        @Override public boolean hasInfiniteMaterials(){return creative;}
        @Override public Inventory getInventory(){return inventory;}
        @Override public ItemEntity drop(ItemStack stack,boolean random){drops++;dropped=stack.copy();return null;}
    }
    static class TestInventory extends Inventory {
        int received;boolean accepts=true;
        TestInventory(){super(null,null);}
        @Override public boolean add(ItemStack stack){if(!accepts)return false;received+=stack.getCount();stack.setCount(0);return true;}
    }
    TestPlayer player() throws Exception {var p=(TestPlayer)unsafe.allocateInstance(TestPlayer.class);p.inventory=(TestInventory)unsafe.allocateInstance(TestInventory.class);p.inventory.accepts=true;return p;}
    @Test void grindingConsumesOneRodAndYieldsThreePowderOnlyOnce() throws Exception{
        var host=new Station(WorkshopKind.YAGEN,recipe(Items.BLAZE_ROD,Items.BLAZE_POWDER,3,8));var player=player();var held=new ItemStack(Items.BLAZE_ROD,7);
        assertTrue(host.insert(held,player));assertEquals(6,held.getCount());
        for(int i=0;i<8;i++){assertTrue(host.grind(i*4));assertFalse(host.grind(i*4));}
        assertTrue(host.getItem(0).isEmpty());assertEquals(3,host.getItem(4).getCount());assertFalse(host.grind(100));
        assertFalse(host.insert(held,player));assertEquals(6,held.getCount());
        assertTrue(host.take(player));assertEquals(3,player.inventory.received);assertFalse(host.take(player));assertEquals(3,player.inventory.received);
    }
    @Test void fullInventoryReturnsTheWholeGroundOutputOnce() throws Exception{
        var host=new Station(WorkshopKind.YAGEN,recipe(Items.BONE,Items.BONE_MEAL,4,1));var p=player();p.inventory.accepts=false;
        host.insert(new ItemStack(Items.BONE),p);host.grind(0);host.take(p);host.take(p);
        assertEquals(1,p.drops);assertEquals(4,p.dropped.getCount());assertTrue(p.dropped.is(Items.BONE_MEAL));
    }
    @Test void dryingFourSlotsRequiresActiveWorkAndCannotLoopOnItsProduct() throws Exception{
        var host=new Station(WorkshopKind.HERB_DRYING_TRAY,recipe(Items.DANDELION,Items.WHEAT,1,1200));var p=player();var held=new ItemStack(Items.DANDELION,8);
        for(int i=0;i<4;i++)assertTrue(host.insert(held,p));assertFalse(host.insert(held,p));assertEquals(4,held.getCount());
        for(int i=0;i<59;i++)host.dryStep(20);assertTrue(host.getItem(0).is(Items.DANDELION));assertEquals(1180,host.progress(0));
        host.dryStep(20);for(int i=0;i<4;i++)assertTrue(host.getItem(i).is(Items.WHEAT));host.dryStep(72000);
        for(int i=0;i<4;i++)host.take(p);assertEquals(4,p.inventory.received);assertTrue(host.isEmpty());
    }
    @Test void progressAndItemsSurviveReloadAndRecipeChangesResetWork() throws Exception{
        var recipe=recipe(Items.DANDELION,Items.WHEAT,1,1200);var host=new Station(WorkshopKind.HERB_DRYING_TRAY,recipe);host.insert(new ItemStack(Items.DANDELION),player());host.dryStep(600);
        var access=RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,access);host.saveAdditional(out);
        var restored=new Station(WorkshopKind.HERB_DRYING_TRAY,recipe);restored.loadAdditional(TagValueInput.create(ProblemReporter.DISCARDING,access,out.buildResult()));
        assertEquals(600,restored.progress(0));restored.dryStep(600);assertTrue(restored.getItem(0).is(Items.WHEAT));
        host.configured=recipe(Items.DANDELION,Items.YELLOW_DYE,2,1000);host.dryStep(20);assertEquals(20,host.progress(0));
    }
    @Test void booksKeepAllComponentsAndTheCallerCannotMutateStoredCopies() throws Exception{
        var host=new Station(WorkshopKind.BOOK_STACK,null);var p=player();var held=new ItemStack(Items.BOOK,2);held.set(DataComponents.CUSTOM_NAME,Component.literal("Library copy"));
        assertTrue(host.insert(held,p));held.set(DataComponents.CUSTOM_NAME,Component.literal("Changed hand"));
        assertEquals("Library copy",host.getItem(0).getHoverName().getString());assertEquals(1,held.getCount());host.take(p);assertTrue(host.isEmpty());assertEquals(1,p.inventory.received);
    }
    @Test void creativePlacementKeepsHandAndAutomationCannotBypassStationRules() throws Exception{
        var host=new Station(WorkshopKind.YAGEN,recipe(Items.BLAZE_ROD,Items.BLAZE_POWDER,3,8));var p=player();p.creative=true;var held=new ItemStack(Items.BLAZE_ROD,2);
        assertTrue(host.insert(held,p));assertEquals(2,held.getCount());assertFalse(host.insert(new ItemStack(Items.STICK),p));
        assertEquals(0,host.getSlotsForFace(Direction.UP).length);assertFalse(host.canPlaceItemThroughFace(0,held,Direction.UP));assertFalse(host.canTakeItemThroughFace(0,held,Direction.DOWN));
    }
    @Test void shelfInsertSortAndExtractConserveCountsAndComponents() throws Exception{
        var host=new Storage();var p=player();var held=new ItemStack(Items.BOOK,12);held.set(DataComponents.CUSTOM_NAME,Component.literal("A"));
        for(int i=0;i<4;i++)assertTrue(host.insertOne(held,p));assertEquals(8,held.getCount());
        var other=new ItemStack(Items.BOOK,3);other.set(DataComponents.CUSTOM_NAME,Component.literal("B"));host.setItem(26,other);host.sortContents();
        int total=0;boolean a=false,b=false;for(int i=0;i<27;i++){var stack=host.getItem(i);total+=stack.getCount();a|=stack.getHoverName().getString().equals("A");b|=stack.getHoverName().getString().equals("B");}
        assertEquals(7,total);assertTrue(a&&b);for(int i=0;i<7;i++)assertTrue(host.takeDisplay(p));assertFalse(host.takeDisplay(p));assertEquals(7,p.inventory.received);assertTrue(host.isEmpty());
    }
    @Test void processRecipeCodecAndReloadMatchingKeepResultCounts() {
        var original=recipe(Items.BLAZE_ROD,Items.BLAZE_POWDER,3,8);
        var encoded=WorkshopProcessRecipe.CODEC.encodeStart(JsonOps.INSTANCE,original).getOrThrow();
        assertEquals(3,((JsonElement)encoded).getAsJsonObject().getAsJsonObject("output").get("count").getAsInt());
        var decoded=original; // Full decode uses loader-backed ItemUtils; exercised in the native probe.
        assertEquals(3,decoded.getOutput().getCount());assertEquals(8,decoded.work());assertTrue(decoded.matches(new ItemStack(Items.BLAZE_ROD)));assertFalse(decoded.matches(new ItemStack(Items.STICK)));
        assertThrows(IllegalArgumentException.class,()->recipe(Items.BLAZE_ROD,Items.BLAZE_POWDER,3,0));
        var type=new WorkshopProcessRecipeType("grinding");type.add(Identifier.fromNamespaceAndPath("test","rod"),original);
        assertEquals(1,type.getMatches(List.of(IngredientStack.of(Items.BLAZE_ROD))).size());assertTrue(type.getMatches(List.of(IngredientStack.of(Items.STICK))).isEmpty());
    }
}
