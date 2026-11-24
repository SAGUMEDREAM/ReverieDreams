package cc.thonly.reverie_dreams.fabric.gametest;

import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopKind;
import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopStationBlock;
import cc.thonly.reverie_dreams.block.entity.WorkshopStationBlockEntity;
import cc.thonly.reverie_dreams.gui.block.WorkshopRecipeGui;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.WorkshopProcessRecipe;
import cc.thonly.reverie_dreams.registry.content.block.RDJapaneseSet2Blocks;
import cc.thonly.reverie_dreams.registry.content.item.RDWorkshopItems;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.SguiUtils;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public final class HerbDryingTrayUiClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        var world = context.worldBuilder().create();
        try {
            context.getInput().resizeWindow(1280, 720);
            var reload = context.computeOnClient(client -> {
                client.options.guiScale().set(2);
                client.options.pauseOnLostFocus = false;
                client.getLanguageManager().setSelected("zh_cn");
                client.options.languageCode = "zh_cn";
                client.resizeGui();
                return client.reloadResourcePacks();
            });
            context.waitFor(client -> reload.isDone() && client.getOverlay() == null, 1200);
            reload.join();
            require(context.computeOnClient(client -> I18n.exists("workshop.reverie_dreams.drying.covered")),
                    "Generated Chinese drying status is loaded");
            testTray(context, world);
        } finally {
            closeWorld(context, world);
        }
        System.out.println("HERB_DRYING_TRAY_UI_GAMETEST_PASS: three-row catalogue, pagination, four drying slots, sunlight, rain, cover, night and retrieval");
    }

    private static void testTray(ClientGameTestContext context, TestSingleplayerContext world) {
        var block = (WorkshopStationBlock) RDJapaneseSet2Blocks.get(WorkshopKind.HERB_DRYING_TRAY).asBlock();
        var state = block.defaultBlockState();
        var host = world.getServer().computeOnServer(server -> {
            var player = server.getPlayerList().getPlayers().getFirst();
            var level = server.overworld();
            var pos = player.blockPosition().offset(2, 1, 0);
            level.setBlockAndUpdate(pos, state);
            require(level.getBlockEntity(pos) instanceof WorkshopStationBlockEntity, "Tray has a station block entity");
            var tray = (WorkshopStationBlockEntity) level.getBlockEntity(pos);
            open(block, tray, player);
            var gui = currentGui(player);
            require(Set.of(stack(gui, 1).getItem(), stack(gui, 2).getItem(), stack(gui, 3).getItem())
                    .equals(Set.of(Items.FERN, Items.DANDELION, Items.BLUE_ORCHID)), "Built-in recipe shows three alternatives");
            require(stack(gui, 7).is(RDWorkshopItems.DRIED_HERBS.value()), "Built-in output is dried herbs");
            require(stack(gui, 18).isEmpty() && stack(gui, 26).isEmpty(), "Single page has no navigation arrows");
            player.containerMenu.clicked(7, 0, ContainerInput.QUICK_MOVE, player);
            require(tray.isEmpty() && player.containerMenu.getCarried().isEmpty()
                    && player.getInventory().countItem(RDWorkshopItems.DRIED_HERBS.value()) == 0, "Preview cannot dispense items");
            click(gui, 24, ContainerInput.PICKUP);
            require(!gui.isOpen(), "Close button closes the catalogue");
            for (Item item : new Item[]{Items.FERN, Items.DANDELION, Items.BLUE_ORCHID, Items.FERN}) {
                var input = new ItemStack(item);
                block.useItemOn(input, state, level, pos, player, InteractionHand.MAIN_HAND, hit(tray));
            }
            require(tray.getItem(3).is(Items.FERN), "All four world positions can be filled from the hand");
            var excess = new ItemStack(Items.DANDELION);
            require(!block.useItemOn(excess, state, level, pos, player, InteractionHand.MAIN_HAND, hit(tray)).consumesAction()
                    && excess.getCount() == 1, "Fifth input stays in the hand");
            open(block, tray, player);
            return tray;
        });
        capture(context, "herb-drying-tray", false, Items.SUNFLOWER);
        world.getServer().runOnServer(server -> server.overworld().setBlockAndUpdate(host.getBlockPos().above(2), Blocks.STONE.defaultBlockState()));
        capture(context, "herb-drying-tray-covered", true, Items.OAK_SLAB);
        int coveredProgress = world.getServer().computeOnServer(server -> host.progress(0));
        context.waitTicks(25);
        world.getServer().runOnServer(server -> {
            require(host.progress(0) == coveredProgress, "A roof pauses actual progress");
            var level = server.overworld();
            level.removeBlock(host.getBlockPos().above(2), false);
            server.setWeatherParameters(0, 6000, true, false);
            level.setRainLevel(1);
        });
        waitForStatus(context, Items.WATER_BUCKET);
        context.waitTicks(25);
        world.getServer().runOnServer(server -> {
            require(host.progress(0) == coveredProgress, "Rain pauses actual progress");
            server.setWeatherParameters(6000, 0, false, false);
            server.overworld().setRainLevel(0);
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "time set midnight");
        });
        waitForStatus(context, Items.BLACK_DYE);
        context.waitTicks(25);
        world.getServer().runOnServer(server -> {
            require(host.progress(0) == coveredProgress, "Night pauses actual progress");
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "time set noon");
        });
        waitForStatus(context, Items.SUNFLOWER);
        context.waitTicks(25);
        world.getServer().runOnServer(server -> {
            require(host.progress(0) > coveredProgress, "Open sky and sunlight resume actual progress");
            var player = server.getPlayerList().getPlayers().getFirst();
            var status = stack(currentGui(player), 22).get(DataComponents.LORE);
            require(status != null && status.lines().size() == 4, "Status lists the four occupied positions");
            currentGui(player).close();
            player.setShiftKeyDown(true);
            open(block, host, player);
            player.setShiftKeyDown(false);
            require(host.getItem(3).isEmpty() && host.getItem(2).is(Items.BLUE_ORCHID)
                    && player.getInventory().countItem(Items.FERN) == 1, "Sneak-use takes the highest occupied slot, including unfinished herbs");
            host.dryStep(1200);
            require(host.getItem(0).is(RDWorkshopItems.DRIED_HERBS.value()), "Finished herbs replace the input in place");
            checkPagination(block, host, player);
        });
    }

    private static void checkPagination(WorkshopStationBlock block, WorkshopStationBlockEntity host, ServerPlayer player) {
        var type = RecipeManager.HERB_DRYING;
        var original = type.getRegistryView();
        var first = Identifier.fromNamespaceAndPath("reverie_dreams_test", "a_first");
        var last = Identifier.fromNamespaceAndPath("reverie_dreams_test", "z_last");
        try {
            type.add(last, new WorkshopProcessRecipe("minecraft:wheat", IngredientStack.of(Items.BREAD), 40));
            type.add(first, new WorkshopProcessRecipe("minecraft:beetroot", IngredientStack.of(Items.BEETROOT_SOUP), 60));
            open(block, host, player);
            var gui = currentGui(player);
            require(stack(gui, 16).is(Items.BEETROOT_SOUP), "Injected recipes are sorted by ID");
            click(gui, 26, ContainerInput.PICKUP);
            require(stack(gui, 7).is(Items.BREAD) && stack(gui, 16).isEmpty()
                    && stack(gui, 26).isEmpty(), "Second page shows its final recipe and removes Next");
            click(gui, 18, ContainerInput.PICKUP);
            require(stack(gui, 7).is(RDWorkshopItems.DRIED_HERBS.value()) && stack(gui, 18).isEmpty(), "Previous returns to the first page");
            gui.close();
            type.clear();
            open(block, host, player);
            gui = currentGui(player);
            require(stack(gui, 5).is(Items.PAPER) && stack(gui, 7).isEmpty()
                    && stack(gui, 18).isEmpty() && stack(gui, 26).isEmpty(), "Empty catalogue has a message and no page arrows");
            gui.close();
        } finally {
            type.clear();
            original.forEach(type::add);
        }
    }

    private static BlockHitResult hit(WorkshopStationBlockEntity host) {
        var pos = host.getBlockPos();
        return new BlockHitResult(pos.getCenter(), Direction.UP, pos, false);
    }

    private static void open(WorkshopStationBlock block, WorkshopStationBlockEntity host, ServerPlayer player) {
        block.useWithoutItem(host.getBlockState(), player.level(), host.getBlockPos(), player, hit(host));
    }

    private static WorkshopRecipeGui currentGui(ServerPlayer player) {
        if (SguiUtils.getCurrentGui(player) instanceof WorkshopRecipeGui gui)
            return gui;
        throw new AssertionError("Normal empty-hand use must open the recipe catalogue");
    }

    private static ItemStack stack(WorkshopRecipeGui gui, int slot) {
        var element = gui.getGuiElement(slot);
        return element == null ? ItemStack.EMPTY : element.getItemStack();
    }

    private static void click(WorkshopRecipeGui gui, int slot, ContainerInput input) {
        gui.getGuiElement(slot).getGuiCallback().click(slot, ClickType.MOUSE_LEFT, input, gui);
    }

    private static void waitForStatus(ClientGameTestContext context, Item icon) {
        context.waitFor(client -> client.screen instanceof ContainerScreen screen
                && screen.getTitle().getString().contains("\ue00a") && screen.getMenu().getRowCount() == 3
                && screen.getMenu().getSlot(22).getItem().is(icon), 240);
    }

    private static void capture(ClientGameTestContext context, String name, boolean hoverStatus, Item icon) {
        waitForStatus(context, icon);
        context.getInput().setCursorPos(hoverStatus ? 640 : 20, hoverStatus ? 314 : 20);
        context.waitTicks(6);
        String output = System.getProperty("reverie.npcUiClientTest.output");
        if (output == null)
            return;
        try {
            Files.copy(context.takeScreenshot(name), Path.of(output, name + ".png"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new AssertionError("Cannot save drying tray screenshot", e);
        }
    }

    private static void closeWorld(ClientGameTestContext context, TestSingleplayerContext world) {
        var server = world.getServer().computeOnServer(current -> current);
        try {
            context.runOnClient(client -> {
                // Avoid waiting on the GameTest-paused server from the client thread.
                var field = Minecraft.class.getDeclaredField("singleplayerServer");
                field.setAccessible(true);
                field.set(client, null);
                var saving = Component.translatable("menu.savingLevel");
                client.level.disconnect(saving);
                client.disconnect(new GenericMessageScreen(saving), false);
            });
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Cannot close the drying tray test world", e);
        }
        context.waitFor(client -> client.level == null, 1200);
        if (!server.isShutdown()) world.getServer().runOnServer(current -> current.halt(false));
        context.waitFor(client -> server.isShutdown(), 1200);
        context.setScreen(TitleScreen::new);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
