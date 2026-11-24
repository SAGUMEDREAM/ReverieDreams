package cc.thonly.reverie_dreams.command.service.debug;

import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.mixin.accessor.ItemCooldownsAccessor;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemCooldowns;

public final class DebugResetItemCooldownCommandService implements CommandService {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("debug_reset_item_using_time")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    if (!source.isPlayer()) {
                        return 0;
                    }
                    ServerPlayer player = source.getPlayer();
                    assert player != null;
                    ItemCooldowns cooldowns = player.getCooldowns();
                    ItemCooldownsAccessor accessor = (ItemCooldownsAccessor) cooldowns;
                    accessor.reverie_dreams$cooldowns().clear();
                    return 1;
                });
    }
}
