package cc.thonly.reverie_dreams.command.service;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.runtime.DanmakuPatternRuntime;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.util.command.PermissionPredicates;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class CastPatternCommandService implements CommandService {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("cast_pattern")
                .requires(PermissionPredicates.isGameMasters())
                .then(
                        BuiltInRegistryProviders.getSuggestProvider(this::execute, ResourceKey.createRegistryKey(ReverieDreams.id("danmaku_pattern")))
                );
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) {
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        assert player != null;
        Identifier id = IdentifierArgument.getId(context, "id");
        DanmakuPattern pattern = BuiltInRegistryProviders.DANMAKU_PATTERN.findValue(id).orElse(null);
        if (pattern == null) {
            source.sendFailure(Component.literal("Invalid danmaku pattern id."));
            return 0;
        }
        DanmakuPatternRuntime.startFromView((ServerLevel) player.level(), player, pattern);
        return 1;
    }
}
