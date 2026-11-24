package cc.thonly.reverie_dreams.fabric.gametest;

import cc.thonly.reverie_dreams.block.entity.GensokyoAltarBlockEntity;
import cc.thonly.reverie_dreams.block.entity.KitchenwareBlockEntity;
import cc.thonly.reverie_dreams.block.entity.StrengthenTableBlockEntity;
import cc.thonly.reverie_dreams.block.props.GensokyoAltarBlock;
import cc.thonly.reverie_dreams.gui.block.GensokyoAltarGui;
import cc.thonly.reverie_dreams.gui.block.KitchenBlockGui;
import cc.thonly.reverie_dreams.gui.block.StrengthTableGui;
import cc.thonly.reverie_dreams.gui.recipe.display.GensokyoAltarDisplayView;
import cc.thonly.reverie_dreams.gui.recipe.display.KitchenBlockDisplayView;
import cc.thonly.reverie_dreams.gui.recipe.display.StrengthTableDisplayView;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.item.danmaku.DanmakuItem;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.GensokyoAltarRecipe;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.recipe.type.GensokyoAltarRecipeType;
import cc.thonly.reverie_dreams.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.recipe.view.RecipeKeyEntry;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDKitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public final class WorkstationUiClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        var world = context.worldBuilder().create();
        try {
            context.getInput().resizeWindow(1280, 720);
            var reload = context.computeOnClient(client -> {
                client.options.guiScale().set(2);
                client.options.renderDistance().set(4);
                client.options.pauseOnLostFocus = false;
                client.getLanguageManager().setSelected("zh_cn");
                client.options.languageCode = "zh_cn";
                client.resizeGui();
                return client.reloadResourcePacks();
            });
            context.waitFor(client -> reload.isDone() && client.getOverlay() == null, 1200);
            reload.join();
            require(context.computeOnClient(client -> net.minecraft.client.resources.language.I18n.exists("gui.reverie_dreams.kitchen.start")),
                    "Generated Chinese translations are loaded");
            this.testKitchen(context, world);
            this.testAltar(context, world);
            this.testStrength(context, world);
        } catch (IOException e) {
            throw new AssertionError("Cannot save workstation screenshots", e);
        } finally {
            this.closeWorld(context, world);
        }
        System.out.println("WORKSTATION_UI_CLIENT_GAMETEST_PASS: six pages, kitchen pagination and submission, altar guidance, strength preview and claim");
    }

    private void testKitchen(ClientGameTestContext context, TestSingleplayerContext world) throws IOException {
        var type = (KitchenRecipeType) RecipeManager.KITCHEN_TYPE;
        var pot = KitchenRecipeType.TypeInstance.COOKING_POT;
        var single = new KitchenRecipe(pot, List.of(IngredientStack.of(Items.PRISMARINE_SHARD)),
                IngredientStack.of(Items.BAKED_POTATO), 5);
        var additional = List.of(IngredientStack.of(Items.PRISMARINE_SHARD), IngredientStack.of(Items.EMERALD_BLOCK));
        var fixtureKeys = new ArrayList<Identifier>();
        try {
            var kitchen = world.getServer().computeOnServer(server -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                var level = server.overworld();
                var pos = player.blockPosition().offset(2, 0, 3);
                var block = RDKitchenBlocks.COOKING_POT.asBlock();
                level.setBlockAndUpdate(pos, block.defaultBlockState());
                var entity = (KitchenwareBlockEntity) level.getBlockEntity(pos);
                require(entity != null && pot.equals(entity.getTypeInstance()), "Cooking pot resolves its recipe type");
                var key = fixture("single_dish");
                fixtureKeys.add(key);
                type.register(pot, key, single);
                for (int i = 0; i < 21; i++) {
                    var id = fixture("extra_dish_" + i);
                    fixtureKeys.add(id);
                    type.register(pot, id, new KitchenRecipe(pot, additional, IngredientStack.of(Items.BREAD), 5));
                }
                entity.getInventory().setItem(0, new ItemStack(Items.PRISMARINE_SHARD, 2));
                entity.getInventory().setItem(1, new ItemStack(Items.EMERALD_BLOCK));
                var gui = new KitchenBlockGui<>(block, entity, player);
                require(gui.open(), "Kitchen opens");
                gui.onTick();
                click(gui, 34);
                require(gui.getGuiElement(28) != null && gui.getGuiElement(34) == null, "Kitchen reaches final page");
                entity.getInventory().setItem(1, ItemStack.EMPTY);
                gui.onTick();
                require(gui.getGuiElement(28) == null && gui.getGuiElement(1).getItemStack().is(Items.BAKED_POTATO),
                        "Shrinking matches returns to the populated page");
                require(!player.containerMenu.getSlot(43).mayPlace(new ItemStack(Items.DIRT)), "Kitchen output only permits taking");
                return gui;
            });
            capture(context, "kitchen", "\ub003", 6);
            world.getServer().runOnServer(server -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                var entity = (KitchenwareBlockEntity) server.overworld().getBlockEntity(player.blockPosition().offset(2, 0, 3));
                var staleChoice = kitchen.getGuiElement(1);
                entity.getInventory().setItem(0, ItemStack.EMPTY);
                staleChoice.getGuiCallback().click(1, ClickType.MOUSE_LEFT, ContainerInput.PICKUP, kitchen);
                require(entity.getPreOutput().isEmpty() && kitchen.isOpen(), "Old recipe button cannot start cooking after ingredients change");
                entity.getInventory().setItem(0, new ItemStack(Items.PRISMARINE_SHARD, 2));
                kitchen.onTick();
                click(kitchen, 1);
                require(entity.getPreOutput().getLazyStack().is(Items.BAKED_POTATO)
                        && entity.getInventory().getItem(0).getCount() == 1 && !kitchen.isOpen(),
                        "Selecting a current dish consumes one ingredient and starts the scheduled output");
                require(new KitchenBlockDisplayView(player, RecipeKeyEntry.of(single.getId(), single), null).open(),
                        "Kitchen recipe opens");
            });
            capture(context, "kitchen-recipe", "\ue008", 3);
        } finally {
            world.getServer().runOnServer(server -> type.kitchenRegistries.get(pot).keySet().removeAll(fixtureKeys));
        }
    }

    private void testAltar(ClientGameTestContext context, TestSingleplayerContext world) throws IOException {
        var recipe = new GensokyoAltarRecipe(IngredientStack.of(Items.PRISMARINE_CRYSTALS),
                List.of(IngredientStack.of(Items.AMETHYST_SHARD)), IngredientStack.of(Items.QUARTZ));
        var key = fixture("ritual");
        var type = GensokyoAltarRecipeType.getInstance();
        try {
            world.getServer().runOnServer(server -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                var level = server.overworld();
                var pos = player.blockPosition().offset(8, 0, 8);
                var block = (GensokyoAltarBlock) RDBlocks.GENSOKYO_ALTAR.asBlock();
                var state = block.defaultBlockState();
                level.setBlockAndUpdate(pos, state);
                var missing = block.findStructureIssue(level, pos);
                require(missing != null && missing.y() == 0 && missing.z() == -4, "Identifies the missing altar pillar");
                var firstGui = new GensokyoAltarGui(player, state, level, pos);
                require(firstGui.getGuiElement(44).getItemStack().is(Items.COMPASS), "Altar exposes structure guidance");
                for (int i = 0; i < GensokyoAltarBlock.OFFSETS.length - 1; i++) {
                    int[] offset = GensokyoAltarBlock.OFFSETS[i];
                    for (int y = 0; y < 3; y++) {
                        var pillar = y == 2 ? RDWoodBlocks.BLESSED_SPIRITUAL_LOG.asBlock() : RDWoodBlocks.SPIRITUAL_BUNDLE.strippedLog().asBlock();
                        level.setBlockAndUpdate(pos.offset(offset[0], y, offset[1]), pillar.defaultBlockState());
                    }
                }
                type.add(key, recipe);
                var entity = (GensokyoAltarBlockEntity) level.getBlockEntity(pos);
                require(entity != null, "Altar entity exists");
                entity.getInventory().setItem(8, recipe.getCore().build());
                var issue = type.findSingleSlotFix(GensokyoAltarBlock.readIngredients(entity.getInventory()));
                require(issue != null && issue.index() == 0, "Finds the only missing ritual slot");
                entity.getInventory().setItem(0, recipe.getSlots().getFirst().build());
                var gui = new GensokyoAltarGui(player, state, level, pos);
                require(gui.getGuiElement(44).getItemStack().is(Items.AMETHYST_SHARD) && gui.open(), "Shows a ready altar");
            });
            capture(context, "altar", "\ub002", 5);
            world.getServer().runOnServer(server -> require(new GensokyoAltarDisplayView(
                    server.getPlayerList().getPlayers().getFirst(), RecipeKeyEntry.of(key, recipe), null).open(), "Altar recipe opens"));
            capture(context, "altar-recipe", "\ue007", 5);
        } finally {
            world.getServer().runOnServer(server -> type.remove(key));
        }
    }

    private void testStrength(ClientGameTestContext context, TestSingleplayerContext world) throws IOException {
        var strength = world.getServer().computeOnServer(server -> {
            var player = server.getPlayerList().getPlayers().getFirst();
            var level = server.overworld();
            var pos = player.blockPosition().offset(-3, 0, 3);
            level.setBlockAndUpdate(pos, RDBlocks.STRENGTH_TABLE.asBlock().defaultBlockState());
            var entity = (StrengthenTableBlockEntity) level.getBlockEntity(pos);
            require(entity != null, "Strength table entity exists");
            var item = BuiltInRegistries.ITEM.stream().filter(value -> value instanceof DanmakuItem).findFirst().orElseThrow();
            entity.getInventory().setItem(0, item.getDefaultInstance());
            entity.getInventory().setItem(1, RDItems.SPEED_FEATHER.createStack());
            var gui = new StrengthTableGui(player, entity, false);
            require(gui.open() && gui.getGuiElement(6).getItemStack().is(item), "Strength previews an upgrade");
            require(gui.getGuiElement(6).getItemStack().has(DataComponents.LORE), "Upgrade preview explains the change");
            entity.getInventory().setItem(1, new ItemStack(Items.DIRT));
            gui.onTick();
            require(gui.getGuiElement(6).getItemStack().is(Items.BARRIER), "Wrong material clears stale result");
            entity.getInventory().setItem(1, ItemStack.EMPTY);
            gui.onTick();
            require(gui.getGuiElement(6).getItemStack().is(Items.PAPER), "Missing material gets its own status");
            entity.getInventory().setItem(1, RDItems.SPEED_FEATHER.createStack());
            gui.onTick();
            return gui;
        });
        capture(context, "strength", "\ub004", 1);
        world.getServer().runOnServer(server -> {
            var player = server.getPlayerList().getPlayers().getFirst();
            var entity = (StrengthenTableBlockEntity) server.overworld().getBlockEntity(player.blockPosition().offset(-3, 0, 3));
            var properties = RDDataComponentTypes.DANMAKU_PROPERTIES.value();
            var expected = strength.getGuiElement(6).getItemStack().get(properties);
            click(strength, 6);
            require(entity.getInventory().isEmpty() && expected != null && expected.equals(
                    player.getInventory().getItem(0).get(properties)),
                    "Strength applies the previewed properties and grants the result to the player");
            var item = BuiltInRegistries.ITEM.stream().filter(value -> value instanceof DanmakuItem).findFirst().orElseThrow();
            var recipe = RecipeManager.STRENGTH_TABLE.getMatches(List.of(IngredientStack.of(item),
                    IngredientStack.of(RDItems.SPEED_FEATHER.createStack()))).getFirst();
            require(new StrengthTableDisplayView(player,
                    RecipeKeyEntry.of(fixture("upgrade"), recipe), null).open(), "Strength recipe opens");
        });
        capture(context, "strength-recipe", "\ue009", 1);
    }

    private static Identifier fixture(String name) {
        return Identifier.fromNamespaceAndPath("reverie_dreams_test", name);
    }

    private static void click(SimpleGui gui, int slot) {
        var element = gui.getGuiElement(slot);
        require(element != null, "Expected a clickable item at slot " + slot);
        element.getGuiCallback().click(slot, ClickType.MOUSE_LEFT, ContainerInput.PICKUP, gui);
    }

    private static void capture(ClientGameTestContext context, String name, String glyph, int rows) throws IOException {
        context.waitFor(client -> client.screen instanceof ContainerScreen screen
                && screen.getTitle().getString().contains(glyph) && screen.getMenu().getRowCount() == rows, 200);
        context.getInput().setCursorPos(20, 20);
        context.waitTicks(8);
        Path screenshot = context.takeScreenshot(name);
        String output = System.getProperty("reverie.npcUiClientTest.output");
        if (output != null) {
            Path directory = Files.createDirectories(Path.of(output, "screenshots"));
            Files.copy(screenshot, directory.resolve(name + ".png"), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void closeWorld(ClientGameTestContext context, TestSingleplayerContext world) {
        var server = world.getServer().computeOnServer(current -> current);
        try {
            context.runOnClient(client -> {
                // Keep disconnect from blocking on Fabric GameTest's paused server phase.
                var field = Minecraft.class.getDeclaredField("singleplayerServer");
                field.setAccessible(true);
                field.set(client, null);
                var saving = Component.translatable("menu.savingLevel");
                client.level.disconnect(saving);
                client.disconnect(new GenericMessageScreen(saving), false);
            });
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Cannot close the workstation test world", e);
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
