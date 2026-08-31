package cc.thonly.reverie_dreams.command;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.ReverieDreamsConfiguration;
import cc.thonly.reverie_dreams.api.dialog.DialogAPI;
import cc.thonly.reverie_dreams.api.registry.BookPageManager;
import cc.thonly.reverie_dreams.data.BeverageProperty;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.data.craftengine.BlockDefinitionList;
import cc.thonly.reverie_dreams.data.craftengine.CraftEngineDefinition;
import cc.thonly.reverie_dreams.data.craftengine.CraftEngineProvider;
import cc.thonly.reverie_dreams.data.craftengine.ItemDefinitionList;
import cc.thonly.reverie_dreams.data.danmaku.SpellcardRenderer;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.SpellCardFrameConfig;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.dialog.DialogFiles;
import cc.thonly.reverie_dreams.gui.recipe.RecipeTypeCategoryGui;
import cc.thonly.reverie_dreams.mixin.accessor.ItemCooldownsAccessor;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.RecipeWorkbench;
import cc.thonly.reverie_dreams.recipe.RecipeWorkbenchRegistry;
import cc.thonly.reverie_dreams.registry.RegistryEntryTranslatable;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import cc.thonly.reverie_dreams.server.PlayerSettings;
import cc.thonly.reverie_dreams.util.ImageToTextScanner;
import cc.thonly.reverie_dreams.util.nbs.Midi2Nbs;
import cc.thonly.reverie_dreams.util.test.ModTest;
import cc.thonly.reverie_dreams.util.PlatformContext;
import cc.thonly.reverie_dreams.util.command.PermissionPredicates;
import cc.thonly.reverie_dreams.util.math.ModMth;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import lombok.extern.slf4j.Slf4j;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.level.storage.loot.LootTable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.Locale;

@SuppressWarnings({"unchecked", "rawtypes"})
@Slf4j
public class THCommand {
    public static final SuggestionProvider<CommandSourceStack> settingNameSuggestions =
            (context, builder) -> {
                for (String name : PlayerSettings.DEFINES.keySet()) {
                    builder.suggest(name);
                }

                return builder.buildFuture();
            };
    public static final SuggestionProvider<CommandSourceStack> midiNameSuggestions =
            (context, builder) -> {
                Path dir = Path.of("./config/reverie_dreams/nota");

                if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                    return builder.buildFuture();
                }

                try (Stream<Path> files = Files.list(dir)) {
                    files
                            .filter(Files::isRegularFile)
                            .filter(path -> {
                                String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                                return name.endsWith(".mid") || name.endsWith(".midi");
                            })
                            .map(path -> path.getFileName().toString())
                            .filter(name -> name.toLowerCase(Locale.ROOT)
                                    .startsWith(builder.getRemaining().toLowerCase(Locale.ROOT)))
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .forEach(builder::suggest);

                } catch (IOException e) {
                    log.error("Error: ", e);
                }
                builder.suggest("*");

