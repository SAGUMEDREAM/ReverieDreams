package cc.thonly.reverie_dreams.command.service.debug;

import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.RecipeWorkbench;
import cc.thonly.reverie_dreams.recipe.RecipeWorkbenchRegistry;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

@Slf4j
public final class DebugRecipeBlockCommandService implements CommandService {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        var root = Commands.literal("debug_get_recipe_with_block")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        for (Map.Entry<String, RecipeWorkbench<?>> mapEntry : RecipeWorkbenchRegistry.entries()) {
            String key = mapEntry.getKey();
            RecipeWorkbench<?> entry = mapEntry.getValue();
            root.then(
                    Commands.literal(key).then(Commands.argument("recipe_id", IdentifierArgument.id())
                            .suggests(RecipeManager.getSuggestions(entry.getRecipeType()))
                            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                            .executes(context -> execute(entry, context))
                    )
            );
        }
        return root;
    }

    private static <R extends BaseRecipe> int execute(RecipeWorkbench<R> recipeEntry, CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) {
            return 0;
        }
        Identifier recipeId = IdentifierArgument.getId(context, "recipe_id");
        MinecraftServer server = source.getServer();
        RecipeWorkbench.SaveFunction<R> function = recipeEntry.getFunction();
        try {
            ItemStack itemStack = function.save(server.registryAccess(), recipeId, recipeEntry);
            if (itemStack == null || itemStack.isEmpty()) {
                return 0;
            }
            ServerPlayer player = source.getPlayer();
            if (player != null) {
                player.addItem(itemStack);
            }
        } catch (Exception e) {
            log.error("Error: ", e);
            throw new RuntimeException(e);
        }
        return 1;
    }
}
