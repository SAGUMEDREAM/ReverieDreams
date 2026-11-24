package cc.thonly.reverie_dreams.command.service.debug;

import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.util.test.ModTest;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public final class DebugTestCommandService implements CommandService {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("test").executes(context -> {
            CommandSourceStack source = context.getSource();

            if (!source.isPlayer()) {
                return 0;
            }

            ServerPlayer player = source.getPlayer();
            if (player != null) {
                ModTest.ByCommand.onTest(player);
            }

            return 1;
        });
    }
}