                return builder.buildFuture();
            };

    public THCommand() {

    }

    public LiteralArgumentBuilder<CommandSourceStack> makeInstance(
            CommandDispatcher<CommandSourceStack> dispatcher,
            HolderLookup.Provider registryAccess
    ) {

        var root = Commands.literal("touhou");
        var help = Commands.literal("help")
                .executes(this::help);
        var get_sc_with_spell_config = Commands.literal("get_spellcard_with_config")
                .requires(PermissionPredicates.isGameMasters())
                .then(
                        BuiltInRegistryProviders.getSuggestProvider(this::getItemWithDanmakuConfig, ResourceKey.createRegistryKey(ReverieDreams.id("danmaku_config")))
                );
        var with_food_property = Commands.literal("with_food_property")
                .requires(PermissionPredicates.isGameMasters())
                .then(
                        BuiltInRegistryProviders.getSuggestProvider(this::withFoodProperties, ResourceKey.createRegistryKey(ReverieDreams.id("food_property")))
                );
        var with_beverage_property = Commands.literal("with_beverage_property")
                .requires(PermissionPredicates.isGameMasters())
                .then(
                        BuiltInRegistryProviders.getSuggestProvider(this::withDrinkProperties, ResourceKey.createRegistryKey(ReverieDreams.id("beverage_property")))
                );
        var cachedAllSkins = Commands.literal("start-cached-skins")
                .requires(PermissionPredicates.isGameMasters())
                .executes(this::cachedAllSkins);
        var parseMidi2Nbt = Commands.literal("parse_nbs_midi")
                .then(Commands.argument(
                                "file_name",
                                StringArgumentType.string()
                        ).suggests(midiNameSuggestions)
                        .requires(PermissionPredicates.isGameMasters())
                        .executes(this::parseMidi2Nbd)
                );
        var recipe = Commands.literal("recipe")
                .executes(this::recipe);
        var registry = Commands.literal("registry")
                .requires(PermissionPredicates.isGameMasters())
                .then(
                        BuiltInRegistryProviders.getSuggestProvider(this::registry)
                );
        var registry_tag = Commands.literal("registry_tag")
                .requires(PermissionPredicates.isGameMasters())
                .then(
                        BuiltInRegistryProviders.getSuggestTagProvider(this::registryTag)
                );
//        var dialog = Commands.literal("dialog")
//                .then(
//                        Commands.argument("value", StringArgumentType.string())
//                                .suggests(new DialogSuggestionProvider())
//                                .executes(this::openDialog)
//                );
        var settings = Commands.literal("settings")
                .then(
                        Commands.literal("get")
                                .then(
                                        Commands.argument(
                                                        "name",
                                                        StringArgumentType.word()
                                                )
                                                .suggests(settingNameSuggestions)
                                                .executes(this::getPlayerSettingValue)
                                )
                )
                .then(
                        Commands.literal("set")
                                .then(
                                        Commands.argument(
                                                        "name",
                                                        StringArgumentType.word()
                                                )
                                                .suggests(settingNameSuggestions)
                                                .then(
                                                        Commands.argument(
                                                                        "value",
                                                                        StringArgumentType.greedyString()
                                                                )
                                                                .executes(this::setPlayerSettingValue)
                                                )
                                )
                );
        var video = Commands.literal("video")
                .requires(PermissionPredicates.isGameMasters())
                .then(
                        Commands.literal("play")
                                .then(
                                        Commands.argument("target", EntityArgument.entity())
                                                .then(
                                                        Commands.argument("file", StringArgumentType.string())
                                                                .suggests(new DialogFiles.FilesSuggestionProvider())
                                                                .executes(this::playVideo)
                                                                .then(
                                                                        Commands.argument("sound", IdentifierArgument.id())
                                                                                .suggests(SuggestionProviders.cast(SuggestionProviders.AVAILABLE_SOUNDS))
                                                                                .executes(this::playVideo)
                                                                )
                                                )
                                )

                )
                .then(
                        Commands
                                .literal("reload")
                                .executes(this::reloadVideo)
                );
        var reloadConfig = Commands.literal("reload_config")
                .requires(PermissionPredicates.isGameMasters())
                .executes(this::reloadConfig);
        var loadRootPage = Commands.literal("load_root_guide_page")
                .requires(PermissionPredicates.isGameMasters())
                .then(
                        Commands.argument("namespace", StringArgumentType.string())
                                .executes(this::loadRootPage)
                );
        var about = Commands.literal("about")
                .executes(this::about);

        root.executes(this::run);
        root.then(about);
        root.then(cachedAllSkins);
        root.then(get_sc_with_spell_config);
        root.then(help);
        root.then(loadRootPage);
        root.then(parseMidi2Nbt);
        root.then(recipe);
        root.then(registry);
        root.then(registry_tag);
        root.then(reloadConfig);
        root.then(settings);
        root.then(video);
        root.then(with_beverage_property);
        root.then(with_food_property);
        // root.then(dialog);
        if (PlatformContext.isDevMode()) {
            var debugGetChest = Commands.literal("debug_get_loot_with_chest")
                    .then(Commands.argument("loot_table", ResourceOrIdArgument.lootTable(CommandBuildContext.simple(registryAccess, FeatureFlagSet.of())))
                            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                            .executes(this::debugFastChestLoot));
            root.then(debugGetChest);
            var debugGetRecipeWithBlock = Commands.literal("debug_get_recipe_with_block")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
            for (Map.Entry<String, RecipeWorkbench<?>> mapEntry : RecipeWorkbenchRegistry.entries()) {
                String key = mapEntry.getKey();
                RecipeWorkbench<?> entry = mapEntry.getValue();
                debugGetRecipeWithBlock.then(
                        Commands.literal(key).then(Commands.argument("recipe_id", IdentifierArgument.id())
                                .suggests(RecipeManager.getSuggestions(entry.getRecipeType()))
                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .executes((context) -> this.debugFastRecipeBlock(entry, context))
                        )
                );
            }
            root.then(debugGetRecipeWithBlock);
            var debugResetItemCd = Commands.literal("debug_reset_item_using_time")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
            var debug_generate_craft_engine = Commands.literal("debug_generate_craft_engine")
                    .executes(this::generateCraftEngineConfig);
            root.then(debug_generate_craft_engine);
            debugResetItemCd.executes(this::resetItemCd);
            root.then(debugResetItemCd);
            if (PlatformContext.isDevModeByIDEA()) {
                var debugTempTest = Commands.literal("test").executes(this::tempTest);
                root.then(debugTempTest);
            }
        }

        return root;
    }

    private int tempTest(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!source.isPlayer()) {
            return 0;
        }

        ServerPlayer player = source.getPlayer();
        if (player != null) {
            ModTest.ByCommand.onTest(player);
        }

        return 1;
    }

    private int parseMidi2Nbd(CommandContext<CommandSourceStack> context) {
        String fileName = StringArgumentType.getString(context, "file_name");
        CommandSourceStack source = context.getSource();

        Path basePath = Path.of("./config/reverie_dreams/nota");

        try {
            Files.createDirectories(basePath);

            // *
            if ("*".equals(fileName)) {
                int total = 0;
                int success = 0;
                int skipped = 0;
                int failed = 0;

                try (var stream = Files.list(basePath)) {
                    for (Path inputPath : stream
                            .filter(Files::isRegularFile)
                            .filter(path -> {
                                String name = path.getFileName()
                                        .toString()
                                        .toLowerCase(Locale.ROOT);
                                return name.endsWith(".mid") || name.endsWith(".midi");
                            })
                            .sorted(Comparator.comparing(
                                    path -> path.getFileName().toString(),
                                    String.CASE_INSENSITIVE_ORDER
                            ))
                            .toList()) {

                        total++;

                        String inputName = inputPath.getFileName().toString();
                        String lowerName = inputName.toLowerCase(Locale.ROOT);

                        String outputFileName;
                        if (lowerName.endsWith(".mid")) {
                            outputFileName =
                                    inputName.substring(0, inputName.length() - 4) + ".nbs";
                        } else {
                            outputFileName =
                                    inputName.substring(0, inputName.length() - 5) + ".nbs";
                        }

                        Path outputPath = basePath.resolve(outputFileName);

                        // 已经存在 NBS
                        if (Files.exists(outputPath)) {
                            skipped++;
                            source.sendSystemMessage(
                                    Component.literal(
                                            "Skip: %s (NBS already exists)"
                                                    .formatted(inputName)
                                    )
                            );
                            continue;
                        }

                        // 检查文件是否被占用
                        try (FileChannel channel = FileChannel.open(
                                inputPath,
                                StandardOpenOption.READ,
                                StandardOpenOption.WRITE
                        )) {
                            try (FileLock lock = channel.tryLock()) {
                                if (lock == null) {
                                    skipped++;
                                    source.sendSystemMessage(
                                            Component.literal(
                                                    "Skip: %s (file is in use)"
                                                            .formatted(inputName)
                                            )
                                    );
                                    continue;
                                }

                                try {
                                    Midi2Nbs.midi2nbs(
                                            inputPath,
                                            outputPath,
                                            new Midi2Nbs.Options()
                                    );

                                    success++;

                                    source.sendSystemMessage(
                                            Component.literal(
                                                    "Success: %s -> %s"
                                                            .formatted(
                                                                    inputName,
                                                                    outputFileName
                                                            )
                                            )
                                    );
                                } catch (Exception e) {
                                    failed++;
                                    log.error("Error parsing MIDI {}", inputPath, e);

                                    source.sendFailure(
                                            Component.literal(
                                                    "Failure parse: %s"
                                                            .formatted(inputName)
                                            )
                                    );
                                }
                            }
                        } catch (OverlappingFileLockException e) {
                            skipped++;
                            source.sendSystemMessage(
                                    Component.literal(
                                            "Skip: %s (file is in use)"
                                                    .formatted(inputName)
                                    )
                            );
                        } catch (IOException e) {
                            skipped++;
                            log.error("Unable to access MIDI {}", inputPath, e);

                            source.sendFailure(
                                    Component.literal(
                                            "Cannot access: %s"
                                                    .formatted(inputName)
                                    )
                            );
                        }
                    }
                }

                source.sendSystemMessage(
                        Component.literal(
                                "MIDI conversion finished: total=%d, success=%d, skipped=%d, failed=%d"
                                        .formatted(
                                                total,
                                                success,
                                                skipped,
                                                failed
                                        )
                        )
                );

                return success > 0 ? 1 : 0;
            }

            // 单个文件
            Path inputPath = basePath.resolve(fileName).normalize();

            if (!Files.isRegularFile(inputPath)) {
                source.sendFailure(
                        Component.literal(
                                "MIDI file not found: %s".formatted(fileName)
                        )
                );
                return 0;
            }

            String lowerName = fileName.toLowerCase(Locale.ROOT);

            if (!lowerName.endsWith(".mid") && !lowerName.endsWith(".midi")) {
                source.sendFailure(
                        Component.literal(
                                "Not a MIDI file: %s".formatted(fileName)
                        )
                );
                return 0;
            }

            String outputFileName;

            if (lowerName.endsWith(".mid")) {
                outputFileName =
                        fileName.substring(0, fileName.length() - 4) + ".nbs";
            } else {
                outputFileName =
                        fileName.substring(0, fileName.length() - 5) + ".nbs";
            }

            Path outputPath = basePath.resolve(outputFileName);

            // 对应 NBS 已经存在
            if (Files.exists(outputPath)) {
                source.sendSystemMessage(
                        Component.literal(
                                "Skip: %s already exists"
                                        .formatted(outputFileName)
                        )
                );
                return 1;
            }

            // 检查 MIDI 是否被占用
            try (FileChannel channel = FileChannel.open(
                    inputPath,
                    StandardOpenOption.READ,
                    StandardOpenOption.WRITE
            )) {
                try (FileLock lock = channel.tryLock()) {
                    if (lock == null) {
                        source.sendFailure(
                                Component.literal(
                                        "MIDI file is in use: %s"
                                                .formatted(fileName)
                                )
                        );
                        return 0;
                    }

                    try {
                        Midi2Nbs.midi2nbs(
                                inputPath,
                                outputPath,
                                new Midi2Nbs.Options()
                        );

                        source.sendSystemMessage(
                                Component.literal(
                                        "Success save to %s"
                                                .formatted(outputPath)
                                )
                        );
                    } catch (Exception e) {
                        log.error("Error parsing MIDI {}", inputPath, e);

                        source.sendFailure(
                                Component.literal("Failure parse")
                        );

                        return 0;
                    }
                }
            } catch (OverlappingFileLockException e) {
                source.sendFailure(
                        Component.literal(
                                "MIDI file is in use: %s"
                                        .formatted(fileName)
                        )
                );
                return 0;
            } catch (IOException e) {
                log.error("Unable to access MIDI {}", inputPath, e);

                source.sendFailure(
                        Component.literal(
                                "Cannot access MIDI file"
                        )
                );
                return 0;
            }

            return 1;
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return 0;
    }

    private int setPlayerSettingValue(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!source.isPlayer()) {
            return 0;
        }

        ServerPlayer player = source.getPlayer();
        PlayerSettings settings = PlayerSettings.get(player);

        String name = StringArgumentType.getString(context, "name");

        PlayerSettings.KeyValue<?> keyValue = settings.get(name);

        if (keyValue == null) {
            source.sendFailure(
                    Component.literal("未知设置: " + name)
            );
            return 0;
        }

        Object value;

        switch (keyValue.type()) {
            case BOOL -> value =
                    BoolArgumentType.getBool(context, "value");

            case STRING -> value =
                    StringArgumentType.getString(context, "value");

            case NUMBER -> value =
                    DoubleArgumentType.getDouble(context, "value");

            default -> {
                return 0;
            }
        }

        settings.set(name, value);

        source.sendSystemMessage(Component.literal("Set %s = %s".formatted(name, value)));

        return 1;
    }

    private int getPlayerSettingValue(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) {
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        PlayerSettings playerSettings = PlayerSettings.get(player);
        String name = StringArgumentType.getString(context, "name");
        Object object = playerSettings.get(name);
        source.sendSystemMessage(Component.literal("Get %s = %s".formatted(name, object)));
        return 1;
    }

    private int reloadConfig(CommandContext<CommandSourceStack> context) {
        ConfigHolder<ReverieDreamsConfiguration> configHolder = AutoConfig.getConfigHolder(ReverieDreamsConfiguration.class);
        if (configHolder == null) {
            return 1;
        }
        configHolder.load();
        context.getSource().sendSystemMessage(Component.literal("Reload Success"));
        return 0;
    }

    private int generateCraftEngineConfig(CommandContext<CommandSourceStack> context) {
        CraftEngineDefinition craftEngineDefinition = CraftEngineProvider.fromNamespace(ReverieDreams.MOD_ID);
        if (craftEngineDefinition == null) {
            return 1;
        }
        BlockDefinitionList blockDefinitions = craftEngineDefinition.getBlockDefinitions();
        ItemDefinitionList itemDefinitions = craftEngineDefinition.getItemDefinitions();
        Path path = Path.of("./config/reverie_dreams/craft_engine/");
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            try {
                Files.createDirectories(path);
            } catch (Exception e) {
                log.error("Error:", e);
            }
        }
        CraftEngineProvider.toFile(itemDefinitions, path.resolve("./items.yml"));
        CraftEngineProvider.toFile(blockDefinitions, path.resolve("./blocks.yml"));
        context.getSource().sendSystemMessage(Component.literal("Success"));
        return 0;
    }

    private int loadRootPage(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) {
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        String namespace = StringArgumentType.getString(context, "namespace");
        if (namespace == null) {
            return 0;
        }
        if (player == null) {
            return 0;
        }
        try {
            BookPageManager.getInstance().openRoot(namespace, player);
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return 1;
    }

    private int run(CommandContext<CommandSourceStack> context) {
        MutableComponent text = Component.translatable("command.touhou.suggest_help");
        context.getSource().sendSuccess(() -> text.setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), false);
        return 1;
    }

    private int resetItemCd(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) {
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        assert player != null;
        ItemCooldowns cooldowns = player.getCooldowns();
        ItemCooldownsAccessor accessor = (ItemCooldownsAccessor) cooldowns;
        accessor.reverie_dreams$cooldowns().clear();
        return 1;
    }

    private <R extends BaseRecipe> int debugFastRecipeBlock(RecipeWorkbench<R> recipeEntry, CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) {
            return 0;
        }
        Identifier recipeId = IdentifierArgument.getId(context, "recipe_id");
        MinecraftServer server = source.getServer();
        RecipeWorkbench.SaveFunction<R> function = recipeEntry.getFunction();
        try {
            ItemStack itemStack = function.save(server.registryAccess(), recipeId, recipeEntry);
            if (itemStack == null || itemStack.isEmpty()) {
                return 0;
            }
            ServerPlayer player = source.getPlayer();
            if (player != null) {
                player.addItem(itemStack);
            }
        } catch (Exception e) {
            log.error("Error: ", e);
            throw new RuntimeException(e);
        }
        return 1;
    }

    private int debugFastChestLoot(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) {
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        assert player != null;
        try {
            Holder<LootTable> holder = ResourceOrIdArgument.getLootTable(context, "loot_table");
            holder.unwrapKey().ifPresent(key -> {
                ItemStack itemStack = Items.CHEST.getDefaultInstance();
                itemStack.set(DataComponents.CONTAINER_LOOT, new SeededContainerLoot(key, 0L));
                boolean success = player.getInventory().add(itemStack);
                if (!success) {
                    player.drop(itemStack, false);
                }
            });
        } catch (Exception err) {
            log.error("Error: ", err);
            return 1;
        }
        return 0;
    }

    private int withFoodProperties(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) {
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        assert player != null;
        Identifier id = IdentifierArgument.getId(context, "id");
        FoodProperty property = BuiltInRegistryProviders.FOOD_PROPERTY.getValue(id);
        if (property == null) {
            source.sendFailure(Component.literal("Invalid resource key."));
            return 0;
        }
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.isEmpty()) {
            source.sendFailure(Component.literal("§cYour slot item is empty."));
            return 0;
        }
        List<FoodProperty> props = new ArrayList<>(itemStack.getOrDefault(RDDataComponentTypes.FOOD_PROPERTIES.value(), new ArrayList<>()));
        if (!props.contains(property)) {
            props.add(property);
        }
        itemStack.set(RDDataComponentTypes.FOOD_PROPERTIES.value(), props);
        return 1;
    }

    private int withDrinkProperties(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) {
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        assert player != null;
        Identifier id = IdentifierArgument.getId(context, "id");
        BeverageProperty property = BuiltInRegistryProviders.BEVERAGE_PROPERTY.getValue(id);
        if (property == null) {
            source.sendFailure(Component.literal("Invalid resource key."));
            return 0;
        }
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.isEmpty()) {
            source.sendFailure(Component.literal("§cYour slot item is empty."));
            return 0;
        }
        List<BeverageProperty> props = new ArrayList<>(itemStack.getOrDefault(RDDataComponentTypes.BEVERAGE_PROPERTIES.value(), new ArrayList<>()));
        if (!props.contains(property)) {
            props.add(property);
        }
        itemStack.set(RDDataComponentTypes.BEVERAGE_PROPERTIES.value(), props);
        return 1;
    }

    private int getItemWithDanmakuConfig(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) {
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        assert player != null;
        Identifier id = IdentifierArgument.getId(context, "id");

        SpellCardFrameConfig config = BuiltInRegistryProviders.DANMAKU_CONFIG.getValue(id);
        if (config == null) {
            source.sendFailure(Component.literal("Invalid resource key."));
            return 0;
        }
        ItemStack itemStack = RDItems.SPELLCARD.createStack();
        itemStack.set(RDDataComponentTypes.SPELL_CARD_COMPONENT.value(), new SpellcardRenderer(List.of(List.of(config))));
        player.addItem(itemStack);
        return 1;
    }

    private int cachedAllSkins(CommandContext<CommandSourceStack> context) {
        for (SkinType skinType : BuiltInRegistryProviders.SKIN_TYPE_MERGED) {
            try {
                if (skinType.getProperty() == null) {
                    throw new NullPointerException();
                }
            } catch (Exception e) {
                log.error("Can't request get skin {}", skinType.getId());
                context.getSource().sendSuccess(() -> Component.literal("§d× Can't get skin %s Succeed".formatted(skinType.getId())), false);
                continue;
            }
            context.getSource().sendSuccess(() -> Component.literal("§a√ Get skin %s Succeed".formatted(skinType.getId())), false);
        }
        context.getSource().sendSuccess(() -> Component.literal("§e Cached skin worker has finished"), false);
        return 1;
    }

    private int registry(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Identifier registryKeyId = IdentifierArgument.getId(context, "registry_key");
        Identifier id = IdentifierArgument.getId(context, "id");

        ResourceKey<Registry<Object>> registryKey = ResourceKey.createRegistryKey(registryKeyId);
        RegistryProvider<?> registry = BuiltInRegistryProviders.ROOT.get(registryKey);
        if (registry == null) {
            source.sendFailure(Component.literal("Registry not found: ").append(Component.literal(registryKey.toString())));
            return 0;
        }

        Object value = registry.getValue(id);
        MutableComponent msg = Component.literal("")
                .append(Component.literal("=== ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(ResourceKey.create(registryKey, id).toString()).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" ===\n").withStyle(ChatFormatting.GOLD));

        if (value == null) {
            msg.append(Component.literal("No entry found for this ID.").withStyle(ChatFormatting.RED));
            source.sendSystemMessage(msg);
            return 0;
        }

        if (value instanceof RegistryEntryTranslatable translatable) {
            msg.append(Component.literal("Translation: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.translatable(translatable.translateKey()).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal("\n"));
        }

        msg.append(Component.literal("Object: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(value.toString()).withStyle(ChatFormatting.AQUA));

        source.sendSystemMessage(msg);
        return 1;
    }

    private int registryTag(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Identifier registryKeyId = IdentifierArgument.getId(context, "registry_key");
        Identifier id = IdentifierArgument.getId(context, "id");

        ResourceKey<Registry<Object>> registryKey = ResourceKey.createRegistryKey(registryKeyId);
        RegistryProvider<?> registry = BuiltInRegistryProviders.ROOT.get(registryKey);
        if (registry == null) {
            source.sendFailure(Component.literal("Registry not found: ").append(Component.literal(registryKey.toString())));
            return 0;
        }
        TagKey<?> tagKey = TagKey.create(registry.key(), id);
        List<Holder> list = ModMth.toList(registry.getTagOrEmpty((TagKey) tagKey));
        source.sendSystemMessage(Component.literal("Registry Tag Name: %s".formatted(id)));
        List<Identifier> ids = new ArrayList<>();
        for (Holder holder : list) {
            holder.unwrapKey().ifPresent((key) -> {
                if (key instanceof ResourceKey resourceKey) {
                    ids.add(resourceKey.identifier());
                }
            });
        }
        source.sendSystemMessage(Component.literal("%s".formatted(ids)));
        return 1;
    }

    private int reloadVideo(CommandContext<CommandSourceStack> context) {
        DialogFiles.reload();
        context.getSource().sendSuccess(() -> Component.translatable("command.touhou.video.reload"), false);
        return 1;
    }

    private int playVideo(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "target");
            String file = StringArgumentType.getString(context, "file");
            Identifier soundEventId = null;
            SoundEvent soundEvent = null;
            try {
                soundEventId = IdentifierArgument.getId(context, "sound");
            } catch (Exception ignored) {
            }
            if (soundEventId != null) {
                soundEvent = SoundEvent.createVariableRangeEvent(soundEventId);
            }
            context.getSource().sendSuccess(() -> Component.translatable("command.touhou.video.reload"), false);
            DialogAPI.play(player, file, soundEvent);
            context.getSource().sendSuccess(() -> Component.translatable("command.touhou.video.load.done"), false);
        } catch (Exception err) {
            log.error("Can't play video", err);
        }
        return 1;
    }

    private int help(CommandContext<CommandSourceStack> context) {
        List<String> keys = List.of(
                "command.touhou.help.title",
                "command.touhou.help.help",
                "command.touhou.help.recipe",
                "command.touhou.help.about",
                "command.touhou.help.empty"
        );

        for (String key : keys) {
            context.getSource().sendSuccess(() -> Component.translatable(key).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)), false);
        }
        return 1;
    }

//    @SuppressWarnings("unchecked")
//    private int exportRegistries(CommandContext<CommandSourceStack> context) {
//        List<String> lines = new LinkedList<>();
//        CommandSourceStack source = context.getSource();
//        MinecraftServer server = source.getServer();
//        RegistryAccess.Frozen registryManager = server.registryAccess();
//        Stream<RegistryAccess.RegistryEntry<Object>> entryStream = (Stream<RegistryAccess.RegistryEntry<Object>>) (Object) registryManager.registries();
//        List<RegistryAccess.RegistryEntry<Object>> list = entryStream.toList();
//        for (RegistryAccess.RegistryEntry<Object> entry : list) {
//            ResourceKey<? extends Registry<Object>> key = entry.key();
//            Registry<Object> registry = entry.value();
//
//            lines.add("===== Registry: " + key.identifier() + " =====");
//            registry.forEach(obj -> {
//                int rawId = registry.getId(obj);
//                Identifier id = registry.getKey(obj);
//                if (rawId == 97) {
//                    lines.add("!!! Found 97 in " + key.identifier() + " = " + id);
//                }
//            });
//        }
//        DebugExportWriter output = DebugExportWriter.OUTPUT;
//        for (String line : lines) {
//            output.write(line);
//        }
//        output.export();
//        return 1;
//    }

    private int about(CommandContext<CommandSourceStack> context) {
        Class<?> clazz = ReverieDreams.class;
        ImageToTextScanner instance = ImageToTextScanner.createInstance(clazz);
        String path = ImageToTextScanner.ofNamespace(ReverieDreams.MOD_ID, "icon_about.png");
        BufferedImage iconBuffer = instance.loadImageFromJar(path);
        List<Component> iconText = instance.renderImageToText(iconBuffer, 16, 16);

        String[] infoKeys = new String[]{
                "command.touhou.about.line1",
                "command.touhou.about.line2",
                "command.touhou.about.line3",
                "command.touhou.about.line4",
                "command.touhou.about.line5",
                "command.touhou.about.line6",
                "command.touhou.about.title",
                "command.touhou.about.version",
                "command.touhou.about.author",
                "command.touhou.about.line10",
                "command.touhou.about.line11"
        };

        List<Component> rightTexts = new ArrayList<>();
        for (String key : infoKeys) {
            if (key.equals("command.touhou.about.version")) {
                rightTexts.add(Component.translatable(key, PlatformContext.VERSION.get()));
            } else {
                rightTexts.add(Component.translatable(key));
            }
        }

        while (rightTexts.size() < iconText.size()) {
            rightTexts.add(Component.literal(""));
        }

        for (int i = 0; i < iconText.size(); i++) {
            Component left = iconText.get(i);
            Component right = rightTexts.get(i).copy().withStyle(ChatFormatting.WHITE);
            context.getSource().sendSuccess(() -> Component.empty().append(left).append(Component.literal("  ")).append(right), false);
        }

        return 1;
    }

    private int recipe(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player != null) {
            RecipeTypeCategoryGui.create(player);
        }
        return 1;
    }
}
