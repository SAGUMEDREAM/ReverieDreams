package cc.thonly.reverie_dreams.command.service;

import cc.thonly.reverie_dreams.command.CommandService;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class HelpCommandService implements CommandService {
    private static final String[] HELP_KEYS = {
            "command.touhou.help.header",

            "command.touhou.help.common",
            "command.touhou.help.help",
            "command.touhou.help.about",
            "command.touhou.help.recipe",
            "command.touhou.help.set_music",

//            "command.touhou.help.settings",
            "command.touhou.help.settings_get",
            "command.touhou.help.settings_set",

//            "command.touhou.help.resources",
            "command.touhou.help.parse_midi",
            "command.touhou.help.reload_config",

//            "command.touhou.help.registry",
            "command.touhou.help.registry_tag",

//            "command.touhou.help.video",
            "command.touhou.help.video_play",
            "command.touhou.help.video_reload",

            "command.touhou.help.footer"
    };

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("help").executes(context -> {
            for (String key : HELP_KEYS) {
                context.getSource().sendSuccess(
                        () -> Component.translatable(key)
                                .withStyle(ChatFormatting.WHITE),
                        false
                );
            }

            return 1;
        });
    }
}
