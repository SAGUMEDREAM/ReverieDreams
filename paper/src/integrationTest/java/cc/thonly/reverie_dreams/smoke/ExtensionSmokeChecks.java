package cc.thonly.reverie_dreams.smoke;

import cc.thonly.reverie_dreams.paper.ReverieDreamsPlugin;
import cc.thonly.reverie_dreams.paper.danmaku.DanmakuModule;
import cc.thonly.reverie_dreams.paper.danmaku.DanmakuMath;
import cc.thonly.reverie_dreams.paper.danmakucrafting.DanmakuCraftingModule;
import cc.thonly.reverie_dreams.paper.danmakucrafting.DanmakuTableController;
import cc.thonly.reverie_dreams.paper.farming.FarmingModule;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.function.BiConsumer;
import cc.thonly.reverie_dreams.paper.specialitems.SpecialItemsModule;
import java.util.Arrays;
import java.util.Random;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.block.entity.SimpleStorageBlockEntityController;
import net.momirealms.craftengine.core.world.BlockPos;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Sheep;

/** Real server checks for source tools, farming and dynamic crafting, never shipped with the plugin. */
final class ExtensionSmokeChecks {
    private static final NamespacedKey SENTINEL = new NamespacedKey("smoke", "unrelated");
    private ExtensionSmokeChecks() { }

    static void verifyDye(BiConsumer<Boolean, String> check, World world, ConfigurationSection recipe) {
        ItemStack original = item(recipe.getString("target"));
        original.setData(DataComponentTypes.DAMAGE, 7);
        original.editMeta(meta -> meta.getPersistentDataContainer().set(SENTINEL, PersistentDataType.STRING, "keep"));
        ItemStack[] matrix = new ItemStack[9];
        matrix[0] = original; matrix[1] = new ItemStack(Material.BLUE_DYE); matrix[2] = new ItemStack(Material.WHITE_DYE);
        ItemStack result = Bukkit.craftItem(matrix, world);
        ItemStack expected = DanmakuModule.service().rules().dye(original, List.of(DyeColor.BLUE, DyeColor.WHITE));
        check.accept(result != null && !result.isEmpty() && result.getAmount() == 1
                        && CraftEngineItems.getCustomItemId(original).equals(CraftEngineItems.getCustomItemId(result))
                        && result.getDataOrDefault(DataComponentTypes.DAMAGE, 0) == 7
                        && "keep".equals(result.getPersistentDataContainer().get(SENTINEL, PersistentDataType.STRING)),
                "native dye preserves bullet identity durability and unrelated metadata: " + recipe.getString("target"));
        check.accept(result.getData(DataComponentTypes.DYED_COLOR) != null
                        && result.getData(DataComponentTypes.DYED_COLOR).color().equals(expected.getData(DataComponentTypes.DYED_COLOR).color()),
                "native dye uses source RGB and brightness blend: " + recipe.getString("target"));
    }

    static void verify(BiConsumer<Boolean, String> check, World world, boolean restart) {
        verifyTools(check);
        verifyWaterproofDye(check, world);
        verifyFarming(check, world, restart);
        verifyTables(check, world, restart);
        verifyStorage(check, world, restart);
        verifySpecialItems(check, world);
    }

    private static void verifyTools(BiConsumer<Boolean, String> check) {
        var tools = ReverieDreamsPlugin.getMod().getTools();
        check.accept(tools.items().size() == 18, "all eighteen original metal and dream tools available");
        for (var rule : tools.items().values()) {
            ItemStack tool = item(rule.id());
            check.accept(tool.getDataOrDefault(DataComponentTypes.MAX_DAMAGE, 0) == rule.durability()
                            && tool.getData(DataComponentTypes.ENCHANTABLE).value() == rule.enchantability(),
                    "native tool durability and enchantability: " + rule.id());
            ItemStack material = item(switch (rule.material()) {
                case "silver" -> "reverie_dreams:silver_ingot";
                case "magic_ice" -> "reverie_dreams:ice_scales";
                case "dream" -> "reverie_dreams:dream_crystal_fragment";
                default -> throw new AssertionError("Unexpected repair material: " + rule.material());
            });
            tool.setData(DataComponentTypes.DAMAGE, rule.durability() / 4);
            var repair = tools.previewRepair(tool, material).orElseThrow();
            check.accept(repair.materialCount() == 1 && repair.result().getDataOrDefault(DataComponentTypes.DAMAGE, 0) == 0,
                    "tool exact quarter repair consumes one original ingredient: " + rule.id());
            check.accept(tools.previewRepair(tool, new ItemStack(Material.IRON_INGOT)).isEmpty(), "tool rejects unrelated repair ingredient: " + rule.id());
        }
    }

