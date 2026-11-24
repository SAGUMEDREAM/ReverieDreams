package cc.thonly.reverie_dreams.world.gensokyo.update;

import cc.thonly.reverie_dreams.util.command.FilteredSuggestions;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoChunkGenerator;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.*;

/**
 * Operator commands; wildcard expands registered structure landmarks, never the entire dimension.
 */
public final class StructureUpdateCommands {
    private StructureUpdateCommands() {
    }

    private static StructureUpdateService service(CommandSourceStack source) {
        var level = source.getServer().getLevel(RDBuiltinLevels.getGensokyo());
        if (level == null || !(level.getChunkSource().getGenerator() instanceof GensokyoChunkGenerator generator))
            throw new IllegalStateException("Gensokyo dimension is unavailable");
        return generator.authoredStructures(level).updates();
    }

    public static LiteralArgumentBuilder<CommandSourceStack> command() {
        var root = Commands.literal("structure").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        for (String operation : List.of("status", "preview", "update", "rebuild")) {
            var action = Commands.literal(operation);
            action.then(Commands.literal("*").executes(context -> run(context.getSource(), operation, "*")));
            action.then(Commands.argument("landmark", net.minecraft.commands.arguments.IdentifierArgument.id()).suggests((context, builder) -> {
                for (String id : service(context.getSource()).landmarkIds())
                    FilteredSuggestions.resource(builder, Identifier.parse(id));
                return builder.buildFuture();
            }).executes(context -> run(context.getSource(), operation, net.minecraft.commands.arguments.IdentifierArgument.getId(context, "landmark").toString())));
            if (operation.equals("status"))
                action.executes(context -> run(context.getSource(), operation, "*"));
            root.then(action);
        }
        for (String operation : List.of("job", "cancel", "rollback", "resume"))
            root.then(Commands.literal(operation).then(Commands.argument("job", StringArgumentType.word()).executes(context -> runJob(context.getSource(), operation, StringArgumentType.getString(context, "job")))));
        return root;
    }

    private static int run(CommandSourceStack source, String operation, String selector) {
        try {
            var service = service(source);
            List<String> ids = selector.equals("*") ? service.landmarkIds() : List.of(Identifier.parse(selector).toString());
            if (ids.stream().anyMatch(id -> !service.landmarkIds().contains(id)))
                throw new IllegalArgumentException("Unknown template landmark: " + selector);
            if (operation.equals("status") || operation.equals("preview")) {
                for (String line : operation.equals("preview") ? service.preview(selector) : service.status(selector))
                    source.sendSuccess(() -> Component.literal(line), false);
                if (operation.equals("preview"))
                    source.sendSuccess(() -> Component.literal("只读预览：升级使用已保存基线保留玩家改动；rebuild恢复结构几何，仍保护容器/NPC/已领取奖励。执行前会扫描实际区块、备份并等待安全卸载。"), false);
            } else
                for (String id : ids) {
                    UUID job = service.enqueue(id, operation.equals("rebuild"), false);
                    source.sendSuccess(() -> Component.literal("已排队 " + operation + " " + id + "：" + job + "。玩家及外部加载占用将延后处理；不会删除区块或重置奖励。"), true);
                }
            return ids.size();
        } catch (Exception error) {
            source.sendFailure(Component.literal(error.getMessage() == null ? error.toString() : error.getMessage()));
            return 0;
        }
    }

    private static int runJob(CommandSourceStack source, String operation, String value) {
        try {
            var service = service(source);
            UUID id = UUID.fromString(value);
            if (operation.equals("cancel"))
                service.cancel(id);
            else if (operation.equals("rollback"))
                service.rollback(id);
            else if (operation.equals("resume"))
                service.resume(id);
            source.sendSuccess(() -> Component.literal(service.jobStatus(id)), true);
            return 1;
        } catch (Exception error) {
            source.sendFailure(Component.literal(error.getMessage() == null ? error.toString() : error.getMessage()));
            return 0;
        }
    }
}
