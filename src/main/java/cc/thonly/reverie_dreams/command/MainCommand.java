package cc.thonly.reverie_dreams.command;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.DrinkProperty;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.data.danmaku.SpellcardRenderer;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.SpellCardFrameConfig;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.debug.DebugExportWriter;
import cc.thonly.reverie_dreams.dialog.DialogFiles;
import cc.thonly.reverie_dreams.dialog.DialogInit;
import cc.thonly.reverie_dreams.dialog.DialogPlayer;
import cc.thonly.reverie_dreams.gui.recipe.RecipeTypeCategoryGui;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import cc.thonly.reverie_dreams.registry.interfaces.Translatable;
import cc.thonly.reverie_dreams.util.ConstantInfo;
import cc.thonly.reverie_dreams.util.ImageToTextScanner;
import cc.thonly.reverie_dreams.util.command.PermissionPredicate;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.level.storage.loot.LootTable;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Slf4j
public class MainCommand implements CommandInit.CommandRegistration {

    public static class DialogSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            for (String string : DialogInit.ARGS_DIALOG.keySet()) {
                builder.suggest(string);
            }
            return builder.buildFuture();
        }
    }


    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher,
                         CommandBuildContext access,
                         Commands.CommandSelection environment
    ) {
        var root = Commands.literal("touhou");
        var help = Commands.literal("help")
                .executes(this::help);
        var get_sc_with_spell_config = Commands.literal("get_spellcard_with_config")
                .requires(PermissionPredicate.isGameMasters())
                .then(
                        RegistryHandlers.getSuggestProvider(this::getItemWithDanmakuConfig, ResourceKey.createRegistryKey(ReverieDreams.id("danmaku_config")))
                );
        var with_food_property = Commands.literal("with_food_property")
                .requires(PermissionPredicate.isGameMasters())
                .then(
                        RegistryHandlers.getSuggestProvider(this::withFoodProperties, ResourceKey.createRegistryKey(ReverieDreams.id("food_property")))
                );
        var with_drink_property = Commands.literal("with_drink_property")
                .requires(PermissionPredicate.isGameMasters())
                .then(
                        RegistryHandlers.getSuggestProvider(this::withDrinkProperties, ResourceKey.createRegistryKey(ReverieDreams.id("drink_property")))
                );
        var cachedAllSkins = Commands.literal("start-cached-skins")
                .requires(PermissionPredicate.isGameMasters())
                .executes(this::cachedAllSkins);
        var recipe = Commands.literal("recipe")
                .executes(this::recipe);
        var registry = Commands.literal("registry")
                .requires(PermissionPredicate.isGameMasters())
                .then(
                        RegistryHandlers.getSuggestProvider(this::registry)
                );
        var dialog = Commands.literal("dialog")
                .then(
                        Commands.argument("value", StringArgumentType.string())
                                .suggests(new DialogSuggestionProvider())
                                .executes(this::dialog)
                );
        var video = Commands.literal("video")
                .requires(PermissionPredicate.isGameMasters())
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
        var about = Commands.literal("about")
                .executes(this::about);

        root.executes(this::run);
        root.then(help);
        root.then(get_sc_with_spell_config);
        root.then(with_food_property);
        root.then(with_drink_property);
        root.then(cachedAllSkins);
        root.then(recipe);
        root.then(registry);
        root.then(dialog);
        root.then(video);
        root.then(about);
        if (ConstantInfo.isDevMode()) {
            var debugGetChest = Commands.literal("debug-chest")
                    .then(Commands.argument("loot_table", ResourceOrIdArgument.lootTable(access))
                            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                            .executes(this::debugFastChestLoot));
            root.then(debugGetChest);
        }

        dispatcher.register(root);
    }

    private int run(CommandContext<CommandSourceStack> context) {
        MutableComponent text = Component.translatable("command.touhou.suggest_help");
        context.getSource().sendSuccess(() -> text.setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), false);
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
        FoodProperty property = RegistryHandlers.FOOD_PROPERTY.getValue(id);
        if (property == null) {
            source.sendFailure(Component.literal("Invalid resource key."));
            return 0;
        }
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.isEmpty()) {
            source.sendFailure(Component.literal("§cYour hand item is empty."));
            return 0;
        }
        List<String> strings = new ArrayList<>(itemStack.getOrDefault(RDDataComponents.FOOD_PROPERTIES, new ArrayList<>()));
        if (!strings.contains(property.getId().toString())) {
            strings.add(property.getId().toString());
        }
        itemStack.set(RDDataComponents.FOOD_PROPERTIES, strings);
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
        DrinkProperty property = RegistryHandlers.DRINK_PROPERTY.getValue(id);
        if (property == null) {
            source.sendFailure(Component.literal("Invalid resource key."));
            return 0;
        }
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.isEmpty()) {
            source.sendFailure(Component.literal("§cYour hand item is empty."));
            return 0;
        }
        List<String> strings = new ArrayList<>(itemStack.getOrDefault(RDDataComponents.DRINK_PROPERTIES, new ArrayList<>()));
        if (!strings.contains(property.getId().toString())) {
            strings.add(property.getId().toString());
        }
        itemStack.set(RDDataComponents.DRINK_PROPERTIES, strings);
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

        SpellCardFrameConfig config = RegistryHandlers.DANMAKU_CONFIG.getValue(id);
        if (config == null) {
            source.sendFailure(Component.literal("Invalid resource key."));
            return 0;
        }
        ItemStack itemStack = RDItems.SPELLCARD.getDefaultInstance();
        itemStack.set(RDDataComponents.SPELL_CARD_COMPONENT, new SpellcardRenderer(List.of(List.of(config))));
        player.addItem(itemStack);
        return 1;
    }

    private int cachedAllSkins(CommandContext<CommandSourceStack> context) {
        for (SkinType skinType : RegistryHandlers.SKIN_TYPE) {
            try {
                if (skinType.get() == null) {
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
        RegistryHandler<?> registry = RegistryHandlers.ROOT.get(registryKey);
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

        if (value instanceof Translatable translatable) {
            msg.append(Component.literal("Translation: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.translatable(translatable.translateKey()).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal("\n"));
        }

        msg.append(Component.literal("Object: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(value.toString()).withStyle(ChatFormatting.AQUA));

        source.sendSystemMessage(msg);
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
            DialogPlayer.play(player, file, soundEvent);
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

    private int dialog(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) {
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        String value = StringArgumentType.getString(context, "value");
        Dialog dialog = DialogInit.ARGS_DIALOG.get(value);
        if (player != null && dialog != null) {
            player.openDialog(Holder.direct(dialog));
        }
        return 1;
    }

    @SuppressWarnings("unchecked")
    private int exportRegistries(CommandContext<CommandSourceStack> context) {
        List<String> lines = new LinkedList<>();
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        RegistryAccess.Frozen registryManager = server.registryAccess();
        Stream<RegistryAccess.RegistryEntry<Object>> entryStream = (Stream<RegistryAccess.RegistryEntry<Object>>) (Object) registryManager.registries();
        List<RegistryAccess.RegistryEntry<Object>> list = entryStream.toList();
        for (RegistryAccess.RegistryEntry<Object> entry : list) {
            ResourceKey<? extends Registry<Object>> key = entry.key();
            Registry<Object> registry = entry.value();

            lines.add("===== Registry: " + key.identifier() + " =====");
            registry.forEach(obj -> {
                int rawId = registry.getId(obj);
                Identifier id = registry.getKey(obj);
                if (rawId == 97) {
                    lines.add("!!! Found 97 in " + key.identifier() + " = " + id);
                }
            });
        }
        DebugExportWriter output = DebugExportWriter.OUTPUT;
        for (String line : lines) {
            output.write(line);
        }
        output.export();
        return 1;
    }

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
                rightTexts.add(Component.translatable(key, ConstantInfo.VERSION));
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