    private static void verifyWaterproofDye(BiConsumer<Boolean, String> check, World world) {
        for (String path : List.of("waterproof_hat", "waterproof_clothing", "waterproof_leggings", "waterproof_boots")) {
            ItemStack input = item("reverie_dreams:" + path);
            input.setData(DataComponentTypes.DAMAGE, 3);
            ItemStack[] matrix = new ItemStack[9]; matrix[0] = input; matrix[1] = new ItemStack(Material.RED_DYE);
            ItemStack output = Bukkit.craftItem(matrix, world);
            check.accept(output != null && !output.isEmpty() && CraftEngineItems.getCustomItemId(input).equals(CraftEngineItems.getCustomItemId(output))
                            && output.getDataOrDefault(DataComponentTypes.DAMAGE, 0) == 3 && output.getData(DataComponentTypes.DYED_COLOR) != null,
                    "waterproof armor uses native dye crafting and preserves wear: " + path);
        }
    }

    private static void verifyFarming(BiConsumer<Boolean, String> check, World world, boolean restart) {
        var binding = FarmingModule.bindPaddy();
        check.accept(binding.stateCount() == 8 && binding.moisture() == 7, "all eight paddy moisture states bind to real CE states");
        check.accept(FarmingModule.crops().size() == 11, "all eleven source crops available");
        int i = 0;
        for (var rule : FarmingModule.crops().values()) {
            Block soil = world.getBlockAt((i % 6) * 2, 159, (i / 6) * 2);
            Block crop = soil.getRelative(0, 1, 0); i++;
            if (restart) {
                FarmingModule.assertPaddyRoundTrip(soil.getBlockData());
                check.accept(FarmingModule.age(crop) == rule.maxAge(), "mature crop and real paddy survive restart: " + rule.id());
                check.accept(FarmingModule.harvest(crop, null) && FarmingModule.age(crop) == 0, "mature harvest replants once: " + rule.id());
                CraftEngineBlocks.remove(crop, null, false, false, true);
                CraftEngineBlocks.remove(soil, null, false, false, true);
                soil.getRelative(0, -1, 0).setType(Material.AIR, false);
            } else {
                soil.getRelative(0, -1, 0).setType(Material.STONE, false);
                place(soil, "reverie_dreams:paddy_field");
                place(crop, rule.id());
                check.accept(FarmingModule.age(crop) == 0 && FarmingModule.sourceMoisture(crop) == 1 && FarmingModule.growthDenominator(crop) == 16,
                        "source crop starts at zero; paddy grants no vanilla farmland growth bonus: " + rule.id());
                check.accept(!FarmingModule.tryRandomTick(crop, 8, 0) && !FarmingModule.tryRandomTick(crop, 15, 1)
                                && FarmingModule.tryRandomTick(crop, 15, 0) && FarmingModule.age(crop) == 1,
                        "crop respects original brightness and random growth gates: " + rule.id());
                check.accept(FarmingModule.boneMeal(crop, 3) && FarmingModule.age(crop) == Math.min(4, rule.maxAge()),
                        "crop bone meal uses original one-to-three age gain: " + rule.id());
                FarmingModule.advance(crop, 100);
                check.accept(FarmingModule.age(crop) == rule.maxAge() && !FarmingModule.advance(crop, 1), "crop clamps to original mature age: " + rule.id());
            }
        }
    }

