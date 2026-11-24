package cc.thonly.reverie_dreams.command.service.settings;

import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.server.PlayerSettings;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class SettingsSetCommandService implements CommandService {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("set")
                .then(
                        Commands.argument(
                                        "name",
                                        StringArgumentType.word()
                                )
                                .suggests(SettingsCommandService.NAME_SUGGESTIONS)
                                .then(
                                        Commands.argument(
                                                        "value",
                                                        StringArgumentType.greedyString()
                                                )
                                                .executes(context -> {
                                                    CommandSourceStack source = context.getSource();
                                                    if (!source.isPlayer()) {
                                                        return 0;
                                                    }
                                                    ServerPlayer player = source.getPlayer();
                                                    PlayerSettings settings = PlayerSettings.get(player);
                                                    String name = StringArgumentType.getString(context, "name");
                                                    PlayerSettings.KeyValue<?> keyValue = settings.get(name);
                                                    if (keyValue == null) {
                                                        source.sendFailure(Component.translatable("command.touhou.player_setting.unknown", name));
                                                        return 0;
                                                    }
                                                    Object value;
                                                    switch (keyValue.type()) {
                                                        case BOOL -> value = BoolArgumentType.getBool(context, "value");
                                                        case STRING -> value = StringArgumentType.getString(context, "value");
                                                        case NUMBER -> value = DoubleArgumentType.getDouble(context, "value");
                                                        default -> {
                                                            return 0;
                                                        }
                                                    }
                                                    settings.set(name, value);
                                                    source.sendSystemMessage(Component.translatable("command.touhou.player_setting.set", name, value));
                                                    return 1;
                                                })
                                )
                );
    }
}
