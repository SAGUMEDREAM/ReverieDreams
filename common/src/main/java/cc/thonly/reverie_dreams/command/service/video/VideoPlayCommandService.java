package cc.thonly.reverie_dreams.command.service.video;

import cc.thonly.reverie_dreams.api.dialog.DialogVideoAPI;
import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.server.dialog.video.DialogFiles;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;

@Slf4j
public final class VideoPlayCommandService implements CommandService {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("play")
                .then(
                        Commands.argument("target", EntityArgument.entity())
                                .then(
                                        Commands.argument("file", StringArgumentType.string())
                                                .suggests(new DialogFiles.FilesSuggestionProvider())
                                                .executes(this::execute)
                                                .then(
                                                        Commands.argument("sound", IdentifierArgument.id())
                                                                .suggests(SuggestionProviders.cast(SuggestionProviders.AVAILABLE_SOUNDS))
                                                                .executes(this::execute)
                                                )
                                )
                );
    }

    private int execute(CommandContext<CommandSourceStack> context) {
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
            DialogVideoAPI.play(player, file, soundEvent);
            context.getSource().sendSuccess(() -> Component.translatable("command.touhou.video.load.done"), false);
        } catch (Exception err) {
            log.error("Can't play video", err);
        }
        return 1;
    }
}
