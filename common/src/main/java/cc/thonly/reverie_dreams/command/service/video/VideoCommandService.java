package cc.thonly.reverie_dreams.command.service.video;

import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.util.command.PermissionPredicates;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class VideoCommandService implements CommandService {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("video")
                .requires(PermissionPredicates.isGameMasters())
                .then(new VideoPlayCommandService().build())
                .then(new VideoReloadCommandService().build());
    }
}
