package cc.thonly.reverie_dreams.command.service;

import cc.thonly.reverie_dreams.api.registry.BookPageManager;
import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.util.command.PermissionPredicates;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

@Slf4j
public final class LoadRootGuidePageCommandService implements CommandService {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("load_root_guide_page")
                .requires(PermissionPredicates.isGameMasters())
                .then(
                        Commands.argument("namespace", StringArgumentType.string())
                                .executes(context -> {
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
                                })
                );
    }
}
