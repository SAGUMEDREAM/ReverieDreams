package cc.thonly.reverie_dreams.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

public class CommandInit {

    public static void initialize(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        THCommand command = new THCommand();
        dispatcher.register(command.makeInstance(dispatcher, context));
    }

}
