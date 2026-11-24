package cc.thonly.reverie_dreams.command.service.video;

import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.server.dialog.video.DialogFiles;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class VideoReloadCommandService implements CommandService {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("reload").executes(context -> {
            DialogFiles.reload();
            context.getSource().sendSuccess(() -> Component.translatable("command.touhou.video.reload"), false);
            return 1;
        });
    }
}
