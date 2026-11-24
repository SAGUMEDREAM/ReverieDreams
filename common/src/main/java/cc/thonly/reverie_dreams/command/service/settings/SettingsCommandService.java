package cc.thonly.reverie_dreams.command.service.settings;

import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.server.PlayerSettings;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class SettingsCommandService implements CommandService {
    static final SuggestionProvider<CommandSourceStack> NAME_SUGGESTIONS =
            (context, builder) -> {
                for (String name : PlayerSettings.DEFINES.keySet()) {
                    cc.thonly.reverie_dreams.util.command.FilteredSuggestions.value(builder,name);
                }

                return builder.buildFuture();
            };

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("settings")
                .then(new SettingsGetCommandService().build())
                .then(new SettingsSetCommandService().build());
    }
}
