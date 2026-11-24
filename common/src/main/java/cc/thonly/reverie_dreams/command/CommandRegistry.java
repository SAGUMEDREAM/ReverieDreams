package cc.thonly.reverie_dreams.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommandRegistry {

    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher,
                                       CommandBuildContext context,
                                       Commands.CommandSelection selection) {
        CommandTouhou command = new CommandTouhou();
        dispatcher.register(command.makeInstance(dispatcher, context));
    }

}