    private static void verifyTables(BiConsumer<Boolean, String> check, World world, boolean restart) {
        var rules = DanmakuCraftingModule.rules();
        check.accept(rules.tableRecipes().size() == 176 && rules.shapeRecipes().size() == 11, "source 176 table recipes and eleven exact drawing matrices loaded");
        for (var row : rules.shapeRecipes()) {
            String type = row.getAsJsonObject("result").getAsJsonObject("components").getAsJsonObject("reverie_dreams:shape").get("id").getAsString();
            var shape = rules.shapeFor(type);
            ItemStack creator = rules.createCreator(type);
            check.accept(rules.draw(creator, shape) != null, "exact six by six drawing creates source recipe: " + type);
            shape[0][0] = !shape[0][0];
            check.accept(rules.draw(creator, shape) == null, "changed drawing cannot silently match original shape: " + type);
        }
        Block tableBlock = world.getBlockAt(12, 160, 2), strengthBlock = world.getBlockAt(14, 160, 2);
        if (!restart) {
            for (Block block : List.of(tableBlock, strengthBlock)) block.getRelative(0, -1, 0).setType(Material.STONE, false);
            place(tableBlock, "reverie_dreams:danmaku_crafting_table"); place(strengthBlock, "reverie_dreams:strength_table");
        }
        var table = DanmakuCraftingModule.controllerAt(tableBlock);
        var strength = DanmakuCraftingModule.controllerAt(strengthBlock);
        check.accept(table != null && table.available() && strength != null && strength.available(), "both crafting tables have independent live CE controllers");
        if (!restart) {
            fill(table, rules.createCreator("amulet"));
            ItemStack[] before = table.snapshot().items().stream().map(v -> v == null ? null : v.clone()).toArray(ItemStack[]::new);
            var full = Bukkit.createInventory(null, 9); for (int n = 0; n < 9; n++) full.setItem(n, new ItemStack(Material.STONE, 64));
            check.accept(table.craftTo(full).status() == DanmakuTableController.Status.INVENTORY_FULL
                            && Arrays.equals(before, table.snapshot().items().toArray(ItemStack[]::new)),
                    "full destination rejects table output without consuming ingredients");
            ItemStack output = table.craft().output();
            check.accept("reverie_dreams:danmaku/amulet/red".equals(id(output)) && table.preview() == null,
                    "table consumes exact original recipe once and produces colored amulet");
            fill(table, rules.createCreator("amulet"));
            ItemStack bullet = DanmakuModule.service().rules().create("amulet", "red");
            var p = DanmakuModule.service().rules().properties(bullet).withDamage(4).withSpeed(1.25f, 1);
            bullet = DanmakuModule.service().rules().configure(bullet, p, DanmakuMath.Config.EMPTY);
            strength.offer(0, bullet); strength.offer(1, rules.createTemplate("fan"));
        } else {
            check.accept(table.preview() != null && table.snapshot().items().get(2).getAmount() == 35, "table exact ingredient quantities survive restart");
            var output = table.craft();
            check.accept(output.status() == DanmakuTableController.Status.SUCCESS && table.craft().status() == DanmakuTableController.Status.NO_RECIPE,
                    "restarted table transfers its result exactly once");
            var upgraded = strength.craft();
            var p = DanmakuModule.service().rules().properties(upgraded.output());
            check.accept(upgraded.status() == DanmakuTableController.Status.SUCCESS && p.damage() == 4 && p.actualSpeed() == 1.25f
                            && p.templateId().equals("reverie_dreams:fan") && strength.preview() == null,
                    "strength table preserves bullet stats while consuming only one template through restart");
            for (Block block : List.of(tableBlock, strengthBlock)) {
                CraftEngineBlocks.remove(block, null, false, false, true); block.getRelative(0, -1, 0).setType(Material.AIR, false);
            }
        }
    }

    private static void fill(DanmakuTableController table, ItemStack creator) {
        ItemStack[] inputs = {new ItemStack(Material.RED_DYE, 4), item("reverie_dreams:danmaku_core"), item("reverie_dreams:power"), item("reverie_dreams:point"), creator};
        inputs[1].setAmount(4); inputs[2].setAmount(35); inputs[3].setAmount(35);
        for (int i = 0; i < inputs.length; i++) if (table.offer(i, inputs[i]) != null) throw new AssertionError("Table rejected source slot " + i);
    }

