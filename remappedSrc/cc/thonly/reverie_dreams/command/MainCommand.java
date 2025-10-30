package cc.thonly.reverie_dreams.command;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.debug.DebugExportWriter;
import cc.thonly.reverie_dreams.dialog.DialogFiles;
import cc.thonly.reverie_dreams.dialog.DialogInit;
import cc.thonly.reverie_dreams.dialog.DialogPlayer;
import cc.thonly.reverie_dreams.gui.recipe.RecipeTypeCategoryGui;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.registry.Translatable;
import cc.thonly.reverie_dreams.util.ImageToTextScanner;
import cc.thonly.reverie_dreams.util.ConstantInfo;
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
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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
        var recipe = Commands.literal("recipe")
                .executes(this::recipe);
        var registry = Commands.literal("registry")
                .requires(source -> source.hasPermission(2))
                .then(
                        RegistryManager.getSuggestProvider(this::registry)
                );
        var dialog = Commands.literal("dialog")
                .then(
                        Commands
                                .argument("value", StringArgumentType.string())
                                .suggests(new DialogSuggestionProvider())
                                .executes(this::dialog)
                );
        var video = Commands.literal("video")
                .requires(source -> source.hasPermission(2))
                .then(
                        Commands.literal("play")
                                .then(
                                        Commands.argument("target", EntityArgument.entity())
                                                .then(
                                                        Commands.argument("file", StringArgumentType.string())
                                                                .suggests(new DialogFiles.FilesSuggestionProvider())
                                                                .executes(this::playVideo)
                                                                .then(
                                                                        Commands.argument("sound", ResourceLocationArgument.id())
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
        root.then(recipe);
        root.then(registry);
        root.then(dialog);
        root.then(video);
        root.then(about);

        dispatcher.register(root);
    }

    private int run(CommandContext<CommandSourceStack> context) {
        MutableComponent text = Component.translatable("command.touhou.suggest_help");
        context.getSource().sendSuccess(() -> text.setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), false);
        return 1;
    }

    private int registry(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        ResourceLocation registryKeyId = ResourceLocationArgument.getId(context, "registry_key");
        ResourceLocation id = ResourceLocationArgument.getId(context, "id");

        if (registryKeyId == null || id == null) {
            source.sendFailure(Component.literal("Invalid identifier format."));
            return 0;
        }

        ResourceKey<Registry<Object>> registryKey = ResourceKey.createRegistryKey(registryKeyId);
        IntrinsicalRegister<?> registry = RegistryManager.ROOT.get(registryKey);
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
            ResourceLocation soundEventId = null;
            SoundEvent soundEvent = null;
            try {
                soundEventId = ResourceLocationArgument.getId(context, "sound");
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

            lines.add("===== Registry: " + key.location() + " =====");
            registry.forEach(obj -> {
                int rawId = registry.getId(obj);
                ResourceLocation id = registry.getKey(obj);
                if (rawId == 97) {
                    lines.add("!!! Found 97 in " + key.location() + " = " + id);
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
        Class<?> clazz = Touhou.class;
        ImageToTextScanner instance = ImageToTextScanner.createInstance(clazz);
        String path = ImageToTextScanner.ofNamespace(Touhou.MOD_ID, "icon_about.png");
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
