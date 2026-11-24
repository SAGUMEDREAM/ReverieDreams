package cc.thonly.reverie_dreams.command.service;

import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.gui.recipe.RecipeTypeCategoryGui;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public final class RecipeCommandService implements CommandService {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("recipe").executes(context -> {
            ServerPlayer player = context.getSource().getPlayer();
            if (player != null) {
                RecipeTypeCategoryGui.create(player);
            }
            return 1;
        });
    }
}
