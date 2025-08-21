package cc.thonly.reverie_dreams.command;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.dialog.DialogFiles;
import cc.thonly.reverie_dreams.dialog.DialogPlayer;
import cc.thonly.reverie_dreams.gui.recipe.RecipeTypeCategoryGui;
import cc.thonly.reverie_dreams.util.ImageToTextScanner;
import cc.thonly.reverie_dreams.dialog.DialogInit;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class MainCommand implements CommandInit.CommandRegistration {

    public static class DialogSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            for (String string : DialogInit.ARGS_DIALOG.keySet()) {
                builder.suggest(string);
            }
            return builder.buildFuture();
        }
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher,
                         CommandRegistryAccess access,
                         CommandManager.RegistrationEnvironment environment
    ) {
        dispatcher.register(
                CommandManager.literal("touhou")
                        .executes(this::run)
                        .then(
                                CommandManager.literal("help")
                                        .executes(this::help)
                        )
                        .then(
                                CommandManager.literal("recipe")
                                        .executes(this::recipe)
                        )
                        .then(
                                CommandManager.literal("dialog")
                                        .then(
                                                CommandManager
                                                        .argument("value", StringArgumentType.string())
                                                        .suggests(new DialogSuggestionProvider())
                                                        .executes(this::dialog)
                                        )
                        )
                        .then(
                                CommandManager.literal("video")
                                        .requires(source -> source.hasPermissionLevel(2))
                                        .then(
                                                CommandManager.literal("play")
                                                        .then(
                                                                CommandManager.argument("target", EntityArgumentType.entity())
                                                                        .then(
                                                                                CommandManager.argument("file", StringArgumentType.string())
                                                                                        .suggests(new DialogFiles.FilesSuggestionProvider())
                                                                                        .executes(this::playVideo)
                                                                                        .then(
                                                                                                CommandManager.argument("sound", IdentifierArgumentType.identifier())
                                                                                                        .suggests(SuggestionProviders.cast(SuggestionProviders.AVAILABLE_SOUNDS))
                                                                                                        .executes(this::playVideo)
                                                                                        )
                                                                        )
                                                        )

                                        )
                                        .then(
                                                CommandManager
                                                        .literal("reload")
                                                        .executes(this::reloadVideo)
                                        )
                        )
                        .then(
                                CommandManager.literal("about")
                                        .executes(this::about)
                        )
                        .then(
                                CommandManager.literal("ui_relay_recipe")
                                        .executes((context) -> 0)
                        )
        );
    }

    private int run(CommandContext<ServerCommandSource> context) {
        MutableText text = Text.translatable("command.touhou.suggest_help");
        context.getSource().sendFeedback(() -> text.setStyle(Style.EMPTY.withColor(Formatting.YELLOW)), false);
        return 1;
    }

    private int reloadVideo(CommandContext<ServerCommandSource> context) {
        DialogFiles.reload();
        context.getSource().sendFeedback(()-> Text.translatable("command.touhou.video.reload"), false);
        return 1;
    }

    private int playVideo(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "target");
            String file = StringArgumentType.getString(context, "file");
            Identifier soundEventId = null;
            SoundEvent soundEvent = null;
            try {
                soundEventId = IdentifierArgumentType.getIdentifier(context, "sound");
            } catch (Exception ignored) {
            }
            if (soundEventId != null) {
                soundEvent = SoundEvent.of(soundEventId);
            }
            context.getSource().sendFeedback(()-> Text.translatable("command.touhou.video.reload"), false);
            DialogPlayer.play(player, file, soundEvent);
            context.getSource().sendFeedback(()-> Text.translatable("command.touhou.video.load.done"), false);
        } catch (Exception err) {
            log.error("Can't play video", err);
        }
        return 1;
    }

    private int help(CommandContext<ServerCommandSource> context) {
        List<String> keys = List.of(
                "command.touhou.help.title",
                "command.touhou.help.help",
                "command.touhou.help.recipe",
                "command.touhou.help.about",
                "command.touhou.help.empty"
        );

        for (String key : keys) {
            context.getSource().sendFeedback(() -> Text.translatable(key).setStyle(Style.EMPTY.withColor(Formatting.WHITE)), false);
        }
        return 1;
    }

    private int dialog(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!source.isExecutedByPlayer()) {
            return 0;
        }
        ServerPlayerEntity player = source.getPlayer();
        String value = StringArgumentType.getString(context, "value");
        Dialog dialog = DialogInit.ARGS_DIALOG.get(value);
        if (player != null && dialog != null) {
            player.openDialog(RegistryEntry.of(dialog));
        }
        return 1;
    }

    private int about(CommandContext<ServerCommandSource> context) {
        Class<?> clazz = Touhou.class;
        ImageToTextScanner instance = ImageToTextScanner.createInstance(clazz);
        String path = ImageToTextScanner.ofNamespace(Touhou.MOD_ID, "icon.png");
        BufferedImage iconBuffer = instance.loadImageFromJar(path);
        List<Text> iconText = instance.renderImageToText(iconBuffer, 16, 16);

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

        List<Text> rightTexts = new ArrayList<>();
        for (String key : infoKeys) {
            if (key.equals("command.touhou.about.version")) {
                rightTexts.add(Text.translatable(key, Touhou.VERSION));
            } else {
                rightTexts.add(Text.translatable(key));
            }
        }

        while (rightTexts.size() < iconText.size()) {
            rightTexts.add(Text.literal(""));
        }

        for (int i = 0; i < iconText.size(); i++) {
            Text left = iconText.get(i);
            Text right = rightTexts.get(i).copy().formatted(Formatting.WHITE);
            context.getSource().sendFeedback(() -> Text.empty().append(left).append(Text.literal("  ")).append(right), false);
        }

        return 1;
    }

    private int recipe(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player != null) {
            RecipeTypeCategoryGui.create(player);
        }
        return 1;
    }
}
