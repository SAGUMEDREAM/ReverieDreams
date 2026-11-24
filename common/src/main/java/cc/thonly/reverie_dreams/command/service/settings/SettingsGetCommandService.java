package cc.thonly.reverie_dreams.command.service.settings;

import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.server.PlayerSettings;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class SettingsGetCommandService implements CommandService {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("get")
                .then(
                        Commands.argument(
                                        "name",
                                        StringArgumentType.word()
                                )
                                .suggests(SettingsCommandService.NAME_SUGGESTIONS)
                                .executes(context -> {
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
                                })
                );
    }
}
