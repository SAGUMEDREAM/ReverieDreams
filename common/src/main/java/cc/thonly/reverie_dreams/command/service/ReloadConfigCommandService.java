package cc.thonly.reverie_dreams.command.service;

import cc.thonly.reverie_dreams.ReverieDreamsConfiguration;
import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.util.command.PermissionPredicates;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class ReloadConfigCommandService implements CommandService {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("reload_config")
                .requires(PermissionPredicates.isGameMasters())
                .executes(context -> {
                    ConfigHolder<ReverieDreamsConfiguration> configHolder = AutoConfig.getConfigHolder(ReverieDreamsConfiguration.class);

                    if (configHolder == null) {
                        context.getSource().sendFailure(Component.translatable("command.touhou.reload_config.failed"));
                        return 0;
                    }

                    configHolder.load();
                    context.getSource().sendSystemMessage(Component.translatable("command.touhou.reload_config.success"));

                    return 1;
                });
    }
}
