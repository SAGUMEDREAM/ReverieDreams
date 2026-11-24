package cc.thonly.reverie_dreams.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

@FunctionalInterface
public interface CommandService {
    LiteralArgumentBuilder<CommandSourceStack> build();
}
