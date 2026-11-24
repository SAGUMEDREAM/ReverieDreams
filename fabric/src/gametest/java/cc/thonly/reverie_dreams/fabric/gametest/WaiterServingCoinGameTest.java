package cc.thonly.reverie_dreams.fabric.gametest;

import cc.thonly.reverie_dreams.block.restaurant.RestaurantBlock;
import cc.thonly.reverie_dreams.block.entity.CustomChestBlockEntity;
import cc.thonly.reverie_dreams.block.entity.RestaurantBlockEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.inventory.NPCWorkInventory;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.NPCStates;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDBeverageItems;
import cc.thonly.reverie_dreams.registry.content.item.RDCuisineItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;

public final class WaiterServingCoinGameTest {
    private static void prepareFloor(GameTestHelper helper, int size) {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
    }

    private static void setupRestaurant(GameTestHelper helper, ServerPlayer player, BlockPos bellRel, BlockPos menuRel) {
        helper.setBlock(bellRel, RDBlocks.MYSTIA_BELL.asBlock().defaultBlockState());
        helper.setBlock(menuRel, RDBlocks.MENU_BOOK.asBlock().defaultBlockState());
        RestaurantBlockEntity bell = helper.getBlockEntity(bellRel, RestaurantBlockEntity.class);
        RestaurantBlockEntity menu = helper.getBlockEntity(menuRel, RestaurantBlockEntity.class);
        bell.claim(player);
        menu.claim(player);
        menu.menu().toggle(false, BuiltInRegistries.ITEM.getKey(RDCuisineItems.SEAFOOD_MISO_SOUP.asItem()));
        menu.menu().toggle(true, BuiltInRegistries.ITEM.getKey(RDBeverageItems.BEER.asItem()));
        bell.bind(menu, player);
        helper.setBlock(bellRel, bell.getBlockState().setValue(RestaurantBlock.OPEN, true));
        helper.setBlock(menuRel, menu.getBlockState().setValue(RestaurantBlock.OPEN, true));
    }

    private static NPCSimpleEntity setupWaiter(GameTestHelper helper, ServerPlayer player, BlockPos pos, BlockPos workingPosRel) {
        NPCSimpleEntity npc = helper.spawn(RDEntityTypes.NPC_SIMPLE_ENTITY.value(), pos);
        npc.setOwner(player);
        npc.setRoleType(BuiltInRegistryProviders.NPC_ROLE_TYPE.stream().findFirst().orElseThrow());
        npc.setWorkingPos(helper.absolutePos(workingPosRel));
        npc.setNPCState(NPCStates.WORKING);
        npc.setWorkMode(NPCWorkModes.SERVING);
        return npc;
    }

    private static ItemEntity spawnCoin(GameTestHelper helper, BlockPos posRel, ItemStack stack) {
        BlockPos abs = helper.absolutePos(posRel);
        ItemEntity item = new ItemEntity(helper.getLevel(), abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5, stack);
        item.setPickUpDelay(0);
        helper.getLevel().addFreshEntity(item);
        return item;
    }

    @GameTest(setupTicks = 1, maxTicks = 150)
    public void waiterPicksUpCoinWhenCapacityAvailable(GameTestHelper helper) {
        prepareFloor(helper, 10);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos bellPos = new BlockPos(5, 1, 5);
        BlockPos menuPos = new BlockPos(5, 1, 6);
        setupRestaurant(helper, player, bellPos, menuPos);

        NPCSimpleEntity waiter = setupWaiter(helper, player, new BlockPos(4, 1, 4), bellPos);
        ItemEntity coin = spawnCoin(helper, new BlockPos(6, 1, 4), new ItemStack(RDItems.COPPER_COIN.asItem(), 5));

        helper.startSequence()
                .thenWaitUntil(() -> {
                    helper.assertTrue(coin.isRemoved() || coin.getItem().isEmpty(), "ground coin was picked up");
                    int count = NPCWorkInventory.count(waiter.getInventory(), s -> s.is(RDItems.COPPER_COIN.asItem()));
                    helper.assertTrue(count == 5, "waiter collected coins into cargo");
                })
                .thenSucceed();
    }

