package cc.thonly.reverie_dreams.smoke;

import cc.thonly.reverie_dreams.paper.ReverieDreamsPlugin;
import cc.thonly.reverie_dreams.paper.production.ProductionController;
import cc.thonly.reverie_dreams.paper.production.ProductionModule;
import cc.thonly.reverie_dreams.paper.kitchen.KitchenModule;
import cc.thonly.reverie_dreams.paper.kitchen.KitchenController;
import cc.thonly.reverie_dreams.paper.altar.AltarModule;
import cc.thonly.reverie_dreams.paper.altar.AltarController;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffectType;
import cc.thonly.reverie_dreams.paper.danmaku.DanmakuModule;
import cc.thonly.reverie_dreams.paper.danmaku.DanmakuMath;
import cc.thonly.reverie_dreams.paper.danmaku.DanmakuRules;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Wolf;
import cc.thonly.reverie_dreams.paper.npc.NPCMode;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.StonecuttingRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import io.papermc.paper.datacomponent.DataComponentTypes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import cc.thonly.reverie_dreams.paper.farming.FarmingModule;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.Base64;
import java.util.Objects;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;

/** Only installed by scripts/run_smoke.py into its disposable test world. */
public final class SmokeProbe extends JavaPlugin {
    private final List<String> checks = new ArrayList<>();
    private final Map<String, ConfigurationSection> items = new LinkedHashMap<>();
    private final Map<String, ConfigurationSection> blocks = new LinkedHashMap<>();
    private final Map<String, ConfigurationSection> recipes = new LinkedHashMap<>();
    private final Map<String, String> customTagItems = new LinkedHashMap<>();
    private final Path fixture = Path.of("probe-fixture.yml");
    private final YamlConfiguration result = new YamlConfiguration();
    private World world;
    private boolean restart;
    private boolean finished;
    private ProductionController ice;
    private ProductionController shortBarrel;
    private ProductionController longBarrel;
    private KitchenController shortKitchen;
    private KitchenController longKitchen;
    private KitchenController automatedKitchen;
    private UUID persistentNPC;
    private UUID replacedAvatar;
    private KitchenController fastKitchen;
    private AltarController altar;
    private final List<Sheep> danmakuSubjects = new ArrayList<>();
    private UUID directBullet;
    private UUID lifetimeBullet;