    private static void verifyStorage(BiConsumer<Boolean, String> check, World world, boolean restart) {
        int index = 0;
        for (String path : List.of("silver_chest", "wooden_box")) {
            Block block = world.getBlockAt(6 + index++ * 4, 170, 4);
            if (!restart) place(block, "reverie_dreams:" + path);
            var entity = BukkitAdaptor.adapt(world).storageWorld()
                    .getBlockEntityAtIfLoaded(new BlockPos(block.getX(), block.getY(), block.getZ()));
            var controller = entity == null ? null : entity.controller.get(SimpleStorageBlockEntityController.class, 0);
            check.accept(controller != null && controller.inventory().getSize() == 27 && controller.inventory().getMaxStackSize() == 99,
                    "native box has original 27 independent slots and accepts source 96-stack coins: " + path);
            var inventory = controller.inventory();
            if (!restart) {
                ItemStack coins = item("reverie_dreams:gold_coin"); coins.setAmount(96);
                coins.editMeta(meta -> meta.getPersistentDataContainer().set(SENTINEL, PersistentDataType.STRING, "stored"));
                inventory.setItem(0, coins); inventory.setItem(26, new ItemStack(Material.DIAMOND, 7));
                entity.world.blockEntityChanged(entity.pos);
                block.getRelative(0, -1, 0).setType(Material.HOPPER, false);
            } else {
                var hopper = (Hopper) block.getRelative(0, -1, 0).getState();
                int coins = 0, extracted = 0;
                for (ItemStack stack : inventory.getContents()) if (id(stack).equals("reverie_dreams:gold_coin")) {
                    coins += stack.getAmount(); check.accept("stored".equals(stack.getPersistentDataContainer().get(SENTINEL, PersistentDataType.STRING)), "stored coin metadata survived: " + path);
                }
                for (ItemStack stack : hopper.getInventory().getContents()) if (id(stack).equals("reverie_dreams:gold_coin")) {
                    coins += stack.getAmount(); extracted += stack.getAmount();
                    check.accept("stored".equals(stack.getPersistentDataContainer().get(SENTINEL, PersistentDataType.STRING)), "hopper coin metadata survived: " + path);
                }
                check.accept(coins == 96 && extracted > 0 && inventory.getItem(26).getType() == Material.DIAMOND && inventory.getItem(26).getAmount() == 7,
                        "native box and real hopper conserve all coins and last slot through restart: " + path);
                hopper.getInventory().clear(); block.getRelative(0, -1, 0).setType(Material.AIR, false);
                inventory.clear(); CraftEngineBlocks.remove(block, null, false, false, true);
            }
        }
    }

    private static void verifySpecialItems(BiConsumer<Boolean, String> check, World world) {
        var service = SpecialItemsModule.service();
        var rows = service.domain().getAsJsonObject("implemented");
        check.accept(rows.size() == 12, "first twelve special abilities have real item definitions");
        for (var entry : rows.entrySet()) {
            var stack = service.create(entry.getKey()); var rule = entry.getValue().getAsJsonObject();
            check.accept(stack != null && entry.getKey().equals(id(stack)), "source special identity resolves: " + entry.getKey());
            if (rule.has("durability")) check.accept(stack.getDataOrDefault(DataComponentTypes.MAX_DAMAGE, 0) == rule.get("durability").getAsInt(), "source special durability: " + entry.getKey());
        }
        for (String name : List.of("hakuroken", "rokanken", "iron_bar", "oni_iron_bar")) {
            var stack = service.create(name); int quarter = stack.getData(DataComponentTypes.MAX_DAMAGE) / 4; stack.setData(DataComponentTypes.DAMAGE, quarter);
            var repair = service.previewRepair(stack, item("reverie_dreams:" + (name.endsWith("bar") ? "oni_horn" : "silver_block")));
            check.accept(repair != null && repair.materialCount() == 1 && repair.result().getDataOrDefault(DataComponentTypes.DAMAGE, 0) == 0,
                    "special weapon quarter repair consumes one original ingredient: " + name);
        }
        check.accept(service.create("iron_bar").getData(DataComponentTypes.WEAPON) == null && service.create("oni_iron_bar").getData(DataComponentTypes.WEAPON) == null,
                "source iron bars retain their original absence of melee weapon wear component");
        var attacker = world.spawn(new Location(world, 12, 190, 8), Sheep.class, entity -> {
            entity.setAI(false); entity.setGravity(false); entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20); entity.setHealth(10);
        });
        var target = world.spawn(new Location(world, 13, 190, 8), Sheep.class, entity -> { entity.setAI(false); entity.setGravity(false); });
        try {
            check.accept(service.applyMelee(attacker, target, service.create("death_scythe"), new Random() { @Override public float nextFloat() { return 0; } })
                            && attacker.getHealth() == 11.5, "death scythe successful native effect heals original one and a half health");
            check.accept(!service.applyMelee(attacker, target, service.create("death_scythe"), new Random() { @Override public float nextFloat() { return .19f; } })
                            && attacker.getHealth() == 11.5, "death scythe rejects exact original nineteen percent threshold");
        } finally { attacker.remove(); target.remove(); }
    }

    private static void place(Block block, String id) {
        if (!CraftEngineBlocks.place(block.getLocation(), CraftEngineBlocks.byId(Key.of(id)).defaultState(), false)) throw new AssertionError("Cannot place " + id);
    }
    private static ItemStack item(String id) { return CraftEngineItems.byId(id).buildBukkitItem(); }
    private static String id(ItemStack item) { var key = item == null ? null : CraftEngineItems.getCustomItemId(item); return key == null ? "" : key.asString(); }
}
