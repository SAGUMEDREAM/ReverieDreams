package cc.thonly.reverie_dreams.block.kitchen;

import cc.thonly.reverie_dreams.api.registry.block.CuisineBlockBindings;
import cc.thonly.reverie_dreams.block.entity.PlateBlockEntity;
import cc.thonly.reverie_dreams.event.block.PlateBlockEventHandlers;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.registry.delegate.ItemDelegate;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.util.IdentityHashMap;
import java.util.List;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.Consumable;
import static org.junit.jupiter.api.Assertions.*;

/** Calls the actual interaction handlers; fixtures replace world/network plumbing only. */
class PlateCuisineTest {
    static Unsafe unsafe;
    static PlateBlock plate;
    static TestFood food;

    @BeforeAll static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        var f=Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true); unsafe=(Unsafe)f.get(null);
        withIntrusiveRegistry(BuiltInRegistries.BLOCK, () -> plate=new PlateBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("test", "plate")))));
        withIntrusiveRegistry(BuiltInRegistries.ITEM, () -> food=new TestFood(new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM,Identifier.fromNamespaceAndPath("test","food")))
                .food(new FoodProperties.Builder().nutrition(4).build())));
        for (Item item : new Item[]{Items.AIR, Items.APPLE, Items.CARROT, Items.BOWL, Items.STICK}) {
            if (!item.builtInRegistryHolder().areComponentsBound()) item.builtInRegistryHolder().bindComponents(
                    DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE,64).build());
        }
        food.builtInRegistryHolder().bindKey(ResourceKey.create(Registries.ITEM,Identifier.fromNamespaceAndPath("test","food")));
        food.builtInRegistryHolder().bindComponents(DataComponentMap.builder()
                .set(DataComponents.MAX_STACK_SIZE,64)
                .set(DataComponents.FOOD,new FoodProperties.Builder().nutrition(4).build())
                .set(DataComponents.CONSUMABLE,Consumable.builder().build()).build());
    }

    static void withIntrusiveRegistry(Object registry, Runnable action) throws Exception {
        var holders=MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
        var frozen=MappedRegistry.class.getDeclaredField("frozen");holders.setAccessible(true);frozen.setAccessible(true);
        var old=holders.get(registry);boolean freeze=frozen.getBoolean(registry);
        try {frozen.setBoolean(registry,false);holders.set(registry,new IdentityHashMap<>());action.run();}
        finally {holders.set(registry,old);frozen.setBoolean(registry,freeze);}
    }

    record Fixture(World world, TestPlate contents, TestPlayer player) { }
    Fixture fixture() throws Exception {
        var world=(World)unsafe.allocateInstance(World.class);
        var contents=(TestPlate)unsafe.allocateInstance(TestPlate.class);contents.stack=IngredientStack.empty();world.plate=contents;
        world.acceptsSpawns=true;
        var player=(TestPlayer)unsafe.allocateInstance(TestPlayer.class);player.hungry=true;
        player.fixtureLevel=world;player.main=ItemStack.EMPTY;player.off=ItemStack.EMPTY;
        world.state=plate.defaultBlockState();
        player.inventory=(TestInventory)unsafe.allocateInstance(TestInventory.class);
        player.inventory.accepts=true;
        food.calls=0;food.remainder=ItemStack.EMPTY;
        return new Fixture(world,contents,player);
    }
    InteractionResult place(Fixture f, ItemStack stack, InteractionHand hand) {
        return plate.useItemOn(stack,plate.defaultBlockState(),f.world,BlockPos.ZERO,f.player,hand,null);
    }
    InteractionResult emptyHand(Fixture f) {
        // Match vanilla's item-first dispatch, including when the main hand is empty.
        var state=plate.defaultBlockState();
        var hit=new BlockHitResult(Vec3.ZERO,Direction.UP,BlockPos.ZERO,false);
        var result=state.useItemOn(ItemStack.EMPTY,f.world,f.player,InteractionHand.MAIN_HAND,hit);
        return result instanceof InteractionResult.TryEmptyHandInteraction ? state.useWithoutItem(f.world,f.player,hit) : result;
    }

    @Test void emptyMainHandCanEjectNonFoodThroughItemUseCallback() throws Exception {
        var f=fixture();var stack=new ItemStack(Items.STICK);
        stack.set(DataComponents.CUSTOM_NAME,Component.literal("Display item"));
        place(f,stack,InteractionHand.MAIN_HAND);
        var hit=new BlockHitResult(Vec3.ZERO,Direction.UP,BlockPos.ZERO,false);
        var result=plate.defaultBlockState().useItemOn(ItemStack.EMPTY,f.world,f.player,InteractionHand.MAIN_HAND,hit);
        assertTrue(result.consumesAction());assertTrue(f.contents.stack.isEmpty());
        assertEquals(1,f.world.drops);assertTrue(f.world.dropped.getItem().is(Items.STICK));
        assertEquals(Component.literal("Display item"),f.world.dropped.getItem().get(DataComponents.CUSTOM_NAME));
    }

    @Test void heldItemEjectsExistingContentsWithoutBeingConsumedOrReplacingThem() throws Exception {
        var f=fixture();place(f,new ItemStack(Items.STICK),InteractionHand.MAIN_HAND);
        var held=new ItemStack(Items.APPLE,3);
        var hit=new BlockHitResult(Vec3.ZERO,Direction.UP,BlockPos.ZERO,false);
        var result=plate.defaultBlockState().useItemOn(held,f.world,f.player,InteractionHand.MAIN_HAND,hit);
        assertTrue(result.consumesAction());assertTrue(f.contents.stack.isEmpty());
        assertEquals(3,held.getCount());assertEquals(1,f.world.drops);
        assertTrue(f.world.dropped.getItem().is(Items.STICK));
    }

    @Test void offHandPlacementConsumesExactlyOneAndKeepsComponents() throws Exception {
        var f=fixture();ItemStack stack=new ItemStack(Items.APPLE,12);
        stack.set(DataComponents.CUSTOM_NAME,Component.literal("Special dish"));
        place(f,stack,InteractionHand.OFF_HAND);
        assertEquals(11,stack.getCount());assertEquals(1,f.contents.stack.getCount());
        assertEquals(stack.get(DataComponents.CUSTOM_NAME),f.contents.stack.build().get(DataComponents.CUSTOM_NAME));
    }
    @Test void occupiedFoodPlateEjectsItsServingWithoutPlacingTheHeldItem() throws Exception {
        var f=fixture();place(f,new ItemStack(Items.APPLE),InteractionHand.MAIN_HAND);
        var other=new ItemStack(Items.CARROT,3);place(f,other,InteractionHand.MAIN_HAND);
        assertEquals(3,other.getCount());assertTrue(f.contents.stack.isEmpty());
        assertEquals(1,f.world.drops);assertTrue(f.world.dropped.getItem().is(Items.APPLE));
    }
    @Test void emptyHandEjectsTheServingAndSecondClickDoesNothing() throws Exception {
        var f=fixture();place(f,new ItemStack(Items.APPLE),InteractionHand.MAIN_HAND);emptyHand(f);
        assertTrue(f.contents.stack.isEmpty());assertTrue(f.world.dropped.getItem().is(Items.APPLE));
        assertEquals(new Vec3(0,0.2,0),f.world.dropped.getDeltaMovement());
        assertEquals(Vec3.ZERO,f.world.dropped.position());
        assertFalse(emptyHand(f).consumesAction());assertEquals(1,f.world.drops);
    }
    @Test void sneakEatingCallsFoodOnceAndPersistsEmptyState() throws Exception {
        var f=fixture();place(f,new ItemStack(food),InteractionHand.MAIN_HAND);f.player.sneak=true;
        emptyHand(f);assertEquals(1,food.calls);assertTrue(f.contents.stack.isEmpty());
        emptyHand(f);assertEquals(1,food.calls);
    }
    @Test void fullInventoryStillEjectsTheServingOnceInsteadOfLosingIt() throws Exception {
        var f=fixture();f.player.inventory.accepts=false;
        place(f,new ItemStack(Items.APPLE),InteractionHand.MAIN_HAND);emptyHand(f);emptyHand(f);
        assertTrue(f.contents.stack.isEmpty());assertTrue(f.world.dropped.getItem().is(Items.APPLE));
        assertEquals(1,f.world.drops);assertEquals(0,f.player.inventory.additions);
    }
    @Test void sneakEventEatsPlateFoodWithEitherHandOccupiedWithoutChangingHeldItems() throws Exception {
        for (InteractionHand heldHand : InteractionHand.values()) {
            var f=fixture();f.player.sneak=true;f.contents.stack=IngredientStack.of(food);
            var held=new ItemStack(Items.STICK,12);
            if(heldHand==InteractionHand.MAIN_HAND)f.player.main=held;else f.player.off=held;
            assertTrue(PlateBlockEventHandlers.onRightClickBlock(f.player,InteractionHand.MAIN_HAND,BlockPos.ZERO,Direction.UP).consumesAction());
            assertEquals(1,food.calls);assertTrue(f.contents.stack.isEmpty());assertEquals(12,held.getCount());
            assertEquals(0,f.world.drops);
        }
    }
    @Test void sneakEventDoesNotEatTwiceThroughOffhandAndRespectsHunger() throws Exception {
        var f=fixture();f.player.sneak=true;f.contents.stack=IngredientStack.of(food,2);
        PlateBlockEventHandlers.onRightClickBlock(f.player,InteractionHand.MAIN_HAND,BlockPos.ZERO,Direction.UP);
        PlateBlockEventHandlers.onRightClickBlock(f.player,InteractionHand.OFF_HAND,BlockPos.ZERO,Direction.UP);
        assertEquals(1,food.calls);assertEquals(1,f.contents.stack.getCount());
        f.player.hungry=false;
        assertEquals(InteractionResult.PASS,PlateBlockEventHandlers.onRightClickBlock(f.player,InteractionHand.MAIN_HAND,BlockPos.ZERO,Direction.UP));
        assertEquals(1,food.calls);assertEquals(1,f.contents.stack.getCount());
    }
    @Test void sneakEventOnlyPredictsOnClientAndLeavesSpectatorsAndOtherBlocksAlone() throws Exception {
        var f=fixture();f.player.sneak=true;f.contents.stack=IngredientStack.of(food);f.world.client=true;
        assertTrue(PlateBlockEventHandlers.onRightClickBlock(f.player,InteractionHand.MAIN_HAND,BlockPos.ZERO,Direction.UP).consumesAction());
        assertEquals(0,food.calls);assertFalse(f.contents.stack.isEmpty());
        f.world.client=false;f.player.spectator=true;
        assertEquals(InteractionResult.PASS,PlateBlockEventHandlers.onRightClickBlock(f.player,InteractionHand.MAIN_HAND,BlockPos.ZERO,Direction.UP));
        f.player.spectator=false;f.player.sneak=false;
        assertEquals(InteractionResult.PASS,PlateBlockEventHandlers.onRightClickBlock(f.player,InteractionHand.MAIN_HAND,BlockPos.ZERO,Direction.UP));
        f.player.sneak=true;f.world.state=Blocks.STONE.defaultBlockState();
        assertEquals(InteractionResult.PASS,PlateBlockEventHandlers.onRightClickBlock(f.player,InteractionHand.MAIN_HAND,BlockPos.ZERO,Direction.UP));
        assertEquals(0,food.calls);assertFalse(f.contents.stack.isEmpty());
    }
    @Test void eatingStoresRemainderWithoutConsumingItAgain() throws Exception {
        var f=fixture();place(f,new ItemStack(food),InteractionHand.MAIN_HAND);f.player.sneak=true;
        food.remainder=new ItemStack(Items.BOWL);emptyHand(f);
        assertEquals(1,food.calls);assertTrue(f.contents.stack.build().is(Items.BOWL));
        emptyHand(f);assertEquals(1,food.calls);f.player.sneak=false;emptyHand(f);
        assertTrue(f.world.dropped.getItem().is(Items.BOWL));assertEquals(1,f.world.drops);
    }
    @Test void fullPlayerAndNonFoodCannotTriggerConsumption() throws Exception {
        var f=fixture();place(f,new ItemStack(food),InteractionHand.MAIN_HAND);
        f.player.sneak=true;f.player.hungry=false;emptyHand(f);
        assertEquals(0,food.calls);assertFalse(f.contents.stack.isEmpty());
        f.contents.stack=IngredientStack.of(Items.STICK);f.player.hungry=true;emptyHand(f);
        assertTrue(f.contents.stack.build().is(Items.STICK));assertEquals(0,food.calls);assertEquals(0,f.world.drops);
    }
    @Test void emptyHandCallbackAlsoEjectsNonFoodWithoutEatingIt() throws Exception {
        var f=fixture();f.contents.stack=IngredientStack.of(Items.BOWL);
        var result=plate.defaultBlockState().useWithoutItem(f.world,f.player,new BlockHitResult(Vec3.ZERO,Direction.UP,BlockPos.ZERO,false));
        assertTrue(result.consumesAction());assertTrue(f.contents.stack.isEmpty());
        assertEquals(1,f.world.drops);assertTrue(f.world.dropped.getItem().is(Items.BOWL));assertEquals(0,food.calls);
    }
    @Test void clientPredictionLeavesThePlateAndWorldUntouched() throws Exception {
        var f=fixture();f.world.client=true;f.contents.stack=IngredientStack.of(Items.STICK);
        assertEquals(InteractionResult.SUCCESS,emptyHand(f));
        assertTrue(f.contents.stack.build().is(Items.STICK));assertEquals(0,f.world.drops);
    }
    @Test void rejectedEntitySpawnKeepsTheOriginalItemOnThePlate() throws Exception {
        var f=fixture();f.contents.stack=IngredientStack.of(Items.STICK);f.world.acceptsSpawns=false;
        assertEquals(InteractionResult.FAIL,emptyHand(f));
        assertTrue(f.contents.stack.build().is(Items.STICK));assertEquals(0,f.world.drops);
    }
    @Test void creativePlacementKeepsTheHeldStack() throws Exception {
        var f=fixture();f.player.creative=true;var stack=new ItemStack(Items.APPLE,3);place(f,stack,InteractionHand.MAIN_HAND);
        assertEquals(3,stack.getCount());assertEquals(1,f.contents.stack.getCount());
    }
    @Test void lookupAcceptsDifferentHolderImplementationsAndRejectsDuplicateBindings() {
        CuisineBlockBindings.bind(ItemDelegate.of(Items.APPLE.builtInRegistryHolder()),Blocks.CAKE.builtInRegistryHolder());
        assertEquals(Blocks.CAKE.builtInRegistryHolder(),CuisineBlockBindings.find(new ItemStack(Items.APPLE)).orElseThrow());
        assertTrue(CuisineBlockBindings.find(ItemStack.EMPTY).isEmpty());
        assertTrue(CuisineBlockBindings.find(new ItemStack(Items.CARROT)).isEmpty());
        assertThrows(IllegalArgumentException.class,()->CuisineBlockBindings.bind(Items.APPLE.builtInRegistryHolder(),Blocks.STONE.builtInRegistryHolder()));
        assertThrows(IllegalArgumentException.class,()->CuisineBlockBindings.bind(Holder.direct(Items.CARROT),Blocks.STONE.builtInRegistryHolder()));
    }

    @Test void zeroCountLegacyContentsCannotKeepAPlateOccupied() throws Exception {
        var contents=(StatePlate)unsafe.allocateInstance(StatePlate.class);
        contents.setItem(new IngredientStack(Items.APPLE,0));
        assertTrue(contents.getItem().isEmpty());
        contents.setItem(new IngredientStack(Items.APPLE,-1));
        assertTrue(contents.getItem().isEmpty());
    }
    @Test void storedContentsDoNotAliasTheCallersMutableStack() throws Exception {
        var contents=(StatePlate)unsafe.allocateInstance(StatePlate.class);
        var source=IngredientStack.of(Items.APPLE);contents.setItem(source);source.setCount(0);
        assertEquals(1,contents.getItem().getCount());assertEquals(1,contents.changes);
    }
    @Test void yawOnlyUpdatesAreSavedAndInvalidYawDoesNotSpamUpdates() throws Exception {
        var contents=(StatePlate)unsafe.allocateInstance(StatePlate.class);
        contents.setYaw(90);assertEquals(90,contents.getYaw());assertEquals(1,contents.changes);
        contents.setYaw(90);assertEquals(1,contents.changes);
        contents.setYaw(Float.NaN);assertEquals(0,contents.getYaw());assertEquals(2,contents.changes);
        contents.setYaw(Float.POSITIVE_INFINITY);assertEquals(2,contents.changes);
    }

    static class StatePlate extends PlateBlockEntity {
        int changes;
        StatePlate(){super(BlockPos.ZERO,Blocks.AIR.defaultBlockState());}
        @Override public void setChanged(){changes++;}
    }

    static class World extends ServerLevel {
        TestPlate plate;ItemEntity dropped;int drops;boolean client,acceptsSpawns;
        BlockState state;
        World(){super(null,null,null,null,null,null,false,0,List.of(),false);}
        @Override public BlockEntity getBlockEntity(BlockPos pos){return plate;}
        @Override public BlockState getBlockState(BlockPos pos){return state;}
        @Override public boolean isClientSide(){return client;}
        @Override public boolean addFreshEntity(Entity entity){if(!acceptsSpawns)return false;dropped=(ItemEntity)entity;drops++;return true;}
    }
    static class TestPlate extends PlateBlockEntity {
        IngredientStack stack;float rotation;
        TestPlate(){super(BlockPos.ZERO,Blocks.AIR.defaultBlockState());}
        @Override public IngredientStack getItem(){return stack;}
        @Override public void setItem(IngredientStack stack){this.stack=stack;}
        @Override public void setYaw(float yaw){rotation=yaw;}
    }
    static class TestPlayer extends Player {
        boolean sneak,hungry,creative;TestInventory inventory;ItemStack dropped;int dropCount;
        World fixtureLevel;ItemStack main,off;boolean spectator;
        TestPlayer(){super(null,null);}
        @Override public GameType gameMode(){return creative?GameType.CREATIVE:GameType.SURVIVAL;}
        @Override public boolean isShiftKeyDown(){return sneak;}
        @Override public Level level(){return fixtureLevel;}
        @Override public boolean isSpectator(){return spectator;}
        @Override public ItemStack getItemInHand(InteractionHand hand){return hand==InteractionHand.MAIN_HAND?main:off;}
        @Override public boolean canEat(boolean always){return hungry||always;}
        @Override public boolean hasInfiniteMaterials(){return creative;}
        @Override public Inventory getInventory(){return inventory;}
        @Override public ItemEntity drop(ItemStack stack,boolean randomThrow){dropped=stack.copy();dropCount++;return null;}
    }
    static class TestInventory extends Inventory {
        ItemStack received;int additions;boolean accepts;
        TestInventory(){super(null,null);}
        @Override public boolean add(ItemStack stack){if(!accepts)return false;received=stack.copy();stack.setCount(0);additions++;return true;}
    }
    static class TestFood extends Item {
        int calls;ItemStack remainder;
        TestFood(Properties properties){super(properties);}
        @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
            calls++;stack.consume(1,user);return remainder.isEmpty()?stack:remainder.copy();
        }
    }
}