    @GameTest(setupTicks = 1, maxTicks = 200)
    public void waiterDepositsCoinsToBarrelWhenBackpackFull(GameTestHelper helper) {
        prepareFloor(helper, 10);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos bellPos = new BlockPos(5, 1, 5);
        BlockPos menuPos = new BlockPos(5, 1, 6);
        setupRestaurant(helper, player, bellPos, menuPos);

        BlockPos barrelPos = new BlockPos(3, 1, 4);
        helper.setBlock(barrelPos, Blocks.BARREL.defaultBlockState());
        BarrelBlockEntity barrel = helper.getBlockEntity(barrelPos, BarrelBlockEntity.class);
        barrel.setItem(0, new ItemStack(RDItems.COPPER_COIN.asItem(), 1));

        NPCSimpleEntity waiter = setupWaiter(helper, player, new BlockPos(4, 1, 4), bellPos);
        for (int i = 0; i < NPCWorkInventory.CARGO_SIZE; i++) {
            waiter.getInventory().setItem(i, new ItemStack(RDItems.COPPER_COIN.asItem(), 96));
        }

        ItemEntity coin = spawnCoin(helper, new BlockPos(6, 1, 4), new ItemStack(RDItems.COPPER_COIN.asItem(), 5));

        helper.startSequence()
                .thenWaitUntil(() -> {
                    int barrelCoins = 0;
                    for (int i = 0; i < barrel.getContainerSize(); i++) {
                        ItemStack s = barrel.getItem(i);
                        if (s.is(RDItems.COPPER_COIN.asItem())) {
                            barrelCoins += s.getCount();
                        }
                    }
                    helper.assertTrue(barrelCoins > 1, "waiter deposited coins into the recognized barrel");
                })
                .thenSucceed();
    }

    @GameTest(setupTicks = 1, maxTicks = 200)
    public void waiterDepositsCoinsToSilverChestWhenBackpackFull(GameTestHelper helper) {
        prepareFloor(helper, 10);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos bellPos = new BlockPos(5, 1, 5);
        BlockPos menuPos = new BlockPos(5, 1, 6);
        setupRestaurant(helper, player, bellPos, menuPos);

        BlockPos chestPos = new BlockPos(3, 1, 4);
        helper.setBlock(chestPos, RDBlocks.SILVER_CHEST_BLOCK.chestBlock().asBlock().defaultBlockState());
        CustomChestBlockEntity chest = helper.getBlockEntity(chestPos, CustomChestBlockEntity.class);
        chest.setItem(0, new ItemStack(RDItems.SILVER_COIN.asItem(), 1));

        NPCSimpleEntity waiter = setupWaiter(helper, player, new BlockPos(4, 1, 4), bellPos);
        for (int i = 0; i < NPCWorkInventory.CARGO_SIZE; i++) {
            waiter.getInventory().setItem(i, new ItemStack(RDItems.SILVER_COIN.asItem(), 96));
        }

        ItemEntity coin = spawnCoin(helper, new BlockPos(6, 1, 4), new ItemStack(RDItems.SILVER_COIN.asItem(), 5));

        helper.startSequence()
                .thenWaitUntil(() -> {
                    int chestCoins = 0;
                    for (int i = 0; i < chest.getContainerSize(); i++) {
                        ItemStack s = chest.getItem(i);
                        if (s.is(RDItems.SILVER_COIN.asItem())) {
                            chestCoins += s.getCount();
                        }
                    }
                    helper.assertTrue(chestCoins > 1, "waiter deposited coins into the recognized silver chest");
                })
                .thenSucceed();
    }

    @GameTest(setupTicks = 1, maxTicks = 100)
    public void waiterIgnoresBarrelWithoutCoins(GameTestHelper helper) {
        prepareFloor(helper, 10);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos bellPos = new BlockPos(5, 1, 5);
        BlockPos menuPos = new BlockPos(5, 1, 6);
        setupRestaurant(helper, player, bellPos, menuPos);

        BlockPos barrelPos = new BlockPos(3, 1, 4);
        helper.setBlock(barrelPos, Blocks.BARREL.defaultBlockState());
        BarrelBlockEntity barrel = helper.getBlockEntity(barrelPos, BarrelBlockEntity.class);
        barrel.setItem(0, new ItemStack(Items.APPLE, 5));

        NPCSimpleEntity waiter = setupWaiter(helper, player, new BlockPos(4, 1, 4), bellPos);
        for (int i = 0; i < NPCWorkInventory.CARGO_SIZE; i++) {
            waiter.getInventory().setItem(i, new ItemStack(Items.APPLE, 64));
        }

        ItemEntity coin = spawnCoin(helper, new BlockPos(6, 1, 4), new ItemStack(RDItems.COPPER_COIN.asItem(), 5));

        helper.startSequence()
                .thenIdle(40)
                .thenExecute(() -> {
                    helper.assertTrue(coin.isAlive() && !coin.isRemoved(), "coin remains uncollected because backpack is full and no valid container exists");
                    helper.assertTrue(barrel.getItem(0).is(Items.APPLE) && barrel.getItem(0).getCount() == 5, "barrel without coins was not used as a coin container");
                })
                .thenSucceed();
    }
}