    @Override
    public void onEnable() {
        StorageSpecialSmokeChecks.install(this);
        restart = Files.isRegularFile(fixture);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            world = Bukkit.getWorlds().getFirst();
            world.setChunkForceLoaded(0, 0, true);
            world.getChunkAt(0, 0).load();
            Bukkit.getScheduler().runTaskLater(this, () -> {
                try {
                    if (Files.isRegularFile(Path.of("gensokyo-native-points.json"))) {
                        WorldSmokeChecks.prepare(this).whenComplete((unused, failure) -> Bukkit.getScheduler().runTask(this, () -> {
                            if (failure != null) finish(failure); else verify();
                        }));
                    } else verify();
                } catch (Throwable failure) { finish(failure); }
            }, 20L);
        }, 100L);
    }

    private void verify() {
        restart = Files.isRegularFile(fixture);
        result.set("stage", restart ? "restart" : "initial");
        try {
            require(Bukkit.getPluginManager().isPluginEnabled("ReverieDreams"), "ReverieDreams enabled");
            Path configuration = Path.of("plugins/CraftEngine/resources/reverie_dreams/configuration");
            try (var paths = Files.list(configuration)) {
                for (Path path : paths.filter(p -> p.toString().endsWith(".yml")).sorted().toList()) {
                    YamlConfiguration yaml = new YamlConfiguration();
                    yaml.load(path.toFile());
                    collect(yaml, "items", items);
                    collect(yaml, "blocks", blocks);
                    collect(yaml, "recipes", recipes);
                }
            }
            for (String id : items.keySet()) {
                require(CraftEngineItems.byId(id) != null, "item registered: " + id);
                for (String tag : items.get(id).getStringList("settings.tags")) customTagItems.putIfAbsent(tag, id);
                require(matches(item(id), id, 1), "custom item identity: " + id);
                var nativeFood = items.get(id).getConfigurationSection("data.food");
                if (nativeFood != null) {
                    var food = item(id).getData(DataComponentTypes.FOOD);
                    require(food != null && food.nutrition() == nativeFood.getInt("nutrition")
                                    && Math.abs(food.saturation() - nativeFood.getDouble("saturation")) < 0.00001
                                    && item(id).getData(DataComponentTypes.CONSUMABLE) != null,
                            "native food nutrition saturation and consumption available: " + id);
                }
            }
            for (String id : List.of("copper_coin", "silver_coin", "gold_coin")) require(item("reverie_dreams:" + id).getMaxStackSize() == 96, "coin keeps source stack size 96: " + id);
            for (String id : List.of("role_card", "role_archive", "soul_card")) require(item("reverie_dreams:" + id).getMaxStackSize() == 1, "role item keeps source stack size one: " + id);
            for (String id : blocks.keySet()) require(CraftEngineBlocks.byId(Key.of(id)) != null, "block registered: " + id);
            require(CraftEngineFurniture.byId(Key.of("reverie_dreams:chair")) != null, "chair furniture registered");
            for (String id : items.keySet()) if (items.get(id).getString("behavior.type", "").equals("furniture_item")) {
                require(CraftEngineFurniture.byId(Key.of(id)) != null, "furniture registered: " + id);
            }
            verifyRecipesInBatches();
        } catch (Throwable failure) {
            finish(failure);
        }
    }

    private void verifyAfterRecipes() {
        try {
            verifyMaterialIdentity();
            if (restart) verifyRestoredFixtures();
            else {
                verifyDrops();
                placeFixtures();
            }
            verifyFoodAndProduction();
            verifyNPCs();
            verifyKitchen();
            verifyEquipment();
            verifyAltarAndBook();
            verifyDanmaku();
            ExtensionSmokeChecks.verify(this::require, world, restart);
            NPCAiSmokeChecks.verify(this, this::require, world, restart);
            StorageSpecialSmokeChecks.verify(this::require, world, restart);
            ExpansionSmokeChecks.verify(this::require, world);
            NPCStorageSmokeChecks.verify(this, this::require, world, restart);
            PlantsSmokeChecks.verify(this::require, world, restart);
            WeaponsSmokeChecks.verify(this::require, world, restart);
            if (Files.isRegularFile(Path.of("gensokyo-native-points.json"))) WorldSmokeChecks.verify(this::require);
            require(Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "touhou help"), "Paper touhou help command registered");
            require(Boolean.TRUE.equals(item("reverie_dreams:dream_crystal_fragment").getItemMeta().getEnchantmentGlintOverride()),
                    "dream crystal fragment keeps enchantment glint");
            startFurnaceChecks();
            if (!restart) verifyPluginTickFreeze();
        } catch (Throwable failure) {
            finish(failure);
        }
    }

    private void verifyRecipesInBatches() {
        var pending = recipes.entrySet().iterator();
        new BukkitRunnable() {
            @Override public void run() {
                try {
                    // Keep actual crafting on the main thread, but bound each test heartbeat.
                    for (int i = 0; i < 12 && pending.hasNext(); i++) verifyRecipe(pending.next());
                    if (pending.hasNext()) return;
                    cancel();
                    // Restored projectiles must not age out while unrelated source-map or recipe tests run.
                    world.setChunkForceLoaded(32, 0, true);
                    world.getChunkAt(32, 0).load();
                    Bukkit.getScheduler().runTaskLater(SmokeProbe.this, SmokeProbe.this::verifyAfterRecipes, 20L);
                } catch (Throwable failure) { cancel(); finish(failure); }
            }
        }.runTaskTimer(this, 1L, 1L);
    }

    private void verifyRecipe(Map.Entry<String, ConfigurationSection> entry) {
            String id = entry.getKey();
            ConfigurationSection recipe = entry.getValue();
            var registered = Bukkit.getRecipe(NamespacedKey.fromString(id));
            require(registered != null, "recipe registered: " + id);
            String type = recipe.getString("type");
            if (type.equals("dye")) {
                ExtensionSmokeChecks.verifyDye(this::require, world, recipe);
            } else if (type.equals("smelting") || type.equals("blasting")) {
                require(registered instanceof CookingRecipe<?>, "cooking recipe type: " + id);
                CookingRecipe<?> cooking = (CookingRecipe<?>) registered;
                require(cooking.getCookingTime() == recipe.getInt("time")
                        && Math.abs(cooking.getExperience() - recipe.getDouble("experience")) < 0.00001,
                        "source cooking time and experience: " + id);
            } else if (type.equals("stonecutting")) {
                require(registered instanceof StonecuttingRecipe, "stonecutter recipe type: " + id);
                StonecuttingRecipe cutting = (StonecuttingRecipe) registered;
                require(cutting.getInputChoice().test(item(recipe.getString("ingredients")))
                                && matches(cutting.getResult(), recipe.getString("result.id"), recipe.getInt("result.count")),
                        "source stonecutting input and output: " + id);
            } else {
                ItemStack[] matrix = new ItemStack[9];
                if (type.equals("shaped")) {
                    List<String> pattern = recipe.getStringList("pattern");
                    ConfigurationSection ingredients = recipe.getConfigurationSection("ingredients");
                    for (int row = 0; row < pattern.size(); row++) {
                        for (int column = 0; column < pattern.get(row).length(); column++) {
                            char symbol = pattern.get(row).charAt(column);
                            if (symbol != ' ') {
                                String ingredient = ingredients.getString(String.valueOf(symbol));
                                matrix[row * 3 + column] = ingredient.startsWith("#")
                                        ? tagRepresentative(ingredient) : item(ingredient);
                            }
                        }
                    }
                } else {
                    List<String> ingredients = recipe.getStringList("ingredients");
                    for (int i = 0; i < ingredients.size(); i++) {
                        String ingredient = ingredients.get(i);
                        matrix[i] = ingredient.startsWith("#") ? tagRepresentative(ingredient) : item(ingredient);
                    }
                }
                ItemStack output = Bukkit.craftItem(matrix, world);
                if (id.equals("reverie_dreams:kawara_tiles") && matches(output, "minecraft:bricks", 1)) {
                    require(matches(registered.getResult(), recipe.getString("result.id"), recipe.getInt("result.count")),
                            "original kawara recipe retains output despite vanilla bricks collision");
                    require(recipes.containsKey("reverie_dreams:compat/kawara_tiles_from_bricks"),
                            "kawara has equal-cost stonecutting entry for vanilla recipe collision");
                } else if (id.equals("reverie_dreams:yotsume_gaki") && matches(output, "minecraft:oak_fence", 3)) {
                    require(matches(registered.getResult(), recipe.getString("result.id"), recipe.getInt("result.count")),
                            "original yotsume recipe retains output despite vanilla oak fence collision");
                    require(recipes.containsKey("reverie_dreams:compat/yotsume_gaki_from_oak_fence"),
                            "yotsume has equal-cost oak fence conversion for vanilla recipe collision");
                } else if (id.equals("reverie_dreams:stepping_stone") && matches(output, "minecraft:stone_pressure_plate", 1)) {
                    require(matches(registered.getResult(), recipe.getString("result.id"), recipe.getInt("result.count")), "source stepping stone recipe retains original result");
                    ItemStack[] conversion = new ItemStack[9]; conversion[0] = new ItemStack(Material.STONE_PRESSURE_PLATE);
                    ItemStack converted = Bukkit.craftItem(conversion, world);
                    require(matches(converted, "reverie_dreams:stepping_stone", 4) && converted.getAmount() * output.getAmount() == recipe.getInt("result.count"), "pressure plate conversion preserves exact stepping stone material cost");
                } else {
                    String collision = cushionCollision(id, output);
                    if (collision == null) {
                        require(matches(output, recipe.getString("result.id"), recipe.getInt("result.count")),
                                "actual crafting output: " + id);
                    } else {
                        require(matches(registered.getResult(), recipe.getString("result.id"), recipe.getInt("result.count")),
                                "source cushion or curtain recipe retains output despite identical-pattern collision: " + id);
                        String local = id.substring(id.indexOf(':') + 1);
                        String compat = "reverie_dreams:compat/" + local + "_from_" + collision.substring(collision.indexOf(':') + 1);
                        require(recipes.containsKey(compat), "identical-pattern collision has equal-cost conversion: " + compat);
                        var conversion = Bukkit.getRecipe(NamespacedKey.fromString(compat));
                        require(conversion instanceof StonecuttingRecipe cutting
                                        && cutting.getInputChoice().test(item(collision))
                                        && matches(cutting.getResult(), recipe.getString("result.id"), 1),
                                "cushion and curtain stonecutter conversion preserves the source result at equal cost: " + id);
                    }
                }
            }
    }

    private static final Map<String, Set<String>> CUSHION_COLLISIONS = Map.ofEntries(
            Map.entry("reverie_dreams:zabuton", Set.of("reverie_dreams:white_zabuton", "reverie_dreams:noren_white")),
            Map.entry("reverie_dreams:white_zabuton", Set.of("reverie_dreams:zabuton", "reverie_dreams:noren_white")),
            Map.entry("reverie_dreams:noren_white", Set.of("reverie_dreams:zabuton", "reverie_dreams:white_zabuton")),
            Map.entry("reverie_dreams:red_zabuton", Set.of("reverie_dreams:noren_red")),
            Map.entry("reverie_dreams:noren_red", Set.of("reverie_dreams:red_zabuton")),
            Map.entry("reverie_dreams:black_zabuton", Set.of("reverie_dreams:noren_black")),
            Map.entry("reverie_dreams:noren_black", Set.of("reverie_dreams:black_zabuton")),
            Map.entry("reverie_dreams:brown_zabuton", Set.of("reverie_dreams:noren_brown")),
            Map.entry("reverie_dreams:noren_brown", Set.of("reverie_dreams:brown_zabuton")),
            Map.entry("reverie_dreams:green_zabuton", Set.of("reverie_dreams:noren_green")),
            Map.entry("reverie_dreams:noren_green", Set.of("reverie_dreams:green_zabuton")),
            Map.entry("reverie_dreams:blue_zabuton", Set.of("reverie_dreams:noren_indigo")),
            Map.entry("reverie_dreams:noren_indigo", Set.of("reverie_dreams:blue_zabuton")));

    private static String cushionCollision(String id, ItemStack output) {
        var collisions = CUSHION_COLLISIONS.get(id);
        if (collisions == null) return null;
        var actual = CraftEngineItems.getCustomItemId(output);
        if (actual == null) return null;
        String actualId = actual.asString();
        return collisions.contains(actualId) ? actualId : null;
    }

    private void verifyMaterialIdentity() {
        for (Material material : List.of(Material.IRON_INGOT, Material.IRON_NUGGET, Material.EMERALD)) {
            ItemStack[] matrix = new ItemStack[9];
            for (int i = 0; i < matrix.length; i++) matrix[i] = new ItemStack(material);
            ItemStack output = Bukkit.craftItem(matrix, world);
            require(output.isEmpty() || CraftEngineItems.getCustomItemId(output) == null,
                    "vanilla material cannot create custom resources: " + material);
        }
        ItemStack[] matrix = new ItemStack[9];
        for (int i = 0; i < matrix.length; i++) matrix[i] = item(i == 0 ? "reverie_dreams:blue_orb" : "reverie_dreams:red_orb");
        ItemStack output = Bukkit.craftItem(matrix, world);
        require(output.isEmpty() || CraftEngineItems.getCustomItemId(output) == null, "different orb colors cannot be mixed for compression");
        matrix = new ItemStack[9];
        matrix[0] = new ItemStack(Material.PAPER);
        output = Bukkit.craftItem(matrix, world);
        require(output.isEmpty() || CraftEngineItems.getCustomItemId(output) == null, "vanilla paper cannot be decompressed as a custom block");
    }

    private void verifyDrops() {
        for (String id : List.of("silver_ore", "deepslate_silver_ore")) {
            require(validDrops(drops(id), Set.of("reverie_dreams:raw_silver"), 1, 1), "ore drops raw silver: " + id);
        }
        Map<String, String> moonDrops = Map.of("moon_iron_ore", "raw_iron", "moon_gold_ore", "raw_gold",
                "moon_diamond_ore", "diamond", "moon_quartz_ore", "quartz");
        for (var entry : moonDrops.entrySet()) {
            require(validDrops(drops(entry.getKey()), Set.of("minecraft:" + entry.getValue()), 1, 1),
                    "moon ore drops original vanilla resource: " + entry.getKey());
        }
        Set<String> colors = Set.of("reverie_dreams:red_orb", "reverie_dreams:blue_orb", "reverie_dreams:yellow_orb",
                "reverie_dreams:green_orb", "reverie_dreams:purple_orb");
        for (String id : List.of("orb_ore", "deepslate_orb_ore")) {
            for (int i = 0; i < 24; i++) {
                if (!validDrops(drops(id), colors, 1, 6)) throw new AssertionError("Invalid orb loot: " + id);
            }
            checks.add("24 orb loot samples preserve guaranteed drop and allowed range: " + id);
        }
        for (int i = 0; i < 24; i++) {
            if (!validDrops(drops("dream_crystal_ore"), Set.of("reverie_dreams:dream_crystal_fragment"), 1, 3)) {
                throw new AssertionError("Invalid dream crystal loot");
            }
        }
        checks.add("24 dream crystal loot samples preserve guaranteed drop and allowed range");
        for (String id : blocks.keySet()) {
            if (id.endsWith("_block")) require(validDrops(drops(id.substring(id.indexOf(':') + 1)), Set.of(id), 1, 1), "storage block drops itself: " + id);
        }
    }

    private List<ItemStack> drops(String path) {
        Location location = new Location(world, 7, 82, 7);
        for (Item entity : world.getEntitiesByClass(Item.class)) {
            if (entity.getLocation().distanceSquared(location) < 9) entity.remove();
        }
        if (!CraftEngineBlocks.place(location, Key.of("reverie_dreams:" + path), false)
                || !CraftEngineBlocks.remove(location.getBlock(), null, false, true, false)) {
            throw new AssertionError("Cannot place/remove loot fixture: " + path);
        }
        List<ItemStack> drops = new ArrayList<>();
        for (Item entity : world.getEntitiesByClass(Item.class)) {
            if (entity.getLocation().distanceSquared(location) < 9) {
                drops.add(entity.getItemStack().clone());
                entity.remove();
            }
        }
        return drops;
    }

    private static boolean validDrops(List<ItemStack> drops, Set<String> allowed, int minimum, int maximum) {
        int count = 0;
        for (ItemStack drop : drops) {
            Key id = CraftEngineItems.getCustomItemId(drop);
            String name = id == null ? drop.getType().getKey().toString() : id.asString();
            if (!allowed.contains(name)) return false;
            count += drop.getAmount();
        }
        return count >= minimum && count <= maximum;
    }

    private void placeFixtures() throws Exception {
        int index = 0;
        List<String> placedBlocks = new ArrayList<>();
        for (String id : blocks.keySet()) {
            // Crop persistence is exercised separately on its real source soil.
            if (FarmingModule.crops().containsKey(id)) continue;
            if (PlantsSmokeChecks.plant(id)) continue;
            Location location = fixtureLocation(index++);
            location.clone().subtract(0, 1, 0).getBlock().setType(Material.STONE);
            require(CraftEngineBlocks.place(location, Key.of(id), false) && CraftEngineBlocks.isCustomBlock(location.getBlock()),
                    "persistent fixture placed: " + id);
            placedBlocks.add(id);
        }
        world.getBlockAt(2, 79, 2).setType(Material.STONE);
        var placed = CraftEngineFurniture.place(new Location(world, 2.5, 80, 2.5), Key.of("reverie_dreams:chair"), "ground", false);
        require(placed != null && placed.isValid(), "chair fixture placed");
        Entity chair = world.getEntities().stream().filter(CraftEngineFurniture::isFurniture)
                .filter(entity -> CraftEngineFurniture.getLoadedFurnitureByMetaEntity(entity) == placed)
                .findFirst().orElseThrow(() -> new AssertionError("Missing chair meta entity"));
        YamlConfiguration saved = new YamlConfiguration();
        saved.set("chair_uuid", chair.getUniqueId().toString());
        saved.set("blocks", placedBlocks);
        int indexFurniture = 0;
        for (String id : items.keySet()) {
            if (id.equals("reverie_dreams:chair") || !items.get(id).getString("behavior.type", "").equals("furniture_item")) continue;
            Location location = new Location(world, indexFurniture % 8 * 2 + .5, 84, indexFurniture / 8 * 2 + .5, indexFurniture % 16 * 22.5f, 0);
            location.clone().subtract(0, 1, 0).getBlock().setType(Material.STONE);
            var furniture = CraftEngineFurniture.place(location, Key.of(id), "ground", false);
            require(furniture != null && furniture.isValid(), "native furniture placed: " + id);
            Entity meta = world.getEntities().stream().filter(CraftEngineFurniture::isFurniture)
                    .filter(entity -> CraftEngineFurniture.getLoadedFurnitureByMetaEntity(entity) == furniture).findFirst().orElseThrow();
            saved.set("furniture_meta." + id, meta.getUniqueId().toString());
            indexFurniture++;
        }
        saved.save(fixture.toFile());
    }

    private void verifyRestoredFixtures() {
        YamlConfiguration saved = YamlConfiguration.loadConfiguration(fixture.toFile());
        int index = 0;
        for (String id : saved.getStringList("blocks")) {
            var block = fixtureLocation(index++).getBlock();
            var state = CraftEngineBlocks.getCustomBlockState(block);
            require(state != null && state.owner().value() == CraftEngineBlocks.byId(Key.of(id)), "block survived restart: " + id);
            require(CraftEngineBlocks.remove(block), "restored block removed: " + id);
        }
        Entity chair = Bukkit.getEntity(UUID.fromString(saved.getString("chair_uuid")));
        require(chair != null && CraftEngineFurniture.isFurniture(chair)
                && CraftEngineFurniture.getLoadedFurnitureByMetaEntity(chair) != null, "chair survived restart");
        require(CraftEngineFurniture.remove(chair, true, false), "restored chair removed");
        ConfigurationSection furniture = saved.getConfigurationSection("furniture_meta");
        if (furniture != null) for (String id : furniture.getKeys(false)) {
            Entity meta = Bukkit.getEntity(UUID.fromString(furniture.getString(id)));
            require(meta != null && CraftEngineFurniture.isFurniture(meta) && CraftEngineFurniture.getLoadedFurnitureByMetaEntity(meta) != null,
                    "Fumo furniture survived restart: " + id);
            require(CraftEngineFurniture.remove(meta, true, false), "restored Fumo removed: " + id);
        }
    }

    private void startFurnaceChecks() {
        for (int x : List.of(4, 5)) {
            world.getBlockAt(x, 80, 4).setType(Material.AIR);
            world.getBlockAt(x, 80, 4).setType(x == 4 ? Material.FURNACE : Material.BLAST_FURNACE);
            Furnace furnace = (Furnace) world.getBlockAt(x, 80, 4).getState();
            furnace.getInventory().setSmelting(item("reverie_dreams:raw_silver"));
            furnace.getInventory().setFuel(new ItemStack(Material.COAL, 2));
        }
        new BukkitRunnable() {
            private int ticks;

            @Override
            public void run() {
                try {
                    ticks++;
                    boolean done = true;
                    for (int x : List.of(4, 5)) {
                        Furnace furnace = (Furnace) world.getBlockAt(x, 80, 4).getState();
                        done &= matches(furnace.getInventory().getResult(), "reverie_dreams:silver_ingot", 1);
                    }
                    if (done) {
                        checks.add("real furnace and blast furnace produce custom silver ingots");
                        result.set("furnace_poll_ticks", ticks);
                        cancel();
                        checkProductionCompletion();
                    } else if (ticks >= 400) {
                        throw new AssertionError("Furnace or blast furnace did not produce custom silver within 400 ticks");
                    }
                } catch (Throwable failure) {
                    cancel();
                    finish(failure);
                }
            }
        }.runTaskTimer(this, 1L, 1L);
    }

    private void finish(Throwable failure) {
        if (finished) return;
        finished = true;
        Bukkit.getScheduler().cancelTasks(this);
        for (World loaded : Bukkit.getWorlds()) loaded.removePluginChunkTickets(this);
        if (world != null) {
            world.setChunkForceLoaded(0, 0, false);
            world.setChunkForceLoaded(32, 0, false);
        }
        result.set("status", failure == null ? "PASS" : "FAIL");
        result.set("checks", checks);
        if (failure != null) {
            result.set("failure", failure.toString());
            getSLF4JLogger().error("Migration smoke probe failed", failure);
        }
        try {
            world.save();
            result.save(Path.of(restart ? "probe-restart-result.yml" : "probe-initial-result.yml").toFile());
        } catch (Exception saveFailure) {
            getSLF4JLogger().error("Cannot save smoke result", saveFailure);
        }
        getSLF4JLogger().info("Migration smoke probe {}: {} checks", result.getString("status"), checks.size());
    }

    private static void collect(YamlConfiguration yaml, String name, Map<String, ConfigurationSection> output) {
        ConfigurationSection section = yaml.getConfigurationSection(name);
        if (section != null) for (String id : section.getKeys(false)) {
            if (output.putIfAbsent(id, section.getConfigurationSection(id)) != null) throw new AssertionError("Duplicate " + name + ": " + id);
        }
    }

    private Location fixtureLocation(int index) {
        if (index >= 256) throw new AssertionError("Block fixture grid exceeds forced chunk");
        return new Location(world, index % 16, 100 + index / 256 * 3, index / 16);
    }

    private static ItemStack representative(RecipeChoice choice) {
        if (choice instanceof RecipeChoice.ExactChoice exact) return exact.getChoices().getFirst().clone();
        if (choice instanceof RecipeChoice.MaterialChoice materials) return new ItemStack(materials.getChoices().getFirst());
        throw new AssertionError("Unsupported recipe ingredient choice: " + choice);
    }

    private ItemStack tagRepresentative(String reference) {
        String tagName = reference.substring(1);
        String custom = customTagItems.get(tagName);
        if (custom != null) return item(custom);
        var tag = Bukkit.getTag(Tag.REGISTRY_ITEMS, NamespacedKey.fromString(tagName), Material.class);
        if (tag == null || tag.getValues().isEmpty()) throw new AssertionError("Unknown or empty item tag: " + reference);
        return new ItemStack(tag.getValues().stream().sorted().findFirst().orElseThrow());
    }

    private void verifyFoodAndProduction() throws Exception {
        var food = ReverieDreamsPlugin.getMod().getFood();
        var saved = YamlConfiguration.loadConfiguration(fixture.toFile());
        if (!restart) {
            Sheep subject = world.spawn(new Location(world, 13.5, 80, 13.5), Sheep.class, sheep -> {
                sheep.setAI(false);
                sheep.setGravity(false);
                sheep.setInvulnerable(true);
                sheep.setRemoveWhenFarAway(false);
                sheep.setPersistent(true);
            });
            var milk = food.apply(subject, item("reverie_dreams:beverage/milk"));
            require(milk.alcoholProtectionTicks() == 1500 && food.protectionTicks(subject) == 1500, "milk grants original 1500 tick alcohol protection");
            saved.set("food_subject", subject.getUniqueId().toString());
            ice = placeMachine(10, "ice_making_machine");
            shortBarrel = placeMachine(11, "brewing_barrel");
            longBarrel = placeMachine(512, "brewing_barrel");
            require(ice.startIce() && ice.snapshot().ticksRemaining() == 160 && ice.snapshot().iceOutputs() == 8,
                    "ice maker reserves eight outputs with original 160 tick duration");
            require(ice.extractOutput() == null && !ice.startIce(), "ice maker cannot extract early or accept overlapping work");
            shortBarrel.offerInput(0, new ItemStack(Material.SNOWBALL));
            shortBarrel.offerInput(1, new ItemStack(Material.ICE));
            shortBarrel.offerInput(2, new ItemStack(Material.COCOA_BEANS));
            require(!shortBarrel.startBrewing(), "barrel recipe rejects wrong ordered prefix");
            shortBarrel.takeInput(0);
            shortBarrel.takeInput(1);
            shortBarrel.offerInput(0, new ItemStack(Material.ICE));
            shortBarrel.offerInput(1, new ItemStack(Material.SNOWBALL));
            require(shortBarrel.startBrewing() && shortBarrel.snapshot().totalTicks() == 60, "ordered affgado recipe starts with original 60 ticks");
            for (int slot = 0; slot < 3; slot++) {
                ItemStack remainder = longBarrel.offerInput(slot, new ItemStack(Material.WHEAT_SEEDS, 3));
                require(remainder != null && remainder.getAmount() == 2, "barrel accepts one per slot without losing remainder: " + slot);
            }
            require(longBarrel.startBrewing() && longBarrel.snapshot().totalTicks() == 840, "beer uses original 840 tick timer");
            require(longBarrel.takeInput(0) == null && longBarrel.extractOutput() == null, "working barrel locks consumed inputs and premature output");
            saved.save(fixture.toFile());
        } else {
            Entity subject = Bukkit.getEntity(UUID.fromString(saved.getString("food_subject")));
            require(subject instanceof Sheep && food.protectionTicks((Sheep) subject) > 0 && food.protectionTicks((Sheep) subject) < 1500,
                    "alcohol protection timer survives restart and continues ticking");
            subject.remove();
            ice = ProductionModule.controllerAt(world.getBlockAt(10, 80, 12));
            shortBarrel = ProductionModule.controllerAt(world.getBlockAt(11, 80, 12));
            longBarrel = ProductionModule.controllerAt(world.getBlockAt(512, 80, 12));
            require(ice != null && ice.snapshot().iceOutputs() == 1 && ice.comparatorSignal() == 2,
                    "ice remaining reserved output and comparator survive real CE chunk save/load");
            require(shortBarrel != null && shortBarrel.snapshot().servings() == 0, "drained barrel remains empty after restart");
            require(longBarrel != null && longBarrel.snapshot().ticksRemaining() > 0 && longBarrel.snapshot().ticksRemaining() < saved.getInt("beer_remaining"),
                    "in-progress barrel retains recipe and remaining ticks through real controller persistence");
            require(longBarrel.snapshot().recipeId().equals("reverie_dreams:beverage-beer"), "barrel recipe ID survived restart");
        }
    }

    private ProductionController placeMachine(int x, String id) {
        Location location = new Location(world, x, 80, 12);
        location.clone().subtract(0, 1, 0).getBlock().setType(Material.STONE);
        require(CraftEngineBlocks.place(location, Key.of("reverie_dreams:" + id), false), "production block placed: " + x);
        ProductionController controller = ProductionModule.controllerAt(location.getBlock());
        require(controller != null && controller.available(), "per-position controller available: " + x);
        return controller;
    }

    private void verifyNPCs() throws Exception {
        var service = ReverieDreamsPlugin.getMod().getNPC().service();
        var saved = YamlConfiguration.loadConfiguration(fixture.toFile());
        UUID owner = UUID.fromString("bd1158d8-8941-4cef-baf8-c98f2ebf3912");
        require(service.roles().size() == 110, "all 110 original role domain records loaded");
        UUID npcId;
        if (!restart) {
            var role = service.roles().stream().filter(candidate -> candidate.textureKey() != null).findFirst().orElseThrow();
            for (int x = 6; x <= 10; x++) for (int z = 11; z <= 15; z++) world.getBlockAt(x, 79, z).setType(Material.STONE);
            npcId = service.spawnRole(role.id(), new Location(world, 8.5, 80, 13.5));
            require(service.recruit(npcId, owner), "trusted NPC recruitment assigns owner");
            service.stay(npcId, owner);
            ItemStack[] inventory = new ItemStack[24];
            inventory[0] = item("reverie_dreams:silver_ingot");
            inventory[0].setAmount(7);
            inventory[18] = new ItemStack(Material.DIAMOND_HELMET);
            inventory[22] = new ItemStack(Material.DIAMOND_SWORD);
            inventory[23] = new ItemStack(Material.SHIELD);
            service.storeInventory(npcId, owner, inventory);
            inventory[0].setAmount(1);
            require(service.getInventory(npcId, owner)[0].getAmount() == 7, "NPC inventory owns clones instead of caller references");
            boolean refused = false;
            try { service.storeInventory(npcId, UUID.randomUUID(), new ItemStack[24]); }
            catch (SecurityException expected) { refused = true; }
            require(refused, "NPC inventory rejects other owners");
            saved.set("npc_uuid", npcId.toString());
            saved.set("npc_role", role.id());
            saved.save(fixture.toFile());
        } else npcId = UUID.fromString(saved.getString("npc_uuid"));
        var snapshot = service.inspect(npcId);
        require(snapshot.roleId().equals(saved.getString("npc_role")) && owner.equals(snapshot.ownerId()) && snapshot.mode() == NPCMode.STAY,
                "NPC role owner and stay mode retained: " + (restart ? "restart" : "initial"));
        require(snapshot.inventory().length == 24 && matches(snapshot.inventory()[0], "reverie_dreams:silver_ingot", 7)
                        && snapshot.inventory()[18].getType() == Material.DIAMOND_HELMET
                        && snapshot.inventory()[22].getType() == Material.DIAMOND_SWORD
                        && snapshot.inventory()[23].getType() == Material.SHIELD,
                "NPC 24-slot storage and equipment retained: " + (restart ? "restart" : "initial"));
        Entity carrier = Bukkit.getEntity(npcId);
        Entity avatar = Bukkit.getEntity(snapshot.avatarId());
        require(carrier instanceof Wolf wolf && wolf.isInvisible() && avatar instanceof Mannequin
                        && carrier.getPassengers().contains(avatar), "NPC has invisible carrier and persistent humanoid avatar");
        var role = service.roles().stream().filter(candidate -> candidate.id().equals(snapshot.roleId())).findFirst().orElseThrow();
        require(((Mannequin) avatar).getProfile().equals(role.profile()), "mannequin uses original resource-pack skin profile");
        require(!role.profile().dynamic(), "resource-pack skin profile is static and needs no remote resolution");
        require(service.canonicalId(avatar).orElseThrow().equals(npcId), "avatar interaction resolves to canonical NPC identity");
        persistentNPC = npcId;
        if (!restart) {
            replacedAvatar = avatar.getUniqueId();
            ((Mannequin) avatar).setHealth(0);
        } else ((Wolf) carrier).setHealth(0);
    }

    private void checkProductionCompletion() throws Exception {
        StorageSpecialSmokeChecks.complete(this::require, world, restart);
        ExpansionSmokeChecks.complete(this::require, world);
        NPCStorageSmokeChecks.complete(this::require, world, restart);
        WeaponsSmokeChecks.complete(this::require);
        PlantsSmokeChecks.complete(this::require, world);
        if (!restart) {
            var service = ReverieDreamsPlugin.getMod().getNPC().service();
            var npc = service.inspect(persistentNPC);
            require(!npc.avatarId().equals(replacedAvatar) && Bukkit.getEntity(npc.avatarId()) instanceof Mannequin,
                    "independent mannequin death repairs appearance without killing canonical NPC");
            require(world.getEntitiesByClass(Item.class).stream().noneMatch(entity -> Set.of(Material.DIAMOND_HELMET, Material.DIAMOND_SWORD, Material.SHIELD).contains(entity.getItemStack().getType())),
                    "mannequin cannot duplicate copied equipment on independent death");
            require(ice.snapshot().ticksRemaining() == 0 && ice.comparatorSignal() == 15, "real ice maker tick completes with full comparator signal");
            for (int i = 0; i < 7; i++) require(validDrops(List.of(ice.extractOutput()), Set.of("minecraft:ice", "minecraft:snowball", "reverie_dreams:magic_ice"), 1, 1), "ice output allowed member: " + i);
            require(ice.snapshot().iceOutputs() == 1 && ice.comparatorSignal() == 2, "partial ice extraction updates comparator without restarting work");
            require(shortBarrel.snapshot().servings() == 9, "barrel yields nine bottled servings");
            for (int i = 0; i < 9; i++) require(matches(shortBarrel.extractOutput(), "reverie_dreams:beverage/affgado", 1), "affgado serving: " + i);
            require(shortBarrel.extractOutput() == null, "barrel cannot duplicate tenth serving");
            require(longBarrel.snapshot().ticksRemaining() > 0, "long recipe still running before save");
            require(shortKitchen.snapshot().ticksRemaining() == 0 && matches(shortKitchen.extractOutput(64), "reverie_dreams:cuisine/dew_boiled_eggs", 1),
                    "native kitchen ticker produces original dew boiled eggs");
            require(shortKitchen.extractOutput(64) == null, "kitchen cannot duplicate extracted meal");
            Hopper hopper = (Hopper) world.getBlockAt(9, 79, 12).getState();
            require(matches(hopper.getInventory().getItem(0), "reverie_dreams:cuisine/dew_boiled_eggs", 1)
                            && automatedKitchen.item(5) == null, "real vanilla hopper pulls output through CE native kitchen container");
            require(longKitchen.working() && longKitchen.snapshot().ticksRemaining() > 0, "long kitchen recipe still processing before chunk unload");
            require(matches(fastKitchen.extractOutput(64), "reverie_dreams:cuisine/dew_boiled_eggs", 1), "remembered recipe uses real kitchen ticking and original output");
            verifyDanmakuCompletion();
            var saved = YamlConfiguration.loadConfiguration(fixture.toFile());
            saved.set("beer_remaining", longBarrel.snapshot().ticksRemaining());
            saved.set("kitchen_remaining", longKitchen.snapshot().ticksRemaining());
            var bullets = DanmakuModule.service();
            ItemStack resume = bullets.rules().create("amulet", "red");
            resume = bullets.rules().configure(resume, bullets.rules().properties(resume).withSpeed(.01f, 1).withDamage(0), DanmakuMath.Config.EMPTY);
            UUID resumeId = bullets.launch(null, resume, new Location(world, 514.5, 150, 12.5), 0, 0).getFirst();
            saved.set("danmaku_resume", resumeId.toString());
            WeaponsSmokeChecks.prepareResume(world, saved);
            saved.save(fixture.toFile());
            world.setChunkForceLoaded(32, 0, false);
            world.unloadChunkRequest(32, 0);
            new BukkitRunnable() {
                int ticks;
                @Override public void run() {
                    try {
                        if (!world.isChunkLoaded(32, 0)) {
                            require(longKitchen.snapshot().ticksRemaining() > 0 && longBarrel.snapshot().ticksRemaining() > 0,
                                    "working kitchen and barrel chunk unloaded with progress remaining");
                            cancel();
                            finish(null);
                        } else if (++ticks > 120) {
                            throw new AssertionError("Test chunk remains loaded after unload request; forced=" + world.isChunkForceLoaded(32, 0)
                                    + ", plugin tickets=" + world.getPluginChunkTickets(32, 0));
                        }
                    } catch (Throwable failure) { cancel(); finish(failure); }
                }
            }.runTaskTimer(this, 1, 1);
        } else {
            new BukkitRunnable() {
                int ticks;
                @Override public void run() {
                    try {
                        if (longBarrel.snapshot().ticksRemaining() == 0 && !longKitchen.working()) {
                            var service = ReverieDreamsPlugin.getMod().getNPC().service();
                            Item archive = world.getEntitiesByClass(Item.class).stream().filter(entity -> service.isArchive(entity.getItemStack())).findFirst().orElseThrow();
                            var archived = service.inspectArchive(archive.getItemStack());
                            require(persistentNPC.equals(archived.entityId()) && archived.inventory().length == 24
                                            && matches(archived.inventory()[0], "reverie_dreams:silver_ingot", 7)
                                            && archived.inventory()[23].getType() == Material.SHIELD,
                                    "canonical NPC death archives complete original inventory exactly once");
                            archive.remove();
                            require(longBarrel.snapshot().servings() == 9, "resumed beer finishes with nine servings");
                            for (int i = 0; i < 9; i++) require(matches(longBarrel.extractOutput(), "reverie_dreams:beverage/beer", 1), "resumed beer serving: " + i);
                            require(longBarrel.extractOutput() == null, "resumed barrel cannot duplicate output");
                            require(ice.extractOutput() != null && ice.extractOutput() == null, "restored ice output extracted exactly once");
                            require(matches(longKitchen.extractOutput(64), "reverie_dreams:cuisine/oedo_boat_festival", 1), "kitchen resumes saved recipe and produces original meal");
                            require(longKitchen.extractOutput(64) == null, "resumed kitchen output transfers exactly once");
                            for (int x : List.of(8, 9, 10, 11, 14, 512, 513)) require(CraftEngineBlocks.remove(world.getBlockAt(x, 80, 12)), "drained machine removal: " + x);
                            require(CraftEngineBlocks.remove(world.getBlockAt(8, 70, 8)), "drained altar removed");
                            cancel();
                            finish(null);
                        } else if (++ticks > 900) throw new AssertionError("Resumed beer timer stalled");
                    } catch (Throwable failure) { cancel(); finish(failure); }
                }
            }.runTaskTimer(this, 1, 1);
        }
    }

    private void verifyKitchen() throws Exception {
        var saved = YamlConfiguration.loadConfiguration(fixture.toFile());
        if (!restart) {
            shortKitchen = placeKitchen(8, "steamer");
            automatedKitchen = placeKitchen(9, "steamer");
            world.getBlockAt(9, 79, 12).setType(Material.HOPPER);
            longKitchen = placeKitchen(513, "cutting_board");
            for (var controller : List.of(shortKitchen, automatedKitchen)) {
                controller.offerInput(0, item("reverie_dreams:ingredient/dew"));
                controller.offerInput(1, new ItemStack(Material.EGG));
                require(controller.choices().stream().anyMatch(choice -> choice.recipeId().equals("reverie_dreams:dew_boiled_eggs") && choice.baseTicks() == 85),
                        "kitchen presents original recipe and 3 seconds plus 25 tick duration");
                require(controller.start("reverie_dreams:dew_boiled_eggs"), "steamer starts source dew boiled eggs recipe");
            }
            List<String> fish = List.of("ingredient/salmon", "ingredient/tuna", "ingredient/supreme_tuna", "ingredient/trout", "magic_ice");
            for (int slot = 0; slot < fish.size(); slot++) longKitchen.offerInput(slot, item("reverie_dreams:" + fish.get(slot)));
            require(longKitchen.start("reverie_dreams:oedo_boat_festival") && longKitchen.snapshot().totalTicks() == 505,
                    "cutting board starts original 24 seconds plus 25 tick recipe");
            require(longKitchen.extractOutput(64) == null, "kitchen prevents premature extraction");
        } else {
            shortKitchen = KitchenModule.controllerAt(world.getBlockAt(8, 80, 12));
            automatedKitchen = KitchenModule.controllerAt(world.getBlockAt(9, 80, 12));
            longKitchen = KitchenModule.controllerAt(world.getBlockAt(513, 80, 12));
            require(shortKitchen != null && shortKitchen.item(5) == null, "drained kitchen stays drained after restart");
            require(longKitchen != null && longKitchen.working() && longKitchen.snapshot().recipeId().equals("reverie_dreams:oedo_boat_festival")
                            && longKitchen.snapshot().ticksRemaining() > 0 && longKitchen.snapshot().ticksRemaining() < saved.getDouble("kitchen_remaining"),
                    "kitchen recipe and in-progress countdown survive chunk unload and server restart");
        }
    }

    private KitchenController placeKitchen(int x, String id) {
        var location = new Location(world, x, 80, 12);
        location.clone().subtract(0, 1, 0).getBlock().setType(Material.STONE);
        require(CraftEngineBlocks.place(location, Key.of("reverie_dreams:" + id), false), "kitchen placed: " + x);
        var controller = KitchenModule.controllerAt(location.getBlock());
        require(controller != null && controller.available(), "independent kitchen controller available: " + x);
        return controller;
    }

    private void verifyEquipment() {
        var equipment = ReverieDreamsPlugin.getMod().getEquipment();
        require(equipment.items().size() == 25, "all 25 equipment rules loaded");
        for (var rule : equipment.items().values()) {
            ItemStack item = item(rule.id());
            var equippable = item.getData(DataComponentTypes.EQUIPPABLE);
            require(item.getMaxStackSize() == 1 && item.getData(DataComponentTypes.MAX_DAMAGE) == rule.durability()
                            && equippable != null && equippable.slot() == rule.slot()
                            && item.getData(DataComponentTypes.ENCHANTABLE).value() == rule.enchantability(),
                    "native source durability slot enchantability: " + rule.id());
        }
        Sheep subject = world.spawn(new Location(world, 13, 80, 14), Sheep.class, sheep -> {
            sheep.setAI(false); sheep.setGravity(false);
            sheep.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20); sheep.setHealth(20);
        });
        subject.getEquipment().setBoots(item("reverie_dreams:low_gravity_boot"));
        equipment.applyTick(subject);
        require(subject.hasPotionEffect(PotionEffectType.JUMP_BOOST) && subject.getPotionEffect(PotionEffectType.JUMP_BOOST).getAmplifier() == 1
                        && subject.hasPotionEffect(PotionEffectType.SLOW_FALLING), "low gravity boots apply source jump and slow falling effects");
        subject.getEquipment().setChestplate(item("reverie_dreams:dream_chestplate"));
        equipment.applyTick(subject);
        require(subject.hasPotionEffect(PotionEffectType.REGENERATION), "equipped dream armor regenerates");
        subject.getEquipment().setHelmet(item("reverie_dreams:waterproof_hat"));
        subject.getEquipment().setChestplate(item("reverie_dreams:waterproof_clothing"));
        subject.getEquipment().setLeggings(item("reverie_dreams:waterproof_leggings"));
        subject.getEquipment().setBoots(item("reverie_dreams:waterproof_boots"));
        equipment.applyTick(subject);
        require(equipment.hasFullWaterproof(subject) && subject.hasPotionEffect(PotionEffectType.WATER_BREATHING), "complete waterproof set grants source breathing effect");
        subject.getEquipment().setHelmet(null);
        require(!equipment.hasFullWaterproof(subject), "partial waterproof set does not qualify");
        subject.getEquipment().setHelmet(item("reverie_dreams:crown_of_the_underworld"));
        subject.setHealth(10);
        require(equipment.applyCrownInstant(subject, PotionEffectType.INSTANT_HEALTH, 0, 1, null, null) && subject.getHealth() < 10,
                "crown reverses instant healing into source damage");
        double damaged = subject.getHealth();
        require(equipment.applyCrownInstant(subject, PotionEffectType.INSTANT_DAMAGE, 0, 1, null, null) && subject.getHealth() > damaged,
                "crown reverses instant damage into source healing");
        subject.remove();
        ItemStack helmet = item("reverie_dreams:silver_helmet");
        helmet.editMeta(meta -> ((Damageable) meta).setDamage(41));
        ItemStack silver = item("reverie_dreams:silver_ingot"); silver.setAmount(2);
        var repair = equipment.previewRepair(helmet, silver).orElseThrow();
        require(repair.materialCount() == 1 && ((Damageable) repair.result().getItemMeta()).getDamage() == 0, "exact quarter durability repair consumes one material");
        require(equipment.previewRepair(helmet, new ItemStack(Material.IRON_INGOT)).isEmpty(), "vanilla iron cannot repair custom silver armor");
    }

    private void verifyAltarAndBook() throws Exception {
        var saved = YamlConfiguration.loadConfiguration(fixture.toFile());
        var books = AltarModule.books();
        if (!restart) {
            fastKitchen = placeKitchen(14, "steamer");
            fastKitchen.offerInput(0, item("reverie_dreams:ingredient/dew"));
            fastKitchen.offerInput(1, new ItemStack(Material.EGG));
            ItemStack book = books.remember(item("reverie_dreams:fast_book_item"), "reverie_dreams:dew_boiled_eggs");
            require(books.startRemembered(book, fastKitchen) && fastKitchen.snapshot().totalTicks() == 70,
                    "remembered book preserves distinct 3 seconds plus two ingredient delays, 70 ticks");
            saved.set("book_bytes", Base64.getEncoder().encodeToString(book.serializeAsBytes()));
            var location = new Location(world, 8, 70, 8);
            location.clone().subtract(0, 1, 0).getBlock().setType(Material.STONE);
            require(CraftEngineBlocks.place(location, Key.of("reverie_dreams:gensokyo_altar"), false), "altar placed");
            altar = AltarModule.controllerAt(location.getBlock());
            require(altar != null && altar.available(), "independent altar controller available");
            JsonObject source;
            try (var reader = new InputStreamReader(ReverieDreamsPlugin.getMod().getResource("migration/altar-recipes.json"), StandardCharsets.UTF_8)) {
                source = JsonParser.parseReader(reader).getAsJsonObject();
            }
            JsonObject card = source.getAsJsonArray("recipes").asList().stream().map(value -> value.getAsJsonObject())
                    .filter(recipe -> recipe.get("id").getAsString().equals("reverie_dreams:protagonist_group")).findFirst().orElseThrow();
            fillAltar(card);
            var before = altar.snapshot().items();
            require(altar.craft().status() == AltarController.CraftStatus.MISSING_STRUCTURE && before.equals(altar.snapshot().items()),
                    "missing altar structure refuses crafting without consuming ingredients");
            for (int[] offset : AltarController.OFFSETS) for (int height = 0; height <= 2; height++) {
                require(CraftEngineBlocks.place(new Location(world, 8 + offset[0], 70 + height, 8 + offset[1]),
                        Key.of("reverie_dreams:" + (height == 2 ? "blessed_spiritual_log" : "stripped_spiritual_log")), false), "altar structure log placed");
            }
            require(altar.structure().valid(), "all 24 original altar logs validated");
            var crafted = altar.craft();
            require(crafted.status() == AltarController.CraftStatus.SUCCESS && altar.item(8).isSimilar(ReverieDreamsPlugin.getMod().getNPC().interaction().createRoleCard("reverie_dreams:protagonist_group")),
                    "altar produces real NPC group card with durable group metadata");
            for (int slot = 0; slot < 8; slot++) require(altar.item(slot) == null, "altar consumes exact required count: " + slot);
            saved.save(fixture.toFile());
        } else {
            altar = AltarModule.controllerAt(world.getBlockAt(8, 70, 8));
            require(altar != null && altar.structure().valid() && altar.item(8).isSimilar(ReverieDreamsPlugin.getMod().getNPC().interaction().createRoleCard("reverie_dreams:protagonist_group")),
                    "altar group card and source structure survive actual restart");
            require(altar.take(8, 64) != null && altar.take(8, 64) == null, "restored altar transfers output exactly once");
            ItemStack book = ItemStack.deserializeBytes(Base64.getDecoder().decode(saved.getString("book_bytes")));
            require("reverie_dreams:dew_boiled_eggs".equals(books.rememberedRecipe(book)), "full fast recipe snapshot survives native item serialization");
            fastKitchen = KitchenModule.controllerAt(world.getBlockAt(14, 80, 12));
            require(fastKitchen != null && fastKitchen.item(5) == null, "drained fast kitchen survives restart");
        }
    }

    private void fillAltar(JsonObject recipe) {
        for (int slot = 0; slot < 9; slot++) {
            JsonObject definition = slot == 8 ? recipe.getAsJsonObject("core") : recipe.getAsJsonArray("slots").get(slot).getAsJsonObject();
            ItemStack input = item(definition.get("id").getAsString()); input.setAmount(definition.get("count").getAsInt());
            require(altar.offer(slot, input) == null, "altar source ingredient accepted: " + slot);
        }
    }

    private void verifyDanmaku() {
        var service = DanmakuModule.service();
        var rules = service.rules();
        require(rules.types().size() == 11, "all eleven ordinary danmaku types loaded");
        var registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.DAMAGE_TYPE);
        require(registry.get(NamespacedKey.fromString("reverie_dreams:danmaku_generic")) != null
                        && registry.get(NamespacedKey.fromString("reverie_dreams:danmaku_real")) != null,
                "Paper bootstrap registers both source damage types");
        if (restart) {
            var saved = YamlConfiguration.loadConfiguration(fixture.toFile());
            UUID id = UUID.fromString(saved.getString("danmaku_resume"));
            var restored = service.snapshots().stream().filter(snapshot -> snapshot.id().equals(id)).findFirst().orElseThrow();
            require(restored.age() > 0 && restored.age() < 200 && restored.properties().speed() == .01f,
                    "in-flight danmaku properties and age survive chunk unload and actual restart");
            require(service.cancel(id) && !service.cancel(id), "restored projectile cancels exactly once");
            return;
        }
        Sheep owner = bulletSubject(2.5, 130, 12.5);
        Sheep target = bulletSubject(7.5, 130, 12.5);
        Sheep generic = bulletSubject(7.5, 140, 6.5);
        Sheep real = bulletSubject(7.5, 140, 10.5);
        generic.getAttribute(Attribute.ARMOR).setBaseValue(20);
        real.getAttribute(Attribute.ARMOR).setBaseValue(20);
        ItemStack base = rules.create("amulet", "red");
        ItemStack hit = rules.configure(base, rules.properties(base).withDamage(4), DanmakuMath.Config.EMPTY);
        directBullet = service.launch(owner, hit, new Location(world, 2.5, 130.65, 12.5), 0, -90).getFirst();
        service.launch(owner, hit, new Location(world, 2.5, 140.65, 6.5), 0, -90);
        var p = rules.properties(hit);
        var realProperties = new DanmakuRules.Properties(p.templateId(), p.count(), p.damage(), "reverie_dreams:danmaku_real", p.scale(), p.speed(), p.acceleration(), p.tile(), p.infinite(), p.speedVersion(), p.hitboxScale());
        service.launch(owner, rules.configure(base, realProperties, DanmakuMath.Config.EMPTY), new Location(world, 2.5, 140.65, 10.5), 0, -90);
        var legacy = rules.properties(base).withSpeed(2, 0);
        require(legacy.actualSpeed() == 1 && legacy.withSpeed(2, 1).actualSpeed() == 2 && legacy.withSpeed(9, 1).actualSpeed() == 4,
                "danmaku preserves legacy speed version conversion and source upper clamp");
        var burst = rules.configure(base, p.withCount(100).withTemplate("reverie_dreams:fan"), DanmakuMath.Config.EMPTY);
        var ids = service.launch(owner, burst, new Location(world, 2, 200, 2), 0, 0);
        require(ids.size() == 64, "same-use repeated templates share original 64 bullet budget");
        for (UUID id : ids) if (!service.cancel(id)) throw new AssertionError("Budget projectile leaked");
        ItemStack slow = rules.configure(base, p.withSpeed(.01f, 1).withDamage(0), DanmakuMath.Config.EMPTY);
        lifetimeBullet = service.launch(owner, slow, new Location(world, 2, 180, 2), 0, 0).getFirst();
        require(service.snapshots().stream().filter(snapshot -> snapshot.id().equals(lifetimeBullet)).findFirst().orElseThrow().duration() == 200,
                "ordinary projectile has original 200 tick lifetime");
    }

    private Sheep bulletSubject(double x, double y, double z) {
        Sheep subject = world.spawn(new Location(world, x, y, z), Sheep.class, sheep -> {
            sheep.setAI(false); sheep.setGravity(false); sheep.setRemoveWhenFarAway(false);
            sheep.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20); sheep.setHealth(20);
        });
        danmakuSubjects.add(subject);
        return subject;
    }

    private void verifyDanmakuCompletion() {
        var service = DanmakuModule.service();
        Sheep target = danmakuSubjects.get(1), generic = danmakuSubjects.get(2), real = danmakuSubjects.get(3);
        require(Math.abs(target.getHealth() - 16) < .01, "ordinary projectile hits exactly once with source damage");
        require(target.getVelocity().lengthSquared() < .001, "registered projectile damage preserves no-knockback tag");
        require(target.getLocation().distanceSquared(new Location(world, 7.5, 130, 12.5)) < .001,
                "projectile never displaces its no-knockback target");
        require(Math.abs(real.getHealth() - 16) < .01 && generic.getHealth() > real.getHealth(),
                "real danmaku bypasses armor while generic danmaku respects it");
        require(service.recentImpacts().stream().anyMatch(impact -> impact.bullet().equals(directBullet) && target.getUniqueId().equals(impact.target())),
                "projectile records physical hit target");
        require(service.snapshots().stream().noneMatch(snapshot -> snapshot.id().equals(lifetimeBullet)), "ordinary projectile expires after real 200 ticks");
        for (Sheep subject : danmakuSubjects) subject.remove();
    }

    private void verifyPluginTickFreeze() {
        var manager = Bukkit.getServer().getServerTickManager();
        var bullets = DanmakuModule.service();
        Sheep protectedSheep = world.getEntitiesByClass(Sheep.class).stream()
                .filter(sheep -> ReverieDreamsPlugin.getMod().getFood().protectionTicks(sheep) > 0).findFirst().orElseThrow();
        manager.setFrozen(true);
        // Native TickRateManager publishes runsNormally at its tick boundary. Sample after that boundary.
        Bukkit.getScheduler().runTaskLater(this, () -> {
            try {
                require(manager.isFrozen() && !manager.isRunningNormally(), "native tick boundary entered frozen state");
                int productionTicks = longBarrel.snapshot().ticksRemaining(); double kitchenTicks = longKitchen.snapshot().ticksRemaining();
                var before = bullets.snapshots().stream().filter(b -> b.id().equals(lifetimeBullet)).findFirst().orElseThrow();
                int milkTicks = ReverieDreamsPlugin.getMod().getFood().protectionTicks(protectedSheep);
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    try {
                        var after = bullets.snapshots().stream().filter(b -> b.id().equals(lifetimeBullet)).findFirst().orElseThrow();
                        require(manager.isFrozen() && longBarrel.snapshot().ticksRemaining() == productionTicks && longKitchen.snapshot().ticksRemaining() == kitchenTicks,
                                "native time freeze pauses CE production and kitchen progression");
                        require(before.age() == after.age() && before.position().equals(after.position()), "native time freeze pauses plugin projectile age and motion");
                        require(ReverieDreamsPlugin.getMod().getFood().protectionTicks(protectedSheep) == milkTicks,
                                "milk protection pauses with the original living entity effect clock");
                    } catch (Throwable failure) { finish(failure); }
                    finally { manager.setFrozen(false); }
                }, 18L);
            } catch (Throwable failure) { manager.setFrozen(false); finish(failure); }
        }, 2L);
    }

    private ItemStack item(String id) {
        if (id.startsWith("minecraft:")) return new ItemStack(Objects.requireNonNull(Material.matchMaterial(id)));
        return CraftEngineItems.byId(id).buildBukkitItem();
    }

    private static boolean matches(ItemStack item, String id, int count) {
        if (item == null || item.isEmpty() || item.getAmount() != count) return false;
        Key custom = CraftEngineItems.getCustomItemId(item);
        return id.startsWith("minecraft:") ? custom == null && item.getType() == Material.matchMaterial(id) : Key.of(id).equals(custom);
    }

    private void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks.add(message);
    }
}
